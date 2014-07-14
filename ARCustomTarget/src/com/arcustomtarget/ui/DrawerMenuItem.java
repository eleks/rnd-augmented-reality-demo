package com.arcustomtarget.ui;

import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;

public class DrawerMenuItem {
	public String mCaption = "caption";
	public Drawable mDrawable = null;
	public Fragment mFragment = null;

	public DrawerMenuItem(String aCaption) {
		mCaption = aCaption;
	}

	public DrawerMenuItem(String aCaption, Drawable aDrawable) {
		mCaption = aCaption;
		mDrawable = aDrawable;
	}

	public DrawerMenuItem(String aCaption, Drawable aDrawable, Fragment aFragment) {
		mCaption = aCaption;
		mDrawable = aDrawable;
		mFragment = aFragment;
	}
	
}
