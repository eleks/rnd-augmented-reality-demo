package com.ar.vuforiatemplate.objects;

import java.util.Vector;

import com.ar.vuforiatemplate.meshobjects.MeshObject;
import com.ar.vuforiatemplate.objects.ARObjectRender.AspectRatioType;
import com.ar.vuforiatemplate.shaders.OpenGLBinaryShaders;

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
