/*==============================================================================
 Copyright (c) 2012-2013 Qualcomm Connected Experiences, Inc.
 All Rights Reserved.
 ==============================================================================*/

package com.ar.vuforiatemplate.core;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.ar.vuforiatemplate.objects.ARObjectManagement;
import com.ar.vuforiatemplate.objects.ARObjectRender;
import com.ar.vuforiatemplate.shaders.OpenGLShaders;
import com.ar.vuforiatemplate.utils.LoadingDialogHandler;
import com.ar.vuforiatemplate.utils.SampleApplicationControl;
import com.ar.vuforiatemplate.utils.SampleApplicationException;
import com.ar.vuforiatemplate.utils.SampleApplicationGLView;
import com.ar.vuforiatemplate.utils.SampleApplicationSession;
import com.ar.vuforiatemplate.ux.GestureInfo;
import com.ar.vuforiatemplate.ux.MultiGestureListener;
import com.ar.vuforiatemplate.video.FullscreenPlayback;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.ImageTracker;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vuforia;

public abstract class ActivityImageTargets extends Activity implements
		SampleApplicationControl, ActivityTargetsEvents, MultiGestureListener {
	private static final String LOGTAG = "ImageTargets";

	// AR Mediator
	protected ARObjectsMediator _arObjectsMediator;

	private SampleApplicationSession _vuforiaAppSession;

	private DataSet _currentDataset;
	private int _currentDatasetSelectionIndex = 0;
	private ArrayList<String> _datasetStrings = new ArrayList<String>();

	// Our OpenGL view:
	private SampleApplicationGLView _GlView;

	// Our renderer:
	private ImageTargetRenderer _renderer;

	private GestureDetector _gestureDetector = null;
	private SimpleOnGestureListener _simpleListener = null;

	private boolean _switchDatasetAsap = false;
	private boolean _flash = false;

	private View _flashOptionView;

	private RelativeLayout _UILayout;

	private int _loadingIndicatorId;
	private int _cameraOverlayLayout;

	public LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(
			this);

	boolean _isDroidDevice = false;

	public ActivityImageTargets(int aLoadingIndicatorId,
			int aCameraOverlayLayout) {
		_loadingIndicatorId = aLoadingIndicatorId;
		_cameraOverlayLayout = aCameraOverlayLayout;
	}

	// Called when the activity first starts or the user navigates back to an
	// activity.
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(LOGTAG, "onCreate");
		super.onCreate(savedInstanceState);

		_vuforiaAppSession = new SampleApplicationSession(this);

		startLoadingAnimation();

		_vuforiaAppSession.initAR(this,
				ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		_simpleListener = new SimpleOnGestureListener();
		_gestureDetector = new GestureDetector(this, _simpleListener);

		// Load any sample specific textures:
		loadTextures();

		_isDroidDevice = android.os.Build.MODEL.startsWith("Droid");

		_gestureDetector.setOnDoubleTapListener(new OnDoubleTapListener() {
			public boolean onDoubleTapEvent(MotionEvent e) {
				// We do not react to this event
				return false;
			}

			@Override
			public boolean onDoubleTap(MotionEvent arg0) {
				return false;
			}

			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				int numTrackables = _currentDataset.getNumTrackables();
				for (int i = 0; i < numTrackables; i++) {
					Trackable trackable = _currentDataset.getTrackable(i);
					String targetName = trackable.getName();
					if (_renderer != null
							&& _renderer.isTapOnScreenInsideTarget(targetName,
									e.getX(), e.getY())) {
						onTargetClicked(targetName);
					}
				}
				return false;
			}
		});
	}

	protected void addDataset(String aDataSet) {
		_datasetStrings.add(aDataSet);
	}

	// Called when the activity will start interacting with the user.
	@Override
	protected void onResume() {
		Log.d(LOGTAG, "onResume");
		super.onResume();

		// This is needed for some Droid devices to force portrait
		if (_isDroidDevice) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		try {
			_vuforiaAppSession.resumeAR();
		} catch (SampleApplicationException e) {
			Log.e(LOGTAG, e.getString());
		}

		// Resume the GL view:
		if (_GlView != null) {
			_GlView.setVisibility(View.VISIBLE);
			_GlView.onResume();
		}

		if (null != _arObjectsMediator)
			_arObjectsMediator.onActivityResume();
	}

	// Callback for configuration changes the activity handles itself
	@Override
	public void onConfigurationChanged(Configuration config) {
		Log.d(LOGTAG, "onConfigurationChanged");
		super.onConfigurationChanged(config);

		_vuforiaAppSession.onConfigurationChanged();
	}

	// Called when the system is about to start resuming a previous activity.
	@SuppressLint("NewApi")
	@Override
	protected void onPause() {
		Log.d(LOGTAG, "onPause");
		super.onPause();

		if (_GlView != null) {
			_GlView.setVisibility(View.INVISIBLE);
			_GlView.onPause();
		}

		// Turn off the flash
		if (_flashOptionView != null && _flash) {
			// OnCheckedChangeListener is called upon changing the checked state
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				((Switch) _flashOptionView).setChecked(false);
			} else {
				((CheckBox) _flashOptionView).setChecked(false);
			}
		}

		try {
			_vuforiaAppSession.pauseAR();
		} catch (SampleApplicationException e) {
			Log.e(LOGTAG, e.getString());
		}

		if (null != _arObjectsMediator)
			_arObjectsMediator.onActivityPause();
	}

	// The final call you receive before your activity is destroyed.
	@Override
	protected void onDestroy() {
		Log.d(LOGTAG, "onDestroy");
		super.onDestroy();

		try {
			_vuforiaAppSession.stopAR();
		} catch (SampleApplicationException e) {
			Log.e(LOGTAG, e.getString());
		}

		System.gc();

		_arObjectsMediator.clearTextures();
	}

	// Initializes AR application components.
	private void initApplicationAR() {
		// Create OpenGL ES view:
		int depthSize = 16;
		int stencilSize = 0;
		boolean translucent = Vuforia.requiresAlpha();

		_GlView = new SampleApplicationGLView(this);
		_GlView.init(translucent, depthSize, stencilSize);

		_renderer = new ImageTargetRenderer(this, _vuforiaAppSession);
		_GlView.setRenderer(_renderer);
	}

	private void startLoadingAnimation() {
		LayoutInflater inflater = LayoutInflater.from(this);
		_UILayout = (RelativeLayout) inflater.inflate(_cameraOverlayLayout,
				null, false);

		_UILayout.setVisibility(View.VISIBLE);
		_UILayout.setBackgroundColor(Color.BLACK);

		// Gets a reference to the loading dialog
		loadingDialogHandler.mLoadingDialogContainer = _UILayout
				.findViewById(_loadingIndicatorId);

		// Shows the loading indicator at start
		loadingDialogHandler
				.sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);

		// Adds the inflated layout to the view
		addContentView(_UILayout, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
	}

	// Methods to load and destroy tracking data.
	@Override
	public boolean doLoadTrackersData() {
		TrackerManager tManager = TrackerManager.getInstance();
		ImageTracker imageTracker = (ImageTracker) tManager
				.getTracker(ImageTracker.getClassType());
		if (imageTracker == null)
			return false;

		if (_currentDataset == null)
			_currentDataset = imageTracker.createDataSet();

		if (_currentDataset == null)
			return false;

		if (!_currentDataset.load(
				_datasetStrings.get(_currentDatasetSelectionIndex),
				DataSet.STORAGE_TYPE.STORAGE_APPRESOURCE))
			return false;

		if (!imageTracker.activateDataSet(_currentDataset))
			return false;

		int numTrackables = _currentDataset.getNumTrackables();
		for (int count = 0; count < numTrackables; count++) {
			Trackable trackable = _currentDataset.getTrackable(count);

			String name = "Current Dataset : " + trackable.getName();
			trackable.setUserData(name);
			Log.d(LOGTAG, "UserData:Set the following user data "
					+ (String) trackable.getUserData());
		}

		return true;
	}

	@Override
	public boolean doUnloadTrackersData() {
		// Indicate if the trackers were unloaded correctly
		boolean result = true;

		TrackerManager tManager = TrackerManager.getInstance();
		ImageTracker imageTracker = (ImageTracker) tManager
				.getTracker(ImageTracker.getClassType());
		if (imageTracker == null)
			return false;

		if (_currentDataset != null && _currentDataset.isActive()) {
			if (imageTracker.getActiveDataSet().equals(_currentDataset)
					&& !imageTracker.deactivateDataSet(_currentDataset)) {
				result = false;
			} else if (!imageTracker.destroyDataSet(_currentDataset)) {
				result = false;
			}

			_currentDataset = null;
		}

		return result;
	}

	@Override
	public void onInitARDone(SampleApplicationException exception) {

		if (exception == null) {
			initApplicationAR();

			_renderer.mIsActive = true;

			// Now add the GL surface view. It is important
			// that the OpenGL ES surface view gets added
			// BEFORE the camera is started and video
			// background is configured.
			addContentView(_GlView, new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));

			// Sets the UILayout to be drawn in front of the camera
			_UILayout.bringToFront();

			// Sets the layout background to transparent
			_UILayout.setBackgroundColor(Color.TRANSPARENT);

			try {
				_vuforiaAppSession.startAR(CameraDevice.CAMERA.CAMERA_DEFAULT);
			} catch (SampleApplicationException e) {
				Log.e(LOGTAG, e.getString());
			}
		} else {
			Log.e(LOGTAG, exception.getString());
			finish();
		}
	}

	@Override
	public void onQCARUpdate(State state) {
		if (_switchDatasetAsap) {
			_switchDatasetAsap = false;
			TrackerManager tm = TrackerManager.getInstance();
			ImageTracker it = (ImageTracker) tm.getTracker(ImageTracker
					.getClassType());
			if (it == null || _currentDataset == null
					|| it.getActiveDataSet() == null) {
				Log.d(LOGTAG, "Failed to swap datasets");
				return;
			}

			doUnloadTrackersData();
			doLoadTrackersData();
		}
	}

	@Override
	public boolean doInitTrackers() {
		// Indicate if the trackers were initialized correctly
		boolean result = true;

		TrackerManager tManager = TrackerManager.getInstance();
		Tracker tracker;

		// Trying to initialize the image tracker
		tracker = tManager.initTracker(ImageTracker.getClassType());
		if (tracker == null) {
			Log.e(LOGTAG,
					"Tracker not initialized. Tracker already initialized or the camera is already started");
			result = false;
		} else {
			Log.i(LOGTAG, "Tracker successfully initialized");
		}
		return result;
	}

	@Override
	public boolean doStartTrackers() {
		// Indicate if the trackers were started correctly
		boolean result = true;

		Tracker imageTracker = TrackerManager.getInstance().getTracker(
				ImageTracker.getClassType());
		if (imageTracker != null)
			imageTracker.start();

		return result;
	}

	@Override
	public boolean doStopTrackers() {
		// Indicate if the trackers were stopped correctly
		boolean result = true;

		Tracker imageTracker = TrackerManager.getInstance().getTracker(
				ImageTracker.getClassType());
		if (imageTracker != null)
			imageTracker.stop();

		return result;
	}

	@Override
	public boolean doDeinitTrackers() {
		// Indicate if the trackers were deinitialized correctly
		boolean result = true;

		TrackerManager tManager = TrackerManager.getInstance();
		tManager.deinitTracker(ImageTracker.getClassType());

		return result;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return _gestureDetector.onTouchEvent(event);
	}

	final public static int CMD_BACK = -1;
	final public static int CMD_EXTENDED_TRACKING = 1;
	final public static int CMD_AUTOFOCUS = 2;
	final public static int CMD_FLASH = 3;
	final public static int CMD_CAMERA_FRONT = 4;
	final public static int CMD_CAMERA_REAR = 5;
	final public static int CMD_DATASET_START_INDEX = 6;

	protected boolean startExtendedTrackingIfNeeded(Set<String> trackablesName) {
		int numTrackables = _currentDataset.getNumTrackables();
		boolean started = false;

		for (int count = 0; count < numTrackables; count++) {
			Trackable trackable = _currentDataset.getTrackable(count);
			String name = trackable.getName();
			Log.d(LOGTAG, "TARGET : " + name); // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			if (_arObjectsMediator.needsExtendedTracking(name)
					&& trackablesName.contains(name)) {
				started |= trackable.startExtendedTracking();
			}
		}

		return started;
	}

	protected boolean stopExtendedTracking() {
		int numTrackables = _currentDataset.getNumTrackables();
		boolean stopped = false;

		for (int count = 0; count < numTrackables; count++) {
			Trackable trackable = _currentDataset.getTrackable(count);
			if (trackable.stopExtendedTracking())
				stopped = true;
		}

		return stopped;
	}

	@Override
	public boolean onGesture(GestureInfo aGestureInfo) {
		// focus on tap
		if (aGestureInfo.mType == GestureInfo.GESTURE_SINGLE_TAP_UP) {
			final Handler autofocusHandler = new Handler();
			autofocusHandler.postDelayed(new Runnable() {
				public void run() {
					boolean result = CameraDevice.getInstance().setFocusMode(
							CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);

					if (!result)
						Log.e("SingleTapUp", "Unable to trigger focus");
				}
			}, 500L);

			return true;
		}

		return false;
	}

	public class VideoFullscreenCallBack implements ARObjectManagement.CallBack {
		private Activity _activity;
		private String _fileName;

		public VideoFullscreenCallBack(Activity aActivity, String aFileName) {
			_activity = aActivity;
			_fileName = aFileName;
		}

		public void callBackMethod() {
			Intent intent = new Intent(_activity, FullscreenPlayback.class);
			intent.putExtra("movieName", _fileName);
			intent.putExtra("shouldPlayImmediately", true);
			intent.putExtra("currentSeekPosition", 0);
			intent.putExtra("requestedOrientation", 0);
			startActivityForResult(intent, 1);
		}
	}

	@Override
	public void onTargetClicked(String targetName) {
		if (!_arObjectsMediator.onTargetClicked(targetName)) {
			// do smth.
		}
	}

	@Override
	public void onTargetBeforeRender(String targetName, TrackableResult aResult) {
		_arObjectsMediator.onTargetBeforeRender(targetName, aResult);
	}

	@Override
	public void initRendering() {
		_arObjectsMediator.initRendering();
	}

	public void compileShaders(Map<String, OpenGLShaders> aShaders) {
		_arObjectsMediator.compileShaders(aShaders);
	}

	@Override
	public ARObjectRender getRenderObject(String aTargetName,
			TrackableResult aResult) {
		return _arObjectsMediator.getRenderObject(aTargetName, aResult);
	}

	@Override
	public ARObjectsMediator getARObjectsMediator() {
		return _arObjectsMediator;
	}

	@Override
	public void updateActiveARObjects(Set<String> trackablesName) {
		// AR mediator update active object
		_arObjectsMediator.updateActiveAR(trackablesName);
	}

}
