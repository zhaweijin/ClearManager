package com.hiveview.clear.manager;

import android.graphics.drawable.Drawable;

public class AppInfo {

	private String packname;
	private Long data_size;  //cache size + data size
	private Long app_size;
	private String versionName;
	private int versionCode;
	private Long cacheSize;
	private String appName;
	private boolean isSystemApp=false;
	private Drawable icon;
	private int bgId;
	
	public Drawable getIcon() {
		return icon;
	}
	public void setIcon(Drawable icon) {
		this.icon = icon;
	}
	public boolean isSystemApp() {
		return isSystemApp;
	}
	public void setSystemApp(boolean isSystemApp) {
		this.isSystemApp = isSystemApp;
	}
	
	public String getPackname() {
		return packname;
	}
	public void setPackname(String packname) {
		this.packname = packname;
	}
	public Long getDataSize() {
		return data_size;
	}
	public void setDataSize(Long size) {
		this.data_size = size;
	}
	public Long getApp_size() {
		return app_size;
	}
	public void setApp_size(Long app_size) {
		this.app_size = app_size;
	}
	
	///////////////
	
	public String getVersionName() {
		return versionName;
	}
	
	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}
	
	public int getVersionCode() {
		return versionCode;
	}
	
	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}
	
	/////////////////
	
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public int getBgId() {
		return bgId;
	}
	public void setBgId(int bgId) {
		this.bgId = bgId;
	}
	public Long getCacheSize() {
		return cacheSize;
	}
	public void setCacheSize(Long cacheSize) {
		this.cacheSize = cacheSize;
	}
	
	
	
}
