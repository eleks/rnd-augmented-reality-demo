/*==============================================================================
 Copyright (c) 2012-2013 Qualcomm Connected Experiences, Inc.
 All Rights Reserved.
 ==============================================================================*/

package com.ar.vuforiatemplate.utils;

// Used to send back to the activity any error during vuforia processes
public class SampleApplicationException extends Exception {
	private static final long serialVersionUID = 2L;

	public static final int INITIALIZATION_FAILURE = 0;
	public static final int VUFORIA_ALREADY_INITIALIZATED = 1;
	public static final int TRACKERS_INITIALIZATION_FAILURE = 2;
	public static final int LOADING_TRACKERS_FAILURE = 3;
	public static final int UNLOADING_TRACKERS_FAILURE = 4;
	public static final int TRACKERS_DEINITIALIZATION_FAILURE = 5;
	public static final int CAMERA_INITIALIZATION_FAILURE = 6;
	public static final int SET_FOCUS_MODE_FAILURE = 7;
	public static final int ACTIVATE_FLASH_FAILURE = 8;

	private int _code = -1;
	private String _string = "";

	public SampleApplicationException(int code, String description) {
		super(description);
		_code = code;
		_string = description;
	}

	public int getCode() {
		return _code;
	}

	public String getString() {
		return _string;
	}
}
