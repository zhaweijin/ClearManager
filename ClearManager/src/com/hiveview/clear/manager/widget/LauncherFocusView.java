package com.hiveview.clear.manager.widget;

 
import com.hiveview.clear.manager.R;
import com.hiveview.clear.manager.Utils;

import android.R.integer;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;
 

/**
 * 
 * @ClassName: FocusView
 * @Description: TODO
 * @author: 陈丽晓
 * @date 2014-8-27 下午9:05:49
 * 
 */
@SuppressLint("NewApi")
public class LauncherFocusView extends RelativeLayout {
	/**
	 * 焦点动画移动的时间
	 */
	private final int duration = 200;

	/**
	 * 属性动画使用的包装层，方便操作宽度和高度的变化
	 */
	private ViewWrapper viewWrapper = null;

	/**
	 * View的x和y的坐标的偏移量,考虑的是焦点图片的光圈
	 */
	public int xyOffset = 0;

	/**
	 * View的width和height的坐标的偏移量,考虑的是焦点图片的光圈
	 */
	private int wOffset = -6;

	private int hOffset = -10;

	/**
	 * 外部调用根据特殊的需要，设置y方向额外的偏移量
	 */
	private int outYOffset = 0;

	/**
	 * 外部调用根据特殊的需要，设置x方向额外的偏移量
	 */
	private int outXOffset = 0;

	/**
	 * 焦点框View的宽度变化的属性值对象
	 */
	PropertyValuesHolder pvhWidth = null;

	/**
	 * 焦点框View的高度变化的属性值对象
	 */
	PropertyValuesHolder pvhHeight = null;

	/**
	 * 焦点框View多属性动画变化的动画对象
	 */
	ObjectAnimator whAnimator = null;

	/**
	 * 高性能的View多属性动画对象
	 */
	ViewPropertyAnimator vpaXY = null;
	
 
	public Context mContext;

	/**
	 * 先快后慢的动画差值器
	 */
	private AccelerateDecelerateInterpolator interpolator = null;

	private boolean isInitPositionOnScreen = false;

	/**
	 * 获取View在屏幕中坐标的数组
	 */
	private int[] location = new int[2];

	private View currentFocusView = null;

	private FocusViewAnimatorEndListener animatorEndListener = null;

	private int oldX = 0;
	private int oldY = 0;
	

	// hyl add
	private final String TAG = "FocusClear";

	public LauncherFocusView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();
	}

	public LauncherFocusView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public LauncherFocusView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	private void init() {
		setBackgroundResource(R.drawable.main_button_selected);
		setAlpha(0f);
		setFocusable(false);
		viewWrapper = new ViewWrapper(this);
		interpolator = new AccelerateDecelerateInterpolator();
		 
	}
	
	
	
	
	public void setBg(int id){
		setBackgroundResource(id);
	}

	/**
	 * 设置焦点图片，必须是.9格式的图片
	 * 
	 * @Title: FocusView
	 * @author:陈丽晓
	 * @Description: TODO
	 * @param resid
	 */
	public void setFocusBackgroundResource(int resid) {
		setBackgroundResource(resid);
	}

	/**
	 * 在Activity的onWindowFocusChanged方法中调用此方法
	 * 
	 * @Title: FocusView
	 * @author:陈丽晓
	 * @Description: TODO
	 * @param firstCallRequestFocusOfView
	 *            在某个Activity中指定第一个要获取焦点的View
	 */
	public void initFocusView(View firstCallRequestFocusOfView, boolean isScale, float scale) {

		if(firstCallRequestFocusOfView==null)
			return;
		firstCallRequestFocusOfView.requestFocus();

		if (isInitPositionOnScreen) {// 保证初始化一次
			return;
		}
        Utils.print(TAG, "init");
		// 计算目标焦点View的坐标
		firstCallRequestFocusOfView.getLocationOnScreen(location);
		int targetX = location[0];
		int targetY = location[1];
		
		Utils.print(TAG, "x=="+targetX+",y=="+targetY);

		// 设置焦点View的在屏幕中的坐标
		int x = targetX - xyOffset - outXOffset-(wOffset==0?0:wOffset/2);
		int y = targetY - xyOffset - outYOffset-(hOffset==0?0:hOffset/2);
		setX(x);
		setY(y);	
 
		Utils.print(TAG,"----------item.width = " +firstCallRequestFocusOfView.getWidth());
		Utils.print(TAG,"----------item.height = " +firstCallRequestFocusOfView.getHeight());
		viewWrapper.setWidth(firstCallRequestFocusOfView.getWidth() + wOffset);
		viewWrapper.setHeight(firstCallRequestFocusOfView.getHeight() + hOffset);

		if (isScale) {// 放大到scale倍数
			setScaleX(scale);
			setScaleY(scale);
		}

		setAlpha(1f);

		currentFocusView = firstCallRequestFocusOfView;

		// 初始化多属性的动画对象，用于改变焦点框View的宽和高
		pvhWidth = PropertyValuesHolder.ofInt("width", firstCallRequestFocusOfView.getWidth()+30);
		pvhHeight = PropertyValuesHolder.ofInt("height", firstCallRequestFocusOfView.getHeight());
	
/*//		Log.v(TAG, "pvhWidth="+pvhWidth+",pvhHeight="+pvhHeight);
		whAnimator = ObjectAnimator.ofPropertyValuesHolder(viewWrapper, pvhWidth, pvhHeight);
		// 设置动画时间和速度变化
		whAnimator.setDuration(duration).setInterpolator(interpolator);*/

		vpaXY = animate().x(x).y(y)
				.setDuration(duration).setListener(new FocusViewMoveListener())
				.setInterpolator(interpolator);

		// 初始化完成
		isInitPositionOnScreen = true;
	}
	 

	/**
	 * 焦点框View动画移动到当前获得焦点的View，调用此方法焦点框View没有放大效果
	 * 
	 * @Title: FocusView
	 * @author:陈丽晓
	 * @Description: TODO
	 * @param tagetView
	 *            目前获得焦点的View
	 */
	public void moveTo(View tagetView) {

		if (!isInitPositionOnScreen) {// 防止Activity中某个View在界面刚刚构建完成时抢焦点，造成的动画效果
			return;
		}

        Utils.print(TAG, "move..........");
		int targetWidth = tagetView.getWidth() + wOffset;
		int targetHeight = tagetView.getHeight() + hOffset;
		
		targetWidth = targetWidth;

		Utils.print(TAG, "#########targetWidth = " + targetWidth);
		Utils.print(TAG, "#########targetHeight = " + targetHeight);

		tagetView.getLocationOnScreen(location);
		int targetX = location[0];
		int targetY = location[1];
		Utils.print(TAG, "x------ " +targetX +",y===="+targetY);
		// 计算焦点框View在屏幕中的坐标
		int x = targetX - xyOffset - outXOffset-(wOffset==0?0:wOffset/2);
		int y = targetY - xyOffset - outYOffset-(hOffset==0?0:hOffset/2);
		
		
		currentFocusView = tagetView;
 
		pvhWidth.setIntValues(viewWrapper.getWidth(), targetWidth);
		pvhHeight.setIntValues(viewWrapper.getHeight(), targetHeight);

//		whAnimator.start();

		vpaXY.x(x).y(y);

		

		oldX = targetX;
		oldY = targetY;
	}
 

	/**
	 * 返回动画的终点位置的x坐标
	 * 
	 * @Title: FocusView
	 * @author:陈丽晓
	 * @Description: TODO
	 * @return
	 */
	public int getOldX() {
		return oldX;
	}

	/**
	 * 返回动画的终点位置的y坐标
	 * 
	 * @Title: FocusView
	 * @author:陈丽晓
	 * @Description: TODO
	 * @return
	 */
	public int getOldY() {
		return oldY;
	}

	/**
	 * 返回View的x和y的坐标的偏移量,考虑的是焦点图片的光圈
	 * 
	 * @Title: FocusView
	 * @author:陈丽晓
	 * @Description: TODO
	 * @return
	 */
	public int getXYOffset() {
		return xyOffset;
	}

	/**
	 * 瞬间移动焦点框View，并以渐变的方式显示出来
	 * 
	 * @Title: FocusView
	 * @author:陈丽晓
	 * @Description: TODO
	 * @param target
	 */
	public void moveToByAlpha(View target) {

		setAlpha(0f);
		// 计算目标焦点View的坐标
		target.getLocationOnScreen(location);
		int targetX = location[0];
		int targetY = location[1];

		// 设置焦点View的在屏幕中的坐标
		setX(targetX - xyOffset - outXOffset);
		setY(targetY - xyOffset - outYOffset);
		// 设置焦点View的宽度和高度
		viewWrapper.setWidth(target.getWidth() + wOffset);
		viewWrapper.setHeight(target.getHeight() + hOffset);
		setAlpha(1f);

		oldX = targetX;
		oldY = targetY;

	}

	/**
	 * 焦点框移动动画监听器
	 * 
	 * @ClassName: FocusViewMoveListener
	 * @Description: TODO
	 * @author:陈丽晓
	 * @date 2014-9-10 下午9:04:02
	 * 
	 */
	class FocusViewMoveListener implements AnimatorListener {

		@Override
		public void onAnimationStart(Animator animation) {
			if (null != animatorEndListener) {
				animatorEndListener.OnAnimateStart(currentFocusView);
			}
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			if (null != animatorEndListener) {
				animatorEndListener.OnAnimateEnd(currentFocusView);
			}
		}

		@Override
		public void onAnimationCancel(Animator animation) {
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
		}

	}

	public int getOutYOffset() {
		return outYOffset;
	}

	public void setOutYOffset(int outYOffset) {
		this.outYOffset = outYOffset;
	}

	public int getOutXOffset() {
		return outXOffset;
	}

	public void setOutXOffset(int outXOffset) {
		this.outXOffset = outXOffset;
	}

	public void animateWidthAndHeight(View target) {
		// 设置变化过的宽度和高度
		pvhWidth.setIntValues(viewWrapper.getWidth(), target.getWidth());
		pvhHeight.setIntValues(viewWrapper.getHeight(), target.getHeight());
		// 启动焦点View坐标和高度宽度变化
		whAnimator.start();
	}

	public void moveToByParams(int x, int y, int width, int height) {
		// 坐标变化
		if (x > 0 && y > 0) {
			animate().x(x).y(y);
		} else if (x > 0) {
			animate().x(x);
		} else if (y > 0) {
			animate().y(y);
		}

		animate().setListener(new AnimatorListenerAdapter() {

		});

		Log.d("DesktopView", "moveToByParams " + x);
		oldX = x;

		// 宽度和高度变化
		if ((width + xyOffset) != getWidth()) {
			pvhWidth.setIntValues(viewWrapper.getWidth(), width + wOffset);
		}

		if ((height + xyOffset) != getWidth()) {
			pvhHeight.setIntValues(viewWrapper.getHeight(), height + hOffset);
		}
		whAnimator.start();
	}

	/**
	 * 焦点框的View移动结束后回到此接口
	 * 
	 * @ClassName: FocusViewAnimatorEndListener
	 * @Description: TODO
	 * @author: 陈丽晓
	 * @date 2014-9-11 上午12:23:28
	 * 
	 */
	public interface FocusViewAnimatorEndListener {
		public void OnAnimateStart(View currentFocusView);

		public void OnAnimateEnd(View currentFocusView);
	}

	public void setAnimatorEndListener(FocusViewAnimatorEndListener animatorEndListener) {
		this.animatorEndListener = animatorEndListener;
	}
}
