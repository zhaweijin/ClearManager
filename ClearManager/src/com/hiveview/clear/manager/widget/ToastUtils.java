package com.hiveview.clear.manager.widget;



import com.hiveview.clear.manager.R;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
 

public class ToastUtils {

	private static Toast mToast = null;
	/***
	 * 自定义toast的样式
	 */
	public static void showToast(Context context, String text,int duration) {
		if (null == mToast) {
			mToast = new Toast(context);
		}
		View v = LayoutInflater.from(context).inflate(R.layout.mytoast, null);
        TextView textView = (TextView) v.findViewById(R.id.text);
        textView.setText(text);
        mToast.setView(v);
        mToast.setDuration(duration);
        mToast.setGravity(Gravity.TOP, 0, 0);
		mToast.show();
	}
  
}
