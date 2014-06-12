package com.ar.vuforiatemplate.shaders;

public class HueAnimationShaders implements OpenGLShaders {

    private static final String CUBE_MESH_VERTEX_SHADER = " \n" 
    		+ "\n"
            + "attribute vec4 vertexPosition; \n"
            + "attribute vec4 vertexNormal; \n"
            + "attribute vec2 vertexTexCoord; \n" 
            + "\n"
            + "varying vec2 texCoord; \n" 
            + "varying vec4 normal; \n" 
            + "\n"
            + "uniform mat4 modelViewProjectionMatrix; \n" 
            + "\n"
            + "void main() \n" 
            + "{ \n"
            + "   gl_Position = modelViewProjectionMatrix * vertexPosition; \n"
            + "   normal = vertexNormal; \n" 
            + "   texCoord = vertexTexCoord; \n"
            + "} \n";

        private static final String CUBE_MESH_FRAGMENT_SHADER = " \n" 
        	+ "\n"
            + "precision mediump float; \n" 
        	+ " \n" 
        	+ "varying vec2 texCoord; \n"
            + "varying vec4 normal; \n"
        	+ " \n"
            + "uniform sampler2D texSampler2D; \n"
        	+ "uniform float animSec; \n"
        	+ " \n" 
            + "void main() \n"
            + "{ \n"
            + "   vec4 color = texture2D(texSampler2D, texCoord); \n"
            + "   gl_FragColor = color * vec4(animSec, animSec, 0.0, color[0]); \n"
            + "} \n";

    	@Override
    	public String getVertexShader() {
    		return CUBE_MESH_VERTEX_SHADER;
    	}

    	@Override
    	public String getFragmentShader() {
    		return CUBE_MESH_FRAGMENT_SHADER;
    	}

		@Override
		public boolean isTransparent() {
			return true;
		}

}
