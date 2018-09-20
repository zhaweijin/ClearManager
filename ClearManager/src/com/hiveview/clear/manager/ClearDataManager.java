package com.hiveview.clear.manager;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.IPackageDataObserver;
import android.R.integer;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.Environment;
import android.os.Message;
import android.text.style.BackgroundColorSpan;
import android.util.Log;

 


/** * 本应用数据清除管理器 */
public class ClearDataManager {
	
 
	private String TAG = "ClearDataManager";
	private PackageManager packageManager;
	private Context mContext;
	/*private HashMap<String, Long> dataSizeMap = new HashMap<String, Long>();
	private List<Map.Entry<String, Long>> mHashMapEntryList;
	private ArrayList<String> packageList = new ArrayList<String>();*/
	private ArrayList<AppInfo> mAppInfos = new ArrayList<AppInfo>();
	private ArrayList<String> packageList = new ArrayList<String>();
	 
	private PackageInfo packageInfo;
    private ClearUserDataObserver mClearDataObserver;
    private ClearDataPackageInfo clearDataPackageInfo;
    
    private ArrayList<String> blackList = new ArrayList<String>();
    private int clearAllAppSize = 0;
    
    private int currentScan = 0;
 
	
	public ClearDataManager(Context context,PackageInfo packageInfo,ClearDataPackageInfo clearDataPackageInfo) {
		// TODO Auto-generated constructor stub
		mContext = context;
		this.packageInfo = packageInfo;
		this.clearDataPackageInfo = clearDataPackageInfo;
		packageManager = context.getPackageManager();
		if (mClearDataObserver == null) {
            mClearDataObserver = new ClearUserDataObserver();
        }
		initBlackList();
		 
	}
	
	
	public ClearDataManager(Context context,ClearDataPackageInfo clearDataPackageInfo) {
		// TODO Auto-generated constructor stub
		mContext = context;
		this.clearDataPackageInfo = clearDataPackageInfo;
		packageManager = context.getPackageManager();
		if (mClearDataObserver == null) {
            mClearDataObserver = new ClearUserDataObserver();
        }
		initBlackList();
	}
	
	
	public void oneKeySdcardData(){
		try {
			Utils.print(TAG, "clear sdcard data");
			deleteFileDir(Environment.getExternalStorageDirectory());
			//老版支持，夏普和rk平台不支持，为适配rk平台的hiveviewcore2包，暂时所有平台都去掉这个功能
			//import com.hiveview.manager.RootManager;
			//RootManager.getRootManager().rmTool("/data/local/tmp/");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}

	/**
	 * @author carter
	 * @fucntion 删除目录文件
	 */
	public boolean deleteFileDir(File f) {
		boolean result = false;
 
		if(!isNeedScanDir(f.getAbsolutePath())){
			return false;
		}
			
		try {
			if (f.exists()) {
				File[] files = f.listFiles();
				if (files != null && files.length > 0) {
					for (File file : files) {
						if (file.isDirectory()) {
							if (deleteFileDir(file))
								result = false;
						} else {
							deleteFile(file);
						}
					}
					f.delete();
					result = true;
				}else {
					f.delete();
					return false;
				}
				
			}
		} catch (Exception e) {
			// TODO: handle exception
			return result;
		}
		return result;
	}

	/**
	 * @author carter
	 * @fucntion 删除文件，根据文件对象
	 */
	public boolean deleteFile(File f) {
		boolean result = false;
		try {
			if (f.exists()) {
				packageInfo.getAppSize(f.length());
				Utils.print(TAG, "delete filepath=="+f.getAbsolutePath());
				f.delete();
				result = true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			return result;
		}
		return result;
	}
	
	final IPackageStatsObserver.Stub mStatsSystemObserver = new IPackageStatsObserver.Stub() {
		public void onGetStatsCompleted(PackageStats stats, boolean succeeded) {
			if (succeeded && stats != null) {
				synchronized (this) {
					Utils.print(TAG, "cache size==" + Utils.getSizeStr(mContext,stats.cacheSize)+"pk=="+stats.packageName);
					long size = stats.cacheSize;
					AppInfo info = new AppInfo();
					info.setPackname(stats.packageName);
					info.setCacheSize(size);
					mAppInfos.add(info);
					
					//存储扫描信息，为了清除时刷新数据需求
					ClearApplication.getInstance().getAppCache().put(stats.packageName, size);
					
					if(packageInfo!=null){
						//更新ui数字大小
						packageInfo.getAppSize(size);
						
						if(mAppInfos.size()==currentScan){
							packageInfo.getSystemAppAllSize(getAllSize(),mAppInfos.size());
							AppInfo[] appInfos = getSortPackageSize();
							packageInfo.getSystemAppTopInfo(appInfos);
						}
					}
				}
			}
		}
	};
	
	
	
	final IPackageStatsObserver.Stub mStatsThridObserver = new IPackageStatsObserver.Stub() {
		public void onGetStatsCompleted(PackageStats stats, boolean succeeded) {
			if (succeeded && stats != null) {
				synchronized (this) {
					Log.v(TAG, "cache size==" + Utils.getSizeStr(mContext,stats.cacheSize));
					long size = stats.cacheSize;
					AppInfo info = new AppInfo();
					info.setPackname(stats.packageName);
					info.setCacheSize(size);
					
					//存储扫描信息，为了清除时刷新数据需求
					ClearApplication.getInstance().getAppCache().put(stats.packageName, size);
					
					mAppInfos.add(info);
					if(packageInfo!=null){
						//更新ui数字大小
						packageInfo.getAppSize(size);
						if(mAppInfos.size()==currentScan){
							packageInfo.getThridAppAllSize(getAllSize(),mAppInfos.size());
							AppInfo[] appInfos = getSortPackageSize();
							packageInfo.getThridAppTopInfo(appInfos);
						}
					}
				}
			}
		}
	};
	
	
	
	private long getAllSize(){
		long total=0;
		for(int i=0;i<mAppInfos.size();i++){
		     total = total + mAppInfos.get(i).getCacheSize();
		}
		return total; 
	}
	
	
	
	 public AppInfo[] getSortPackageSize(){
		  
		 
		 Comparator cmp = new MyComparator();
		 Collections.sort(mAppInfos, cmp);
		 //test
/*		 for(int i=0;i<mAppInfos.size();i++){
			Utils.print(TAG, "datasize==="+mAppInfos.get(i).getPackname()+",size="+mAppInfos.get(i).getCaheSize());
		 }*/
		 //get top 10 app info
		 int length = mAppInfos.size();
		 Utils.print(TAG, "length=="+length);
		 if(length>10)
			 length = 10;
	 
		 
		 AppInfo[] appInfos = new AppInfo[length];
		 for (int i = 0; i < length; i++) {
			 Utils.print(TAG, "key222==="+mAppInfos.get(i).getPackname()+",size="+mAppInfos.get(i).getCacheSize());
			 appInfos[i] = mAppInfos.get(i);
		 }
		 
		 return appInfos;
		 
	 }
	 
 
	/*
	 * 获取系统app信息
	 */
	public void getSystemAppPackageSizeInfo(){

	    ClearApplication.getInstance().getAppCache().clear();
		
		mAppInfos.clear();
		packageList.clear();
		currentScan = 0;
		
		packageList = RunningAppInfo.getAppInfoParam(mContext).getSystemAppInfo();
		/*for(int i=0;i<packageList.size();i++){
			Utils.print(TAG, "=="+packageList.get(i));
		}*/
		int size = packageList.size();
		Utils.print(TAG, "system size=="+size);
		for(int i=0;i<size;i++){
			if(checkBlackList(packageList.get(i)))
				continue;
			currentScan++;
			packageManager.getPackageSizeInfo(packageList.get(i), mStatsSystemObserver);
		}
		
		if(currentScan==0){
			packageInfo.getSystemAppAllSize(getAllSize(),mAppInfos.size());
			AppInfo[] appInfos = getSortPackageSize();
			packageInfo.getSystemAppTopInfo(appInfos);
		}
	}
	
	
	/*
	 * 获取第三方app信息
	 */
	public void getThridAppPackageSizeInfo(){

		mAppInfos.clear();
		packageList.clear();
		currentScan=0;
		
		packageList = RunningAppInfo.getAppInfoParam(mContext).getThirdAppInfo();
		int size = packageList.size();
		Utils.print(TAG, "thrid size=="+size);
		for(int i=0;i<size;i++){
			Utils.print(TAG, ">>>>"+packageList.get(i));
		}
		for(int i=0;i<size;i++){
			if(checkBlackList(packageList.get(i)))
				continue;
			currentScan++;
			packageManager.getPackageSizeInfo(packageList.get(i), mStatsThridObserver);
		}
		if(currentScan==0){
			packageInfo.getThridAppAllSize(getAllSize(),mAppInfos.size());
			AppInfo[] appInfos = getSortPackageSize();
			packageInfo.getThridAppTopInfo(appInfos);
		}
	}
 
	
	
	
	/*
	 * 大小比较
	 */
    public static class MyComparator implements Comparator<AppInfo> {  
        
        @Override  
        public int compare(AppInfo o1, AppInfo o2) {  
            // TODO Auto-generated method stub  
        	if (o1.getCacheSize() < o2.getCacheSize()) {
				return 1;
			} else if (o1.getCacheSize() > o2.getCacheSize()) {
				return -1;
			} else {
				return 0;
			} 
        }  
      
    }  
	
	/*
	 * 回调接口定义
	 */
	public interface PackageInfo {
		void getSystemAppTopInfo(AppInfo[] infos);
		void getThridAppTopInfo(AppInfo[] infos);
		
		void getSystemAppAllSize(Long size,int count);
		void getThridAppAllSize(Long size,int count);
		
		void getAppSize(Long size);
		
		void scanFinished();
	}
	
	public void clearUserData(String packageName){
		if(checkBlackList(packageName))
			return;
		Utils.print(TAG, "clear pk======"+packageName);
		clearAllAppSize++;
		ActivityManager am = (ActivityManager)
                mContext.getSystemService(Context.ACTIVITY_SERVICE);
        boolean res = am.clearApplicationUserData(packageName, mClearDataObserver);
	}
	
	
	public void clearCacheData(String packageName){
		if(checkBlackList(packageName))
			return;
		Utils.print(TAG, "clear pk======"+packageName);
		clearAllAppSize++;
	 
		packageManager.deleteApplicationCacheFiles(packageName, mClearDataObserver);
	}
	
/*	class ClearCacheObserver extends IPackageDataObserver.Stub {
        public void onRemoveCompleted(final String packageName, final boolean succeeded) {
            final Message msg = mHandler.obtainMessage(CLEAR_CACHE);
            msg.arg1 = succeeded ? OP_SUCCESSFUL:OP_FAILED;
            mHandler.sendMessage(msg);
         }
     }*/
	//清理数据，清理缓存共用
	class ClearUserDataObserver extends IPackageDataObserver.Stub {
	       public void onRemoveCompleted(final String packageName, final boolean succeeded) {
	    	   Utils.print(TAG, "clear package="+packageName +" sucess=="+succeeded);
	    	   if (succeeded) {
					synchronized (this) {
						if(clearDataPackageInfo!=null){
							Utils.print(TAG, "clear package>>>>>"+packageName);
							
					        Long size  = 0l;
					        try {
								size = ClearApplication.getInstance().getAppCache().get(packageName);
							} catch (Exception e) {
								// TODO: handle exception
							}
							clearDataPackageInfo.clearUserData(packageName,size,succeeded);
						}	    	
					}
	           }else {
	        	   clearDataPackageInfo.clearUserData(packageName,0l,succeeded);
			}
	    }
	}
	
	
 
	
	
	public interface ClearDataPackageInfo{
		void clearUserData(String packageName,Long size,boolean sucess);
	}

	public void scanFile(){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					//sdcard
					Thread.sleep(1000);
					scanDir(Environment.getExternalStorageDirectory());
					//data tmp
					Thread.sleep(1000);
					scanDir(new File("/data/tmp"));
					Thread.sleep(2000);
					packageInfo.scanFinished();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}).start();
	}
	
    public boolean scanDir(File f){
    	boolean result = false;
    	try {
			if (f.exists()) {
//				Utils.print(TAG, ">>>"+f.getAbsolutePath());
				File[] files = f.listFiles();
				if (files != null && files.length > 0) {
					for (File file : files) {
						if (file.isDirectory()) {
							if(!isNeedScanDir(f.getAbsolutePath())){
								continue;
							}
							if (scanDir(file))
								result = false;
						} else {
							scanFile(file);
						}
					}
					result = true;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			return result;
		}
    	return result;
    }

    /**
	 * @author carter
	 * @fucntion 扫描文件大小
	 */
	public boolean scanFile(File f) {
		boolean result = false;
		try {
			if (f.exists()) {
				packageInfo.getAppSize(f.length());
				Utils.print(TAG, "filepath=="+f.getAbsolutePath());
				result = true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			return result;
		}
		return result;
	}
	
	public void oneKeyClearAllAppData(){
		clearAllAppSize = 0;
		List<ApplicationInfo> applicationInfos = RunningAppInfo.getAppInfoParam(mContext).getInstallAppInfo();
	
		for(int i=0;i<applicationInfos.size();i++){
			Utils.print(TAG, "pg==="+applicationInfos.get(i).packageName);
			clearCacheData(applicationInfos.get(i).packageName);
		}
	}
	
	public int getAllAppSize(){
		return clearAllAppSize;
	}
	
	public void initBlackList(){

		//system
		blackList.add("android");
		blackList.add("com.android.providers.settings");
		//test 
//		blackList.add("com.realtek.dmr");
		blackList.add("com.hiveview.customkeyboard");
		//本身
		blackList.add("com.hiveview.clear.manager");
		//ota
		blackList.add("com.hiveview.upgrade");
		//广告
		blackList.add("com.hiveview.advertisement");
		
		blackList.add("com.hiveview.dataprovider");
		blackList.add("com.hiveview.cloudscreen.launcher");
		blackList.add("com.hiveview.cloudscreen.user");
		
		
		blackList.add("com.hiveview.cloudscreen.appstore");
		
		//北京
/*		blackList.add("com.hiveview.cloudscreen.launcher");
		blackList.add("com.hiveview.cloudscreen.videolive");
		blackList.add("com.hiveview.cloudscreen.tip");
		blackList.add("com.hiveview.dataprovider");
		blackList.add("com.hiveview.cloudscreen.vipvideo");
		blackList.add("com.hiveview.cloudscreen.player");
		blackList.add("com.hiveview.premiere");
		blackList.add("com.hiveview.cloudscreen.appstore");
		blackList.add("com.hiveview.cloudscreen.user");
		blackList.add("com.hiveview.cloudscreen.guide");*/
 
		//系统
		blackList.add("com.android.defcontainer");
	 
	}
	
	public ArrayList<String> getBlackList(){
		return blackList;
	}
	
	public boolean checkBlackList(String packageName){
		boolean result = false;
		
		for(int i=0;i<blackList.size();i++){
 
			if(blackList.get(i).equals(packageName)){
				result = true;
				break;
			}
		}
		return result;
	}
	
	
	public boolean isNeedScanDir(String path){
        boolean result = true;
		
		for(int i=0;i<blackList.size();i++){
            String blacklistpath = Environment.getExternalStorageDirectory().getAbsoluteFile()+"/Android/data/"+blackList.get(i);
			if(path.startsWith(blacklistpath)){
				result = false;
				break;
			}
		}
		return result;
	}
	
}