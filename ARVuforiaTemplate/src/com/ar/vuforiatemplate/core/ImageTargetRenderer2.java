/*==============================================================================
 Copyright (c) 2012-2013 Qualcomm Connected Experiences, Inc.
 All Rights Reserved.
 ==============================================================================*/

package com.ar.vuforiatemplate.core;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.DisplayMetrics;
import android.util.Log;

import com.ar.vuforiatemplate.objects.ARObjectRender;
import com.ar.vuforiatemplate.utils.LoadingDialogHandler;
import com.ar.vuforiatemplate.utils.SampleApplicationSession;
import com.ar.vuforiatemplate.utils.SampleMath;
import com.ar.vuforiatemplate.utils.SampleUtils;
import com.ar.vuforiatemplate.utils.Texture;
import com.qualcomm.vuforia.ImageTarget;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.Vec3F;
import com.qualcomm.vuforia.Vuforia;

// The renderer class for the ImageTargets sample. 
public class ImageTargetRenderer2 implements GLSurfaceView.Renderer {
	private static final String LOGTAG = "ImageTargetRenderer";

	private SampleApplicationSession _vuforiaAppSession;
	private FragmentActivityImageTargets _activity;

	private Renderer _renderer;

	private int _trackableCount = -1;

	public boolean mIsActive = false;

	private Map<String, Matrix44F> _modelViewMatrix = new TreeMap<String, Matrix44F>();
	private Map<String, Vec2F> _targetPositiveDimensions = new TreeMap<String, Vec2F>();

	public ImageTargetRenderer2(FragmentActivityImageTargets activity,
			SampleApplicationSession session) {
		_activity = activity;
		_vuforiaAppSession = session;
	}

	// Called to draw the current frame.
	@Override
	public void onDrawFrame(GL10 gl) {
		if (!mIsActive)
			return;

		// Call our function to render content
		renderFrame();
	}

	// Called when the surface is created or recreated.
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");

		initRendering();

		// Call Vuforia function to (re)initialize rendering after first use
		// or after OpenGL ES context was lost (e.g. after onPause/onResume):
		_vuforiaAppSession.onSurfaceCreated();
	}

	// Called when the surface changed size.
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");

		// Call Vuforia function to handle render surface size changes:
		_vuforiaAppSession.onSurfaceChanged(width, height);

	}

	// Function for initializing the renderer.
	private void initRendering() {
		_renderer = Renderer.getInstance();

		GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
				: 1.0f);

		_activity.initRendering();
		_activity.compileShaders();
		_activity.updateRendering();

		// Hide the Loading Dialog
		_activity.loadingDialogHandler
				.sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);

	}

	// The render function.
	private void renderFrame() {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		State state = _renderer.begin();
		_renderer.drawVideoBackground();

		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		// handle face culling, we need to detect if we are using reflection
		// to determine the direction of the culling
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glCullFace(GLES20.GL_BACK);
		if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
			GLES20.glFrontFace(GLES20.GL_CW); // Front camera
		else
			GLES20.glFrontFace(GLES20.GL_CCW); // Back camera

		_modelViewMatrix.clear();
		_targetPositiveDimensions.clear();

		Set<String> trackableNames = new TreeSet<String>();

		_activity.customTargetRenderer();

		// did we find any trackables this frame?
		for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
			TrackableResult result = state.getTrackableResult(tIdx);
			Trackable trackable = result.getTrackable();
			ImageTarget imageTarget = (ImageTarget) result.getTrackable();
			String targetName = trackable.getName();

			_activity.onTargetTrack(trackable);

			trackableNames.add(targetName);

			ARObjectRender renderObject = _activity.getRenderObject(targetName,
					result);
			if (null == renderObject || null == renderObject.mMeshObject
					|| null == renderObject.mBinaryShader)
				continue;

			_activity.onTargetBeforeRender(targetName, result);

			Matrix44F modelViewMatrix_Vuforia = Tool
					.convertPose2GLMatrix(result.getPose());
			float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();

			_modelViewMatrix.put(targetName, modelViewMatrix_Vuforia);
			_targetPositiveDimensions.put(targetName, imageTarget.getSize());

			double objectScaleX = renderObject.mScaleX;
			double objectScaleY = renderObject.mScaleY;
			double objectScaleZ = renderObject.mScaleZ;

			int textureIndex = 0;
			int glTextureType = GLES20.GL_TEXTURE_2D;
			// if (renderObject.mGLTextureIndex != -1) {
			// textureIndex = renderObject.mGLTextureIndex;
			// } else
			// {
			// // if (_textures.size() > renderObject.mTextureIndex )
			// // textureIndex =
			// _textures.get(renderObject.mTextureIndex).mTextureID[0];
			// Texture t = renderObject.mTexture;
			// textureIndex = t.mTextureID[0];
			// }

			Texture tex = _activity.getARObjectsMediator().getTexture(
					renderObject.mTextureName);
			if (null != tex) {
				textureIndex = tex.mTextureID[0];
				glTextureType = tex.mGLTextureType;
			} else
				textureIndex = 0;

			// deal with the modelview and projection matrices
			float[] modelViewProjection = new float[16];

			// if (renderObject.mMeshObject.getClass() !=
			// SampleApplication3DModel.class) {
			// Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f,
			// (float) objectScaleZ);
			// } else {
			// Matrix.rotateM(modelViewMatrix, 0, 90.0f, 1.0f, 0, 0);
			// Matrix.rotateM(modelViewMatrix, 0, 90.0f, 0, 1.0f, 0);
			// }

			Matrix.rotateM(modelViewMatrix, 0, (float) renderObject.mRotationX,
					1.0f, 0, 0);
			Matrix.rotateM(modelViewMatrix, 0, (float) renderObject.mRotationY,
					0, 1.0f, 0);
			Matrix.rotateM(modelViewMatrix, 0, (float) renderObject.mRotationZ,
					0, 0, 1.0f);

			Matrix.scaleM(modelViewMatrix, 0, (float) objectScaleX,
					(float) objectScaleY, (float) objectScaleZ);

			Matrix.multiplyMM(modelViewProjection, 0, _vuforiaAppSession
					.getProjectionMatrix().getData(), 0, modelViewMatrix, 0);

			// activate the shader program and bind the vertex/normal/tex coords
			GLES20.glUseProgram(renderObject.mBinaryShader.mShaderProgramID);

			if (renderObject.mBinaryShader.mTransparent) {
				GLES20.glEnable(GLES20.GL_BLEND);
				GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,
						GLES20.GL_ONE_MINUS_SRC_ALPHA);
			}

			// Texture
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(glTextureType, textureIndex);

			GLES20.glUniform1i(renderObject.mBinaryShader.mTexSampler2DHandle,
					0);
			GLES20.glUniform1i(
					renderObject.mBinaryShader.mTexSamplerExternalOESHandler, 0); // !!!

			// pass the model view matrix to the shader
			GLES20.glUniformMatrix4fv(
					renderObject.mBinaryShader.mMvpMatrixHandle, 1, false,
					modelViewProjection, 0);

			// set uniform variables
			long time = System.currentTimeMillis();
			float t = (time % 2000) / 1000.f;
			if (t > 1.0)
				t = 2.0f - t;
			GLES20.glUniform1f(renderObject.mBinaryShader.mAnimSecHandler, t);

			// Mesh object
			GLES20.glVertexAttribPointer(
					renderObject.mBinaryShader.mVertexHandle, 3,
					GLES20.GL_FLOAT, false, 0,
					renderObject.mMeshObject.getVertices());
			GLES20.glVertexAttribPointer(
					renderObject.mBinaryShader.mNormalHandle, 3,
					GLES20.GL_FLOAT, false, 0,
					renderObject.mMeshObject.getNormals());
			GLES20.glVertexAttribPointer(
					renderObject.mBinaryShader.mTextureCoordHandle, 2,
					GLES20.GL_FLOAT, false, 0,
					renderObject.mMeshObject.getTexCoords());

			GLES20.glEnableVertexAttribArray(renderObject.mBinaryShader.mVertexHandle);
			GLES20.glEnableVertexAttribArray(renderObject.mBinaryShader.mNormalHandle);
			GLES20.glEnableVertexAttribArray(renderObject.mBinaryShader.mTextureCoordHandle);

			if (renderObject.mMeshObject.selfDraw())
				renderObject.mMeshObject.draw();
			else {
				GLES20.glDrawElements(GLES20.GL_TRIANGLES,
						renderObject.mMeshObject.getNumObjectIndex(),
						renderObject.mMeshObject.getIndicesGLType(),
						renderObject.mMeshObject.getIndices());
			}

			// disable the enabled arrays
			GLES20.glDisableVertexAttribArray(renderObject.mBinaryShader.mVertexHandle);
			GLES20.glDisableVertexAttribArray(renderObject.mBinaryShader.mNormalHandle);
			GLES20.glDisableVertexAttribArray(renderObject.mBinaryShader.mTextureCoordHandle);

			if (renderObject.mBinaryShader.mTransparent) {
				GLES20.glDisable(GLES20.GL_BLEND);
			}

			SampleUtils.checkGLError("Render Frame");
		}

		GLES20.glDisable(GLES20.GL_DEPTH_TEST);

		// test extended tracker
		int trackableCount = state.getNumTrackableResults();

		if (trackableCount != _trackableCount) {
			_activity.updateActiveARObjects(trackableNames);
			_trackableCount = trackableCount;
		}

		// render end
		_renderer.end();
	}

	// NEW !!! //
	boolean isTapOnScreenInsideTarget(String targetName, float x, float y) {
		// Here we calculate that the touch event is inside the target
		Vec3F intersection;
		// Vec3F lineStart = new Vec3F();
		// Vec3F lineEnd = new Vec3F();

		if (!_modelViewMatrix.containsKey(targetName)
				|| !_targetPositiveDimensions.containsKey(targetName))
			return false;

		Matrix44F mat44 = _modelViewMatrix.get(targetName);
		Vec2F vec2f = _targetPositiveDimensions.get(targetName);

		DisplayMetrics metrics = new DisplayMetrics();
		_activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		intersection = SampleMath.getPointToPlaneIntersection(SampleMath
				.Matrix44FInverse(_vuforiaAppSession.getProjectionMatrix()),
				mat44, metrics.widthPixels, metrics.heightPixels, new Vec2F(x,
						y), new Vec3F(0, 0, 0), new Vec3F(0, 0, 1));

		// The target returns as pose the center of the trackable. The following
		// if-statement simply checks that the tap is within this range
		if ((intersection.getData()[0] >= -(vec2f.getData()[0]))
				&& (intersection.getData()[0] <= (vec2f.getData()[0]))
				&& (intersection.getData()[1] >= -(vec2f.getData()[1]))
				&& (intersection.getData()[1] <= (vec2f.getData()[1])))
			return true;
		else
			return false;
	}

}
