package com.ar.vuforiatemplate.objects;

import com.ar.vuforiatemplate.meshobjects.MeshObject;
import com.ar.vuforiatemplate.shaders.OpenGLBinaryShaders;
import com.ar.vuforiatemplate.ux.GestureInfo;
import com.qualcomm.vuforia.TrackableResult;

public class ARObjectManagement {
//	private static String LOGTAG = "ARObjectManagement";

	public ARObjectRender mObjectRender;
	public CallBack mCallBack;
	public CallBack mInternalCallBack;

	public boolean mNeedsExtendedTracking = false;

	public ARObjectManagement(MeshObject aMeshObject,
			OpenGLBinaryShaders aShader) {
		mObjectRender = new ARObjectRender();

		mObjectRender.mMeshObject = aMeshObject;
		mObjectRender.mBinaryShader = aShader;
	}

	public void onTrackingStart(TrackableResult aResult) {
	}

	public void onTrackingRenderUpdate(TrackableResult aResult) {
	}

	public void onTrackingUpdate(TrackableResult aResult) {
	}

	public void onActivityPause() {
	}

	public void onActivityResume() {
	}

	public void onTap() {
	}

	public interface CallBack {
		void callBackMethod();
	}

	public boolean onActivityGesture(GestureInfo aInfo) {
		return false;
	}

	public void onTrackingPause() {
	}

	public void onTrackingResume() {
	}

}
