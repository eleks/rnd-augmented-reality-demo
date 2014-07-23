package com.ar.vuforiatemplate.objects;

import java.util.Vector;

import com.ar.vuforiatemplate.meshobjects.MeshObject;
import com.ar.vuforiatemplate.objects.ARObjectRender.AspectRatio;
import com.ar.vuforiatemplate.shaders.OpenGLBinaryShaders;
import com.qualcomm.vuforia.ImageTarget;
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

		mObjectRender.mAspectRatio = AspectRatio.FIT_ISIDE;
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

		// size
		ImageTarget imageTarget = (ImageTarget) aResult.getTrackable();
		float aX = imageTarget.getSize().getData()[0] / 2.0f;
		float aY = imageTarget.getSize().getData()[1] / 2.0f;
		float aZ = 1.0f;

		mObjectRender.setScaleDeprecated(aX, aY, aZ);

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
}
