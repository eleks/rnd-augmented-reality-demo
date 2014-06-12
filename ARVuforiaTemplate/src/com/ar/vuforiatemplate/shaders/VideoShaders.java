package com.ar.vuforiatemplate.shaders;

public class VideoShaders implements OpenGLShaders {

    public static final String VIDEO_PLAYBACK_VERTEX_SHADER = " \n"
            + "attribute vec4 vertexPosition; \n"
            + "attribute vec4 vertexNormal; \n"
            + "attribute vec2 vertexTexCoord; \n" 
            + "varying vec2 texCoord; \n"
            + "varying vec4 normal; \n"
            + "uniform mat4 modelViewProjectionMatrix; \n" 
            + "\n"
            + "void main() \n" 
            + "{ \n"
            + "   gl_Position = modelViewProjectionMatrix * vertexPosition; \n"
            + "   normal = vertexNormal; \n" 
            + "   texCoord = vertexTexCoord; \n"
            + "} \n";

    public static final String VIDEO_PLAYBACK_FRAGMENT_SHADER = " \n"
            + "#extension GL_OES_EGL_image_external : require \n"
            + "precision mediump float; \n" 
            + "varying vec2 texCoord; \n"
            + "uniform samplerExternalOES texSamplerOES; \n" 
            + " \n"
            + "void main() \n" 
            + "{ \n"
            + "   vec2 coords = vec2(texCoord.x, 1.0-texCoord.y); \n" // frame is upside-down !
            + "   gl_FragColor = texture2D(texSamplerOES, coords); \n" 
            + "} \n";

    	@Override
    	public String getVertexShader() {
    		return VIDEO_PLAYBACK_VERTEX_SHADER;
    	}

    	@Override
    	public String getFragmentShader() {
    		return VIDEO_PLAYBACK_FRAGMENT_SHADER;
    	}

		@Override
		public boolean isTransparent() {
			return false;
		}

}