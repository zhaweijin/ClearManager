package com.hiveview.clear.manager.widget;

 
import com.hiveview.clear.manager.R;
import com.hiveview.clear.manager.Utils;

import android.content.Context;  
import android.graphics.Canvas;  
import android.graphics.Color;  
import android.graphics.Paint;  
import android.util.AttributeSet;  
import android.view.View;  
  
/** 
 * 雪花视图, DELAY时间重绘, 绘制NUM_SNOWFLAKES个雪花 
 * <p/> 
 * Created by wangchenlong on 16/1/24. 
 */  
public class SnowView extends View {  
  
	
    private static final int NUM_SNOWFLAKES = 25; // 雪花数量  
    private static final int DELAY = 5; // 延迟  
    private SnowFlake[] mSnowFlakes; // 雪花  
    private Context mContext;
  
    public SnowView(Context context) {  
        super(context);  
        mContext = context;
    }  
  
    public SnowView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        mContext = context;
    }  
  
    public SnowView(Context context, AttributeSet attrs, int defStyleAttr) {  
        super(context, attrs, defStyleAttr);  
        mContext = context;
    }  
 
  
    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {  
        super.onSizeChanged(w, h, oldw, oldh);  
        if (w != oldw || h != oldh) {  
            initSnow(w, h);  
        }  
    }  
  
    private void initSnow(int width, int height) {  
    	Utils.print(SnowFlake.TAG, "initSnow()");
        setLayerType(View.LAYER_TYPE_SOFTWARE, null); 
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG); // 抗锯齿  
        paint.setAntiAlias(true);
        paint.setColor(mContext.getResources().getColor(R.color.snow)); // 白色雪花  
        paint.setStyle(Paint.Style.FILL); // 填充;  
        mSnowFlakes = new SnowFlake[NUM_SNOWFLAKES];  
        for (int i = 0; i < NUM_SNOWFLAKES; ++i) {  
            mSnowFlakes[i] = SnowFlake.create(mContext,width, height, paint);  
        }  
        
    }  
  
    @Override protected void onDraw(Canvas canvas) {  
        super.onDraw(canvas);  
        for (SnowFlake s : mSnowFlakes) {  
            s.draw(canvas);  
        }  
        // 隔一段时间重绘一次, 动画效果  
        getHandler().postDelayed(runnable, DELAY);  
    }  
  
    // 重绘线程  
    private Runnable runnable = new Runnable() {  
        @Override  
        public void run() {  
            invalidate();  
        }  
    };  
}  
