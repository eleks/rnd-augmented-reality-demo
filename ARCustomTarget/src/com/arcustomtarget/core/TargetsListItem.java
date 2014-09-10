package com.arcustomtarget.core;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.ar.vuforiatemplate.core.ARObjectsMediator;
import com.ar.vuforiatemplate.core.FragmentActivityImageTargets;
import com.ar.vuforiatemplate.objects.ARObjectManagement;
import com.ar.vuforiatemplate.objects.ARObjectRender.AspectRatioType;
import com.ar.vuforiatemplate.objects.ARTexture;
import com.ar.vuforiatemplate.objects.ARVideo;
import com.ar.vuforiatemplate.video.FullscreenPlayback;
import com.arcustomtarget.TargetsFragment;

public class TargetsListItem {
	private static final String LOGTAG = "TargetsListItem";

	public final static int TARGET_TEXT = 0;
	public final static int TARGET_URL = 1;
	public final static int TARGET_VIDEO = 2;

	public String mCaption = "caption";
	public int mType = TARGET_TEXT;
	public String mData = "";
	public String mTargetName = "";
	public boolean mTracking = false;

	public TargetsListItem(String aName) {
		mCaption = aName;
	}

	public TargetsListItem(String aName, int aType) {
		mCaption = aName;
		mType = aType;
	}

	public TargetsListItem(String aName, String aData, int aType) {
		mCaption = aName;
		mData = aData;
		mType = aType;
	}

	public int getDrawableId() {
		switch (mType) {
		case TargetsListItem.TARGET_TEXT:
			return android.R.drawable.ic_menu_edit;

		case TargetsListItem.TARGET_URL:
			return android.R.drawable.ic_menu_directions;

		case TargetsListItem.TARGET_VIDEO:
			return android.R.drawable.ic_menu_gallery;

		default:
			return android.R.color.transparent;
		}
	}

	public String getDrawableCaption() {
		switch (mType) {
		case TargetsListItem.TARGET_TEXT:
			return "Text";

		case TargetsListItem.TARGET_URL:
			return "URL";

		case TargetsListItem.TARGET_VIDEO:
			return "Video";

		default:
			return "Unknown";
		}
	}

	public ARObjectManagement getARObjectManagement(
			FragmentActivityImageTargets aActivity, ARObjectsMediator arMediator) {
		if (mType == TARGET_URL) {
			ARTexture ar = new ARTexture(arMediator.getModule().getMeshObject(
					"texture"), arMediator.getModule().getShader("transparent",
					true), "images/www_icon.png");
			ar.mObjectRender.mAspectRatioType = AspectRatioType.QUADRATE_INSIDE;
			ar.mObjectRender.setRotation(0.f, 0.f, 90.f);
			ar.mCallBack = new URLCallBack(aActivity, mData);
			return ar;
		}

		if (mType == TARGET_TEXT) {
			Log.i(LOGTAG, "!!!! new text target : " + mData);
			aActivity.needTextTexture(mData);
			ARTexture ar = new ARTexture(arMediator.getModule().getMeshObject(
					"texture"), arMediator.getModule().getShader("transparent",
					true), mData);
			ar.mObjectRender.mAspectRatioType = AspectRatioType.FILL_TARGET;
			return ar;
		}

		if (mType == TARGET_VIDEO) {
			if (mData.equals(TargetsFragment.DATA_VIDEO)) {
				// FIXME: hardcode
				mData = "video/Eleks.mp4";
			}

			ARVideo ar = new ARVideo(arMediator.getModule().getMeshObject(
					"texture"),
					arMediator.getModule().getShader("video", true), mData,
					aActivity);
			ar.mInternalCallBack = new VideoFullscreenCallBack(aActivity, mData);
			ar.mObjectRender.mAspectRatioType = AspectRatioType.FIT_INSIDE;
			ar.mObjectRender.setAspectRatio(0.5f);
			ar.mObjectRender.setRotation(0.f, 0.f, 90.f);
			return ar;
		}

		return new ARTexture(arMediator.getModule().getMeshObject("texture"),
				arMediator.getModule().getShader("hue_animation", true),
				"images/wikipedia_mask.png");
	}

	class URLCallBack implements ARObjectManagement.CallBack {
		private String _URL;
		private Activity _activity;

		public URLCallBack(Activity aActivity, String aURL) {
			_activity = aActivity;
			_URL = aURL;
		}

		@Override
		public void callBackMethod() {
			try {
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(_URL));
				_activity.startActivity(i);
			} catch (Exception e) {
				Log.e(LOGTAG, "ERROR: " + e.getMessage());
			}
		}
	}

	class VideoFullscreenCallBack implements ARObjectManagement.CallBack {
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
			intent.putExtra(FullscreenPlayback.EXTRA_CLOSE_ON_TAP, true);
			_activity.startActivityForResult(intent, 1);
		}
	}

}
