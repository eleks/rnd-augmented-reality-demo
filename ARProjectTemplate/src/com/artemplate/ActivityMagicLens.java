package com.artemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import com.ar.vuforiatemplate.objects.ARTexture;
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

		addDataset("vuforia/test.xml");

		// AR Module
		ARModule arModule;

		arModule = new ARModule();

		// MeshObjects
		arModule.addMeshObject("texture", new TextureObject());

		// Targets
		// "wiki"
		ARTexture wiki = new ARTexture(arModule.getMeshObject("texture"),
				arModule.getShader("hue_animation", true),
				"images/wikipedia_mask.png");
		arModule.addARObjectManagement("wiki", wiki);

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
		files.add("images/wikipedia_mask.png");

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
		// TODO Auto-generated method stub
	}

}
