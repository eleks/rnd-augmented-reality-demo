package com.ar.vuforia4template.objects;

import java.util.Vector;

import com.ar.vuforia4template.meshobjects.MeshObject;
import com.ar.vuforia4template.objects.ARObjectRender.AspectRatioType;
import com.ar.vuforia4template.shaders.OpenGLBinaryShaders;
import com.qualcomm.vuforia.TrackableResult;

public class ARTextureAnimation extends ARObjectManagement {
	private long _timePrev;
	private long _delta = 500;

	private Vector<String> _textures = new Vector<String>();
	private int _textureIndex = 0;

	public ARTextureAnimation(MeshObject aMeshObject,
			OpenGLBinaryShaders aShader, Vector<String> aTextures) {
		super(aMeshObject, aShader);

		_textures = aTextures;

		mObjectRender.mAspectRatioType = AspectRatioType.FIT_INSIDE;
	}

	private long getTime() {
		return System.currentTimeMillis();
	}

	public void setDelta(long aMilliseconds) {
		_delta = aMilliseconds;
	}

	@Override
	public void onTrackingStart(TrackableResult aResult) {
		super.onTrackingStart(aResult);

		// time & first frame
		_timePrev = getTime();
		_textureIndex = 0;
		if (_textureIndex < _textures.size())
			mObjectRender.mTextureName = _textures.get(_textureIndex);
	}

	@Override
	public void onTrackingUpdate(TrackableResult aResult) {
		super.onTrackingUpdate(aResult);

		long time = getTime();
		if ((time - _timePrev >= _delta) && (_textures.size() != 0)) {
			_textureIndex = (_textureIndex + 1) % _textures.size();
			mObjectRender.mTextureName = _textures.get(_textureIndex);
			_timePrev = time;
		}
	}

	@Override
	public void getTextureNames(Vector<String> aTextures) {
		for (String tex : _textures) {
			aTextures.add(tex);
		}
	}

}
