package com.arcustomtarget.core;

public class TargetsListItem {
	public final static int TARGET_TEXT = 0;
	public final static int TARGET_URL = 1;
	public final static int TARGET_VIDEO = 2;

	public String mCaption = "caption";
	public int mType = TARGET_TEXT;
	public String mData;

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

}
