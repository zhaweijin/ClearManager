package com.hiveview.clear.manager;

import java.util.Locale;

import com.hiveview.clear.manager.widget.AlertDialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ClearWaring extends Activity{

	private Button again_clear;
	private Button recovery;
	
	private Button clear;
	private TextView message;
	
	private Button close;
	
	private Context mContext;
	private boolean ok_finish = false;
	private boolean isFromOwer = true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mContext = this;
        isFromOwer = getIntent().getBooleanExtra("isFromOwer", false);
        isFromOwer=true;
    	initWarningClear();
         
	}
 
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode==KeyEvent.KEYCODE_BACK){
			if(!isFromOwer){//ota 界面
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
 
	
	private void initWarningClear(){
		setContentView(R.layout.storage_alert_dialog);
		
        again_clear = (Button)findViewById(R.id.again_clear);
		recovery = (Button)findViewById(R.id.recovery);
		again_clear.setOnFocusChangeListener(onFocusChangeListener);
		recovery.setOnFocusChangeListener(onFocusChangeListener);
		
		again_clear.setFocusable(true);
		again_clear.requestFocus();
		again_clear.setFocusableInTouchMode(true);
		
		
		TextView message = (TextView)findViewById(R.id.message);
		TextView message2 = (TextView)findViewById(R.id.message2);
		TextView message_recovery = (TextView)findViewById(R.id.message_recovery);
 
		String msg = "";
		if(isFromOwer){
			msg = getResources().getString(R.string.storage_user_experience);
			message2.setVisibility(View.VISIBLE);
		}else {//ota 拉起的，更新提示文字
			msg = getResources().getString(R.string.system_upgrade);
			message2.setVisibility(View.GONE);
		}
		message.setText(String.format(getResources().getString(R.string.storage_waring_message),msg));
		
		if(isZh(this)){
			message_recovery.setText("3、"+getResources().getString(R.string.storage_waring_recovery_tips));
		}else {
			message_recovery.setText("3) "+getResources().getString(R.string.storage_waring_recovery_tips));
		}
		
		again_clear.setOnClickListener(onClickListener);
		recovery.setOnClickListener(onClickListener);
	}
	
	
    View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View arg0, boolean arg1) {
			// TODO Auto-generated method stub
			if(arg1){
				((Button)arg0).setTextColor(Color.WHITE);
			}else {
				((Button)arg0).setTextColor(mContext.getResources().getColor(R.color.alert_dialog_button_unselected_color));
			}
		}
	};
	
	
	View.OnClickListener onClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			switch (arg0.getId()) {
			case R.id.clear:
			case R.id.again_clear:
				Intent intent = new Intent(ClearWaring.this,ClearManager.class);
				startActivity(intent);
				finish();
				break;
			case R.id.recovery:
				initAlertDialog();
				break;
			default:
				break;
			}
		}
	};
	
	 
	/*
	 * 确定对话框提示
	 */
	private void initAlertDialog(){
		final AlertDialog alertDialog = new AlertDialog(this);
 
		Window dialogWindow = alertDialog.getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.width = getResources().getDimensionPixelSize(R.dimen.alert_dialog_width);
		lp.height = getResources().getDimensionPixelSize(R.dimen.alert_dialog_height);
		dialogWindow.setAttributes(lp);
		alertDialog.show();
		
		TextView message = (TextView) alertDialog.findViewById(R.id.message);
//		message.setTextSize(getResources().getDimensionPixelSize(R.dimen.activity_layout_sp_25));//25
		message.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimension(R.dimen.activity_layout_sp_25));//25
		message.setText(getResources().getString(R.string.storage_waring_recovery_tips));
		Button yes = (Button) alertDialog.findViewById(R.id.yes);
		Button no = (Button) alertDialog.findViewById(R.id.no);
		
		no.requestFocus();
		no.setFocusable(true);
		
		yes.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				// TODOAuto-generated method stub
				mContext.sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
				alertDialog.dismiss();
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
	
	
    public boolean isZh(Context context) {  
        Locale locale = context.getResources().getConfiguration().locale;  
        String language = locale.getLanguage();  
        if (language.endsWith("zh"))  
            return true;  
        else  
            return false;  
    }  
}
