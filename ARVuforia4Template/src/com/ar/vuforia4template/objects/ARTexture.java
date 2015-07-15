package com.ar.vuforia4template.objects;

import java.util.Vector;

import com.ar.vuforia4template.meshobjects.MeshObject;
import com.ar.vuforia4template.objects.ARObjectRender.AspectRatioType;
import com.ar.vuforia4template.shaders.OpenGLBinaryShaders;

public class ARTexture extends ARObjectManagement {

	public ARTexture(MeshObject aMeshObject, OpenGLBinaryShaders aShader,
			String aTexture) {
		super(aMeshObject, aShader);

		mObjectRender.mTextureName = aTexture;
		mObjectRender.mAspectRatioType = AspectRatioType.FIT_INSIDE;
	}

	@Override
	public void getTextureNames(Vector<String> aTextures) {
		aTextures.add(mObjectRender.mTextureName);
	}
}
