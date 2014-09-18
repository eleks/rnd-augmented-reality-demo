package com.ar.vuforiatemplate.core;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import android.util.Log;

import com.ar.vuforiatemplate.meshobjects.MeshObject;
import com.ar.vuforiatemplate.objects.ARObjectManagement;
import com.ar.vuforiatemplate.shaders.OpenGLBinaryShaders;
import com.ar.vuforiatemplate.shaders.OpenGLShaders;
import com.ar.vuforiatemplate.utils.Texture;
import com.ar.vuforiatemplate.ux.GestureInfo;

public class ARModule {
	private static final String LOGTAG = "ARModule";

	// Shaders
	private Map<String, OpenGLBinaryShaders> _shaders = new TreeMap<String, OpenGLBinaryShaders>();

	// Mesh Objects
	private Map<String, MeshObject> _meshObjects = new TreeMap<String, MeshObject>();

	// AR Objects
	private Map<String, ARObjectManagement> _arObjects = new TreeMap<String, ARObjectManagement>();

	// Textures
	private Map<String, Texture> _textures = new TreeMap<String, Texture>();

	public OpenGLBinaryShaders getShader(String aShaderName,
			boolean aCreateIfAbsent) {
		if (_shaders.containsKey(aShaderName))
			return _shaders.get(aShaderName);

		if (aCreateIfAbsent) {
			_shaders.put(aShaderName, new OpenGLBinaryShaders());
			return _shaders.get(aShaderName);
		}

		Log.e(LOGTAG, "Shader was not found ! : " + aShaderName);
		return null;
	}

	public void compileShader(String aShaderName, OpenGLShaders aShaders) {
		if (!_shaders.containsKey(aShaderName))
			_shaders.put(aShaderName, new OpenGLBinaryShaders());

		OpenGLBinaryShaders bShaders = _shaders.get(aShaderName);
		bShaders.compile(aShaders);
	}

	public MeshObject getMeshObject(String aMeshObjectName) {
		if (_meshObjects.containsKey(aMeshObjectName))
			return _meshObjects.get(aMeshObjectName);

		return null;
	}

	public void addMeshObject(String aMeshObjectName, MeshObject aMeshObject) {
		_meshObjects.put(aMeshObjectName, aMeshObject);
	}

	public ARObjectManagement getARObjectManagement(String aObjectName) {
		if (_arObjects.containsKey(aObjectName))
			return _arObjects.get(aObjectName);

		return null;
	}

	public void addARObjectManagement(String aObjectName,
			ARObjectManagement aObjectMngmnt) {
		_arObjects.put(aObjectName, aObjectMngmnt);
	}

	public Texture getTexture(String aTextureName) {
		return _textures.get(aTextureName);
	}

	public Collection<Texture> getTextures() {
		return _textures.values();
	}

	public void addTexture(String aTextureName, Texture aTexture) {
		Log.i(LOGTAG, "!!! @@@ addTexture : " + aTextureName);
		_textures.put(aTextureName, aTexture);
	}

	public void clearTextures() {
		_textures.clear();
	}

	public boolean needsExtendedTracking(String arName) {
		if (!_arObjects.containsKey(arName))
			return false;

		return _arObjects.get(arName).mNeedsExtendedTracking;
	}

	public boolean applyGesture(String aARName, GestureInfo aGesture) {
		if (!_arObjects.containsKey(aARName))
			return false;

		return _arObjects.get(aARName).onActivityGesture(aGesture);
	}

	public void onActivityPause() {
		for (ARObjectManagement arObject : _arObjects.values()) {
			arObject.onActivityPause();
		}
	}

	public void onActivityResume() {
		for (ARObjectManagement arObject : _arObjects.values()) {
			arObject.onActivityResume();
		}
	}

	public void onTrackingPause(Set<String> aARObjects) {
		for (String name : aARObjects) {
			if (_arObjects.containsKey(name)) {
				_arObjects.get(name).onTrackingPause();
			}
		}
	}

	public void onTrackingResume(Set<String> aARObjects) {
		for (String name : aARObjects) {
			if (_arObjects.containsKey(name)) {
				_arObjects.get(name).onTrackingResume();
			}
		}
	}

	public void getTextureNames(Vector<String> aTextures) {
		for (ARObjectManagement obj : _arObjects.values()) {
			obj.getTextureNames(aTextures);
		}
	}

}
