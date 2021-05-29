package com.cydroid.softmanager.monitor.utils;

/*
 return:
 0:return 0, means not been rooted 
 1:means been rooted but not damaged,only su is found
 2:means been rooted and damaged
 */

public class CyeePrt {

	static {
		System.loadLibrary("cy_prt_jni");
	}

	public static native int nativeCheckIfRoot();

}