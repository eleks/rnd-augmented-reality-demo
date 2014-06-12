package com.ar.vuforiatemplate.shaders;

import android.opengl.GLES20;

import com.ar.vuforiatemplate.utils.SampleUtils;

public class OpenGLBinaryShaders {
    public int mShaderProgramID;
    public int mVertexHandle;
    public int mNormalHandle;
    public int mTextureCoordHandle;
    public int mMvpMatrixHandle;
    public int mTexSampler2DHandle;
    public int mTexSamplerExternalOESHandler;
    public int mAnimSecHandler;

    public boolean mTransparent = false;

	public OpenGLBinaryShaders() {
	}

	public void compile(OpenGLShaders aShaders) {
        mShaderProgramID = SampleUtils.createProgramFromShaderSrc(
        		aShaders.getVertexShader(),
        		aShaders.getFragmentShader() );

        mVertexHandle = GLES20.glGetAttribLocation(mShaderProgramID, "vertexPosition");
        mNormalHandle = GLES20.glGetAttribLocation(mShaderProgramID, "vertexNormal");
        mTextureCoordHandle = GLES20.glGetAttribLocation(mShaderProgramID, "vertexTexCoord");
        mMvpMatrixHandle = GLES20.glGetUniformLocation(mShaderProgramID, "modelViewProjectionMatrix");
        mTexSampler2DHandle = GLES20.glGetUniformLocation(mShaderProgramID, "texSampler2D");
        mAnimSecHandler = GLES20.glGetUniformLocation(mShaderProgramID, "animSec");
        mTexSamplerExternalOESHandler = GLES20.glGetUniformLocation(mShaderProgramID, "texSamplerOES");

        mTransparent = aShaders.isTransparent();
	}

}
