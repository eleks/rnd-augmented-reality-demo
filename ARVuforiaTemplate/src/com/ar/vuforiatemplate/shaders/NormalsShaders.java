/*==============================================================================
 Copyright (c) 2012-2013 Qualcomm Connected Experiences, Inc.
 All Rights Reserved.
 ==============================================================================*/

package com.ar.vuforiatemplate.shaders;

public class NormalsShaders implements OpenGLShaders
{
    private static final String CUBE_MESH_VERTEX_SHADER = " \n" 
    	+ "\n"
        + "attribute vec4 vertexPosition; \n"
        + "attribute vec4 vertexNormal; \n"
        + "attribute vec2 vertexTexCoord; \n" 
        + "\n"
        + "varying vec2 texCoord; \n" 
    	+ "varying vec4 position; \n" 
        + "varying vec4 normal; \n" 
        + "\n"
        + "uniform mat4 modelViewProjectionMatrix; \n" 
        + "\n"
        + "void main() \n" 
        + "{ \n"
        + "   gl_Position = modelViewProjectionMatrix * vertexPosition; \n"
        + "   normal = vertexNormal; \n" 
        + "   position = gl_Position; \n" 
        + "   texCoord = vertexTexCoord; \n"
        + "} \n";

    private static final String CUBE_MESH_FRAGMENT_SHADER = " \n"
//        + "precision mediump float; \n" 
//        + " \n" 
//    	+ "varying vec2 texCoord; \n" 
//    	+ "varying vec4 position; \n" 
//    	+ "varying vec4 normal; \n" 
//
//    	+ "uniform sampler2D texSampler2D; \n" 
//
//    	+ "void main() { \n" 
//        + "    vec3 camera_position = vec3(10.0, 10.0, 10.0); \n" 
//
//		+ "    vec4 specular = vec4(0.0); \n" 
//        + "    vec4 diffuse; \n" 
//        + "    vec3 norm = normalize(normal.rgb); \n" 
//        + "    vec3 light_source = vec3(10.0, 10.0, 0.0); \n" 
//        + "    vec3 light_vector = light_source - position.rgb; \n" 
//        + "    float dist = length(light_vector); \n" 
//        + "    float attentuation = 1.0; \n" 
//
//        + "    light_vector = normalize(light_vector); \n" 
//        + "    float nxDir = max(0.0, dot(norm, light_vector)); \n"
//        + "    float diffC = nxDir * attentuation; \n"
//        + "    diffuse = vec4(diffC, diffC, diffC, diffC); \n" 
//
//        + "    vec4 texColor = vec4(1.0, 0.0, 0.0, 1.0); // texture2D(texSampler2D, texCoord); \n" 
//        + "    gl_FragColor = (diffuse * vec4(texColor.rgb, 1.0)); \n" 
//        + "} \n" ;

    	+ "\n"
        + "precision mediump float; \n" 
    	+ " \n" 
        + "varying vec2 texCoord; \n"
    	+ "varying vec4 position; \n" 
        + "varying vec4 normal; \n" 
        + " \n"
        + "uniform sampler2D texSampler2D; \n" 
        + " \n" 
        + "void main() \n"
        + "{ \n" 
		+ "    vec3 norm = normalize(normal.rgb); \n" 
		+ "    float minLight = 0.4; \n"
		+ "    float light = dot(norm, vec3(0.7, 0.2, 0.1) ) * (1.0 - minLight) + minLight; \n"
		+ "    gl_FragColor = vec4( vec3(light), 1.0); \n"
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
		return false;
	}

}
