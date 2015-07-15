package com.ar.vuforia4template.objects;

import java.util.Vector;

import com.ar.vuforia4template.core.FragmentActivityImageTargets;
import com.ar.vuforia4template.meshobjects.MeshObject;
import com.ar.vuforia4template.shaders.OpenGLBinaryShaders;
import com.ar.vuforia4template.ux.GestureInfo;
import com.qualcomm.vuforia.TrackableResult;

public class ARObjectManagement {
	// private static String LOGTAG = "ARObjectManagement";

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

	public void getTextureNames(Vector<String> aTextures) {
	}

}
