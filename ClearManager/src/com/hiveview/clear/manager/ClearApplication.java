package com.hiveview.clear.manager;

import java.util.HashMap;

 

import android.app.Application;

public class ClearApplication extends Application{

	private HashMap<String,Long> allAppCacheSize;
	private static ClearApplication instance;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		allAppCacheSize = new HashMap<String, Long>();
		instance = this;
	}
	
	public HashMap<String, Long> getAppCache(){
		return allAppCacheSize;
	}
	
	public static ClearApplication getInstance() {
		return instance;
	}
}
