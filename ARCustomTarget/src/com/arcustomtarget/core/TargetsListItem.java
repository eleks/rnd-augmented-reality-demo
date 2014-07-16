package com.arcustomtarget.core;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.ar.vuforiatemplate.core.ARModule;
import com.ar.vuforiatemplate.objects.ARObjectManagement;
import com.ar.vuforiatemplate.objects.ARTexture;

public class TargetsListItem {
	public final static int TARGET_TEXT = 0;
	public final static int TARGET_URL = 1;
	public final static int TARGET_VIDEO = 2;

	public String mCaption = "caption";
	public int mType = TARGET_TEXT;
	public String mData = "www.google.com";

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
			return android.R.drawable.ic_dialog_email;

		case TargetsListItem.TARGET_URL:
			return android.R.drawable.ic_dialog_info;

		case TargetsListItem.TARGET_VIDEO:
			return android.R.drawable.ic_dialog_map;

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

	public ARObjectManagement getARObjectManagement(Activity aActivity,
			ARModule arModule) {
		if (mType == TARGET_URL) {
			ARTexture ar = new ARTexture(arModule.getMeshObject("texture"),
					arModule.getShader("transparent", true),
					"images/www_icon.png");
			ar.mCallBack = new URLCallBack(aActivity, mData);
			return ar;
		}

		return new ARTexture(arModule.getMeshObject("texture"),
				arModule.getShader("hue_animation", true),
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
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(_URL));
			_activity.startActivity(i);
		}
	}
}
