package com.hiveview.clear.manager.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

/**
 * 动画显示数字
 * Created by fhp on 15/1/7.
 */
public class CountView extends TextView{
    //动画时长 ms
    int duration = 1000;
    float number;
    public ObjectAnimator objectAnimator;
    public CountView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public void showNumberWithAnimation(float fromNumber,float number) {
        //修改number属性，会调用setNumber方法
        objectAnimator=ObjectAnimator.ofFloat(this,"number",fromNumber,number);
        objectAnimator.setDuration(duration);
        //加速器，从慢到快到再到慢
        objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        objectAnimator.start();
    }

    public float getNumber() {
        return number;
    }

    public void setNumber(float number) {
        this.number = number;
        setText(String.format("%1$04.2f",number));
    }
    
    public ObjectAnimator getAnimator(){
    	return objectAnimator;
    }
}
