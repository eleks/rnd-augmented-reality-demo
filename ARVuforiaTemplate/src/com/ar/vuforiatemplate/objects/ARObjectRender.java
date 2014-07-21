package com.ar.vuforiatemplate.objects;

import com.ar.vuforiatemplate.meshobjects.MeshObject;
import com.ar.vuforiatemplate.shaders.OpenGLBinaryShaders;
import com.qualcomm.vuforia.Vec3F;

public class ARObjectRender {

	public enum AspectRatio {
		FILL_TARGET, FIT_ISIDE, FIT_OUTSIDE, QUADRATE_INSIDE, CUSTOM
	};

	AspectRatio mAspectRatio = AspectRatio.FILL_TARGET;
	public float mWidthDivHeight = 1.0f;

	public MeshObject mMeshObject;

	private Vec3F _scale = new Vec3F(1.f, 1.f, 1.f);
	private Vec3F _scaleRel = new Vec3F(1.f, 1.f, 1.f);

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

	public Vec3F getScale() {
		// switch (mAspectRatio) {
		// case FILL_TARGET:
		// return new Vec3F();
		// case CUSTOM:
		// return new Vec3F();
		// case FIT_ISIDE:
		// return new Vec3F();
		// case FIT_OUTSIDE:
		// return new Vec3F();
		// case QUADRATE_INSIDE:
		// return new Vec3F();
		// }
		return new Vec3F(_scale);
	}

	public void setScale(Vec3F aScale) {
		_scale = aScale;
	}

	public void setScale(float aX, float aY, float aZ) {
		_scale = new Vec3F(aX, aY, aZ);
	}

	public void setScaleDeprecated(float aX, float aY, float aZ) {
		_scale = new Vec3F(aX, aY, aZ);
	}

	public void setScaleRelDeprecated(float aX, float aY, float aZ) {
		_scaleRel = new Vec3F(aX, aY, aZ);
	}

	public void addScale(float dX, float dY, float dZ) {
		_scale.getData()[0] += dX;
		_scale.getData()[1] += dY;
		_scale.getData()[2] += dZ;
	}

	public Vec3F getScaleRel() {
		return new Vec3F(_scaleRel);
	}

}
