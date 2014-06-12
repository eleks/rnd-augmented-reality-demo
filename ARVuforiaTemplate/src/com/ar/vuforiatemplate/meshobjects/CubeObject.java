/*==============================================================================
 Copyright (c) 2012-2013 Qualcomm Connected Experiences, Inc.
 All Rights Reserved.
 ==============================================================================*/

package com.ar.vuforiatemplate.meshobjects;

import java.nio.Buffer;


public class CubeObject extends MeshObject
{
    // Data for drawing the 3D plane as overlay
    private static final double _cubeVertices[]  = { 
            -1.00f, -1.00f, 1.00f, // front
            1.00f, -1.00f, 1.00f, 
            1.00f, 1.00f, 1.00f,
            -1.00f, 1.00f, 1.00f,
                
            -1.00f, -1.00f, -1.00f, // back
            1.00f, -1.00f, -1.00f,
            1.00f, 1.00f, -1.00f,
            -1.00f, 1.00f, -1.00f,
            
            -1.00f, -1.00f, -1.00f, // left
            -1.00f, -1.00f, 1.00f,
            -1.00f, 1.00f, 1.00f,
            -1.00f, 1.00f, -1.00f,
            
            1.00f, -1.00f, -1.00f, // right
            1.00f, -1.00f, 1.00f,
            1.00f, 1.00f, 1.00f,
            1.00f, 1.00f, -1.00f,
            
            -1.00f, 1.00f, 1.00f, // top
            1.00f, 1.00f, 1.00f,
            1.00f, 1.00f, -1.00f,
            -1.00f, 1.00f, -1.00f,
            
            -1.00f, -1.00f, 1.00f, // bottom
            1.00f, -1.00f, 1.00f,
            1.00f, -1.00f, -1.00f,
            -1.00f, -1.00f, -1.00f };
    
    
    private static final double _cubeTexcoords[] = { 
            0, 0, 1, 0, 1, 1, 0, 1,
                                                
            1, 0, 0, 0, 0, 1, 1, 1,
                                                
            0, 0, 1, 0, 1, 1, 0, 1,
                                                
            1, 0, 0, 0, 0, 1, 1, 1,
                                                
            0, 0, 1, 0, 1, 1, 0, 1,
                                                
            1, 0, 0, 0, 0, 1, 1, 1 };
    
    
    private static final double _cubeNormals[]   = { 
            0, 0, 1,  0, 0, 1,  0, 0, 1,  0, 0, 1,
            
            0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1,
            
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,
            
            0, 1, 0,  0, 1, 0,  0, 1, 0,  0, 1, 0,
            
            1, 0, 0,  1, 0, 0,  1, 0, 0,  1, 0, 0,
            
            -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0 };
    
    private static final short  _cubeIndices[]   = { 
            0, 1, 2, 0, 2, 3, // front
            4, 6, 5, 4, 7, 6, // back
            8, 9, 10, 8, 10, 11, // left
            12, 14, 13, 12, 15, 14, // right
            16, 17, 18, 16, 18, 19, // top
            20, 22, 21, 20, 23, 22  // bottom
                                                };
    
    private Buffer _vertBuff;
    private Buffer _texCoordBuff;
    private Buffer _normBuff;
    private Buffer _indBuff;
    
    
    public CubeObject()
    {
        _vertBuff = fillBuffer(_cubeVertices);
        _texCoordBuff = fillBuffer(_cubeTexcoords);
        _normBuff = fillBuffer(_cubeNormals);
        _indBuff = fillBuffer(_cubeIndices);
    }
    
    
    @Override
    public Buffer getBuffer(BUFFER_TYPE bufferType)
    {
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
    public int getNumObjectVertex()
    {
        return _cubeVertices.length / 3;
    }
    
    
    @Override
    public int getNumObjectIndex()
    {
        return _cubeIndices.length;
    }


	@Override
	public boolean selfDraw() {
		return false;
	}
}
