package com.ar.vuforiatemplate.objects;

import android.util.Log;

import com.ar.vuforiatemplate.meshobjects.MeshObject;
import com.ar.vuforiatemplate.shaders.OpenGLBinaryShaders;
import com.ar.vuforiatemplate.ux.GestureInfo;
import com.qualcomm.vuforia.TrackableResult;

public class AR3DObject extends ARObjectManagement {
	private static String LOGTAG = "AR3DObject";

	private double _startScaleX;
	private double _startScaleY;
	private double _startScaleZ;

	public AR3DObject(MeshObject aMeshObject, OpenGLBinaryShaders aShader,
			String aFileName) {
		super(aMeshObject, aShader);

		mObjectRender.mTextureName = aFileName;
	}

	@Override
	public void onTrackingStart(TrackableResult aResult) {
		super.onTrackingStart(aResult);
	}

	public void setScale(float aScale) {
		mObjectRender.mScaleX = aScale;
		mObjectRender.mScaleY = aScale;
		mObjectRender.mScaleZ = aScale;
	}

	public void setRotation(float aX, float aY, float aZ) {
		mObjectRender.mRotationX = aX;
		mObjectRender.mRotationY = aY;
		mObjectRender.mRotationZ = aZ;
	}

	@Override
	public boolean onActivityGesture(GestureInfo aInfo) {
		Log.i(LOGTAG, "on GESTURE !!! : " + aInfo.mValue);

		switch (aInfo.mType) {
		// Scale
		case GestureInfo.GESTURE_PINCH:
			switch (aInfo.mState) {
			case GestureInfo.STATE_START:
				_startScaleX = mObjectRender.mScaleX;
				_startScaleY = mObjectRender.mScaleY;
				_startScaleZ = mObjectRender.mScaleZ;
				return true;
			case GestureInfo.STATE_MOVE:
				mObjectRender.mScaleX = _startScaleX * aInfo.mValue;
				mObjectRender.mScaleY = _startScaleY * aInfo.mValue;
				mObjectRender.mScaleZ = _startScaleZ * aInfo.mValue;
				return true;
			case GestureInfo.STATE_FINISH:
				return true;
			}
			break;

		// Move
		case GestureInfo.GESTURE_MOVE_X:
		case GestureInfo.GESTURE_MOVE_Y:
			mObjectRender.mRotationY += 4.0f * aInfo.mValue;
			return true;
		}

		return false;
	}
}
