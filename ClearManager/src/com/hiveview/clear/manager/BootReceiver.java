package com.hiveview.clear.manager;
 
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {  
	 
	Context mContext;
	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		Log.v("ClearManager", "action===" + intent.getAction());
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Log.v("ClearManager","top===" + Utils.getTopAppPackageName(context));
			if (Utils.getRomAvailableSize() < 50 * 1024 * 1024 
					&& !Utils.getTopAppPackageName(mContext).equals(mContext.getPackageName())) {
				//延时５分钟提示空间不够
				Log.v("ClearManager", ">>>>>>start therad");
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							int time = 0;
							while(true){
							   Thread.sleep(1000);
							   time++;
							   Log.v("ClearManager","time===" +time);
							   if(time>=5*60){
								  if (Utils.getRomAvailableSize() < 50 * 1024 * 1024 
											&& !Utils.getTopAppPackageName(mContext).equals(mContext.getPackageName())) {
										//延时５分钟提示空间不够  
								   startWaring(mContext, false);
								   }
								   break;
							   }
							}
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
				}).start();
			} 
		}
	}
  
	public void startWaring(Context context, boolean ok_finish) {
        Log.v("ClearManager", "start");
		ComponentName componentName = new ComponentName(
				context.getPackageName(),
				"com.hiveview.clear.manager.ClearWaring");
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setComponent(componentName);
		intent.putExtra("isFromOwer", true);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}
} 
