package com.ar.vuforiatemplate.objects;

import com.ar.vuforiatemplate.meshobjects.MeshObject;
import com.ar.vuforiatemplate.shaders.OpenGLBinaryShaders;
import com.qualcomm.vuforia.ImageTarget;
import com.qualcomm.vuforia.TrackableResult;

public class ARTexture extends ARObjectManagement {

	public ARTexture(MeshObject aMeshObject, OpenGLBinaryShaders aShader,
			String aTexture) {
		super(aMeshObject, aShader);

		mObjectRender.mTextureName = aTexture;
	}

	@Override
	public void onTrackingStart(TrackableResult aResult) {
		super.onTrackingStart(aResult);

		ImageTarget imageTarget = (ImageTarget) aResult.getTrackable();
		mObjectRender.mScaleX = imageTarget.getSize().getData()[0] / 2.0f;
		mObjectRender.mScaleY = imageTarget.getSize().getData()[1] / 2.0f;
		mObjectRender.mScaleZ = 1.0f;
	}

}
