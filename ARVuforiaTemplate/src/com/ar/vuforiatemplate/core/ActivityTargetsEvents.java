package com.ar.vuforiatemplate.core;

import java.util.Set;

import com.ar.vuforiatemplate.objects.ARObjectRender;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.TrackableResult;

public abstract interface ActivityTargetsEvents {

	void onTargetTrack(Trackable trackable);

	void onTargetClicked(String targetName);

	void onTargetBeforeRender(String targetName, TrackableResult aResult);

	ARObjectRender getRenderObject(String aTargetName, TrackableResult aResult);

	ARObjectsMediator getARObjectsMediator();

	void loadTextures();

	void initRendering();

	void compileShaders();
	
	void updateActiveARObjects(Set<String> trackables);
}
