package com.ar.vuforia4template.meshobjects;

import java.nio.Buffer;

public class Model3DObject extends MeshObject {

	public Model3DObject(String aAssetFileName) {
	}

	@Override
	public Buffer getBuffer(BUFFER_TYPE bufferType) {
		return null;
	}

	@Override
	public int getNumObjectVertex() {
		return 0;
	}

	@Override
	public int getNumObjectIndex() {
		return 0;
	}

	@Override
	public boolean selfDraw() {
		return true;
	}

	@Override
	public void draw() {
	}

}
