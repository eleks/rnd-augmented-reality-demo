package com.ar.vuforiatemplate.core;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.microedition.khronos.opengles.GL10;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;

import com.ar.vuforiatemplate.objects.ARObjectManagement;
import com.ar.vuforiatemplate.objects.ARObjectRender;
import com.ar.vuforiatemplate.shaders.OpenGLShaders;
import com.ar.vuforiatemplate.utils.Texture;
import com.ar.vuforiatemplate.ux.GestureInfo;
import com.qualcomm.vuforia.TrackableResult;

public class ARObjectsMediator {
	private static final String LOGTAG = "ARObjectsMediator";
	private String _targetNamePrev = "";

	private ARModule _arModule;
	private Set<String> _activeARObjects = new TreeSet<String>();
	private Set<String> _textTextures = new TreeSet<String>();

	public ARObjectsMediator(ARModule aARModule) {
		_arModule = aARModule;
	}

	public boolean onTargetClicked(String targetName) {
		ARObjectManagement arObject = _arModule
				.getARObjectManagement(targetName);
		if (null != arObject) {
			if (null != arObject.mCallBack)
				arObject.mCallBack.callBackMethod();

			arObject.onTap();
			Log.i(LOGTAG, "Target clicked : " + targetName);

			return true;
		}

		return false;
	}

	public void onTargetBeforeRender(String targetName, TrackableResult aResult) {
		ARObjectManagement arObject = _arModule
				.getARObjectManagement(targetName);
		if (null == arObject)
			return;

		arObject.onTrackingRenderUpdate(aResult);
	}

	public void compileShaders(Map<String, OpenGLShaders> aShaders) {
		for (String shaderName : aShaders.keySet()) {
			_arModule.compileShader(shaderName, aShaders.get(shaderName));
		}

	}

	public ARObjectRender getRenderObject(String aTargetName,
			TrackableResult aResult) {
		boolean changed = !aTargetName.equals(_targetNamePrev);
		_targetNamePrev = aTargetName;

		ARObjectManagement arObject = _arModule
				.getARObjectManagement(aTargetName);
		if (null == arObject)
			return null;

		if (changed)
			arObject.onTrackingStart(aResult);
		arObject.onTrackingUpdate(aResult);

		return arObject.mObjectRender;
	}

	public Texture getTexture(String aTextureName) {
		return _arModule.getTexture(aTextureName);
	}

	public boolean isTextTexture(String aTexture) {
		return _textTextures.contains(aTexture);
	}

	public void loadTextures(List<String> aTextureFiles, AssetManager aAssetMngr) {
		for (String fileName : aTextureFiles) {
			Texture t = Texture.loadTextureFromApk(fileName, aAssetMngr);
			_arModule.addTexture(fileName, t);
		}

	}

	public void addTextTexture(GL10 gl, String aText) {
		if (!_textTextures.contains(aText)) {
			Texture t = Texture.createTextureWithText(gl, aText, 100, Color.YELLOW);
			_arModule.addTexture(aText, t);
			_textTextures.add(aText);
		}
	}

	public void clearTextures() {
		_arModule.clearTextures();
		_textTextures.clear();
	}

	public void initRendering() {
		for (Texture t : _arModule.getTextures()) {
			GLES20.glGenTextures(1, t.mTextureID, 0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
					t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
					GLES20.GL_UNSIGNED_BYTE, t.mData);
		}

	}

	public boolean needsExtendedTracking(String arName) {
		return _arModule.needsExtendedTracking(arName);
	}

	public void updateActiveAR(Set<String> aActiveARObjects) {
		// pause
		Set<String> oldAR = new TreeSet<String>();
		oldAR.addAll(_activeARObjects);
		oldAR.removeAll(aActiveARObjects);
		_arModule.onTrackingPause(oldAR);

		// resume
		Set<String> newAR = new TreeSet<String>();
		newAR.addAll(aActiveARObjects);
		newAR.removeAll(_activeARObjects);
		_arModule.onTrackingResume(newAR);

		// update set
		_activeARObjects = aActiveARObjects;
	}

	public boolean onGesture(GestureInfo aInfo) {
		boolean result = false;
		for (String name : _activeARObjects) {
			result |= _arModule.applyGesture(name, aInfo);
		}
		return result;
	}

	public void onActivityPause() {
		if (null != _arModule)
			_arModule.onActivityPause();
	}

	public void onActivityResume() {
		if (null != _arModule)
			_arModule.onActivityResume();
	}

	public void trackingPause(Set<String> aARObjects) {
		_arModule.onTrackingPause(aARObjects);
	}

	public void trackingResume(Set<String> aARObjects) {
		_arModule.onTrackingResume(aARObjects);
	}

	public ARModule getModule() {
		return _arModule;
	}

}
