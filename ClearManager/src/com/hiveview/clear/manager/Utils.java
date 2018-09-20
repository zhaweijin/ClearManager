package com.hiveview.clear.manager;

import java.io.File;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;
import android.os.SystemProperties;
import android.preference.PreferenceManager;

public class Utils {

	private final static String TAG = "Utils";
	
	public static void print(String tag,String msg){
		Log.v(tag, msg);
	}
	
	/*
	 * 获取可用的存储空间
	 */
	public static long getRomAvailableSize() {
		File path = Environment.getDataDirectory();
		StatFs statFs = new StatFs(path.getPath());
		long blockSize = statFs.getBlockSize();
		long availableBlocks = statFs.getAvailableBlocks();
		return blockSize * availableBlocks;
	}
	/*
	 * 获取rom总的存储空间
	 */
	public static  String getRomTotalSize() {
		long[] memoryInfo = getStorageMemory();
		String totalStorage = formatTotalSize2String(memoryInfo[0]);
		return totalStorage;
	}
	
	private static long[] getStorageMemory() {
		long[] result = new long[3];
		File file = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(file.getPath());
		long mBlockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		long availiableBlocks = stat.getAvailableBlocks();
		long usedBlocks = formatTotalSize2Value(totalBlocks) / mBlockSize
				- availiableBlocks;
		result[0] = totalBlocks;
		result[1] = availiableBlocks;
		result[2] = usedBlocks;

		return result;
	}
	
	/*
	 * 格式化字符串
	 */
	private static String formatTotalSize2String(long totalSize) {
		String result = "";
		
		File file = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(file.getPath());
		long mBlockSize = stat.getBlockSize();
		
		long gB = 1024 * 1024 * 1024;
		long totalValue = totalSize * mBlockSize;
		Log.e(TAG, "totalValue" + totalValue);
		if (totalValue > (8 * gB)) {
			result = "16 GB";
		} else if (totalValue > (4 * gB)) {
			result = "8 GB";
		} else {
			result = "4 GB";
		}
		return result;
	}
	
	/*
	 * 格式化数值
	 */
	private static long formatTotalSize2Value(long totalSize) {
		long result = 0;
		
		File file = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(file.getPath());
		long mBlockSize = stat.getBlockSize();
		
		long gB = 1024 * 1024 * 1024;
		long totalValue = totalSize * mBlockSize;
		if (totalValue > (8 * gB)) {
			result = 16 * gB;
		} else if (totalValue > (4 * gB)) {
			result = 8 * gB;
		} else {
			result = 4 * gB;
		}
		return result;
	}
	
	
	public static String getSizeStr(Context context,long size) {
        if (size >= 0) {
            return Formatter.formatFileSize(context, size);
        }
        return "";
    }
	
	public static boolean checkPackageIsExist(Context context,
			String packageName) {
		try {
			PackageManager mPm = context.getPackageManager();
			ApplicationInfo mAppInfo = mPm.getApplicationInfo(packageName,
					PackageManager.GET_UNINSTALLED_PACKAGES);
			return true;
		} catch (NameNotFoundException e) {
			Log.e("TAG", "Exception when retrieving package: " + packageName, e);
			// show dialog
			return false;
		}
	}
	
/*	public static void setClearSymbol(Context context,Boolean value){
		 SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		 preferences.edit().putBoolean("less_than_50", value).commit();
	}
	
	public static boolean isWarningStorage(Context context){
		 
		 SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		 return preferences.getBoolean("less_than_50", false);
	}*/
	
/*	public static void startWaring(Context context,boolean ok_finish) {
		Intent intent = new Intent(context, ClearWaring.class);
		intent.putExtra("ok_finish", ok_finish);
		intent.putExtra("isFromOwer", true);
		context.startActivity(intent);
	}*/
	
	public static String getTopAppPackageName(Context context){
		try {
			ActivityManager am = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
			ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
			String currentPackageName = cn.getPackageName();
			return currentPackageName;
		} catch (Exception e) {
			// TODO: handle exception
			return "";
		}
	}
}
