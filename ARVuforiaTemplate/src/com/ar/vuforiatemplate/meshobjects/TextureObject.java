package com.ar.vuforiatemplate.meshobjects;

import java.nio.Buffer;

public class TextureObject extends MeshObject {
    // Data for drawing the 3D plane as overlay
    private static final double _sTextureVertices[]  = { 
        -1.00f, -1.00f, -1.00f, // front
        1.00f, -1.00f, -1.00f, 
        1.00f, 1.00f, -1.00f,
        -1.00f, 1.00f, -1.00f,
        };

    private static final double _sTextureTexcoords[] = { 
    	0, 0, 1, 0, 1, 1, 0, 1,
        };
    
    private static final double _sTextureNormals[]   = { 
    	0, 0, 1,  0, 0, 1,  0, 0, 1,  0, 0, 1,
       	};

    private static final short  _sTextureIndices[]   = { 
    	0, 1, 2, 0, 2, 3, // front
    	};
    
    private Buffer _vertBuff;
    private Buffer _texCoordBuff;
    private Buffer _normBuff;
    private Buffer _indBuff;
    
	public TextureObject() {
        _vertBuff = fillBuffer(_sTextureVertices);
        _texCoordBuff = fillBuffer(_sTextureTexcoords);
        _normBuff = fillBuffer(_sTextureNormals);
        _indBuff = fillBuffer(_sTextureIndices);
	}

	@Override
	public Buffer getBuffer(BUFFER_TYPE bufferType) {
        Buffer result = null;
        switch (bufferType)
        {
            case BUFFER_TYPE_VERTEX:
                result = _vertBuff;
                break;
            case BUFFER_TYPE_TEXTURE_COORD:
                result = _texCoordBuff;
                break;
            case BUFFER_TYPE_INDICES:
                result = _indBuff;
                break;
            case BUFFER_TYPE_NORMALS:
                result = _normBuff;
            default:
                break;
        }
        return result;
	}

	@Override
	public int getNumObjectVertex() {
		return _sTextureVertices.length / 3;
	}

	@Override
	public int getNumObjectIndex() {
		return _sTextureIndices.length;
	}

	@Override
	public boolean selfDraw() {
		return false;
	}

}
