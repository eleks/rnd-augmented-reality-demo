package com.lwiwbuch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

import com.ar.vuforiatemplate.core.ARModule;
import com.ar.vuforiatemplate.core.ARObjectsMediator;
import com.ar.vuforiatemplate.core.ActivityImageTargets;
import com.ar.vuforiatemplate.core.ActivityTargetsEvents;
import com.ar.vuforiatemplate.meshobjects.TextureObject;
import com.ar.vuforiatemplate.meshobjects.WavefrontModelObject;
import com.ar.vuforiatemplate.objects.AR3DObject;
import com.ar.vuforiatemplate.objects.ARObjectManagement;
import com.ar.vuforiatemplate.objects.ARTexture;
import com.ar.vuforiatemplate.objects.ARTextureAnimation;
import com.ar.vuforiatemplate.objects.ARVideo;
import com.ar.vuforiatemplate.ux.GestureInfo;
import com.ar.vuforiatemplate.ux.Gestures;
import com.ar.vuforiatemplate.ux.MultiGestureListener;
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

		boolean medicineMagazine = false;

		if (medicineMagazine) {
			addDataset("Medicine.xml");
		} else {
			addDataset("LWIWBuch.xml");
		}

		// AR Module
		ARModule arModule;

		arModule = new ARModule();

		// MeshObjects
		arModule.addMeshObject("texture", new TextureObject());

		if (!medicineMagazine) {
			try {
				WavefrontModelObject wavefrontObject = new WavefrontModelObject();
				wavefrontObject.loadModel(getResources().getAssets(),
				// "obj/ZolochivCastle.obj");
						"obj/sphere.obj");
				arModule.addMeshObject("castle", wavefrontObject);
			} catch (IOException e) {
				Log.e(LOGTAG, "Unable to load monkey.obj");
			}
		}

		if (medicineMagazine) {
			try {
				WavefrontModelObject wavefrontObject = new WavefrontModelObject();
				wavefrontObject.loadModel(getResources().getAssets(),
						"obj/skeleton_low.obj");
				arModule.addMeshObject("skeleton", wavefrontObject);
			} catch (IOException e) {
				Log.e(LOGTAG, "Unable to load skeleton_low.obj");
			}
		}

		// Logo
		if (!medicineMagazine) {
			// ARObjects
			// Cover
			Vector<String> coverTexAnim = new Vector<String>();
			coverTexAnim.add("cover_ar_yellow_alpha.png");
			coverTexAnim.add("cover_ar_red_alpha.png");

			ARTextureAnimation cover = new ARTextureAnimation(
					arModule.getMeshObject("texture"), arModule.getShader(
							"transparent", true), coverTexAnim);
			arModule.addARObjectManagement("cover", cover);

			// Lwiw buch logo animation
			ARTexture logo = new ARTexture(arModule.getMeshObject("texture"),
					arModule.getShader("hue_animation", true),
					"LWIWBuchLogo_mask.png");
			arModule.addARObjectManagement("LWIWBuchLogo", logo);

			// Lev Danylovych Wikipedia
			ARTexture lev = new ARTexture(arModule.getMeshObject("texture"),
					arModule.getShader("transparent", true), "wiki_logo.png");
			lev.mCallBack = new LevCallBack();
			arModule.addARObjectManagement("LevDanylovych", lev);

			// Lviv Map
			ARTexture lvivMap = new ARTexture(
					arModule.getMeshObject("texture"), arModule.getShader(
							"transparent", true), "googlemaps_icon.png");
			lvivMap.mCallBack = new LvivMapCallBack();
			arModule.addARObjectManagement("LvivMap", lvivMap);

			// Zolochiv Map
			ARTexture zolochivMap = new ARTexture(
					arModule.getMeshObject("texture"), arModule.getShader(
							"transparent", true), "googlemaps_icon.png");
			zolochivMap.mCallBack = new ZolochivMapCallBack();
			arModule.addARObjectManagement("MapZolochiv", zolochivMap);

			// Rynok sq.
			ARTexture rynokSq = new ARTexture(
					arModule.getMeshObject("texture"), arModule.getShader(
							"transparent", true), "directions_icon.png");
			rynokSq.mCallBack = new RynokSqCallBack();
			arModule.addARObjectManagement("RynokSq", rynokSq);

			// Svobody ave.
			ARTexture svobodyAve = new ARTexture(
					arModule.getMeshObject("texture"), arModule.getShader(
							"transparent", true), "directions_icon.png");
			svobodyAve.mCallBack = new SvobodyAveCallBack();
			arModule.addARObjectManagement("SvobodyAve", svobodyAve);

			// Boim Kapelle
			ARVideo boimKapelle = new ARVideo(
					arModule.getMeshObject("texture"), arModule.getShader(
							"video", true), "Video.mp4", this);
			boimKapelle.mObjectRender.mScaleRelX = 0.8f;
			boimKapelle.mObjectRender.mScaleRelY = 0.4f;
			boimKapelle.mInternalCallBack = new VideoFullscreenCallBack(this,
					"Video.mp4");
			arModule.addARObjectManagement("BoimKapelle", boimKapelle);

			// Back cover
			AR3DObject backCover = new AR3DObject(
					arModule.getMeshObject("castle"), arModule.getShader(
							"simple_normal", true), "building/Buildings.jpeg");
			backCover.setScale(5.0f);
			backCover.setRotation(90.0f, 0.0f, 0.0f);
			backCover.mNeedsExtendedTracking = true;
			arModule.addARObjectManagement("CoverLast", backCover);
		}

		// --- MEDICINE BOOK --- ///
		if (medicineMagazine) {
			// Video
			ARVideo medicineVideo = new ARVideo(
					arModule.getMeshObject("texture"), arModule.getShader(
							"video", true), "MedicineVideo.mp4", this);
			medicineVideo.mObjectRender.mScaleRelX = 0.8f;
			medicineVideo.mObjectRender.mScaleRelY = 0.2f;
			medicineVideo.mInternalCallBack = new VideoFullscreenCallBack(this,
					"MedicineVideo.mp4");
			arModule.addARObjectManagement("medicineVideo", medicineVideo);

			// Skeleton
			AR3DObject skeleton = new AR3DObject(
					arModule.getMeshObject("skeleton"), arModule.getShader(
							"simple_normal", true), "building/Buildings.jpeg");
			skeleton.setScale(100.0f);
			skeleton.setRotation(90.0f, 0.0f, 0.0f);
			skeleton.mNeedsExtendedTracking = true;
			arModule.addARObjectManagement("sceleton", skeleton);
		}

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
		files.add("cover_ar_yellow_alpha.png");
		files.add("cover_ar_red_alpha.png");
		files.add("LWIWBuchLogo_mask.png");
		files.add("cover_last.jpg");
		files.add("wiki_logo.png");
		files.add("directions_icon.png");
		files.add("googlemaps_icon.png");
		files.add("building/Buildings.jpeg");

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
	public void onTargetTrack(Trackable arg0) {
	}

}
