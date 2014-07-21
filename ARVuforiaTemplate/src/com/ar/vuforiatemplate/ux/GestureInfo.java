package com.ar.vuforiatemplate.ux;

public class GestureInfo {
	public final static int STATE_START = 100;
	public final static int STATE_MOVE = 101;
	public final static int STATE_FINISH = 102;

	public final static int GESTURE_NONE = 200;
	public final static int GESTURE_PINCH = 201;
	public final static int GESTURE_SWIPE_LEFT = 202;
	public final static int GESTURE_SWIPE_RIGHT = 203;
	public final static int GESTURE_SWIPE_TOP = 204;
	public final static int GESTURE_SWIPE_BOTTOM = 205;
	public final static int GESTURE_MOVE_X = 206;
	public final static int GESTURE_MOVE_Y = 207;
	public final static int GESTURE_POINTER_TAP = 208;
	public final static int GESTURE_DOUBLE_TAP = 209;
	public final static int GESTURE_SINGLE_TAP_UP = 210;

	public int mType = GESTURE_NONE;
	public int mState;
	public float mValue;

	public GestureInfo(int aType, int aState, float aValue) {
		mType = aType;
		mState = aState;
		mValue = aValue;
	}

}
