package com.hiveview.clear.manager;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import com.hiveview.clear.manager.ClearDataManager.ClearDataPackageInfo;
import com.hiveview.clear.manager.ClearDataManager.MyComparator;
import com.hiveview.clear.manager.widget.AlertDialog;
import com.hiveview.clear.manager.widget.LauncherFocusView;
import com.hiveview.clear.manager.widget.LauncherFocusView.FocusViewAnimatorEndListener;
import com.hiveview.clear.manager.widget.SnowView;
import com.hiveview.clear.manager.widget.ToastUtils;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageStatsObserver;
import android.R.bool;
import android.R.integer;
import android.app.Activity;
import android.app.SearchManager.OnDismissListener;
import android.appwidget.AppWidgetProviderInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ManualClearManager extends BaseActivity{

	private ArrayList<String> packList = new ArrayList<String>();
	private ArrayList<AppInfo> appInfos = new ArrayList<AppInfo>();
	private ManualAppAdapter manualAppAdapter;
 
	private String TAG = "ManualClearActivity";
	//top
	private TextView version;
	private TextView rom_size;
		
	private ListView listView;
	
	private int currentViewFlag;
	private final int CLEAR = 0;
	private final int UNINSTALL = 1;
	public static int index = 0;
	public static boolean isAddSystemInfo=false;

	
	private int OP_STATUS = 0;
	
	// focus view
	private View mItemFocusView = null;
	private LauncherFocusView mLauncherFocusView = null;
	private boolean mIsFirstIn = true;
	
	private RunningAppInfo runningAppInfoParam;
    private ClearDataPackageInfo clearDataPackageInfo;
    
    private int currentSelectId = 0;
    private ClearDataManager clearDataManager;
    //初始化执行notifyDataSetChanged后，button焦点消失了的处理办法
    private boolean focusing = false;
    
    private AlertDialog alertDialog;
    private final static int UNINSTALL_DIALOG = 0;
    private final static int CLEAR_DIALOG = 1;
    
    private Long currentAppDataSize=0l;
    private final int CLEAR_APP_DATA_FINISHED = 0;
    private final int UPDATE_LISTVIEW = 1;
    private final int UPDATE_LISTVIEW_INIT_FOCUS = 2;
    private final static int START_SNOW_ANIMATION = 3;
    private final static int UPDATE_CLEAR_APP_STATUS =4;
    private final static int START_GET_PACKAGE_INFO = 5;
    private final static int UPDATE_FOCUS = 6;
    private final static int REMOVE_APP_UPDATE = 7;
    private final static int INIT_LIST_ADAPTER = 8;
    
    private HandlerThread toastHandleThread = new HandlerThread("toast");
    private Handler toastHandler;
    
    private SnowView snowView;
    
    private int buttonKeyCode = 0;
    private int minHeight = 0;
	
    private static ArrayList<String> showlist = new ArrayList<String>();

    private boolean DELETEING = false;
    private boolean remove = false;  
 
    Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case INIT_LIST_ADAPTER:
				manualAppAdapter = new ManualAppAdapter(ManualClearManager.this, appInfos,listView);
				listView.setAdapter(manualAppAdapter);
				listView.setFocusable(false);
				View view = listView.getChildAt(0);
				initListChildView(view);
				/*防止卸载所有app后5键呼出系统应用时，焦点框丢失*/
				mLauncherFocusView.setVisibility(View.VISIBLE);
				Utils.print(TAG, "size===="+appInfos.size());
				break;
			case UPDATE_LISTVIEW:
				manualAppAdapter.notifyDataSetChanged();
				//1.0+需要延时
				mHandler.removeMessages(UPDATE_LISTVIEW_INIT_FOCUS);
				mHandler.sendEmptyMessageDelayed(UPDATE_LISTVIEW_INIT_FOCUS, 300);
				break;
			case UPDATE_LISTVIEW_INIT_FOCUS:
				if (!focusing) {
					focusing = true;
					Utils.print(TAG,">>>>>>"+ (currentSelectId - listView.getFirstVisiblePosition()));
					listView.setFocusable(false);
					view = listView.getChildAt(currentSelectId- listView.getFirstVisiblePosition());

					initListChildView(view);
					focusing = false;
				}
			case START_SNOW_ANIMATION:
				snowView.setVisibility(View.VISIBLE);
				break;
			case UPDATE_CLEAR_APP_STATUS:
				updateTopSize();
				break;
			case START_GET_PACKAGE_INFO:
				Utils.print(TAG, "start getdata...............................");
				getAppPackageSizeInfo();
				break;
			case UPDATE_FOCUS:
				Utils.print(TAG, "update focus");
				view = listView.getChildAt(currentSelectId- listView.getFirstVisiblePosition());
				resetFocus(view);
				break;
			case REMOVE_APP_UPDATE:
				
						currentSelectId--;
						if (currentSelectId <0) {
							currentSelectId = 0;
							listView.setAdapter(manualAppAdapter);
							listView.setSelection(currentSelectId);
							manualAppAdapter.notifyDataSetChanged();
						}else{
						listView.setSelection(currentSelectId);
						manualAppAdapter.notifyDataSetChanged();
				}
				remove = true;
				Log.i(TAG, "remove_app_update  currentSelectId is "+currentSelectId);
				break;
			default:
				break;
			}
 
		}
    	
    };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manual_clear_main_activity);
		
		Utils.print(TAG, "onCreate...............................");
		/*  防止按遥控器清理键后5键功能失效的问题      */
		isAddSystemInfo=false;
		initView();
		initData();
		initShowSystemAppList();
 
		toastHandleThread.start();
		handleToast();
  
		
		initPackageInfo();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				initAdapder();
			}
		}).start();
		listView.setVerticalScrollBarEnabled(false);
		
		/*给ListView设置ScrollListener监听，使ListView在滚动的时候可以清除掉ChildView中获取的焦点*/
		listView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				 if (SCROLL_STATE_TOUCH_SCROLL == scrollState) {
		                View currentFocus = getCurrentFocus();
		                if (currentFocus != null) {
		                    currentFocus.clearFocus();
		                }
		            }
			}
			
			@Override
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}
		});
		
		listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				currentSelectId = arg2;
				
				if (currentSelectId==3) {
					listView.setVerticalScrollBarEnabled(true);
				}else if(currentSelectId<2) {
					listView.setVerticalScrollBarEnabled(false);
				}
				Utils.print(TAG, "select pos ==="+arg2+",   keyFlag=="+currentViewFlag);
                if(currentSelectId!=0){
                	resetFocusTextColor();
                }
                
				initListChildView(arg1);
				DELETEING = false;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});
 
	}
	
	View.OnFocusChangeListener oneKeyClearFocusChangeListener = new View.OnFocusChangeListener() {

		@Override
		public void onFocusChange(View view, boolean arg1) {
			// TODO Auto-generated method stub
			Utils.print(TAG, "arg1=="+arg1+",view id===="+view.getId());
 
  
			int[] location = new int[2];
			view.getLocationOnScreen(location);
			int targetY = location[1];
			
			if(currentSelectId==0 && minHeight==0){
				minHeight = targetY;
			}
			Utils.print(TAG, "currentid==="+currentSelectId+",first view id=="+listView.getFirstVisiblePosition());
			Utils.print(TAG, "targetY===="+targetY);
			Utils.print(TAG, "minHeight=="+minHeight);
			//由于1.0s项目，向下移动的时候，targetY会移动到顶部，这样就导致焦点不正确的问题
	 
			
			if(targetY<=minHeight && currentSelectId!=0){
				if(buttonKeyCode!=KeyEvent.KEYCODE_DPAD_LEFT && buttonKeyCode!=KeyEvent.KEYCODE_DPAD_RIGHT){
					Utils.print(TAG, "current");
					View tempView = listView.getChildAt(currentSelectId-listView.getFirstVisiblePosition());
					initListChildView(tempView);
					return;
				}
				
			}
			
           
			Utils.print(TAG, "deleteing=="+DELETEING);
			boolean re=false;
			// 卸载时超过４行，处理删除后焦点上移
			if (DELETEING && currentSelectId >= 3
					&& buttonKeyCode != KeyEvent.KEYCODE_DPAD_UP
					&& buttonKeyCode != KeyEvent.KEYCODE_DPAD_DOWN) {
				 re = focusViewPersistPosition(targetY);
				 if(re)
					return;
			}
			// 卸载5行数据的情况，卸载项停留在倒数第二个的，也不移动焦点
			if (DELETEING && appInfos.size() == 4 && currentSelectId == 2) {
				re = focusViewPersistPosition(targetY);
				if(re)
					return;
			}
			
			
			
			
			
			
			Utils.print(TAG, "========");
				
			if (arg1) {
				mItemFocusView = view;
				if (mIsFirstIn) {
					mIsFirstIn = false;
					Utils.print(TAG, "focus view init=="+mItemFocusView.getId());
					mLauncherFocusView.setVisibility(View.VISIBLE);
					Log.i(TAG, "***initFocusView***");
					mLauncherFocusView.initFocusView(mItemFocusView, false, 0f);
				} else {
					if (currentSelectId == 2 && remove) {
						remove = false;
						if(buttonKeyCode == KeyEvent.KEYCODE_DPAD_DOWN){
						Log.i(TAG, "***************");
						mLauncherFocusView.moveTo(listView.getChildAt(currentSelectId).findViewById(R.id.uninstall));
						}
					}else {
						Utils.print(TAG, "move view   currentSelectId is "+currentSelectId);
						mLauncherFocusView.moveTo(mItemFocusView);
					}
				}
			}
  
 
			switch (view.getId()) {
			case R.id.uninstall:
				if(arg1){
					listviewFocusDirect(view);
					if(!appInfos.get(currentSelectId).isSystemApp() && !isShowSystemAppList(appInfos.get(currentSelectId).getPackname())){
						((Button)view).setTextColor(getResources().getColor(R.color.main_button_selected));
					}else {
						((Button)view).setTextColor(getResources().getColor(R.color.main_button_unselected));
					}
				}
				else {
					((Button)view).setTextColor(getResources().getColor(R.color.main_button_unselected));
				}
				break;
			case R.id.clear:
				if(arg1){
					listviewFocusDirect(view);
					if(appInfos.get(currentSelectId).getDataSize()!=null && appInfos.get(currentSelectId).getDataSize()!=0 
//							&&!isShowSystemAppList(appInfos.get(currentSelectId).getPackname())
							&& !appInfos.get(currentSelectId).isSystemApp()){
						((Button)view).setTextColor(getResources().getColor(R.color.main_button_selected));
					}else {
						((Button)view).setTextColor(getResources().getColor(R.color.main_button_unselected));
					}
				}
				else {
					((Button)view).setTextColor(getResources().getColor(R.color.main_button_unselected));
				}
				break;
			default:
				break;
			}

		}
	};
	
	
	/**
	 * 确定listview item　button下一个焦点方向
	 * @param view
	 */
	private void listviewFocusDirect(View view){
		if(view.getId()==R.id.clear){
			currentViewFlag = CLEAR;
		}else if(view.getId()==R.id.uninstall){
			currentViewFlag = UNINSTALL;
		}
	}
	
	/*
	 * 卸载、清空按钮操作
	 */
	View.OnClickListener onClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			switch (arg0.getId()) {
			case R.id.clear:
				Utils.print(TAG, "on click clear selectid=="+currentSelectId);
				OP_STATUS = CLEAR;
				Utils.print(TAG, "data==="+appInfos.get(currentSelectId).getDataSize());
				if(appInfos.get(currentSelectId).getDataSize()==0 || appInfos.get(currentSelectId).isSystemApp()){
					toastShow(getResources().getString(R.string.cannot_clear_tips));
				}else{
					initAlertDialog(CLEAR_DIALOG);
				}
				break;
			case R.id.uninstall:
				OP_STATUS = UNINSTALL;
				Utils.print(TAG, "on click uninstall selectid==="+currentSelectId+",appsize="+appInfos.size());
				if(currentSelectId<appInfos.size() && !DELETEING){
					if(isShowSystemAppList(appInfos.get(currentSelectId).getPackname()) 
							|| appInfos.get(currentSelectId).isSystemApp()){
						toastShow(getResources().getString(R.string.cannot_uninstall_tips));
					}else {
						initAlertDialog(UNINSTALL_DIALOG);
					}
				}
				break;
			default:
				break;
			}
		}
	};
	
	
	public void initView(){

		listView = (ListView)findViewById(R.id.app_list);
		version = (TextView)findViewById(R.id.version);
		rom_size = (TextView)findViewById(R.id.rom_size);
		mLauncherFocusView = (LauncherFocusView) findViewById(R.id.focus_view);
		mLauncherFocusView.setBg(R.drawable.button_selected);
		
		snowView = (SnowView)findViewById(R.id.snow);
		
		alertDialog = new AlertDialog(this);
	}
	
	/*
	 * 初始化顶部数据
	 */
	public void initData(){
		try {
			version.setText(getResources().getString(R.string.version) +this.getPackageManager().getPackageInfo(
					getPackageName(), 0).versionName);
		} catch (Exception e) {
			// TODO: handle exception
		}
		rom_size.setText(Utils.getSizeStr(this,Utils.getRomAvailableSize())+"/"+Utils.getRomTotalSize());
 
	}
	
	/*
	 * listview适配器
	 */
	public void initAdapder(){
		try {
			runningAppInfoParam = RunningAppInfo.getAppInfoParam(this);
			List<ApplicationInfo> applicationInfos = runningAppInfoParam.getInstallAppInfo();
			
			ArrayList<AppInfo> systemInfos = new ArrayList<AppInfo>();
			systemInfos.clear();
			//system app
			for (ApplicationInfo app : applicationInfos) {
				if(!isShowSystemAppList(app.packageName) && index !=5){
			        continue;
					}
				AppInfo info = new AppInfo();	   
//	            info.setSystemApp(true);
     			 Utils.print(TAG, "pk=="+app.packageName+",isystem=="+info.isSystemApp());
	            //背景图
	            info.setBgId(getRandomAppBg());
	            //包名
	            info.setPackname(app.packageName);
	            //应用的名字
	            info.setAppName(runningAppInfoParam.getAppName(this, app.packageName));
	            Log.i(TAG, "adapter systemapp appName is "+info.getAppName());
	            //app size
	            PackageInfo in= getPackageManager().getPackageInfo(app.packageName, 0);
			    String dir=in.applicationInfo.sourceDir;
			    long l=(new File(dir)).length();
			    info.setApp_size(l);
			    
			    //data size 
			    info.setDataSize(ClearApplication.getInstance().getAppCache().get(app.packageName));
				
			    //versionName
				String version = in.versionName;
			    info.setVersionName(version);
			    
			  	//versionCode
			  	int code = in.versionCode;
			    info.setVersionCode(code);
			    
	            systemInfos.add(info);				
	        }  
			//app大小排序
			Comparator cmp = new MyComparator();
			Collections.sort(systemInfos, cmp);
			appInfos.addAll(systemInfos);
			
			
			
			
			ArrayList<AppInfo> thridInfos = new ArrayList<AppInfo>();
			thridInfos.clear();
			for (ApplicationInfo app : applicationInfos) {    
				if(clearDataManager.checkBlackList(app.packageName)){
					continue;
				}
				
				if(((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0)){
					continue;
				}
				if(isShowSystemAppList(app.packageName)){
					continue;
				}
				if (index == 5) {
					continue;
				}
				//判断如果是进入清理管家后，安装的app，不显示在列表
				AppInfo info = new AppInfo();
				//判断是否为系统app
	            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0 && !isShowSystemAppList(app.packageName)) {
	            	info.setSystemApp(true);
	            }else {
					info.setSystemApp(false);
				}    
//	            Utils.print(TAG, "pk=="+app.packageName+",isystem=="+info.isSystemApp());
	            //背景图
	            info.setBgId(getRandomAppBg());
	            //包名
	            info.setPackname(app.packageName);
	            //应用的名字
	            info.setAppName(runningAppInfoParam.getAppName(this, app.packageName));
	            Log.i(TAG, "adapter thirdapp appName is "+info.getAppName());
	            //app size
	            PackageInfo in= getPackageManager().getPackageInfo(app.packageName, 0);
			    String dir=in.applicationInfo.sourceDir;
			    long l=(new File(dir)).length();
			    info.setApp_size(l);
			    
			    //data size 
			    info.setDataSize(ClearApplication.getInstance().getAppCache().get(app.packageName));
			    
			    //versionName
				String version = in.versionName;
			    info.setVersionName(version);
			    
			  	//versionCode
			  	int code = in.versionCode;
			    info.setVersionCode(code);
				
			    thridInfos.add(info);
			}
			Utils.print(TAG, "count size=="+thridInfos.size());
			//app大小排序
			Collections.sort(thridInfos, cmp);
			appInfos.addAll(thridInfos);
			
			mHandler.sendEmptyMessage(INIT_LIST_ADAPTER);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		if (index == 5 && !isAddSystemInfo) {
			//延时处理，目的解决焦点异常
			mHandler.sendEmptyMessageDelayed(START_GET_PACKAGE_INFO, 1000);
			}
		}
	
	
	public void initAdapderAgain(){
		try {
			runningAppInfoParam = RunningAppInfo.getAppInfoParam(this);
			List<ApplicationInfo> applicationInfos = runningAppInfoParam.getInstallAppInfo();
			
			ArrayList<AppInfo> systemInfos = new ArrayList<AppInfo>();
			systemInfos.clear();
			//system app
			for (ApplicationInfo app : applicationInfos) {
				if(isShowSystemAppList(app.packageName)){
			        continue;
					}
				if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
					continue;
				}
				AppInfo info = new AppInfo();	   
	            info.setSystemApp(true);
     			 Utils.print(TAG, "pk=="+app.packageName+",isystem=="+info.isSystemApp());
	            //背景图
	            info.setBgId(getRandomAppBg());
	            //包名
	            info.setPackname(app.packageName);
	            //应用的名字
	            info.setAppName(runningAppInfoParam.getAppName(this, app.packageName));
	            Log.i(TAG, "again systemapp appName is "+info.getAppName());
	            //app size
	            PackageInfo in= getPackageManager().getPackageInfo(app.packageName, 0);
			    String dir=in.applicationInfo.sourceDir;
			    long l=(new File(dir)).length();
			    info.setApp_size(l);
			    
			    //data size 
			    info.setDataSize(ClearApplication.getInstance().getAppCache().get(app.packageName));
				
			    //versionName
				String version = in.versionName;
			    info.setVersionName(version);
			    
			  	//versionCode
			  	int code = in.versionCode;
			    info.setVersionCode(code);
			    
	            systemInfos.add(info);				
	        }  
			//app大小排序
			Comparator cmp = new MyComparator();
			Collections.sort(systemInfos, cmp);
			appInfos.addAll(systemInfos);
			
			
			mHandler.sendEmptyMessage(INIT_LIST_ADAPTER);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		if (index == 5 && !isAddSystemInfo) {
			//延时处理，目的解决焦点异常
			mHandler.sendEmptyMessageDelayed(START_GET_PACKAGE_INFO, 1000);
			}
		}
	
	
	
	
	/*
	 * 应用数据大小的系统回调
	 */
	final IPackageStatsObserver.Stub mStatsObserver = new IPackageStatsObserver.Stub() {
		public void onGetStatsCompleted(PackageStats stats, boolean succeeded) {
			if (succeeded && stats != null) {
				synchronized (this) {
					try {
						Utils.print(TAG, "pack==="+stats.packageName);
						long dataSize = stats.dataSize+stats.cacheSize;
						
						Utils.print(TAG, "data size==" + dataSize+"");
	 
						updateAppInfo(stats.packageName,dataSize);
	 
						Utils.print(TAG, "get all app data finished");
						mHandler.sendEmptyMessage(UPDATE_LISTVIEW);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
				}
			}
		}
	};
	private long time,time1,time2 = 0;
	
	/*
	 * 回调数据后，刷新UI
	 */
	private void updateAppInfo(String packageName,Long dataSize){
		for(int i=0;i<appInfos.size();i++){
			if(appInfos.get(i).getPackname().equals(packageName)){
				appInfos.get(i).setDataSize(dataSize);
				break;
			}
		}
	}
	
	/*
	 * 获取系统app的数据大小
	 */
	public void getAppPackageSizeInfo(){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				int size = appInfos.size();
				Utils.print(TAG, "size=="+size);
				for(int i=0;i<size;i++){
					getPackageManager().getPackageSizeInfo(appInfos.get(i).getPackname(), mStatsObserver);
				}
			}
		}).start();
	}
	
	/*
	 * 初始化清除数据的回调
	 */
	public void initPackageInfo(){
		clearDataPackageInfo = new ClearDataPackageInfo() {
			
			@Override
			public void clearUserData(String packageName,Long size,boolean sucess) {
				// TODO Auto-generated method stub
				Utils.print(TAG, "clear end status=="+sucess);
				try {
					//send message update main UI scan result
					updateMainScanResult(packageName);
					Utils.print(TAG, "clear finished");
					toastHandler.sendEmptyMessage(CLEAR_APP_DATA_FINISHED);
					ClearApplication.getInstance().getAppCache().put(packageName, 0l);
					mHandler.sendEmptyMessage(UPDATE_CLEAR_APP_STATUS);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		};
		clearDataManager = new ClearDataManager(this, clearDataPackageInfo);
	}
	
	
	/*
	 * 卸载app系统回调
	 */
	class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
		public void packageDeleted(String packageName, int returnCode) {
			if(returnCode == 1){
				//uninstall sucess,update ui
				synchronized (this) {
					try {
						//send message update main UI scan result
						DELETEING = true;
						
						updateMainScanResult(packageName);
						
						removeAppInfo(packageName);
						ClearApplication.getInstance().getAppCache().remove(packageName);
					
	 
						mHandler.sendEmptyMessage(REMOVE_APP_UPDATE);
						mHandler.sendEmptyMessage(UPDATE_CLEAR_APP_STATUS);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
				}
			}else {
				DELETEING = false;
			}
		}
	}
	
	/**
	 * 更新主要扫描结果，以及主页左边列表显示的数据
	 * @param packageName
	 */
	private void updateMainScanResult(String packageName){
		Intent intent = new Intent(ClearManager.ACTION_UPDATE_SCAN_RESULT);
		intent.putExtra("packagename", packageName);
		intent.putExtra("size", ClearApplication.getInstance().getAppCache().get(packageName));
		sendBroadcast(intent);
	}
	
	/**
	 * 移除列表里面的app数据
	 * @param packageName
	 */
	private void removeAppInfo(String packageName){
		manualAppAdapter.removeAppInfo(packageName);
		int i;
		for(i=0;i<appInfos.size();i++){
			if(appInfos.get(i).getPackname().equals(packageName)){
				break;
			}
		}
		appInfos.remove(i);
	}
	
	/**
	 * 卸载app
	 * @param packageName
	 */
	public void uninstallPackage(String packageName){
		if(isShowSystemAppList(packageName))
			return;

		PackageDeleteObserver observer = new PackageDeleteObserver();
		getPackageManager().deletePackage(packageName, observer,0);
	}
	
	public void clearAppData(){
		Utils.print(TAG, "clear app data pk=="+appInfos.get(currentSelectId).getPackname());
		currentAppDataSize = appInfos.get(currentSelectId).getDataSize();
		clearDataManager.clearUserData(appInfos.get(currentSelectId).getPackname());
	}
	
	/*解决上下按键失效导致5键部分功能缺失的问题*/
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		Utils.print(TAG, "keyCode>>>"+event.getKeyCode());
		buttonKeyCode=event.getKeyCode();
		if (event.getAction()==event.ACTION_UP) {
			if (event.getKeyCode()!=KeyEvent.KEYCODE_MENU) {
				index=0;
				Utils.print(TAG, "UP-menu-keyCode>>>"+index);
			}else {
				index =event.getRepeatCount()>0?0:index;
				Utils.print(TAG, "UP-menu-index>>>"+index);
			}
		}
		if (event.getAction()==event.ACTION_DOWN) {
			if(event.getKeyCode() == KeyEvent.KEYCODE_MENU){
				index++;
				Utils.print(TAG, "DOWN-menu-index>>>"+index);
				index =event.getRepeatCount()>0?0:index;
				if (index == 5 && !isAddSystemInfo) {
					initAdapderAgain();
					isAddSystemInfo=true;
				}
//				test();
//				new Thread(run2).start();  
//				toastShow();
			}else{
				if (event.getKeyCode()==KeyEvent.KEYCODE_BACK) {
					isAddSystemInfo=false;
				}else if (event.getKeyCode()==KeyEvent.KEYCODE_DPAD_DOWN) {
					/*解决长按不显示滚动条的问题*/
					if (event.getRepeatCount()>0) {
						listView.setVerticalScrollBarEnabled(true);
						index=0;
					}
				}
				index=0;
				Utils.print(TAG, "DOWN-total-index>>>"+index);
			}
		}
		return super.dispatchKeyEvent(event);
	}

	private void test(){
		View rootview = ManualClearManager.this.getWindow().getDecorView();
		View aaa = rootview.findFocus();
		Utils.print(TAG, "1id==="+aaa.getId() + "");
	}
 
	
	/*
	 * 处理按键监听
	 */
	private void initListChildView(View view){
		Utils.print(TAG, "initListChildView");
		if(view==null)
			return;
		
		if(DELETEING)
			currentViewFlag = UNINSTALL;
		
		Button clearButton = (Button)view.findViewById(R.id.clear);
		Button uninstallButton = (Button)view.findViewById(R.id.uninstall);
		
		clearButton.setOnFocusChangeListener(oneKeyClearFocusChangeListener);
		uninstallButton.setOnFocusChangeListener(oneKeyClearFocusChangeListener);

		clearButton.setOnClickListener(onClickListener);
		uninstallButton.setOnClickListener(onClickListener);
		
//		clearButton.setOnKeyListener(onKeyListener);
//		uninstallButton.setOnKeyListener(onKeyListener);
		
		clearButton.setTextColor(getResources().getColor(R.color.main_button_unselected));
		uninstallButton.setTextColor(getResources().getColor(R.color.main_button_unselected));
		
		if(currentViewFlag==CLEAR){
			Utils.print(TAG, "clear focus");
			clearButton.setFocusable(true);
			clearButton.requestFocus();
			clearButton.setFocusableInTouchMode(true);
		}else if(currentViewFlag==UNINSTALL){
			Utils.print(TAG, "uninstall focus");
			uninstallButton.setFocusable(true);
			uninstallButton.requestFocus();
			uninstallButton.setFocusableInTouchMode(true);
		}

		if(clearButton.isFocused() && !appInfos.get(currentSelectId).isSystemApp() 
//				&& !isShowSystemAppList(appInfos.get(currentSelectId).getPackname()) 
				&& (appInfos.get(currentSelectId).getDataSize()!=null && appInfos.get(currentSelectId).getDataSize()!=0)){
			Utils.print(TAG, "set color current select id===="+currentSelectId+",name="+appInfos.get(currentSelectId).getAppName());
			clearButton.setTextColor(getResources().getColor(R.color.main_button_selected));
	    }else {
			clearButton.setTextColor(getResources().getColor(R.color.main_button_unselected));
		}
		
		
		if(uninstallButton.isFocused() && !appInfos.get(currentSelectId).isSystemApp()
				&& !isShowSystemAppList(appInfos.get(currentSelectId).getPackname())){
			uninstallButton.setTextColor(getResources().getColor(R.color.main_button_selected));
	    	Utils.print(TAG, "set color current select id===="+currentSelectId+",name="+appInfos.get(currentSelectId).getAppName());
		}else {
			uninstallButton.setTextColor(getResources().getColor(R.color.main_button_unselected));
		}
		
		listviewFocusDirect(view);
		
	}
	
	  
	private void resetFocus(View view){
		Utils.print(TAG, "reset focus initListChildView");
		if(view==null || currentSelectId!=0)
			return;
		Button clearButton = (Button)view.findViewById(R.id.clear);
		Button uninstallButton = (Button)view.findViewById(R.id.uninstall);
		
		clearButton.setOnFocusChangeListener(oneKeyClearFocusChangeListener);
		uninstallButton.setOnFocusChangeListener(oneKeyClearFocusChangeListener);

		clearButton.setOnClickListener(onClickListener);
		uninstallButton.setOnClickListener(onClickListener);
		
//		clearButton.setOnKeyListener(onKeyListener);
//		uninstallButton.setOnKeyListener(onKeyListener);
		
		clearButton.setTextColor(getResources().getColor(R.color.main_button_unselected));
		uninstallButton.setTextColor(getResources().getColor(R.color.main_button_unselected));
		
		if(OP_STATUS==CLEAR){
			Utils.print(TAG, "clear");
			clearButton.setFocusable(true);
			clearButton.requestFocus();
			clearButton.setFocusableInTouchMode(true);
		}else if(OP_STATUS==UNINSTALL){
			Utils.print(TAG, "uninstall");
			uninstallButton.setFocusable(true);
			uninstallButton.requestFocus();
			uninstallButton.setFocusableInTouchMode(true);
		}	
		
		Log.i(TAG, "isShowSystemAppList() == "+isShowSystemAppList(appInfos.get(currentSelectId).getPackname()));
		if(clearButton.isFocused() && !appInfos.get(currentSelectId).isSystemApp() 
//				&& !isShowSystemAppList(appInfos.get(currentSelectId).getPackname())
				&& (appInfos.get(currentSelectId).getDataSize()!=null && appInfos.get(currentSelectId).getDataSize()!=0)){
			clearButton.setTextColor(getResources().getColor(R.color.main_button_selected));
			Utils.print(TAG, "set color current select id===="+currentSelectId+",name="+appInfos.get(currentSelectId).getAppName());
		}else {
			clearButton.setTextColor(getResources().getColor(R.color.main_button_unselected));
		}
		
		
		if(uninstallButton.isFocused() && !appInfos.get(currentSelectId).isSystemApp()
				&& !isShowSystemAppList(appInfos.get(currentSelectId).getPackname())){
			Utils.print(TAG, "set color current select id===="+currentSelectId+",name="+appInfos.get(currentSelectId).getAppName());
			uninstallButton.setTextColor(getResources().getColor(R.color.main_button_selected));
		}
		else {
			uninstallButton.setTextColor(getResources().getColor(R.color.main_button_unselected));
		}
 
		
	}
	
	
	/*
	 * 确定对话框提示
	 * 修改处：弹出对话框时，聚焦至确定按钮
	 */
	private void initAlertDialog(final int dialogID){
		alertDialog = new AlertDialog(this);
 
		Window dialogWindow = alertDialog.getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.width = getResources().getDimensionPixelSize(R.dimen.alert_dialog_width);
		lp.height = getResources().getDimensionPixelSize(R.dimen.alert_dialog_height);
		dialogWindow.setAttributes(lp);
		////////修改处////////
		Button yes = (Button) alertDialog.findViewById(R.id.yes);
		Button no = (Button) alertDialog.findViewById(R.id.no);
		yes.setFocusableInTouchMode(true);
		mItemFocusView = yes;
		mLauncherFocusView.setVisibility(View.VISIBLE);
		Log.i(TAG, "dialog initFocusView");
		mLauncherFocusView.initFocusView(mItemFocusView, false, 0f);
		if(currentSelectId<appInfos.size()){
			alertDialog.show();
		
		TextView message = (TextView) alertDialog.findViewById(R.id.message);
		if(dialogID==CLEAR_DIALOG){
			message.setText(String.format(getResources()
					.getString(R.string.clear_tips),appInfos.get(currentSelectId).getAppName()));
		}else if(dialogID==UNINSTALL_DIALOG){
			String locale = Locale.getDefault().getLanguage();
			if(locale.equals("en")){
				message.setText(getResources().getString(R.string.uninstall_tips));
			}else {
				message.setText(String.format(getResources()
						.getString(R.string.uninstall_tips),appInfos.get(currentSelectId).getAppName()));
			}
		}
	 
		//test
//		message.setText("是否确认清除com.droidamlogic.SubtitleService的数据？");
		
//		Button yes = (Button) alertDialog.findViewById(R.id.yes);
//		Button no = (Button) alertDialog.findViewById(R.id.no);
		yes.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				// TODOAuto-generated method stub
				if(dialogID==CLEAR_DIALOG){
					clearAppData();
				}else if(dialogID==UNINSTALL_DIALOG){
					uninstallPackage(appInfos.get(currentSelectId).getPackname());
				}
				Log.i(TAG, "listView.getCount()=="+listView.getCount());
				Log.i(TAG, "dialog dismiss    currentSelectId  is "+currentSelectId);
				alertDialog.dismiss();
				
				//如果listview为空，则将光标置空
				if (listView.getCount()==1) {
					Log.i(TAG, "null>>>>>>");
					mLauncherFocusView.setVisibility(View.GONE);
				}

			}
		});
		no.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				// TODO Auto-generated method stub
				alertDialog.dismiss();
			}
		});
		
		}
 
	}
	
	/**
	 * 顶部自定义toast显示
	 */
	private void toastShow(){
		Utils.print(TAG, "toast show");
		String text = String.format(getResources().getString(R.string.cleared_data),
				appInfos.get(currentSelectId).getAppName());
		ToastUtils.showToast(ManualClearManager.this, text, Toast.LENGTH_LONG);
		 
	}
	
	/**
	 * 顶部自定义toast显示
	 */
	private void toastShow(String message){
		ToastUtils.showToast(ManualClearManager.this, message, Toast.LENGTH_SHORT);
	}
	
	/**
	 * toast handle 显示
	 */
	private void handleToast(){
		toastHandler = new Handler(toastHandleThread.getLooper()){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case CLEAR_APP_DATA_FINISHED:
					toastShow();
					getPackageManager().getPackageSizeInfo(appInfos.get(currentSelectId).getPackname(), mStatsObserver);
					break;
				default:
					break;
				}
				super.handleMessage(msg);
			}
			
		};
	}
	
	
	/**
	 * 每行app　图标的背景图，随机获取
	 * @return 资源图id
	 */
	private int getRandomAppBg(){
		int[] app_bg = {R.drawable.app_bg_1,R.drawable.app_bg_2,R.drawable.app_bg_3,
				R.drawable.app_bg_4,R.drawable.app_bg_5,R.drawable.app_bg_6,
				R.drawable.app_bg_7};
		
		int id = (int)(Math.random()*app_bg.length)+1;
		if(id==1){
			return R.drawable.app_bg_1;
		}else if(id==2){
			return R.drawable.app_bg_2;
		}else if(id==3){
			return R.drawable.app_bg_3;
		}else if(id==4){
			return R.drawable.app_bg_4;
		}else if(id==5){
			return R.drawable.app_bg_5;
		}else if(id==6){
			return R.drawable.app_bg_6;
		}else if(id==7){
			return R.drawable.app_bg_7;
		}else {
			return R.drawable.app_bg_7;
		}
	}
	
	/**
	 * 移除可删除数据为零，且无法卸载的系统应用
	 */
	private void removeInvalidApp(){
		ArrayList<AppInfo> temAppInfos=new ArrayList<AppInfo>();
		temAppInfos.addAll(appInfos);
 
		
		for(int i=0;i<temAppInfos.size();i++){
			if(temAppInfos.get(i).isSystemApp() && (temAppInfos.get(i).getDataSize()==0)){
				Utils.print(TAG, "remove ui pk>>>>"+temAppInfos.get(i).getPackname());
				removeAppInfo(temAppInfos.get(i).getPackname());
			}
		}
		
		temAppInfos.clear();
		////////////////////////
		manualAppAdapter.notifyDataSetChanged();
		Utils.print(TAG, "remove finish");
		mHandler.sendEmptyMessage(UPDATE_LISTVIEW);
	}
	
	
	private void updateTopSize(){
    	if(rom_size!=null)
    	  rom_size.setText(Utils.getSizeStr(this,Utils.getRomAvailableSize())+"/"+Utils.getRomTotalSize());
    }
	
	
	/*
	 * app大小比较
	 */
    public class MyComparator implements Comparator<AppInfo> {  
        
        @Override  
        public int compare(AppInfo o1, AppInfo o2) {  
            // TODO Auto-generated method stub  
        	if (o1.getApp_size() < o2.getApp_size()) {
				return 1;
			} else if (o1.getApp_size() > o2.getApp_size()) {
				return -1;
			} else {
				return 0;
			} 
        }  
    } 
    
    /**
     * 根据产品定义，允许系统app　本地生活、鹏云课堂、活动专区显示出来
     */
    private void initShowSystemAppList(){
    	//极清首映：com.hiveview.premiere 不允许清除
    	//大麦影视：com.hiveview.cloudscreen.vipvideo　　不允许清除
    	//应用市场：com.hiveview.cloudscreen.appstore　　不允许清除
    	//本地生活：com.hiveview.locallife　　　　　　　
    	//直播com.hiveview.cloudscreen.videolive　　　不允许清除
    	//活动专区 com.hiveview.lotteryactivity
    	//鹏云课堂　com.peng.pengyun_domyboxintegration　
    	showlist.clear();
    	showlist.add("com.hiveview.locallife");
    	showlist.add("com.peng.pengyun_domyboxintegration");
    	showlist.add("com.hiveview.lotteryactivity");
    	showlist.add("com.drpeng.pengchat.tv");
    	showlist.add("com.drmsoft.AndroidDRMPlayer");
    }
    
    
    public static boolean isShowSystemAppList(String packageName){
    	boolean result = false;
    	
    	for(int i=0;i<showlist.size();i++){
    		if(showlist.get(i).equals(packageName)){
    			result = true;
    			break;
    		}
    	}
    	
    	return result;
    }
    
    /**
     *　解决第一行与其他行重复蓝色字体不能恢复的现象
     */
    private void resetFocusTextColor(){
    	
    	Log.v(TAG, "reset focus text color pos 0");
    	View view = listView.getChildAt(0);
    	if(view==null)
    		return;
    		
    	
    	
    	Button clearButton = (Button)view.findViewById(R.id.clear);
		Button uninstallButton = (Button)view.findViewById(R.id.uninstall);
		
		clearButton.setTextColor(getResources().getColor(R.color.main_button_unselected));
		uninstallButton.setTextColor(getResources().getColor(R.color.main_button_unselected));
    }
     
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (manualAppAdapter!=null) {
			manualAppAdapter.removeAllIcon();
		}
	}
	
	public boolean focusViewPersistPosition(int targetY){
		int visiblePos =currentSelectId-listView.getFirstVisiblePosition();
		Utils.print(TAG, "visiable pos=="+visiblePos);
		int height = (visiblePos+1)*minHeight;
		Utils.print(TAG, "height=="+height+",targety=="+targetY);
		if(targetY<height){
			Utils.print(TAG, "current2");
			View tempView = listView.getChildAt(visiblePos);
			initListChildView(tempView);
			Utils.print(TAG, "return======");
			return true;
		}
		return false;
	}
	
	/*
	 * 
	 * */
	protected class MyScrollListener implements OnScrollListener {

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                int visibleItemCount, int totalItemCount) {
            // do nothing 
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (SCROLL_STATE_TOUCH_SCROLL == scrollState) {
                View currentFocus = getCurrentFocus();
                if (currentFocus != null) {
                    currentFocus.clearFocus();
                }
            }
        }

    }
}
