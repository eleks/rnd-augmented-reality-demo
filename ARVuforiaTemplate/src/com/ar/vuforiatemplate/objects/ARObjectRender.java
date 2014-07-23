package com.ar.vuforiatemplate.objects;

import com.ar.vuforiatemplate.meshobjects.MeshObject;
import com.ar.vuforiatemplate.shaders.OpenGLBinaryShaders;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.Vec3F;

public class ARObjectRender {
	public ARObjectRender() {
		mAspectRatioType = AspectRatioType.FILL_TARGET;
	}

	public enum AspectRatioType {
		FILL_TARGET, FIT_INSIDE, FIT_OUTSIDE, QUADRATE_INSIDE, CUSTOM
	};

	public AspectRatioType mAspectRatioType;
	private float mAspectRatio = 1.0f;

	public MeshObject mMeshObject;

	private Vec3F _scaleCustom = new Vec3F(1.f, 1.f, 1.f);

	// rotation
	private Vec3F _rotation = new Vec3F(.0f, .0f, .0f);

	public String mTextureName;

	public OpenGLBinaryShaders mBinaryShader;

	public Vec3F getRotation() {
		return new Vec3F(_rotation);
	}

	public void setRotation(Vec3F aRotation) {
		_rotation = aRotation;
	}

	public void setRotation(float aX, float aY, float aZ) {
		_rotation = new Vec3F(aX, aY, aZ);
	}

	public void addRotation(float aX, float aY, float aZ) {
		_rotation.getData()[0] += aX;
		_rotation.getData()[1] += aY;
		_rotation.getData()[2] += aZ;
	}

	public Vec3F getCustomScale() {
		return new Vec3F(_scaleCustom);
	}

	public Vec3F getScale(Vec2F imageTargetSize) {
		if (mAspectRatioType == AspectRatioType.CUSTOM)
			return new Vec3F(_scaleCustom);

		float scaleX = imageTargetSize.getData()[0] / 2.0f;
		float scaleY = imageTargetSize.getData()[1] / 2.0f;

		switch (mAspectRatioType) {
		case FILL_TARGET:
			return new Vec3F(scaleX, scaleY, 1.0f);
		case FIT_INSIDE: {
			float sx = scaleX;
			float sy = scaleY;
			if (mAspectRatio < 1.0f)
				sx *= mAspectRatio;
			else
				sy /= mAspectRatio;
			return new Vec3F(sx, sy, 1.0f);
		}
		case FIT_OUTSIDE: {
			float sx = scaleX;
			float sy = scaleY;
			if (mAspectRatio > 1.0f)
				sx *= mAspectRatio;
			else
				sy /= mAspectRatio;
			return new Vec3F(sx, sy, 1.0f);
		}
		case QUADRATE_INSIDE: {
			float m = Math.min(scaleX, scaleY);
			return new Vec3F(m, m, 1.0f);
		}
		default:
			break;
		}

		return new Vec3F(_scaleCustom);
	}

	public void setCustomScale(Vec3F aScale) {
		_scaleCustom = aScale;
	}

	public void setCustomScale(float aX, float aY, float aZ) {
		_scaleCustom = new Vec3F(aX, aY, aZ);
	}

	public void addCustomScale(float dX, float dY, float dZ) {
		_scaleCustom.getData()[0] += dX;
		_scaleCustom.getData()[1] += dY;
		_scaleCustom.getData()[2] += dZ;
	}

	public void setAspectRatio(float aAspRatio) {
		mAspectRatio = aAspRatio;
	}
}
