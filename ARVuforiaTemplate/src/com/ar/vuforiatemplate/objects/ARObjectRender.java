package com.ar.vuforiatemplate.objects;

import com.ar.vuforiatemplate.meshobjects.MeshObject;
import com.ar.vuforiatemplate.shaders.OpenGLBinaryShaders;

public class ARObjectRender {

	public MeshObject mMeshObject;

	// real scale
	public double mScaleX = 1.0f;
	public double mScaleY = 1.0f;
	public double mScaleZ = 1.0f;

	// relative scale
	// TODO: use relative scale for all ARElements
	public float mScaleRelX = 1.0f;
	public float mScaleRelY = 1.0f;
	public float mScaleRelZ = 1.0f;
	
	// rotation
	public  double mRotationX = 0.0f;
	public  double mRotationY = 0.0f;
	public  double mRotationZ = 0.0f;

	public String mTextureName;

	public OpenGLBinaryShaders mBinaryShader;

}
