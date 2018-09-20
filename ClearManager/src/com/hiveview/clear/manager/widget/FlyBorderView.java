package com.hiveview.clear.manager.widget;

import com.hiveview.clear.manager.R;
import com.hiveview.clear.manager.Utils;
import com.hiveview.clear.manager.widget.DensityUtil;
import com.hiveview.clear.manager.widget.LauncherFocusView.FocusViewAnimatorEndListener;
import com.hiveview.clear.manager.widget.LauncherFocusView.FocusViewMoveListener;
  
import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;  
import android.graphics.Rect;  
import android.util.AttributeSet;  
import android.view.View;  
import android.view.ViewGroup;  
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;  
  
public class FlyBorderView extends View  
{  
  
    private View mFocusView;  
    private View mSelectView;  
    private boolean isTvScreen = false;
	private boolean isInitPositionOnScreen = false;
	private String TAG = "FlyBorderView"; 
	private int[] location = new int[2];
	private int xyOffset = 0;
	private int outXOffset = 0;
	private int wOffset = -6;
	private int outYOffset = 0;
	private int hOffset = -10;
	private ViewWrapper viewWrapper = null;
	private View currentFocusView = null;
	PropertyValuesHolder pvhHeight = null;
	PropertyValuesHolder pvhWidth = null;
	ViewPropertyAnimator vpaXY = null;
	private AccelerateDecelerateInterpolator interpolator = null;
	private FocusViewAnimatorEndListener animatorEndListener = null;
	private final int duration = 200;
  
    public FlyBorderView(Context context)  
    {  
        super(context);  
        init(context);  
    }  
  
    public FlyBorderView(Context context, AttributeSet attrs)  
    {  
        super(context, attrs);  
        init(context);  
    }  
  
    public FlyBorderView(Context context, AttributeSet attrs, int defStyleAttr)  
    {  
        super(context, attrs, defStyleAttr);  
        init(context);  
    }  
  
    private void init(Context context)  
    {  
    	setBackgroundResource(R.drawable.main_button_selected);
		setAlpha(0f);
		setFocusable(false);
    	viewWrapper = new ViewWrapper(this);
    	interpolator = new AccelerateDecelerateInterpolator();
    }  
  
    public boolean isTvScreen()  
    {  
        return isTvScreen;  
    }  
  
    public void setTvScreen(boolean isTvScreen)  
    {  
        this.isTvScreen = isTvScreen;  
        invalidate();  
    }  
  
    /** 
     * 设置焦点框的移动. 
     */  
    public void setFocusView(View view, float scale)  
    {  
        if (mFocusView != view)  
        {  
            mFocusView = view;  
            runTranslateAnimation(mFocusView, scale, scale);  
        }  
    }  
  
    public void setSelectView(View view)  
    {  
        if (mSelectView != view)  
        {  
            mSelectView = view;  
  
            runTranslateAnimation(mSelectView);  
        }  
    }  
  
    private void runTranslateAnimation(View toView)  
    {  
        Rect fromRect = findLocationWithView(this);  
        Rect toRect = findLocationWithView(toView);  
        int x = toRect.left - fromRect.left;  
        int y = toRect.top - fromRect.top;  
  
        int deltaX = (toView.getWidth() - this.getWidth()) / 2;  
        int deltaY = (toView.getHeight() - this.getHeight()) / 2;  
        // tv  
        if (isTvScreen)  
        {  
            x = DensityUtil.dip2px(this.getContext(), x + deltaX);  
            y = DensityUtil.dip2px(this.getContext(), y + deltaY);  
        }  
        else  
        {  
            x = x + deltaX;  
            y = y + deltaY;  
        }  
        flyWhiteBorder(x, y);  
  
    }  
  
    private void flyWhiteBorder(float x, float y)  
    {  
  
        animate().translationX(x).translationY(y)
        .setDuration(duration)
        .setInterpolator(new DecelerateInterpolator())
        .start();  
  
    }  
  
    public void runTranslateAnimation(View toView, float scaleX, float scaleY)  
    {  
        Rect fromRect = findLocationWithView(this);  
        Rect toRect = findLocationWithView(toView);  
  
        int x = toRect.left - fromRect.left;  
        int y = toRect.top - fromRect.top;  
  
        int deltaX = (toView.getWidth() - this.getWidth()) / 2;  
        int deltaY = (toView.getHeight() - this.getHeight()) / 2;  
        // tv  
        if (isTvScreen)  
        {  
            x = DensityUtil.dip2px(this.getContext(), x + deltaX);  
            y = DensityUtil.dip2px(this.getContext(), y + deltaY);  
        }  
        else  
        {  
            x = x + deltaX;  
            y = y + deltaY;  
        }  
        float toWidth = toView.getWidth() * scaleX;  
        float toHeight = toView.getHeight() * scaleY;  
        int width = (int) (toWidth);  
        int height = (int) (toHeight);  
  
        flyWhiteBorder(width, height, x, y);  
    }  
  
    private void flyWhiteBorder(int width, int height, float x, float y)  
    {  
        int mWidth = this.getWidth();  
        int mHeight = this.getHeight();  
  
        float scaleX = (float) width / (float) mWidth;  
        float scaleY = (float) height / (float) mHeight;  
  
        animate().translationX(x).translationY(y)
        .setDuration(duration)
        .scaleX(scaleX).scaleY(scaleY)
        .setInterpolator(new DecelerateInterpolator())
        .start();  
    }  
  
    public Rect findLocationWithView(View view)  
    {  
        ViewGroup root = (ViewGroup) this.getParent();  
        Rect rect = new Rect();  
        root.offsetDescendantRectToMyCoords(view, rect);  
        return rect;  
    }

    
    
	public void setBg(int buttonSelected) {
		// TODO Auto-generated method stub
		setBackgroundResource(buttonSelected);
	}

	public void initFocusView(View firstCallRequestFocusOfView, boolean isScale, float scale) {
		// TODO Auto-generated method stub
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
	
		vpaXY = animate().x(x).y(y)
				.setDuration(duration)
				.setListener(new FocusViewMoveListener())
				.setInterpolator(interpolator);

		// 初始化完成
		isInitPositionOnScreen = true;
	}
	/**
	 * 
	 * @author Administrator
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
	
	
}  