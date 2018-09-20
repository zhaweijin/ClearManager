package com.hiveview.clear.manager.widget;

 
import com.hiveview.clear.manager.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;


public class AlertDialog extends Dialog {
	private Context mContext;
	private LayoutInflater mFactory = null;
	private View mView = null;
	
	private Button yes;
	private Button no;

	public AlertDialog(Context context) {
		super(context, R.style.CustomProgressNewDialog);
		mContext = context;
		mFactory = LayoutInflater.from(mContext);
		mView = mFactory.inflate(R.layout.alert_dialog, null);
		
		yes = (Button)mView.findViewById(R.id.yes);
		no = (Button)mView.findViewById(R.id.no);
		yes.setOnFocusChangeListener(onFocusChangeListener);
		no.setOnFocusChangeListener(onFocusChangeListener);
		

		final WindowManager.LayoutParams WMLP = this.getWindow().getAttributes();
		WMLP.gravity = Gravity.CENTER;
		this.getWindow().setAttributes(WMLP);

		this.setContentView(mView);
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
}