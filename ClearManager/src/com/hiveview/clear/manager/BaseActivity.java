package com.hiveview.clear.manager;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

public class BaseActivity extends Activity{

	private String TAG = "base";
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Utils.print(TAG, "onstop");
		super.onStop();
		Utils.print(TAG, "#####"+isRunningBackground(this));
		//修改launcher拉起方式，达到了每次进入的时候，重新打开的状态
		if(isRunningBackground(this)){
			ClearApplication.getInstance().getAppCache().clear();
			Utils.print(TAG, "finish");
			finish();
		}
 
	}
	
	
	
	public boolean isRunningBackground(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		String currentPackageName = cn.getPackageName();
		Log.v(TAG, "top package name=" + currentPackageName);
		Log.v(TAG, "current package name=" + context.getPackageName());
		if (currentPackageName != null
				&& currentPackageName.equals(context.getPackageName())) {
			return false;
		}

		return true;
	}
}
