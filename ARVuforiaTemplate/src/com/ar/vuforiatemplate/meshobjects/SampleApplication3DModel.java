/*==============================================================================
 Copyright (c) 2012-2013 Qualcomm Connected Experiences, Inc.
 All Rights Reserved.
 ==============================================================================*/

package com.ar.vuforiatemplate.meshobjects;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.content.res.AssetManager;
import android.opengl.GLES20;

public class SampleApplication3DModel extends MeshObject {
	private ByteBuffer _verts;
	private ByteBuffer _itextCoords;
	private ByteBuffer _norms;
	private int _numVerts = 0;

	public void loadModel(AssetManager assetManager, String filename)
			throws IOException {
		InputStream is = null;
		try {
			is = assetManager.open(filename);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(is));

			String line = reader.readLine();

			int floatsToRead = Integer.parseInt(line);
			_numVerts = floatsToRead / 3;

			_verts = ByteBuffer.allocateDirect(floatsToRead * 4);
			_verts.order(ByteOrder.nativeOrder());
			for (int i = 0; i < floatsToRead; i++) {
				_verts.putFloat(Float.parseFloat(reader.readLine()));
			}
			_verts.rewind();

			line = reader.readLine();
			floatsToRead = Integer.parseInt(line);

			_norms = ByteBuffer.allocateDirect(floatsToRead * 4);
			_norms.order(ByteOrder.nativeOrder());
			for (int i = 0; i < floatsToRead; i++) {
				_norms.putFloat(Float.parseFloat(reader.readLine()));
			}
			_norms.rewind();

			line = reader.readLine();
			floatsToRead = Integer.parseInt(line);

			_itextCoords = ByteBuffer.allocateDirect(floatsToRead * 4);
			_itextCoords.order(ByteOrder.nativeOrder());
			for (int i = 0; i < floatsToRead; i++) {
				_itextCoords.putFloat(Float.parseFloat(reader.readLine()));
			}
			_itextCoords.rewind();

		} finally {
			if (is != null)
				is.close();
		}
	}

	@Override
	public Buffer getBuffer(BUFFER_TYPE bufferType) {
		Buffer result = null;
		switch (bufferType) {
		case BUFFER_TYPE_VERTEX:
			result = _verts;
			break;
		case BUFFER_TYPE_TEXTURE_COORD:
			result = _itextCoords;
			break;
		case BUFFER_TYPE_NORMALS:
			result = _norms;
		default:
			break;
		}
		return result;
	}

	@Override
	public int getNumObjectVertex() {
		return _numVerts;
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
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, getNumObjectVertex());
	}

}
