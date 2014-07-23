package com.ar.vuforiatemplate.objects;

import com.ar.vuforiatemplate.meshobjects.MeshObject;
import com.ar.vuforiatemplate.objects.ARObjectRender.AspectRatio;
import com.ar.vuforiatemplate.shaders.OpenGLBinaryShaders;
import com.qualcomm.vuforia.ImageTarget;
import com.qualcomm.vuforia.TrackableResult;

public class ARTexture extends ARObjectManagement {

	public ARTexture(MeshObject aMeshObject, OpenGLBinaryShaders aShader,
			String aTexture) {
		super(aMeshObject, aShader);

		mObjectRender.mTextureName = aTexture;
		mObjectRender.mAspectRatio = AspectRatio.FIT_ISIDE;
	}

	@Override
	public void onTrackingStart(TrackableResult aResult) {
		super.onTrackingStart(aResult);

		ImageTarget imageTarget = (ImageTarget) aResult.getTrackable();
		float aX = imageTarget.getSize().getData()[0] / 2.0f;
		float aY = imageTarget.getSize().getData()[1] / 2.0f;
		float aZ = 1.0f;

		mObjectRender.setScaleDeprecated(aX, aY, aZ);
	}

}
