package com.ar.vuforiatemplate.objects;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.ar.vuforiatemplate.meshobjects.MeshObject;
import com.ar.vuforiatemplate.objects.ARObjectRender.AspectRatioType;
import com.ar.vuforiatemplate.shaders.OpenGLBinaryShaders;
import com.ar.vuforiatemplate.utils.Texture;
import com.ar.vuforiatemplate.ux.GestureInfo;
import com.ar.vuforiatemplate.video.VideoPlayerHelper;
import com.ar.vuforiatemplate.video.VideoPlayerHelper.MEDIA_TYPE;
import com.qualcomm.vuforia.TrackableResult;

public class ARVideo extends ARObjectManagement {
	private static final String LOGTAG = "ARVideo";

	private String _videoFileName;
	private VideoPlayerHelper _videoPlayerHelper;
	private int _textureId[];
	private int _seekPosition = -1;

	private boolean _isPlaying = false;

	private VideoPlayerHelper.MEDIA_TYPE _videoMediaType;

	public ARVideo(MeshObject aMeshObject, OpenGLBinaryShaders aShader,
			String aFileName, Activity aActivity) {
		super(aMeshObject, aShader);

		_textureId = new int[1];
		_textureId[0] = -1;

		_videoFileName = aFileName;

		_videoPlayerHelper = new VideoPlayerHelper();
		_videoPlayerHelper.init();
		_videoPlayerHelper.setActivity(aActivity);

		mObjectRender.mAspectRatioType = AspectRatioType.FIT_INSIDE;
	}

	@Override
	public void onTrackingUpdate(TrackableResult aResult) {
		super.onTrackingUpdate(aResult);

		if ((null != _videoPlayerHelper)
				&& (_videoPlayerHelper.getCurrentPosition() != -1)) {
			if (_videoMediaType == MEDIA_TYPE.FULLSCREEN)
				Log.i(LOGTAG, "onUpdate: Fullscreen");
			if (_videoMediaType == MEDIA_TYPE.ON_TEXTURE_FULLSCREEN)
				Log.i(LOGTAG, "onUpdate: Texture Fullscreen");

			if (!_isPlaying) {
				play();
				_isPlaying = true;
			}
		}

	}

	@SuppressLint("InlinedApi")
	@Override
	public void onTrackingRenderUpdate(TrackableResult aResult) {
		super.onTrackingRenderUpdate(aResult);

		if (null != _videoPlayerHelper) {

			// craete texture
			if (_textureId[0] == -1) {
				Log.i(LOGTAG, "onRenderUpdate: create texture");
				GLES20.glGenTextures(1, _textureId, 0);
				GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
						_textureId[0]);
				GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
						GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
				GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
						GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
				GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);

				Texture tex = new Texture();
				tex.mGLTextureType = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
				tex.mTextureID[0] = _textureId[0];

				mObjectRender.mTextureName = "video_texture";

				if (_videoPlayerHelper.setupSurfaceTexture(_textureId[0]))
					_videoMediaType = MEDIA_TYPE.FULLSCREEN;
				else
					_videoMediaType = MEDIA_TYPE.ON_TEXTURE_FULLSCREEN;

				_videoPlayerHelper.load(_videoFileName, MEDIA_TYPE.ON_TEXTURE,
						false, 0);
			}

			if (_videoPlayerHelper.getStatus() == VideoPlayerHelper.MEDIA_STATE.PLAYING) {
				_videoPlayerHelper.updateVideoData();
			}

		}

	}

	@Override
	public void onTap() {
		playPause();
	}

	private boolean isPlaying() {
		if (null != _videoPlayerHelper) {
			return !((_videoPlayerHelper.getStatus() == VideoPlayerHelper.MEDIA_STATE.PAUSED)
					|| (_videoPlayerHelper.getStatus() == VideoPlayerHelper.MEDIA_STATE.READY)
					|| (_videoPlayerHelper.getStatus() == VideoPlayerHelper.MEDIA_STATE.STOPPED) || (_videoPlayerHelper
						.getStatus() == VideoPlayerHelper.MEDIA_STATE.REACHED_END));
		}
		return false;
	}

	private boolean play() {
		if ((null != _videoPlayerHelper)
				&& (_videoPlayerHelper.getStatus() != VideoPlayerHelper.MEDIA_STATE.PLAYING)) {

			if ((_videoPlayerHelper.getStatus() == VideoPlayerHelper.MEDIA_STATE.REACHED_END))
				_seekPosition = 0;

			_videoPlayerHelper.play(false, _seekPosition);
			_seekPosition = VideoPlayerHelper.CURRENT_POSITION;
			return true;
		}
		return false;
	}

	private boolean pause() {
		if (null != _videoPlayerHelper) {
			if (_videoPlayerHelper.getStatus() == VideoPlayerHelper.MEDIA_STATE.PLAYING) {
				_videoPlayerHelper.pause();
			}
			return true;
		}
		return false;
	}

	private boolean playPause() {
		if (isPlaying())
			return pause();
		else
			return play();
	}

	@Override
	public void onActivityPause() {
		pause();
	}

	@Override
	public void onActivityResume() {
		play();
	}

	@Override
	public void onTrackingPause() {
		pause();
	}

	@Override
	public void onTrackingResume() {
		play();
	}

	@Override
	public boolean onActivityGesture(GestureInfo aInfo) {
		if (aInfo.mType == GestureInfo.GESTURE_POINTER_TAP) {
			if (null != mInternalCallBack) {
				mInternalCallBack.callBackMethod();
			}
			return true;
		}

		return false;
	}

}
