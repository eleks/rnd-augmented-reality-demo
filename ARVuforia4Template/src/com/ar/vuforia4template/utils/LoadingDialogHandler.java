/*==============================================================================
 Copyright (c) 2012-2013 Qualcomm Connected Experiences, Inc.
 All Rights Reserved.
 ==============================================================================*/

package com.ar.vuforia4template.utils;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.View;

public final class LoadingDialogHandler extends Handler {
	private final WeakReference<Activity> _activity;
	// Constants for Hiding/Showing Loading dialog
	public static final int HIDE_LOADING_DIALOG = 0;
	public static final int SHOW_LOADING_DIALOG = 1;

	public View mLoadingDialogContainer;

	public LoadingDialogHandler(Activity activity) {
		_activity = new WeakReference<Activity>(activity);
	}

	public void handleMessage(Message msg) {
		Activity imageTargets = _activity.get();
		if (imageTargets == null) {
			return;
		}

		if (null != mLoadingDialogContainer) {
			if (msg.what == SHOW_LOADING_DIALOG) {
				mLoadingDialogContainer.setVisibility(View.VISIBLE);
	
			} else if (msg.what == HIDE_LOADING_DIALOG) {
				mLoadingDialogContainer.setVisibility(View.GONE);
			}
		}

	}

}
