package com.ar.vuforiatemplate.objects;

import android.util.Log;

import com.ar.vuforiatemplate.meshobjects.MeshObject;
import com.ar.vuforiatemplate.objects.ARObjectRender.AspectRatioType;
import com.ar.vuforiatemplate.shaders.OpenGLBinaryShaders;
import com.ar.vuforiatemplate.ux.GestureInfo;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.Vec3F;

public class AR3DObject extends ARObjectManagement {
	private static String LOGTAG = "AR3DObject";

	private Vec3F _startScale;

	public AR3DObject(MeshObject aMeshObject, OpenGLBinaryShaders aShader,
			String aFileName) {
		super(aMeshObject, aShader);

		mObjectRender.mTextureName = aFileName;
		mObjectRender.mAspectRatioType = AspectRatioType.CUSTOM;
	}

	@Override
	public void onTrackingStart(TrackableResult aResult) {
		super.onTrackingStart(aResult);
	}

	public void setScale(float aScale) {
		mObjectRender.setCustomScale(aScale, aScale, aScale);
	}

	public void setRotation(float aX, float aY, float aZ) {
		mObjectRender.setRotation(aX, aY, aZ);
	}

	@Override
	public boolean onActivityGesture(GestureInfo aInfo) {
		Log.i(LOGTAG, "on GESTURE !!! : " + aInfo.mValue);

		switch (aInfo.mType) {
		// Scale
		case GestureInfo.GESTURE_PINCH:
			switch (aInfo.mState) {
			case GestureInfo.STATE_START:
				_startScale = mObjectRender.getCustomScale();
				return true;
			case GestureInfo.STATE_MOVE:
				Vec3F scale = new Vec3F(_startScale);
				scale.getData()[0] *= aInfo.mValue;
				scale.getData()[1] *= aInfo.mValue;
				scale.getData()[2] *= aInfo.mValue;
				mObjectRender.setCustomScale(scale);
				return true;
			case GestureInfo.STATE_FINISH:
				return true;
			}
			break;

		// Move
		case GestureInfo.GESTURE_MOVE_X:
		case GestureInfo.GESTURE_MOVE_Y:
			mObjectRender.addRotation(0.f, 4.0f * aInfo.mValue, 0.f);
			return true;
		}

		return false;
	}
}
