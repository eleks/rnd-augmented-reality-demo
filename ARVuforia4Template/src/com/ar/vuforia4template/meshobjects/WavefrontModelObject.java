/*==============================================================================
 Copyright (c) 2012-2013 Qualcomm Connected Experiences, Inc.
 All Rights Reserved.
 ==============================================================================*/

package com.ar.vuforia4template.meshobjects;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Vector;

import android.content.res.AssetManager;
import android.opengl.GLES20;

/*! \brief Brief description.
 *         Class for parsing *.obj files (wavefront)
 *
 *  Parser needs such options :
 *  + Write normals
 *  + Triangulate
 *  (Blender-Export-...)
 */
public class WavefrontModelObject extends MeshObject {
//	private static final String LOGTAG = "WavefrontModelObject";

	private ByteBuffer _verts;
	private ByteBuffer _itextCoords;
	private ByteBuffer _norms;
	private ByteBuffer _indBuff;

	private int _numVerts = 0;
	private int _numIndices = 0;

	private Vector<Float> _v = new Vector<Float>();
	private Vector<Float> _vn = new Vector<Float>();
	private Vector<Integer> _faces = new Vector<Integer>();
	private Vector<Integer> _vtPointer = new Vector<Integer>();
	private Vector<Integer> _vnPointer = new Vector<Integer>();

	public void loadModel(AssetManager assetManager, String filename)
			throws IOException {
		InputStream is = null;
		_v.clear();
		_vn.clear();
		_faces.clear();
		_vtPointer.clear();
		_vnPointer.clear();
		try {
			is = assetManager.open(filename);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(is));

			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("vn")) {
					processVNLine(line);
				} else if (line.startsWith("v")) {
					processVLine(line);
				}
				if (line.startsWith("f")) {
					processFLine(line);
				}

			}

			_numVerts = _v.size() / 3;

			// vertices
			_verts = ByteBuffer.allocateDirect(_v.size() * Float.SIZE);
			_verts.order(ByteOrder.nativeOrder());
			for (int i = 0; i < _v.size(); i++) {
				_verts.putFloat(_v.get(i));
			}
			_verts.rewind();

			// normals
			Vector<Float> normals = new Vector<Float>(_v.size());
			for (int i = 0; i<_v.size(); i++) {
				normals.add( 1.0f );
			}

			for (int i = 0; i < _faces.size(); i++) {
				if (i >= _vnPointer.size())
					continue;

				int vertice_i = _faces.get(i);
				int normal_i = _vnPointer.get(i);

				if (((vertice_i * 3 + 2) < normals.size())
				 && ((normal_i  * 3 + 2) < _vn.size())) {
					normals.set(vertice_i * 3,     _vn.get(normal_i * 3    ));
					normals.set(vertice_i * 3 + 1, _vn.get(normal_i * 3 + 1));
					normals.set(vertice_i * 3 + 2, _vn.get(normal_i * 3 + 2));
				}
			}

			_norms = ByteBuffer.allocateDirect(normals.size() * Float.SIZE);
			_norms.order(ByteOrder.nativeOrder());
			for (int i = 0; i < normals.size(); i++) {
				_norms.putFloat(normals.get(i));
			}
			_norms.rewind();

			// texture coords
			_itextCoords = ByteBuffer.allocateDirect(_v.size() * Float.SIZE);
			_itextCoords.order(ByteOrder.nativeOrder());
			for (int i = 0; i < _v.size(); i++) {
				_itextCoords.putFloat(0.0f);
			}
			_itextCoords.rewind();

			// indexes
			_numIndices = _faces.size();

			_indBuff = ByteBuffer.allocateDirect(_faces.size() * Integer.SIZE);
			_indBuff.order(ByteOrder.nativeOrder());
			for (int i = 0; i < _faces.size(); i++) {
				_indBuff.putInt(_faces.get(i));
			}
			_indBuff.rewind();
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
			break;
		case BUFFER_TYPE_INDICES:
			result = _indBuff;
			break;
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
		return _numIndices;
	}

	@Override
	public int getIndicesGLType() {
		return GLES20.GL_UNSIGNED_INT;
	}

	@Override
	public boolean selfDraw() {
		return false;
	}

	private void processVLine(String line) {
		String[] tokens = line.split("[ ]+");
		int c = tokens.length;
		for (int i = 1; i < c; i++) {
			_v.add(Float.valueOf(tokens[i]));
		}
	}

	private void processVNLine(String line) {
		String[] tokens = line.split("[ ]+");
		int c = tokens.length;
		for (int i = 1; i < c; i++) {
			_vn.add(Float.valueOf(tokens[i]));
		}
	}

	private void processFLine(String line) {
		String[] tokens = line.split("[ ]+");
		int c = tokens.length;

		if (tokens[1].matches("[0-9]+")) {// f: v
			if (c == 4) {// 3 faces
				for (int i = 1; i < c; i++) {
					Integer s = Integer.valueOf(tokens[i]);
					s--;
					_faces.add(s);
				}
			} else {// more faces
				Vector<Integer> polygon = new Vector<Integer>();
				for (int i = 1; i < tokens.length; i++) {
					Integer s = Integer.valueOf(tokens[i]);
					s--;
					polygon.add(s);
				}
				_faces.addAll(Triangulator.triangulate(polygon));// triangulate
																	// the
																	// polygon
																	// and add
																	// the
																	// resulting
																	// faces
			}
		}
		if (tokens[1].matches("[0-9]+/[0-9]+")) {// if: v/vt
			if (c == 4) {// 3 faces
				for (int i = 1; i < c; i++) {
					Integer s = Integer.valueOf(tokens[i].split("/")[0]);
					s--;
					_faces.add(s);
					s = Integer.valueOf(tokens[i].split("/")[1]);
					s--;
					_vtPointer.add(s);
				}
			} else {// triangulate
				Vector<Integer> tmpFaces = new Vector<Integer>();
				Vector<Integer> tmpVt = new Vector<Integer>();
				for (int i = 1; i < tokens.length; i++) {
					Integer s = Integer.valueOf(tokens[i].split("/")[0]);
					s--;
					tmpFaces.add(s);
					s = Integer.valueOf(tokens[i].split("/")[1]);
					s--;
					tmpVt.add(s);
				}
				_faces.addAll(Triangulator.triangulate(tmpFaces));
				_vtPointer.addAll(Triangulator.triangulate(tmpVt));
			}
		}
		if (tokens[1].matches("[0-9]+//[0-9]+")) {// f: v//vn
			if (c == 4) {// 3 faces
				for (int i = 1; i < c; i++) {
					Integer s = Integer.valueOf(tokens[i].split("//")[0]);
					s--;
					_faces.add(s);
					s = Integer.valueOf(tokens[i].split("//")[1]);
					s--;
					_vnPointer.add(s);
				}
			} else {// triangulate
				Vector<Integer> tmpFaces = new Vector<Integer>();
				Vector<Integer> tmpVn = new Vector<Integer>();
				for (int i = 1; i < tokens.length; i++) {
					Integer s = Integer.valueOf(tokens[i].split("//")[0]);
					s--;
					tmpFaces.add(s);
					s = Integer.valueOf(tokens[i].split("//")[1]);
					s--;
					tmpVn.add(s);
				}
				_faces.addAll(Triangulator.triangulate(tmpFaces));
				_vnPointer.addAll(Triangulator.triangulate(tmpVn));
			}
		}
		if (tokens[1].matches("[0-9]+/[0-9]+/[0-9]+")) {// f: v/vt/vn

			if (c == 4) {// 3 faces
				for (int i = 1; i < c; i++) {
					Integer s = Integer.valueOf(tokens[i].split("/")[0]);
					s--;
					_faces.add(s);
					s = Integer.valueOf(tokens[i].split("/")[1]);
					s--;
					_vtPointer.add(s);
					s = Integer.valueOf(tokens[i].split("/")[2]);
					s--;
					_vnPointer.add(s);
				}
			} else {// triangulate
				Vector<Integer> tmpFaces = new Vector<Integer>();
				Vector<Integer> tmpVn = new Vector<Integer>();
				// Vector<Integer> tmpVt=new Vector<Integer>();
				for (int i = 1; i < tokens.length; i++) {
					Integer s = Integer.valueOf(tokens[i].split("/")[0]);
					s--;
					tmpFaces.add(s);
					// s=Integer.valueOf(tokens[i].split("/")[1]);
					// s--;
					// tmpVt.add(s);
					// s=Integer.valueOf(tokens[i].split("/")[2]);
					// s--;
					// tmpVn.add(s);
				}
				_faces.addAll(Triangulator.triangulate(tmpFaces));
				_vtPointer.addAll(Triangulator.triangulate(tmpVn));
				_vnPointer.addAll(Triangulator.triangulate(tmpVn));
			}
		}
	}

}
