package com.hiveview.clear.manager;

 

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.hiveview.clear.manager.ClearDataManager.ClearDataPackageInfo;
import com.hiveview.clear.manager.ClearDataManager.MyComparator;
import com.hiveview.clear.manager.ClearDataManager.PackageInfo;
import com.hiveview.clear.manager.widget.CountView;
import com.hiveview.clear.manager.widget.LauncherFocusView;
import com.hiveview.clear.manager.widget.RecycleSurfaceView;
import com.hiveview.clear.manager.widget.SnowView;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

 
public class ClearManager extends BaseActivity {
	 

	private String TAG = "ClearManager";
	
	//top
	private TextView version;
	private TextView rom_size;
	
	private final int MOVE_X = 450;
	//go left
	private ImageView go_left;
	private LinearLayout layout_go_left;
	private RelativeLayout layout_app_information;
	private TextView allow_clear_tips;
	private LinearLayout left_app_contain;
	private ScrollView scrollview;
	private  int tmp = 0;
	
	//center
	private CountView scan_size;
	private TextView scan_size_unit;
	private TextView scan_result_tips;
	private LinearLayout main_center;
	private RelativeLayout center_cycle_layout;
	private ImageView crycleIv;
	
	//clouds
	private LinearLayout layout_clouds;
	
	//right
	private Button oneKeyCleaner;
	private Button manualCleaner;
	private LinearLayout layout_one_key_operate;
	private LinearLayout layout_go_right;
	private ImageView go_right;
	
	private SnowView snowView;
	
	private ValueAnimator alphaAnimator;
	
	private PackageInfo packageInfo;
	private ClearDataPackageInfo clearDataPackageInfo;
	private ClearDataManager clearDataManager;
	
	private int clearAllAppSize = 0;
	
	private Long systemAppSize;
	private Long thridAppSize;
	
	private int systemAppCount;
	private int thridAppCount;
	
 
	private ArrayList<AppInfo> systemAppInfos = new ArrayList<AppInfo>();
	private ArrayList<AppInfo> thridAppInfos = new ArrayList<AppInfo>();
	private Long totalScanSize=0l;
	private Long oldTotalScanSizeLong=0l;
	
	// focus view
	private View mItemFocusView = null;
	private LauncherFocusView mLauncherFocusView = null;
	private boolean mIsFirstIn = true;
	
//	private AnimationDrawable center_animation;
	
	private ObjectAnimator objectNumberAnimator;
	private String mSize="0";
	private String oldSize = "0";
	private String mUnit="";
	
	private final int SCAN = 0;
	private final int CLEAR = 1;
	private final int SCANING = 2;
	private final int SCAN_END = 3;
	private final int CLEAR_ING = 4;
	private final int CLEAR_END = 5;
	private int OP_STATUS = 0;
	
	
	private final static int UPDATE_SCAN_DATA_SIZE = 0;
	private final static int SCAN_FINISH = 1;
	private final static int START_CRYCLE_ANIMATION = 2;
	private final static int START_SNOW_ANIMATION = 3;
	private final static int CLEAR_FINISHED = 4;
	private final static int SCAN_THRID_APP_PACKAGE_INFO = 5;
	private final static int UPDATE_UNIT = 6;
	private final static int UPDATE_CRYCLE_ANIMATION = 7;
 
	private ObjectAnimator arrowDownAnimator;
	private int MOVE_DULATION = 1000;
	
	public static final String ACTION_UPDATE_SCAN_RESULT = "com.hiveview.clear.manager.update.scan.result";
	
	private RecyleThread recyleThread;  
    private long mFrameSpaceTime = 100;  // 每帧图片的间隔时间  
    private boolean mIsDraw = true; 
    
    private PropertyValuesHolder propertyIvCrycle;
	private ObjectAnimator animatorIvCrycle;
	
	
	Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case START_CRYCLE_ANIMATION:
				startCrycleAnimation();
				break;
			case UPDATE_SCAN_DATA_SIZE:
				updateScanSizeUI();
				break;
			case SCAN_FINISH:
				updateScanFinishedUI();
				break;
			case START_SNOW_ANIMATION:
				snowView.setVisibility(View.VISIBLE);
				break;
			case CLEAR_FINISHED:
				oneKeyClearFinished();
				break;
			case SCAN_THRID_APP_PACKAGE_INFO:
				if(clearDataManager!=null)
				  clearDataManager.getThridAppPackageSizeInfo();
				break;
			case UPDATE_UNIT:
				String unit = (String)msg.obj;
				Utils.print(TAG, "update delay unit>>>>"+unit);
				if(mUnit.equals("B"))
					mUnit = " " +mUnit;
	    		scan_size_unit.setText(unit);
				break;
			case UPDATE_CRYCLE_ANIMATION:
				
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		tmp = 2;
		Utils.print(TAG, "onCreate>>>");
	  
 
		
		
		initView();
		initData();
		
 
		//动画分步执行，避免进入的时候黑屏太长时间
		mHandler.sendEmptyMessageDelayed(START_CRYCLE_ANIMATION, 500);
		mHandler.sendEmptyMessageDelayed(START_SNOW_ANIMATION, 1500);
		scanAllPackageInfo();
 
         
		regeditReceiver();
 
		
	} 
	
	private void initView(){
		version = (TextView)findViewById(R.id.version);
		rom_size = (TextView)findViewById(R.id.rom_size);
		
		go_left = (ImageView)findViewById(R.id.go_left);
		layout_go_left = (LinearLayout)findViewById(R.id.layout_go_left);
		layout_app_information = (RelativeLayout)findViewById(R.id.left_main_layout);
		allow_clear_tips = (TextView)findViewById(R.id.allow_clear_tips);
		left_app_contain = (LinearLayout)findViewById(R.id.contain);
		scrollview  = (ScrollView)findViewById(R.id.scrollview);
		
		scan_size = (CountView)findViewById(R.id.scan_size);
		scan_size_unit = (TextView)findViewById(R.id.scan_size_unit);
		scan_result_tips = (TextView)findViewById(R.id.scan_result_tips);
		main_center = (LinearLayout)findViewById(R.id.main_center);
		center_cycle_layout = (RelativeLayout)findViewById(R.id.center_cycle_layout);
//        recycleSurfaceView = (RecycleSurfaceView)findViewById(R.id.cycle);
		crycleIv = (ImageView)findViewById(R.id.cycle);
		
		
		oneKeyCleaner = (Button)findViewById(R.id.one_key_clear);
		manualCleaner = (Button)findViewById(R.id.manual_clear);
		layout_one_key_operate = (LinearLayout)findViewById(R.id.one_key_operate);
		oneKeyCleaner.setOnFocusChangeListener(oneKeyClearFocusChangeListener);
		manualCleaner.setOnFocusChangeListener(oneKeyClearFocusChangeListener);
		
		allow_clear_tips.setVisibility(View.GONE);
		
		go_right = (ImageView)findViewById(R.id.go_right);
		layout_go_right = (LinearLayout)findViewById(R.id.layout_go_right);
		 
		mLauncherFocusView = (LauncherFocusView) findViewById(R.id.focus_view);
		snowView = (SnowView)findViewById(R.id.snow);
		
		
		layout_clouds = (LinearLayout)findViewById(R.id.layout_clouds);
	}
	
	/*
	 * 初始话基本数据
	 */
	private void initData(){
		try {
			version.setText(getResources().getString(R.string.version) +this.getPackageManager().getPackageInfo(
					getPackageName(), 0).versionName);
		} catch (Exception e) {
			// TODO: handle exception
		}

		rom_size.setText(Utils.getSizeStr(this,Utils.getRomAvailableSize())+"/"+Utils.getRomTotalSize());

  
		scan_size.setText(mSize);
/*        *//**
         * test
         *//*
		scan_size.setText("2224.30");
		scan_size_unit.setText("MB");*/
		
		oneKeyCleaner.setOnClickListener(onClickListener);
		manualCleaner.setOnClickListener(onClickListener);
		
	 
	}
 
	/*
	 * 中心循环转动的动画
	 */
	private void startCrycleAnimation(){
		
		/*center_cycle_layout.setBackgroundResource(R.drawable.scan_loading);
		center_animation = (AnimationDrawable)center_cycle_layout.getBackground();  	          
		center_animation.start();*/
		crycleIv.setBackgroundResource(R.drawable.scan);

		if (propertyIvCrycle == null) {
			propertyIvCrycle = PropertyValuesHolder.ofFloat("Rotation", 0, 359);
		} else {
			propertyIvCrycle.setFloatValues(crycleIv.getRotation(), 359 + crycleIv.getRotation());
		}
		if (animatorIvCrycle == null) {
			animatorIvCrycle = ObjectAnimator.ofPropertyValuesHolder(crycleIv, propertyIvCrycle);
			animatorIvCrycle.setDuration(900);
			animatorIvCrycle.setRepeatCount(-1);
		}
		 
		LinearInterpolator interpolatorIvThumb = new LinearInterpolator();
		animatorIvCrycle.setInterpolator(interpolatorIvThumb);

		
		if (!animatorIvCrycle.isRunning()) {
			animatorIvCrycle.start();
		}
	 
	}
	
	private void stopCrycleAnimation(){
		/*if(center_animation!=null && center_animation.isRunning()){
			center_animation.stop();
			center_cycle_layout.setBackgroundResource(R.drawable.center_max_bg);
		}*/
		if (animatorIvCrycle!=null && animatorIvCrycle.isRunning()) {
			animatorIvCrycle.cancel();
			crycleIv.setBackgroundResource(R.drawable.center_max_bg);
		}
		
	}
	
	public void startRecyle() {
		recyleThread = new RecyleThread();
		recyleThread.start();
		mIsDraw = true;
	}
	
	public void stopRecyle(){
    	mIsDraw = false;
    	center_cycle_layout.setBackgroundResource(R.drawable.center_max_bg);
    }
	
	class RecyleThread extends Thread {
 
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			while (mIsDraw) {
				try {
					// draw
					Thread.sleep(mFrameSpaceTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
 
	
	/*
	 * 中心向左移动的动画
	 */
	private void crycleMoveLeftAnimation(){
		Log.v(TAG, "clear left");
	 
		main_center.animate().setListener(new Animator.AnimatorListener() {
			

			@Override
			public void onAnimationStart(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onAnimationEnd(Animator arg0) {
				// TODO Auto-generated method stub
				mLauncherFocusView.setVisibility(View.VISIBLE);
				layout_one_key_operate.setVisibility(View.VISIBLE);
				
				
				leftArrowAnimation();
				textAplhaAnimation(layout_one_key_operate);
			}
			
			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
		}).translationXBy(-200).setDuration(MOVE_DULATION).alpha(1).start();
	}
	
	/*
	 * textview 渐变动画
	 */
	private void textAplhaAnimation(View view) {

		alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 0, 1);
		alphaAnimator.setInterpolator(new LinearInterpolator());
		alphaAnimator.setDuration(1000);
		alphaAnimator.start();
		alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				// TODO Auto-generated method stub	
				if (tmp==2) {
					oneKeyCleaner.setFocusable(true);
					oneKeyCleaner.requestFocus();
					oneKeyCleaner.setFocusableInTouchMode(true);
					//修改：6479 还原初始代码
//					mLauncherFocusView.setVisibility(View.VISIBLE);
//				}else{
//					mLauncherFocusView.setVisibility(View.VISIBLE);
				}
			}
		});
	}
	
	private void cleanerMoveRightAnimation(){
		Log.v(TAG, "clear move right");
		layout_app_information.setVisibility(View.GONE);
        layout_go_right.setVisibility(View.GONE);
		
		main_center.animate().setListener(new Animator.AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator arg0) {
				// TODO Auto-generated method stub
				/*修改：6479进入清理管家，焦点刚落在一键清理上，快速按左键时无作用不能进入查看详情*/
				mLauncherFocusView.setVisibility(View.VISIBLE);
				layout_one_key_operate.setVisibility(View.VISIBLE);
				layout_one_key_operate.requestFocus();
				textAplhaAnimation(layout_one_key_operate);
				
				leftArrowAnimation();
			 
			}
			
			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
		}).translationXBy(-MOVE_X).setDuration(MOVE_DULATION).alpha(1).start();
	 
	}
	
	
	private void showAppInfomation(){
		
		Utils.print(TAG, "showAppInfomation");
		layout_one_key_operate.setVisibility(View.GONE);
		layout_app_information.setVisibility(View.VISIBLE);
		mLauncherFocusView.setVisibility(View.GONE);
		
		layout_go_left.setVisibility(View.GONE);
	 
        main_center.animate().setListener(new Animator.AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator arg0) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onAnimationEnd(Animator arg0) {
				// TODO Auto-generated method stub
                showLeftAppInfoUI();
                rightArrowAnimation();
			}
			
			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub
			}
		}).translationXBy(MOVE_X).setDuration(MOVE_DULATION).alpha(1).start();
     
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_MENU){
//			centerMoveLeftAnimation();
//			oneKeyClear();
//			 scan_size.getAnimator().cancel();
//			updateOK();

			
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
			//&& layout_one_key_operate.getAlpha() == 1
			if(layout_go_left.getVisibility()==View.VISIBLE){
				layout_go_left.setVisibility(View.VISIBLE);
				layout_go_right.setVisibility(View.GONE);
				//当扫描到的垃圾大小为0就不允许向左移动UI
				if(totalScanSize>0){
					showAppInfomation();
				    cloudsMoveRight();
				}
			}
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && (layout_app_information.getVisibility()==View.VISIBLE)){
			if(layout_go_right.getVisibility()==View.VISIBLE){
				tmp = 1;
				oneKeyCleaner.setFocusable(true);
				oneKeyCleaner.requestFocus();
				oneKeyCleaner.setFocusableInTouchMode(true);
//				mLauncherFocusView.setVisibility(View.VISIBLE);
				cleanerMoveRightAnimation();
				cloudsMoveLeft();
			}
		}else if(keyCode==KeyEvent.KEYCODE_BACK){
			Utils.print(TAG, "back>>>>>>>>>>>>>");
		}
		return super.onKeyDown(keyCode, event);
	}
	
	/*
	 * 1.扫描系统app可清除数据
	 * 2.扫描第三方app可清除数据
	 * 3.扫描内部存储空间可清除数据，排查Android目录
	 * 4.扫描data/tmp目录
	 */
	public void scanAllPackageInfo(){
		packageInfo = new PackageInfo() {

			@Override
			public void scanFinished() {
				// TODO Auto-generated method stub
				//show UI
				Utils.print(TAG, "scan finished");
				if(OP_STATUS!=SCAN_END){
					mHandler.removeMessages(SCAN_FINISH);
					mHandler.sendEmptyMessage(SCAN_FINISH);
				}
			}

			@Override
			public void getAppSize(Long size) {
				// TODO Auto-generated method stub
				Utils.print(TAG, "op===="+size);
				if(OP_STATUS==SCANING){
					totalScanSize = totalScanSize+size;
				}else if(OP_STATUS==CLEAR_ING){
					totalScanSize = totalScanSize-size;
				}
				Utils.print(TAG, "total size=="+totalScanSize);
				mHandler.removeMessages(UPDATE_SCAN_DATA_SIZE);
				mHandler.sendEmptyMessage(UPDATE_SCAN_DATA_SIZE);
			}

			@Override
			public void getSystemAppTopInfo(AppInfo[] infos) {
				// TODO Auto-generated method stub
				AppInfo[] systemApp = infos;
				Utils.print(TAG, "system size=="+systemApp.length);
				for(int i=0;i<systemApp.length;i++){
					systemAppInfos.add(systemApp[i]);
				}
/*				for(int i=0;i<systemApp.length;i++){
					Utils.print(TAG, "system app=="+systemApp[i].getPackname());
				}*/
				mHandler.sendEmptyMessageDelayed(SCAN_THRID_APP_PACKAGE_INFO, 1500);
			}

			@Override
			public void getThridAppTopInfo(AppInfo[] infos) {
				// TODO Auto-generated method stub
				AppInfo[] thridApp = infos;
				Utils.print(TAG, "thrid size=="+thridApp.length);
				for(int i=0;i<thridApp.length;i++){
					thridAppInfos.add(thridApp[i]);
				}
/*				for(int i=0;i<thridApp.length;i++){
					Utils.print(TAG, "thrid app=="+thridApp[i].getPackname());
				}*/
				clearDataManager.scanFile();
			}

			@Override
			public void getSystemAppAllSize(Long size,int count) {
				// TODO Auto-generated method stub
				Utils.print(TAG, "getSystemAppAllSize="+size);
				systemAppSize = size;
				systemAppCount = count;
			}

			@Override
			public void getThridAppAllSize(Long size,int count) {
				// TODO Auto-generated method stub
				Utils.print(TAG, "getThridAppAllSize="+size);
				thridAppSize = size;
				thridAppCount = count;
			}
		};
		
		
		
		clearDataPackageInfo = new ClearDataPackageInfo() {
			
			@Override
			public void clearUserData(String packageName, Long size, boolean sucess) {
				// TODO Auto-generated method stub
				try {
					Utils.print(TAG, "clearAllApp size===="+size);
					clearAllAppSize++;
					if(size!=null && size>=0){
						totalScanSize = totalScanSize - size;
						if(totalScanSize<0)
							totalScanSize = 0l;   //因为存在扫描过后app在后台产生比扫描更多的数据，出现的负数
						mHandler.removeMessages(UPDATE_SCAN_DATA_SIZE);
						mHandler.sendEmptyMessage(UPDATE_SCAN_DATA_SIZE);
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				
				Utils.print(TAG, "clearAllAppSize totalScanSize=="+totalScanSize+",clearAllAppSize=="+clearAllAppSize);
				Utils.print(TAG, "clearDataManager.getAllAppSize=="+clearDataManager.getAllAppSize());
				if(clearAllAppSize==clearDataManager.getAllAppSize()){
					if(OP_STATUS!=CLEAR_END){
						mHandler.removeMessages(CLEAR_FINISHED);
						mHandler.sendEmptyMessageDelayed(CLEAR_FINISHED,2000);
					}
				}
			}
		};
		
		clearDataManager = new ClearDataManager(this, packageInfo,clearDataPackageInfo);
		//先获取系统app的，然后获取第三方app的
		systemAppInfos.clear();
		thridAppInfos.clear();
		totalScanSize = 0l;
		Utils.print(TAG, "scan start....");
		OP_STATUS = SCANING;
		clearDataManager.getSystemAppPackageSizeInfo();
		startCenterNumberAnimation();
 
		
	}
	
	/**
	 * 加载向左，显示列表数据UI
	 */
	public void showLeftAppInfoUI(){
		scrollview.fullScroll(ScrollView.FOCUS_UP);
		left_app_contain.removeAllViews();
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(      
                LinearLayout.LayoutParams.MATCH_PARENT,      
                getResources().getDimensionPixelSize(R.dimen.app_item_head_height));
		
		View view = LayoutInflater.from(this).inflate(R.layout.app_item_head, null);
		TextView name2 = (TextView)view.findViewById(R.id.name2);
		TextView scan_result = (TextView)view.findViewById(R.id.scan_result);
		name2.setText(getResources().getString(R.string.system_app));
		scan_result.setText(String.format(getResources()
				.getString(R.string.scaned),Utils.getSizeStr(this, systemAppSize)));
		layoutParams.topMargin = getResources().getDimensionPixelSize(R.dimen.system_head_marigntop_height);
		
		left_app_contain.addView(view,layoutParams);
		Utils.print(TAG, "sys len=="+systemAppInfos.size());
		
		//第一行的横线
		view = LayoutInflater.from(this).inflate(R.layout.split_item, null);
		layoutParams = new LinearLayout.LayoutParams(      
                LinearLayout.LayoutParams.MATCH_PARENT,      
                getResources().getDimensionPixelSize(R.dimen.app_item_div_height));
		layoutParams.topMargin = getResources().getDimensionPixelSize(R.dimen.split_item_top_marign);
		left_app_contain.addView(view,layoutParams);
		
		
		for(int i=0;i<systemAppInfos.size();i++){
			view = LayoutInflater.from(this).inflate(R.layout.app_item, null);
			TextView name = (TextView)view.findViewById(R.id.name);
			TextView size = (TextView)view.findViewById(R.id.size);
		    name.setText(RunningAppInfo.getAppInfoParam(this).getAppName(this, systemAppInfos.get(i).getPackname()));
		    size.setText(Utils.getSizeStr(this,systemAppInfos.get(i).getCacheSize()));
		    
		    layoutParams = new LinearLayout.LayoutParams(      
	                LinearLayout.LayoutParams.MATCH_PARENT,      
	                getResources().getDimensionPixelSize(R.dimen.app_item_height));
			left_app_contain.addView(view,layoutParams);
			
			//add 横线
			view = LayoutInflater.from(this).inflate(R.layout.split_item, null);
			layoutParams = new LinearLayout.LayoutParams(      
	                LinearLayout.LayoutParams.MATCH_PARENT,      
	                getResources().getDimensionPixelSize(R.dimen.app_item_div_height));
			left_app_contain.addView(view,layoutParams);
		}
		if(systemAppCount>10){    //......
			view = LayoutInflater.from(this).inflate(R.layout.app_item, null);
			TextView name = (TextView)view.findViewById(R.id.name);
			TextView size = (TextView)view.findViewById(R.id.size);
			name.setText("......");
			size.setText("");
			layoutParams = new LinearLayout.LayoutParams(      
	                LinearLayout.LayoutParams.MATCH_PARENT,      
	                getResources().getDimensionPixelSize(R.dimen.app_item_height));
			left_app_contain.addView(view,layoutParams);
			
			//add 横线
			view = LayoutInflater.from(this).inflate(R.layout.split_item, null);
			layoutParams = new LinearLayout.LayoutParams(      
	                LinearLayout.LayoutParams.MATCH_PARENT,      
	                getResources().getDimensionPixelSize(R.dimen.app_item_div_height));
			left_app_contain.addView(view,layoutParams);
		}
		
		
		view = LayoutInflater.from(this).inflate(R.layout.app_item_head, null);
		name2 = (TextView)view.findViewById(R.id.name2);
		scan_result = (TextView)view.findViewById(R.id.scan_result);
		name2.setText(getResources().getString(R.string.thrid_app));
		scan_result.setText(String.format(getResources()
				.getString(R.string.scaned),Utils.getSizeStr(this, thridAppSize)));
		layoutParams = new LinearLayout.LayoutParams(      
                LinearLayout.LayoutParams.MATCH_PARENT,      
                getResources().getDimensionPixelSize(R.dimen.app_item_head_height));
		layoutParams.topMargin = getResources().getDimensionPixelSize(R.dimen.install_head_marigntop_height);
		
		left_app_contain.addView(view,layoutParams);
		Utils.print(TAG, "thrid len=="+thridAppInfos.size());
		
		//第一行的横线
		view = LayoutInflater.from(this).inflate(R.layout.split_item, null);
		layoutParams = new LinearLayout.LayoutParams(      
                LinearLayout.LayoutParams.MATCH_PARENT,      
                getResources().getDimensionPixelSize(R.dimen.app_item_div_height));
	    layoutParams.topMargin = getResources().getDimensionPixelSize(R.dimen.split_item_top_marign);
		left_app_contain.addView(view,layoutParams);
		
		
		for(int i=0;i<thridAppInfos.size();i++){
			view = LayoutInflater.from(this).inflate(R.layout.app_item, null);
			TextView name = (TextView)view.findViewById(R.id.name);
			TextView size = (TextView)view.findViewById(R.id.size);
		    name.setText(RunningAppInfo.getAppInfoParam(this).getAppName(this, thridAppInfos.get(i).getPackname()));
		    size.setText(Utils.getSizeStr(this,thridAppInfos.get(i).getCacheSize()));
		    layoutParams = new LinearLayout.LayoutParams(      
	                LinearLayout.LayoutParams.MATCH_PARENT,      
	                getResources().getDimensionPixelSize(R.dimen.app_item_height));
			left_app_contain.addView(view,layoutParams);
			
			//add 横线
			view = LayoutInflater.from(this).inflate(R.layout.split_item, null);
			layoutParams = new LinearLayout.LayoutParams(      
	                LinearLayout.LayoutParams.MATCH_PARENT,      
	                getResources().getDimensionPixelSize(R.dimen.app_item_div_height));
			left_app_contain.addView(view,layoutParams);
		}
		if(thridAppCount>10){    //......
			view = LayoutInflater.from(this).inflate(R.layout.app_item, null);
			TextView name = (TextView)view.findViewById(R.id.name);
			TextView size = (TextView)view.findViewById(R.id.size);
			name.setText("......");
			size.setText("");
			layoutParams = new LinearLayout.LayoutParams(      
	                LinearLayout.LayoutParams.MATCH_PARENT,      
	                getResources().getDimensionPixelSize(R.dimen.app_item_height));
			left_app_contain.addView(view,layoutParams);
			
			//add 横线
			view = LayoutInflater.from(this).inflate(R.layout.split_item, null);
			layoutParams = new LinearLayout.LayoutParams(      
	                LinearLayout.LayoutParams.MATCH_PARENT,      
	                getResources().getDimensionPixelSize(R.dimen.app_item_div_height));
			left_app_contain.addView(view,layoutParams);
		}
		
		
		left_app_contain.setFocusable(true);
		left_app_contain.requestFocus();
	}
	
	
	View.OnClickListener onClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			switch (arg0.getId()) {
			case R.id.one_key_clear:
				if(!oneKeyCleaner.getText().equals(getResources().getString(R.string.clear_finished))){
					oneKeyClear();
				}
				break;
			case R.id.manual_clear:
				Intent intent = new Intent(ClearManager.this,ManualClearManager.class);
				startActivity(intent);
				break;
			default:
				break;
			}
		}
	};
	
	View.OnFocusChangeListener oneKeyClearFocusChangeListener = new View.OnFocusChangeListener() {

		@Override
		public void onFocusChange(View view, boolean arg1) {
			// TODO Auto-generated method stub
			if (arg1) {
				mItemFocusView = view;
				if (mIsFirstIn) {
					mIsFirstIn = false;
					Utils.print(TAG, "focus view init=="+mItemFocusView.getId());
					mLauncherFocusView.setVisibility(View.VISIBLE);
					mLauncherFocusView.initFocusView(mItemFocusView, false, 0f);
					
				} else {
					mLauncherFocusView.moveTo(mItemFocusView);
				}
			}
			
			switch (view.getId()) {
			case R.id.one_key_clear:
			case R.id.manual_clear:
				if(arg1)
					((Button)view).setTextColor(getResources().getColor(R.color.main_button_selected));
				else {
					((Button)view).setTextColor(getResources().getColor(R.color.main_button_unselected));
				}
				break;
			default:
				break;
			}
		}
	};

	/*
	 * 动态修改单位的位置
	 */
	private void adjustUnitLayout(boolean unitChange){
		String tempSize = mSize;
		
		if(tempSize.length()<0)
			scan_size_unit.setVisibility(View.GONE);
		else {
			scan_size_unit.setVisibility(View.VISIBLE);
		}

	}
	
	
	/**
	 * 动态更新扫描数字
	 */
	private void updateScanSizeUI(){
		try {
			//update scan ui
			Long tempTotalScanSize=totalScanSize;  //total scan size 
			if(tempTotalScanSize<0){
				return;
			}
			String scanSize = Utils.getSizeStr(ClearManager.this, tempTotalScanSize);
			scanSize = scanSize.replaceAll(" ", "");
			Utils.print(TAG, "scanSize=="+scanSize+",tempTotalScanSize=="+tempTotalScanSize);
			
			if(tempTotalScanSize<=900){
				mSize = scanSize.substring(0, scanSize.length()-1);
			}else {
				mSize = scanSize.substring(0, scanSize.length()-2);
			}
			
			Utils.print(TAG, "scanSize=="+scanSize);
			//区别100B,100KB,不同单位长度
			if(tempTotalScanSize<=900){
				mUnit = scanSize.substring(scanSize.length()-1, scanSize.length());
			}else {
				mUnit = scanSize.substring(scanSize.length()-2, scanSize.length());
			}
			Utils.print(TAG, "mSize=="+mSize);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * 扫描完成，更新UI
	 */
	private void updateScanFinishedUI(){
		stopCrycleAnimation();
		if(scan_result_tips!=null)
		   scan_result_tips.setText(getResources().getString(R.string.scan_trash));
 
		//当第一次扫描系统垃圾为0的话，直接显示已经是清理完成状态
		if(totalScanSize<=0){
 
			scan_size.setText(getResources().getString(R.string.ok));
	    	scan_size_unit.setVisibility(View.GONE);
	    	scan_result_tips.setText(getResources().getString(R.string.scaning_ok));
	 
	    	layout_go_left.setVisibility(View.GONE);
		}

		crycleMoveLeftAnimation();
		OP_STATUS = SCAN_END;
	}
	
	/**
	 * 清空完成，扫描数字结果更新提示信息
	 */
	private void updateOK(){
 
		scan_size.setText(getResources().getString(R.string.ok));
    	scan_size_unit.setVisibility(View.GONE);
    	scan_result_tips.setText(getResources().getString(R.string.scaning_ok));
    	oneKeyCleaner.setText(getResources().getString(R.string.clear_finished));
    	
    	layout_go_left.setVisibility(View.GONE);
	}
	
	
	/**
	 * 主页左移动的动画
	 */
	private void leftArrowAnimation(){
		
		layout_go_left.setVisibility(View.VISIBLE);
		layout_go_right.setVisibility(View.GONE);
		
		/*arrowDownAnimator = ObjectAnimator.ofFloat(go_left, "translationX", 20, 0);
		arrowDownAnimator.setRepeatCount(Animation.INFINITE);
		arrowDownAnimator.setRepeatMode(Animation.RESTART);

		arrowDownAnimator.setDuration(2000);
		arrowDownAnimator.setStartDelay(2000);
		arrowDownAnimator.setInterpolator(new BounceInterpolator());
		arrowDownAnimator.start();*/
	}
	
	/**
	 * 主页右移动的动画
	 */
    private void rightArrowAnimation(){
		
    	layout_go_left.setVisibility(View.GONE);
		layout_go_right.setVisibility(View.VISIBLE);
 
		
		/*arrowDownAnimator = ObjectAnimator.ofFloat(go_right, "translationX", 0, 20);
		arrowDownAnimator.setRepeatCount(Animation.INFINITE);
		arrowDownAnimator.setRepeatMode(Animation.RESTART);

		arrowDownAnimator.setDuration(2000);
		arrowDownAnimator.setStartDelay(2000);
		arrowDownAnimator.setInterpolator(new BounceInterpolator());
		arrowDownAnimator.start();*/
	}
    
    /*
     * 一键清理
     */
    private void oneKeyClear(){
    	Utils.print(TAG, "1111");
    	if(animatorIvCrycle.isRunning() && (OP_STATUS==SCAN_END))
    		return;
    	OP_STATUS = CLEAR_ING;
    	clearAllAppSize = 0;
    	layout_go_left.setVisibility(View.GONE);
    	startCenterNumberAnimation();
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					// TODO Auto-generated method stub
					Utils.print(TAG, "2222");
			    	mHandler.sendEmptyMessage(START_CRYCLE_ANIMATION);
			    	Thread.sleep(1000);
			    	//清理sdcard
			    	clearDataManager.oneKeySdcardData();
			    	Thread.sleep(1500);
			    	//清理app
			        clearDataManager.oneKeyClearAllAppData();  
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}).start();
 
    }
    
    
    /**
     * 清理完成，更新UI
     */
    private void oneKeyClearFinished(){
    	Utils.print(TAG, "oneKeyClearFinished");
    	stopCrycleAnimation();
    	allow_clear_tips.setVisibility(View.VISIBLE);
    	
    	updateTopSize();
    	OP_STATUS = CLEAR_END;
    	
    }
    
  
    private void animationInit() {
    	layout_go_left.setVisibility(View.VISIBLE);
		arrowDownAnimator = ObjectAnimator.ofFloat(go_left, "translationX", 20, 0);
		arrowDownAnimator.setRepeatCount(Animation.INFINITE);
		arrowDownAnimator.setRepeatMode(Animation.RESTART);

		arrowDownAnimator.setDuration(2000);
		arrowDownAnimator.setStartDelay(2000);
		arrowDownAnimator.setInterpolator(new BounceInterpolator());
		arrowDownAnimator.start();
	}
    
    
    /**
     * 底部云层右移动画
     */
    private void cloudsMoveRight(){
    	
       layout_clouds.animate().setListener(new Animator.AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator arg0) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onAnimationEnd(Animator arg0) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub
			}
		}).translationXBy(MOVE_X).setDuration(MOVE_DULATION).alpha(1).start();
    }
    
    
    /**
     * 底部云层左移动画
     */
    private void cloudsMoveLeft(){
    	
        layout_clouds.animate().setListener(new Animator.AnimatorListener() {
 			
 			@Override
 			public void onAnimationStart(Animator arg0) {
 				// TODO Auto-generated method stub
 			}
 			
 			@Override
 			public void onAnimationRepeat(Animator arg0) {
 				// TODO Auto-generated method stub
 			}
 			
 			@Override
 			public void onAnimationEnd(Animator arg0) {
 				// TODO Auto-generated method stub
 			}
 			
 			@Override
 			public void onAnimationCancel(Animator arg0) {
 				// TODO Auto-generated method stub
 			}
 		}).translationXBy(-MOVE_X).setDuration(MOVE_DULATION).alpha(1).start();
     }
    
    
    /**
     * 根据当前扫描数字的长度，动态计算，数字，单位直接的间距
     * @param from
     * @param to
     */
    private void updateUnit(float from,float to){
    	 
    	//update unit
    	String oldUnit="B";
    	if(oldTotalScanSizeLong>0){
        	oldUnit = Utils.getSizeStr(this, oldTotalScanSizeLong).replace(" ", "");
        	if(oldTotalScanSizeLong<=900){
        		oldUnit = oldUnit.substring(oldUnit.length()-1, oldUnit.length());
        	}else {
        		oldUnit = oldUnit.substring(oldUnit.length()-2, oldUnit.length());
    		}
    	}
    	
    	if(scan_size_unit!=null && to>0){
    		if(oldUnit.equals("B"))
    			oldUnit = " "+oldUnit;
    		scan_size_unit.setText(oldUnit);
    	}
 		  
    	
    	
    	String fromUnit = oldUnit;
    	String toUnit = mUnit;
    	boolean unitChange = false;
//        Utils.print(TAG, "fromunit=="+fromUnit+",toUnit=="+toUnit);
    	if(OP_STATUS==SCANING){
            if((fromUnit.equals("KB") && toUnit.equals("MB"))||
            		(fromUnit.equals("MB") && toUnit.equals("GB"))){
            	unitChange = true;
            }
    	}else if(OP_STATUS==CLEAR_ING){
    		if((fromUnit.equals("MB") && toUnit.equals("KB"))||
            		(fromUnit.equals("GB") && toUnit.equals("MB"))){
            	unitChange = true;
            }
    	}

    	
    	if(unitChange){
        	Message message = new Message();
        	message.what = UPDATE_UNIT;
        	message.obj = toUnit;
        	long div = (long)((to*1024-from)/1000)+500;
        	Utils.print(TAG, "div-----------"+div);
        	mHandler.sendMessageDelayed(message, div);
    	}
 
    }
    
 
    
    /**
     * 开始数字扫描动画
     */
    private void startCenterNumberAnimation(){
  
    	try {
        	String tempSize = scan_size.getText().toString();
        	Utils.print(TAG, "2mszie====="+mSize);
            String tempMSize = mSize;
        	StringBuffer buffer = new StringBuffer();
            for(int i=0;i<tempMSize.length();i++){
            	if(Character.isDigit(tempMSize.charAt(i)) || tempMSize.charAt(i)=='.'){
            		buffer.append(tempMSize.charAt(i));
            	}else {
    				break;
    			}
            }
            Utils.print(TAG, "3mszie====="+buffer.toString());
        	float from = (float)Double.parseDouble(tempSize);
        	float to = (float)Double.parseDouble(buffer.toString());
        	Utils.print(TAG, "from==="+from+",to=="+to);
     
        	updateUnit(from,to);
     
    		scan_size.showNumberWithAnimation(from,to);
    		scan_size.setNumber(to);
            oldTotalScanSizeLong = totalScanSize;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
 
		scan_size.getAnimator().addListener(new Animator.AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator arg0) {
				// TODO Auto-generated method stub
//				Log.v(TAG, "end......................."+scan_size.getText().toString());
				scan_size.getAnimator().cancel();
				if(animatorIvCrycle==null)
					return;
				Utils.print(TAG, "isrunning=="+animatorIvCrycle.isRunning());
				if(animatorIvCrycle.isRunning())
				   startCenterNumberAnimation();
				
				if(OP_STATUS==CLEAR_END){
					if(!animatorIvCrycle.isRunning())
						updateOK();
				}else if(OP_STATUS==SCAN_END){
					if(!animatorIvCrycle.isRunning() && totalScanSize==0){
						updateOK();
					}
				}
			}
			
			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
		});
         
    }
    
    
     
    
    
    private void updateTopSize(){
    	if(rom_size!=null)
    	  rom_size.setText(Utils.getSizeStr(this,Utils.getRomAvailableSize())+"/"+Utils.getRomTotalSize());
    }
    
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	updateTopSize();
    	
    
    }

    /*　　不允许正在扫描的时候，操作其他的
     * (non-Javadoc)
     * @see android.app.Activity#dispatchKeyEvent(android.view.KeyEvent)
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
    	// TODO Auto-generated method stub
    	if(event.getKeyCode()!=KeyEvent.KEYCODE_BACK && animatorIvCrycle!=null && animatorIvCrycle.isRunning()){
    		return true;
    	}
    	return super.dispatchKeyEvent(event);
    }
    
    
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            
            if(OP_STATUS==CLEAR_END){
            	return;
            }
            
            String packageName = intent.getStringExtra("packagename");
            long size = intent.getLongExtra("size", 0);
            Log.v(TAG, "action=="+action+",size=="+size);
            
 
            //thrid app
            //update left arrow list data size
            for(int i=0;i<thridAppInfos.size();i++){
            	if(thridAppInfos.get(i).getPackname().equals(packageName)){
            		thridAppInfos.get(i).setDataSize(0l);
            		break;
            	}
            }
            //sort thrid app size list
            Comparator cmp = new MyComparator();
            Collections.sort(thridAppInfos, cmp);
         
            
            //system app
            for(int i=0;i<systemAppInfos.size();i++){
            	if(systemAppInfos.get(i).getPackname().equals(packageName)){
            		systemAppInfos.get(i).setDataSize(0l);
            		break;
            	}
            }
            //sort system app size list
            Collections.sort(systemAppInfos, cmp);
            
            String unit = scan_size_unit.getText().toString();
            Utils.print(TAG, "unit=="+unit);
            //update main scan size
            totalScanSize = totalScanSize - size;
 
            String scanSize = Utils.getSizeStr(ClearManager.this, totalScanSize);
    		scanSize = scanSize.replaceAll(" ", "");
    		if(totalScanSize<=900){
    			mUnit = scanSize.substring(scanSize.length()-1, scanSize.length());
    		}else {
    			mUnit = scanSize.substring(scanSize.length()-2, scanSize.length());
			}
    		
    		Utils.print(TAG, "scansize==="+scanSize+",mUnit=="+mUnit);
    		if(totalScanSize<=900){
    			mSize = scanSize.substring(0, scanSize.length()-1);
    		}else {
    			mSize = scanSize.substring(0, scanSize.length()-2);
			}
    		if(!scanSize.contains("."))
    			mSize = mSize+".00";
    		Utils.print(TAG, "mSize==="+mSize);
    		if(scan_size!=null)
    		   scan_size.setText(mSize);
     
    		if(scan_size_unit!=null){
    			if(mUnit.equals("B"))
    				mUnit = " " +mUnit;
    			scan_size_unit.setText(mUnit);
    		}
 
            //检测app是否被卸载,以便更新详情列表
            if(!Utils.checkPackageIsExist(context, packageName)){
            	for(int i=0;i<thridAppInfos.size();i++){
            		if(thridAppInfos.get(i).getPackname().equals(packageName)){
            			thridAppInfos.remove(i);
            			break;
            		}
            	}
            }
    		
    		adjustUnitLayout(unit.equals(mUnit)?false:true);
        }
    };
    
    /*
     * 接收手动清理缓存，然后更新总的扫描结果
     */
    private void regeditReceiver() {
		IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ACTION_UPDATE_SCAN_RESULT);
        registerReceiver(mReceiver, mFilter);
	}
    
    private void unregeditReceiver(){
    	unregisterReceiver(mReceiver);
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	tmp = 0;
    	
    	try {
        	Utils.print(TAG, "onDestory1");
        	if(animatorIvCrycle!=null && animatorIvCrycle.isRunning())
        		animatorIvCrycle.cancel();
      
        	animatorIvCrycle=null;
        	clearDataManager=null;
        	packageInfo = null;
        	
        	if(arrowDownAnimator!=null){
            	arrowDownAnimator.cancel();
            	arrowDownAnimator=null;
        	}
        	
        	scan_size.getAnimator().cancel();

        	if(alphaAnimator!=null){
            	alphaAnimator.cancel();
            	alphaAnimator=null;
        	}

        	if(objectNumberAnimator!=null){
            	objectNumberAnimator.cancel();
            	objectNumberAnimator=null;
        	}
        	
        	layout_clouds.animate().cancel();
		} catch (Exception e) {
			// TODO: handle exception
		}
    	unregeditReceiver();
  
    }
    
  
}
