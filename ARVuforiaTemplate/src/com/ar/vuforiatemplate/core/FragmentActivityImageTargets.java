/*==============================================================================
 Copyright (c) 2012-2013 Qualcomm Connected Experiences, Inc.
 All Rights Reserved.
 ==============================================================================*/

package com.ar.vuforiatemplate.core;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.Switch;

import com.ar.vuforiatemplate.customTarget.RefFreeFrame;
import com.ar.vuforiatemplate.objects.ARObjectManagement;
import com.ar.vuforiatemplate.objects.ARObjectRender;
import com.ar.vuforiatemplate.shaders.OpenGLShaders;
import com.ar.vuforiatemplate.utils.LoadingDialogHandler;
import com.ar.vuforiatemplate.utils.SampleApplicationControl;
import com.ar.vuforiatemplate.utils.SampleApplicationException;
import com.ar.vuforiatemplate.utils.SampleApplicationGLView;
import com.ar.vuforiatemplate.utils.SampleApplicationSession;
import com.ar.vuforiatemplate.utils.Texture;
import com.ar.vuforiatemplate.ux.GestureInfo;
import com.ar.vuforiatemplate.ux.MultiGestureListener;
import com.ar.vuforiatemplate.video.FullscreenPlayback;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.ImageTargetBuilder;
import com.qualcomm.vuforia.ImageTracker;
import com.qualcomm.vuforia.STORAGE_TYPE;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vuforia;

public abstract class FragmentActivityImageTargets extends FragmentActivity
		implements SampleApplicationControl, ActivityTargetsEvents,
		MultiGestureListener {
	private static final String LOGTAG = "ImageTargets";
	public static final int MAX_TRACKABLES = 10;

	// AR Mediator
	protected ARObjectsMediator _arObjectsMediator;

	private SampleApplicationSession _vuforiaAppSession;

	private DataSet _currentDataset;
	private int _currentDatasetSelectionIndex = 0;
	private ArrayList<String> _datasetStrings = new ArrayList<String>();

	// Our OpenGL view:
	private SampleApplicationGLView _GlView;

	// Our renderer:
	private ImageTargetRenderer2 _renderer;

	private boolean _switchDatasetAsap = false;
	private boolean _flash = false;

	private View _flashOptionView;

	// private RelativeLayout _UILayout;
	private ViewGroup _UILayout; // !!!

	private int _loadingIndicatorId;
	private int _cameraOverlayLayout;

	public LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(
			this);

	boolean _isDroidDevice = false;

	// Custom target
	int targetBuilderCounter = 1;
	private RefFreeFrame refFreeFrame;

	protected String _lastTargetName;

	//
	private String _targetNameTrackPrev = "";

	public FragmentActivityImageTargets(int aLoadingIndicatorId,
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

		// Load any sample specific textures:
		loadTextures();

		_isDroidDevice = android.os.Build.MODEL.startsWith("Droid");
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
		// Custom target
		refFreeFrame = new RefFreeFrame(this, _vuforiaAppSession);
		refFreeFrame.init();

		// Create OpenGL ES view:
		int depthSize = 16;
		int stencilSize = 0;
		boolean translucent = Vuforia.requiresAlpha();

		_GlView = new SampleApplicationGLView(this);
		_GlView.init(translucent, depthSize, stencilSize);

		_renderer = new ImageTargetRenderer2(this, _vuforiaAppSession);
		_GlView.setRenderer(_renderer);

		// Custom target
		initCustomTargetBuilder();
	}

	private void startLoadingAnimation() {
		LayoutInflater inflater = LayoutInflater.from(this);
		_UILayout = (ViewGroup) inflater.inflate(_cameraOverlayLayout, null,
				false);

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

		if ((_datasetStrings.size() > _currentDatasetSelectionIndex)
				&& (!_currentDataset.load(
						_datasetStrings.get(_currentDatasetSelectionIndex),
						STORAGE_TYPE.STORAGE_APPRESOURCE)))
			return false;

		if (!imageTracker.activateDataSet(_currentDataset))
			return false;

		int numTrackables = _currentDataset.getNumTrackables();
		Log.d(LOGTAG, "doLoadTrackersData with size : " + numTrackables);
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
			if (null != _UILayout) {
				// _UILayout.bringToFront();

				// Sets the layout background to transparent
				_UILayout.setBackgroundColor(Color.TRANSPARENT);
			}

			try {
				_vuforiaAppSession.startAR(CameraDevice.CAMERA.CAMERA_DEFAULT);
			} catch (SampleApplicationException e) {
				Log.e(LOGTAG, e.getString());
			}
		} else {
			Log.e(LOGTAG, exception.getString());
			finish();
		}

		Log.i(LOGTAG, "onInitARDone");
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

			changeDataSet("");
		}

		// Custom target
		{
			TrackerManager trackerManager = TrackerManager.getInstance();
			ImageTracker imageTracker = (ImageTracker) trackerManager
					.getTracker(ImageTracker.getClassType());

			if (refFreeFrame.hasNewTrackableSource()) {
				Log.d(LOGTAG,
						"Attempting to transfer the trackable source to the dataset");

				// Deactivate current dataset
				imageTracker.deactivateDataSet(imageTracker.getActiveDataSet());

				// Clear the oldest target if the dataset is full or the dataset
				// already contains five user-defined targets.
				if (_currentDataset.hasReachedTrackableLimit()
						|| _currentDataset.getNumTrackables() >= MAX_TRACKABLES)
					_currentDataset.destroy(_currentDataset.getTrackable(0));

				// if (mExtendedTracking && dataSetUserDef.getNumTrackables() >
				// 0)
				// {
				// // We need to stop the extended tracking for the previous
				// target
				// // so we can enable it for the new one
				// int previousCreatedTrackableIndex =
				// dataSetUserDef.getNumTrackables() - 1;
				//
				// dataSetUserDef.getTrackable(previousCreatedTrackableIndex)
				// .stopExtendedTracking();
				// }

				// Add new trackable source
				// Trackable trackable =
				_currentDataset.createTrackable(refFreeFrame
						.getNewTrackableSource());

				// Reactivate current dataset
				imageTracker.activateDataSet(_currentDataset);

				// if (mExtendedTracking)
				// {
				// trackable.startExtendedTracking();
				// }

			}
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

		// Custom target
		if (refFreeFrame != null)
			refFreeFrame.deInit();

		// Tracker manager
		TrackerManager tManager = TrackerManager.getInstance();
		tManager.deinitTracker(ImageTracker.getClassType());

		return result;
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
		if (aGestureInfo.mType == GestureInfo.GESTURE_POINTER_TAP) {
			// focus on tap
			focusCamera();

			// tap on AR object
			int numTrackables = _currentDataset.getNumTrackables();
			for (int i = 0; i < numTrackables; i++) {
				Trackable trackable = _currentDataset.getTrackable(i);
				String targetName = trackable.getName();
				if (_renderer != null
						&& _renderer.isTapOnScreenInsideTarget(targetName,
						// e.getX(), e.getY())) {
								0, 0)) {
					onTargetClicked(targetName);
				}
			}

			return false; // do not block gestures on AR Objects
		}

		return false;
	}

	public void focusCamera() {
		final Handler autofocusHandler = new Handler();
		autofocusHandler.postDelayed(new Runnable() {
			public void run() {
				boolean result = CameraDevice.getInstance().setFocusMode(
						CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);

				if (!result)
					Log.e("SingleTapUp", "Unable to trigger focus");
			}
		}, 500L);
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

		// changeDataSet("");
	}

	public void onTargetTrack(Trackable arg0) {
		if (!_targetNameTrackPrev.equals(arg0.getName())) {
			Log.i(LOGTAG, "!!! target :" + arg0.getName());
			_targetNameTrackPrev = arg0.getName();
		}
	}

	// Custom target methods
	public void targetCreated() {
		if (refFreeFrame != null)
			refFreeFrame.reset();
	}

	public Texture createTexture(String string) {
		return Texture.loadTextureFromApk(string, getAssets());
	}

	public boolean startBuild() {
		TrackerManager trackerManager = TrackerManager.getInstance();
		ImageTracker imageTracker = (ImageTracker) trackerManager
				.getTracker(ImageTracker.getClassType());

		if (imageTracker != null) {
			ImageTargetBuilder targetBuilder = imageTracker
					.getImageTargetBuilder();
			if (targetBuilder != null) {
				// show toast if the frame quality is Low
				if (targetBuilder.getFrameQuality() == ImageTargetBuilder.FRAME_QUALITY.FRAME_QUALITY_LOW) {
					// Context context = getApplicationContext();
					// CharSequence text =
					// "Frame quality was low ! Target is unreliable.";
					// int duration = Toast.LENGTH_LONG;
					// Toast toast = Toast.makeText(context, text, duration);
					// toast.show();
					return false;
				}

				String name;
				do {
					name = "UserTarget-" + targetBuilderCounter;
					_lastTargetName = name;
					Log.d(LOGTAG, "TRYING " + name);
					targetBuilderCounter++;
				} while (!targetBuilder.build(name, 320.0f));

				refFreeFrame.setCreating();
				return true;
			}
		}

		return false;
	}

	void updateRendering() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		refFreeFrame.initGL(metrics.widthPixels, metrics.heightPixels);
	}

	public boolean isUserDefinedTargetsRunning() {
		TrackerManager trackerManager = TrackerManager.getInstance();
		ImageTracker imageTracker = (ImageTracker) trackerManager
				.getTracker(ImageTracker.getClassType());

		if (imageTracker != null) {
			ImageTargetBuilder targetBuilder = imageTracker
					.getImageTargetBuilder();
			if (targetBuilder != null) {
				Log.e(LOGTAG, "Quality> " + targetBuilder.getFrameQuality());
				return (targetBuilder.getFrameQuality() != ImageTargetBuilder.FRAME_QUALITY.FRAME_QUALITY_NONE) ? true
						: false;
			}
		}

		return false;
	}

	private void initCustomTargetBuilder() {
		startUserDefinedTargets();
	}

	private boolean startUserDefinedTargets() {
		Log.d(LOGTAG, "startUserDefinedTargets");

		TrackerManager trackerManager = TrackerManager.getInstance();
		ImageTracker imageTracker = (ImageTracker) (trackerManager
				.getTracker(ImageTracker.getClassType()));
		if (imageTracker != null) {
			ImageTargetBuilder targetBuilder = imageTracker
					.getImageTargetBuilder();

			if (targetBuilder != null) {
				// if needed, stop the target builder
				if (targetBuilder.getFrameQuality() != ImageTargetBuilder.FRAME_QUALITY.FRAME_QUALITY_NONE)
					targetBuilder.stopScan();

				imageTracker.stop();

				targetBuilder.startScan();

			}
		} else
			return false;

		return true;
	}

	public void customTargetRenderer(int aTargetNumber) {
		refFreeFrame.render();
	}

	public void needTextTexture(String aText) {
		_renderer.needTextTexture(aText);
	}

	public void removeTargetFromCurrentDataset(String targetName) {
		TrackerManager tManager = TrackerManager.getInstance();
		ImageTracker imageTracker = (ImageTracker) tManager
				.getTracker(ImageTracker.getClassType());

		int sz = _currentDataset.getNumTrackables();
		for (int i = 0; i < sz; i++) {
			Trackable tr = _currentDataset.getTrackable(i);
			if (tr.getName().equals(targetName)) {
				imageTracker.deactivateDataSet(_currentDataset);
				_currentDataset.destroy(tr);
				imageTracker.activateDataSet(_currentDataset);
				Log.i(LOGTAG, "!!! target removed : " + targetName);
				break;
			}
		}
	}

	public boolean changeDataSet(String aNewDataSet) {
		Log.i(LOGTAG, "Change DataSet to : " + aNewDataSet);

		String oldDatasets = "";

		if (aNewDataSet == null || aNewDataSet.length() == 0) {
			if (_datasetStrings.size() > 0) {
				oldDatasets = _datasetStrings.get(0);
				for (int i = 1; i < _datasetStrings.size(); ++i)
					oldDatasets += " " + _datasetStrings.get(i);
			}
		}

		if (!aNewDataSet.equals(oldDatasets)) {
			_datasetStrings.clear();
			_datasetStrings.add(aNewDataSet);

			doUnloadTrackersData();
			doLoadTrackersData();

			onNewDataSet(oldDatasets, aNewDataSet);
			return true;
		}

		return false;
	}

	public void onNewDataSet(String aOldDataset, String aNewDataset) {
	}

	public Vector<String> getTargetsFromCurrentDataset() {
		Vector<String> vec = new Vector<String>();

		if (null != _currentDataset) {
			int numTrackables = _currentDataset.getNumTrackables();
			for (int count = 0; count < numTrackables; count++)
				vec.add(_currentDataset.getTrackable(count).getName());
		}

		return vec;
	}
}
