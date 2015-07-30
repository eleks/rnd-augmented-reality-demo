package com.arquest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

import com.ar.vuforia4template.core.ARModule;
import com.ar.vuforia4template.core.ARObjectsMediator;
import com.ar.vuforia4template.core.ActivityImageTargets;
import com.ar.vuforia4template.core.ActivityTargetsEvents;
import com.ar.vuforia4template.meshobjects.TextureObject;
import com.ar.vuforia4template.objects.ARObjectManagement;
import com.ar.vuforia4template.objects.ARObjectRender.AspectRatioType;
import com.ar.vuforia4template.objects.ARTexture;
import com.ar.vuforia4template.objects.ARTextureAnimation;
import com.ar.vuforia4template.objects.ARVideo;
import com.ar.vuforia4template.shaders.HueAnimationShaders;
import com.ar.vuforia4template.shaders.NormalsShaders;
import com.ar.vuforia4template.shaders.OpenGLShaders;
import com.ar.vuforia4template.shaders.SimpleShaders;
import com.ar.vuforia4template.shaders.TransparentShaders;
import com.ar.vuforia4template.shaders.VideoShaders;
import com.ar.vuforia4template.ux.GestureInfo;
import com.ar.vuforia4template.ux.Gestures;
import com.ar.vuforia4template.ux.MultiGestureListener;
import com.qualcomm.vuforia.Trackable;

public class ActivityMagicLens extends ActivityImageTargets implements
		ActivityTargetsEvents, MultiGestureListener {

	private static final String LOGTAG = "ActivityMagicLens";
	private Gestures _gestures;

	// UI
	Button _hintButton;
	TextView _hintTextView;
	Button _extendedTrackingButton;

	public ActivityMagicLens() {
		super(R.id.loading_indicator, R.layout.camera_overlay);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(LOGTAG, "onCreate");

		addDataset("ARQuest.xml");
		//addDataset("LWIWBuch.xml");

		// AR Module
		ARModule arModule;

		arModule = new ARModule();

		// MeshObjects
		arModule.addMeshObject("texture", new TextureObject());

//		try {
//			WavefrontModelObject wavefrontObject = new WavefrontModelObject();
//			wavefrontObject.loadModel(getResources().getAssets(),
//					"obj/ZolochivCastle.obj");
//			// "obj/sphere.obj");
//			arModule.addMeshObject("castle", wavefrontObject);
//		} catch (IOException e) {
//			Log.e(LOGTAG, "Unable to load monkey.obj");
//		}

		// ARObjects
		// Text highlight
		ARTexture ar_text = new ARTexture(arModule.getMeshObject("texture"), //+
				arModule.getShader("hue_animation", true),
				"text_key_highlight.png");
		arModule.addARObjectManagement("text_key", ar_text);

		// Escher on device's screen
		ARTexture ar_escher = new ARTexture(arModule.getMeshObject("texture"),
				arModule.getShader("transparent", true), "escher_fish_duck_highlight.png");
		arModule.addARObjectManagement("escher_fish_duck", ar_escher);

		// Screen scan
		ARTexture ar_puzzle = new ARTexture(arModule.getMeshObject("texture"),
				arModule.getShader("transparent", true), "key.png");
		arModule.addARObjectManagement("puzzle4_ok", ar_puzzle);

		// Box to open
		ARTexture ar_doodle_box = new ARTexture(arModule.getMeshObject("texture"),
				arModule.getShader("transparent", true), "open_me.png");
		arModule.addARObjectManagement("doodle_box", ar_doodle_box);


		// create Objects Mediator
		_arObjectsMediator = new ARObjectsMediator(arModule);

		super.onCreate(savedInstanceState);

		// UI
		// Text View
		_hintTextView = (TextView) findViewById(R.id.textHint);

		// Button
		_hintButton = (Button) findViewById(R.id.buttonHint);
		_hintButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					_hintTextView.setVisibility(View.VISIBLE);
					return true;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					_hintTextView.setVisibility(View.INVISIBLE);
					return true;
				}
				return false;
			}
		});

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
		files.add("key.png");
		files.add("open_me.png");
		files.add("text_key_highlight.png");
		files.add("escher_fish_duck_highlight.png");

		_arObjectsMediator.loadTextures(files, getAssets());
	}

	class LevCallBack implements ARObjectManagement.CallBack {
		@Override
		public void callBackMethod() {
			Intent i = new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://en.wikipedia.org/wiki/Leo_I_of_Galicia"));
			startActivity(i);
		}
	}

	class LvivMapCallBack implements ARObjectManagement.CallBack {
		@Override
		public void callBackMethod() {
			Intent i = new Intent(
					Intent.ACTION_VIEW,
					Uri.parse("https://www.google.com/maps/@49.8490146,24.0309371,12z"));
			startActivity(i);
		}
	}

	class ZolochivMapCallBack implements ARObjectManagement.CallBack {
		@Override
		public void callBackMethod() {
			Intent i = new Intent(
					Intent.ACTION_VIEW,
					Uri.parse("https://www.google.com/maps/@49.8035344,24.8982579,15z"));
			startActivity(i);
		}
	}

	class RynokSqCallBack implements ARObjectManagement.CallBack {
		@Override
		public void callBackMethod() {
			Intent i = new Intent(
					Intent.ACTION_VIEW,
					Uri.parse("https://www.google.com/maps/dir//%D0%A0%D0%B0%D1%82%D1%83%D1%88%D0%B0,+%D0%BF%D0%BB.+%D0%A0%D0%B8%D0%BD%D0%BE%D0%BA,+1,+%D0%9B%D1%8C%D0%B2%D1%96%D0%B2,+%D0%9B%D1%8C%D0%B2%D1%96%D0%B2%D1%81%D1%8C%D0%BA%D0%B0+%D0%BE%D0%B1%D0%BB%D0%B0%D1%81%D1%82%D1%8C,+%D0%A3%D0%BA%D1%80%D0%B0%D1%97%D0%BD%D0%B0,+79000/@49.8417156,24.0314535,19z/data=!4m8!4m7!1m0!1m5!1m1!1s0x473add6daf2238f7:0x657ac2a6cdcf320b!2m2!1d24.0316915!2d49.8417365"));
			startActivity(i);
		}
	}

	class SvobodyAveCallBack implements ARObjectManagement.CallBack {
		@Override
		public void callBackMethod() {
			Intent i = new Intent(
					Intent.ACTION_VIEW,
					Uri.parse("https://www.google.com/maps/dir///@49.8433699,24.026633,20z/data=!4m3!4m2!1m0!1m0"));
			startActivity(i);
		}
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
	public void onTargetTrack(Trackable trackable) {
		Log.i(LOGTAG, "tracking: " + trackable.getName());
	}

	@Override
	public void compileShaders() {
		Map<String, OpenGLShaders> shaders = new TreeMap<String, OpenGLShaders>();
		shaders.put("simple", new SimpleShaders());
		shaders.put("simple_normal", new NormalsShaders());
		shaders.put("transparent", new TransparentShaders());
		shaders.put("hue_animation", new HueAnimationShaders());
		shaders.put("video", new VideoShaders());

		_arObjectsMediator.compileShaders(shaders);
	}

}
