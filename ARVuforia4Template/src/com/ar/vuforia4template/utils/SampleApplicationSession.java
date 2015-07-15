/*==============================================================================
 Copyright (c) 2012-2013 Qualcomm Connected Experiences, Inc.
 All Rights Reserved.
 ==============================================================================*/

package com.ar.vuforia4template.utils;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.qualcomm.vuforia.CameraCalibration;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.PIXEL_FORMAT;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.Vec2I;
import com.qualcomm.vuforia.VideoBackgroundConfig;
import com.qualcomm.vuforia.VideoMode;
import com.qualcomm.vuforia.Vuforia;
import com.qualcomm.vuforia.Vuforia.UpdateCallbackInterface;

public class SampleApplicationSession implements UpdateCallbackInterface {

	private static final String LOGTAG = "Vuforia_Sample_Applications";

	// Reference to the current activity
	private Activity _activity;
	private SampleApplicationControl _sessionControl;

	// Flags
	private boolean _started = false;

	// Display size of the device:
	private int _screenWidth = 0;
	private int _screenHeight = 0;

	// The async tasks to initialize the Vuforia SDK:
	private InitVuforiaTask _initVuforiaTask;
	private LoadTrackerTask _loadTrackerTask;

	// An object used for synchronizing Vuforia initialization, dataset loading
	// and the Android onDestroy() life cycle event. If the application is
	// destroyed while a data set is still being loaded, then we wait for the
	// loading operation to finish before shutting down Vuforia:
	private Object _shutdownLock = new Object();

	// Vuforia initialization flags:
	private int _vuforiaFlags = 0;

	// Holds the camera configuration to use upon resuming
	private int _camera = CameraDevice.CAMERA.CAMERA_DEFAULT;

	// Stores the projection matrix to use for rendering purposes
	private Matrix44F _projectionMatrix;

	// Stores orientation
	private boolean _isPortrait = false;

	public SampleApplicationSession(SampleApplicationControl sessionControl) {
		_sessionControl = sessionControl;
	}

	// Initializes Vuforia and sets up preferences.
	public void initAR(Activity activity, int screenOrientation) {
		SampleApplicationException vuforiaException = null;
		_activity = activity;

		if ((screenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR)
				&& (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO))
			screenOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;

		// Apply screen orientation
		_activity.setRequestedOrientation(screenOrientation);

		updateActivityOrientation();

		// Query display dimensions:
		storeScreenDimensions();

		// As long as this window is visible to the user, keep the device's
		// screen turned on and bright:
		_activity.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		_vuforiaFlags = Vuforia.GL_20;

		// Initialize Vuforia SDK asynchronously to avoid blocking the
		// main (UI) thread.
		//
		// NOTE: This task instance must be created and invoked on the
		// UI thread and it can be executed only once!
		if (_initVuforiaTask != null) {
			String logMessage = "Cannot initialize SDK twice";
			vuforiaException = new SampleApplicationException(
					SampleApplicationException.VUFORIA_ALREADY_INITIALIZATED,
					logMessage);
			Log.e(LOGTAG, logMessage);
		}

		if (vuforiaException == null) {
			try {
				_initVuforiaTask = new InitVuforiaTask();
				_initVuforiaTask.execute();
			} catch (Exception e) {
				String logMessage = "Initializing Vuforia SDK failed";
				vuforiaException = new SampleApplicationException(
						SampleApplicationException.INITIALIZATION_FAILURE,
						logMessage);
				Log.e(LOGTAG, logMessage);
			}
		}

		if (vuforiaException != null)
			_sessionControl.onInitARDone(vuforiaException);
	}

	// Starts Vuforia, initialize and starts the camera and start the trackers
	public void startAR(int camera) throws SampleApplicationException {
		String error;
		_camera = camera;
		if (!CameraDevice.getInstance().init(camera)) {
			error = "Unable to open camera device: " + camera;
			Log.e(LOGTAG, error);
			throw new SampleApplicationException(
					SampleApplicationException.CAMERA_INITIALIZATION_FAILURE,
					error);
		}

		configureVideoBackground();

		if (!CameraDevice.getInstance().selectVideoMode(
				CameraDevice.MODE.MODE_DEFAULT)) {
			error = "Unable to set video mode";
			Log.e(LOGTAG, error);
			throw new SampleApplicationException(
					SampleApplicationException.CAMERA_INITIALIZATION_FAILURE,
					error);
		}

		if (!CameraDevice.getInstance().start()) {
			error = "Unable to start camera device: " + camera;
			Log.e(LOGTAG, error);
			throw new SampleApplicationException(
					SampleApplicationException.CAMERA_INITIALIZATION_FAILURE,
					error);
		}

		Vuforia.setFrameFormat(PIXEL_FORMAT.RGB565, true);

		setProjectionMatrix();

		_sessionControl.doStartTrackers();

		try {
			setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);
		} catch (SampleApplicationException exceptionTriggerAuto) {
			setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);
		}
	}

	// Stops any ongoing initialization, stops Vuforia
	public void stopAR() throws SampleApplicationException {
		// Cancel potentially running tasks
		if (_initVuforiaTask != null
				&& _initVuforiaTask.getStatus() != InitVuforiaTask.Status.FINISHED) {
			_initVuforiaTask.cancel(true);
			_initVuforiaTask = null;
		}

		if (_loadTrackerTask != null
				&& _loadTrackerTask.getStatus() != LoadTrackerTask.Status.FINISHED) {
			_loadTrackerTask.cancel(true);
			_loadTrackerTask = null;
		}

		_initVuforiaTask = null;
		_loadTrackerTask = null;

		_started = false;

		stopCamera();

		// Ensure that all asynchronous operations to initialize Vuforia
		// and loading the tracker datasets do not overlap:
		synchronized (_shutdownLock) {

			boolean unloadTrackersResult;
			boolean deinitTrackersResult;

			// Destroy the tracking data set:
			unloadTrackersResult = _sessionControl.doUnloadTrackersData();

			// Deinitialize the trackers:
			deinitTrackersResult = _sessionControl.doDeinitTrackers();

			// Deinitialize Vuforia SDK:
			Vuforia.deinit();

			if (!unloadTrackersResult)
				throw new SampleApplicationException(
						SampleApplicationException.UNLOADING_TRACKERS_FAILURE,
						"Failed to unload trackers\' data");

			if (!deinitTrackersResult)
				throw new SampleApplicationException(
						SampleApplicationException.TRACKERS_DEINITIALIZATION_FAILURE,
						"Failed to deinitialize trackers");

		}
	}

	// Resumes Vuforia, restarts the trackers and the camera
	public void resumeAR() throws SampleApplicationException {
		// Vuforia-specific resume operation
		Vuforia.onResume();

		if (_started)
			startAR(_camera);
	}

	// Pauses Vuforia and stops the camera
	public void pauseAR() throws SampleApplicationException {
		if (_started)
			stopCamera();

		Vuforia.onPause();
	}

	// Gets the projection matrix to be used for rendering
	public Matrix44F getProjectionMatrix() {
		return _projectionMatrix;
	}

	// Callback called every cycle
	@Override
	public void QCAR_onUpdate(State s) {
		_sessionControl.onQCARUpdate(s);
	}

	// Manages the configuration changes
	public void onConfigurationChanged() {
		updateActivityOrientation();

		storeScreenDimensions();

		if (isARRunning()) {
			// configure video background
			configureVideoBackground();

			// Update projection matrix:
			setProjectionMatrix();
		}

	}

	// Methods to be called to handle lifecycle
	public void onResume() {
		Vuforia.onResume();
	}

	public void onPause() {
		Vuforia.onPause();
	}

	public void onSurfaceChanged(int width, int height) {
		Vuforia.onSurfaceChanged(width, height);
	}

	public void onSurfaceCreated() {
		Vuforia.onSurfaceCreated();
	}

	// An async task to initialize Vuforia asynchronously.
	private class InitVuforiaTask extends AsyncTask<Void, Integer, Boolean> {
		// Initialize with invalid value:
		private int mProgressValue = -1;

		protected Boolean doInBackground(Void... params) {
			// Prevent the onDestroy() method to overlap with initialization:
			synchronized (_shutdownLock) {
				Vuforia.setInitParameters(_activity, _vuforiaFlags, "AZ+6FQj/////AAAAAXrrU0/YTURsnKRYhiAjUIVXTohAQ2r5Lge+O5O0cNPEakxgy5JyhDqalr3M4kPMxFuEGkeUssI9SUOWMBVL1u8eDxhpM5jFxdwGM0df7Bq5noGQ3IoPR8ryU2YJEATKyCuo6xX3YfvDVmwuH2QuKwEcFfUBvleQ0a5MIgi5zb9/g9au4Wzmr61fWkNA3q5yLYEZzb9zbUs1/xa34crww8veTh8tSdDWrawPEtyufky+Bl9vZ60yaYSWt7nlUp1on8V84ik4g6Ne1uoNNo+wgF3g80Vgk7qkV12onMOVc/t5CuIxQVF7UfFM1T2yb8agiW2WTmwsHeNHLfg2PKgr7SQJj/JZZe+KjAIueSd5MUKU");

				do {
					// Vuforia.init() blocks until an initialization step is
					// complete, then it proceeds to the next step and reports
					// progress in percents (0 ... 100%).
					// If Vuforia.init() returns -1, it indicates an error.
					// Initialization is done when progress has reached 100%.
					mProgressValue = Vuforia.init();

					// Publish the progress value:
					publishProgress(mProgressValue);

					// We check whether the task has been canceled in the
					// meantime (by calling AsyncTask.cancel(true)).
					// and bail out if it has, thus stopping this thread.
					// This is necessary as the AsyncTask will run to completion
					// regardless of the status of the component that
					// started is.
				} while (!isCancelled() && mProgressValue >= 0
						&& mProgressValue < 100);

				return (mProgressValue > 0);
			}
		}

		protected void onProgressUpdate(Integer... values) {
			// Do something with the progress value "values[0]", e.g. update
			// splash screen, progress bar, etc.
		}

		protected void onPostExecute(Boolean result) {
			// Done initializing Vuforia, proceed to next application
			// initialization status:

			SampleApplicationException vuforiaException = null;

			if (result) {
				Log.d(LOGTAG, "InitVuforiaTask.onPostExecute: Vuforia "
						+ "initialization successful");

				boolean initTrackersResult;
				initTrackersResult = _sessionControl.doInitTrackers();

				if (initTrackersResult) {
					try {
						_loadTrackerTask = new LoadTrackerTask();
						_loadTrackerTask.execute();
					} catch (Exception e) {
						String logMessage = "Loading tracking data set failed";
						vuforiaException = new SampleApplicationException(
								SampleApplicationException.LOADING_TRACKERS_FAILURE,
								logMessage);
						Log.e(LOGTAG, logMessage);
						_sessionControl.onInitARDone(vuforiaException);
					}

				} else {
					vuforiaException = new SampleApplicationException(
							SampleApplicationException.TRACKERS_INITIALIZATION_FAILURE,
							"Failed to initialize trackers");
					_sessionControl.onInitARDone(vuforiaException);
				}
			} else {
				String logMessage;

				// NOTE: Check if initialization failed because the device is
				// not supported. At this point the user should be informed
				// with a message.
				if (mProgressValue == Vuforia.INIT_DEVICE_NOT_SUPPORTED) {
					logMessage = "Failed to initialize Vuforia because this "
							+ "device is not supported.";
				} else {
					logMessage = "Failed to initialize Vuforia.";
				}

				// Log error:
				Log.e(LOGTAG, "InitVuforiaTask.onPostExecute: " + logMessage
						+ " Exiting.");

				// Send Vuforia Exception to the application and call initDone
				// to stop initialization process
				vuforiaException = new SampleApplicationException(
						SampleApplicationException.INITIALIZATION_FAILURE,
						logMessage);
				_sessionControl.onInitARDone(vuforiaException);
			}
		}
	}

	// An async task to load the tracker data asynchronously.
	private class LoadTrackerTask extends AsyncTask<Void, Integer, Boolean> {
		protected Boolean doInBackground(Void... params) {
			// Prevent the onDestroy() method to overlap:
			synchronized (_shutdownLock) {
				// Load the tracker data set:
				return _sessionControl.doLoadTrackersData();
			}
		}

		protected void onPostExecute(Boolean result) {

			SampleApplicationException vuforiaException = null;

			Log.d(LOGTAG, "LoadTrackerTask.onPostExecute: execution "
					+ (result ? "successful" : "failed"));

			if (!result) {
				String logMessage = "Failed to load tracker data.";
				// Error loading dataset
				Log.e(LOGTAG, logMessage);
				vuforiaException = new SampleApplicationException(
						SampleApplicationException.LOADING_TRACKERS_FAILURE,
						logMessage);
			} else {
				// Hint to the virtual machine that it would be a good time to
				// run the garbage collector:
				//
				// NOTE: This is only a hint. There is no guarantee that the
				// garbage collector will actually be run.
				System.gc();

				Vuforia.registerCallback(SampleApplicationSession.this);

				_started = true;
			}

			// Done loading the tracker, update application status, send the
			// exception to check errors
			_sessionControl.onInitARDone(vuforiaException);
		}
	}

	// Stores screen dimensions
	private void storeScreenDimensions() {
		// Query display dimensions:
		DisplayMetrics metrics = new DisplayMetrics();
		_activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		_screenWidth = metrics.widthPixels;
		_screenHeight = metrics.heightPixels;
	}

	// Stores the orientation depending on the current resources configuration
	private void updateActivityOrientation() {
		Configuration config = _activity.getResources().getConfiguration();

		switch (config.orientation) {
		case Configuration.ORIENTATION_PORTRAIT:
			_isPortrait = true;
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			_isPortrait = false;
			break;
		case Configuration.ORIENTATION_UNDEFINED:
		default:
			break;
		}

		Log.i(LOGTAG, "Activity is in "
				+ (_isPortrait ? "PORTRAIT" : "LANDSCAPE"));
	}

	// Method for setting / updating the projection matrix for AR content
	// rendering
	private void setProjectionMatrix() {
		CameraCalibration camCal = CameraDevice.getInstance()
				.getCameraCalibration();
		_projectionMatrix = Tool.getProjectionGL(camCal, 10.0f, 5000.0f);
	}

	private void stopCamera() {
		_sessionControl.doStopTrackers();
		CameraDevice.getInstance().stop();
		CameraDevice.getInstance().deinit();
	}

	// Applies auto focus if supported by the current device
	private boolean setFocusMode(int mode) throws SampleApplicationException {
		boolean result = CameraDevice.getInstance().setFocusMode(mode);

		if (!result)
			throw new SampleApplicationException(
					SampleApplicationException.SET_FOCUS_MODE_FAILURE,
					"Failed to set focus mode: " + mode);

		return result;
	}

	// Configures the video mode and sets offsets for the camera's image
	private void configureVideoBackground() {
		CameraDevice cameraDevice = CameraDevice.getInstance();
		VideoMode vm = cameraDevice
				.getVideoMode(CameraDevice.MODE.MODE_DEFAULT);

		VideoBackgroundConfig config = new VideoBackgroundConfig();
		config.setEnabled(true);
		config.setSynchronous(true);
		config.setPosition(new Vec2I(0, 0));

		int xSize = 0, ySize = 0;
		if (_isPortrait) {
			xSize = (int) (vm.getHeight() * (_screenHeight / (float) vm
					.getWidth()));
			ySize = _screenHeight;

			if (xSize < _screenWidth) {
				xSize = _screenWidth;
				ySize = (int) (_screenWidth * (vm.getWidth() / (float) vm
						.getHeight()));
			}
		} else {
			xSize = _screenWidth;
			ySize = (int) (vm.getHeight() * (_screenWidth / (float) vm
					.getWidth()));

			if (ySize < _screenHeight) {
				xSize = (int) (_screenHeight * (vm.getWidth() / (float) vm
						.getHeight()));
				ySize = _screenHeight;
			}
		}

		config.setSize(new Vec2I(xSize, ySize));

		Log.i(LOGTAG, "Configure Video Background : Video (" + vm.getWidth()
				+ " , " + vm.getHeight() + "), Screen (" + _screenWidth + " , "
				+ _screenHeight + "), mSize (" + xSize + " , " + ySize + ")");

		Renderer.getInstance().setVideoBackgroundConfig(config);

	}

	// Returns true if Vuforia is initialized, the trackers started and the
	// tracker data loaded
	private boolean isARRunning() {
		return _started;
	}

}
