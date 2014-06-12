package com.ar.vuforiatemplate.meshobjects;

import java.util.Vector;

public class Triangulator{
	public static Vector<Integer> triangulate(Vector<Integer> polygon){
		Vector<Integer> triangles=new Vector<Integer>();
		for(int i=1; i<polygon.size()-1; i++){
			triangles.add(polygon.get(0));
			triangles.add(polygon.get(i));
			triangles.add(polygon.get(i+1));
		}
		return triangles;
	}
	
}
