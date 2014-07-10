package com.medicinemagazine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.ar.vuforiatemplate.core.ARModule;
import com.ar.vuforiatemplate.core.ARObjectsMediator;
import com.ar.vuforiatemplate.core.ActivityImageTargets;
import com.ar.vuforiatemplate.core.ActivityTargetsEvents;
import com.ar.vuforiatemplate.meshobjects.TextureObject;
import com.ar.vuforiatemplate.meshobjects.WavefrontModelObject;
import com.ar.vuforiatemplate.objects.AR3DObject;
import com.ar.vuforiatemplate.objects.ARVideo;
import com.ar.vuforiatemplate.shaders.NormalsShaders;
import com.ar.vuforiatemplate.shaders.OpenGLShaders;
import com.ar.vuforiatemplate.shaders.VideoShaders;
import com.ar.vuforiatemplate.ux.GestureInfo;
import com.ar.vuforiatemplate.ux.Gestures;
import com.ar.vuforiatemplate.ux.MultiGestureListener;
import com.qualcomm.vuforia.Trackable;

public class ActivityMagicLens extends ActivityImageTargets implements
		ActivityTargetsEvents, MultiGestureListener {

	private static final String LOGTAG = "ActivityMagicLens";
	private Gestures _gestures;

	// UI
	Button _extendedTrackingButton;

	public ActivityMagicLens() {
		super(R.id.loading_indicator, R.layout.camera_overlay);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(LOGTAG, "onCreate");

		addDataset("vuforia/Medicine.xml");

		// AR Module
		ARModule arModule;

		arModule = new ARModule();

		// MeshObjects
		arModule.addMeshObject("texture", new TextureObject());

		try {
			WavefrontModelObject wavefrontObject = new WavefrontModelObject();
			wavefrontObject.loadModel(getResources().getAssets(),
					"obj/Female_body_medium.obj");
			arModule.addMeshObject("skeleton", wavefrontObject);
		} catch (IOException e) {
			Log.e(LOGTAG, "Unable to load Female_body_medium.obj");
		}

		// Video
		ARVideo medicineVideo = new ARVideo(arModule.getMeshObject("texture"),
				arModule.getShader("video", true), "video/MedicineVideo.mp4", this);
		medicineVideo.mObjectRender.mScaleRelX = 0.8f;
		medicineVideo.mObjectRender.mScaleRelY = 0.2f;
		medicineVideo.mInternalCallBack = new VideoFullscreenCallBack(this,
				"video/MedicineVideo.mp4");
		arModule.addARObjectManagement("medicineVideo", medicineVideo);

		// Skeleton
		AR3DObject skeleton = new AR3DObject(
				arModule.getMeshObject("skeleton"), arModule.getShader(
						"simple_normal", true), "building/Buildings.jpeg");
		skeleton.setScale(100.0f);
		skeleton.setRotation(90.0f, 0.0f, 0.0f);
		skeleton.mNeedsExtendedTracking = true;
		arModule.addARObjectManagement("sceleton", skeleton);

		// create Objects Mediator
		_arObjectsMediator = new ARObjectsMediator(arModule);

		super.onCreate(savedInstanceState);

		// UI
		// Extended tracking button
		_extendedTrackingButton = (Button) findViewById(R.id.buttonExtended);
		_extendedTrackingButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (stopExtendedTracking())
					_extendedTrackingButton.setVisibility(View.INVISIBLE);
			}
		});

		// gestures
		_gestures = new Gestures(this, this);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (null != _gestures)
			_gestures.onTouchEvent(event);

		return super.onTouchEvent(event);
	}

	@Override
	public void loadTextures() {
		List<String> files = new ArrayList<String>();
		_arObjectsMediator.loadTextures(files, getAssets());
	}

	@Override
	public void updateActiveARObjects(Set<String> trackablesName) {
		super.updateActiveARObjects(trackablesName);

		// Extended tracking
		if (startExtendedTrackingIfNeeded(trackablesName)
				&& (null != _extendedTrackingButton)) {
			runOnUiThread(new Runnable() {
				public void run() {
					_extendedTrackingButton.setVisibility(View.VISIBLE);
				}
			});
		}

	}

	@Override
	public boolean onGesture(GestureInfo aGestureInfo) {
		boolean result = super.onGesture(aGestureInfo);

		return _arObjectsMediator.onGesture(aGestureInfo) || result;
	}

	@Override
	public void onTargetTrack(Trackable arg0) {
	}
	
	@Override
	public void compileShaders() {
		Map<String, OpenGLShaders> shaders = new TreeMap<String, OpenGLShaders>();
		shaders.put("simple_normal", new NormalsShaders());
		shaders.put("video", new VideoShaders());

		_arObjectsMediator.compileShaders(shaders);
	}

}
