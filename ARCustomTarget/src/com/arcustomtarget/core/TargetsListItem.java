package com.arcustomtarget.core;

public class TargetsListItem {
	public String mCaption = "caption";
	public int mType = 0;
	public String mURL;

	public TargetsListItem(String aName) {
		mCaption = aName;
	}

	public TargetsListItem(String aName, int aType) {
		mCaption = aName;
		mType = aType;
	}

	public TargetsListItem(String aName, String aURL, int aType) {
		mCaption = aName;
		mURL = aURL;
		mType = aType;
	}

}
