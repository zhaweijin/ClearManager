package com.hiveview.clear.manager;

import java.util.ArrayList;
import java.util.logging.Logger;
 
 



import com.hiveview.clear.manager.IconAsyncImageLoader.ImageCallback;
import com.hiveview.clear.manager.widget.LauncherFocusView;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
 

public class ManualAppAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private Context mContext;

	private String TAG = "ManualAppAdapter";

	
	private ArrayList<AppInfo> appInfos;
	private ListView listView;
	private IconAsyncImageLoader iconAsyncImageLoader;

	  
	
	public class ViewHolder {
		public ImageView icon;
		
		public TextView name;
		public TextView appSize;
		public TextView dataSize;
		public TextView versionNamem;
		public TextView versionName;
		public TextView versionCode;
		
		public Button clear;
		public Button uninstall;
		
		public RelativeLayout layout_icon;
 	}

	
	public ManualAppAdapter(Context context, ArrayList<AppInfo> appInfos,ListView listView) {
		super();
		mContext = context;
		mInflater = LayoutInflater.from(context);
		this.appInfos = appInfos;
		this.listView = listView;
		iconAsyncImageLoader = new IconAsyncImageLoader(mContext);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return appInfos.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return appInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.manual_app_item, null);
			holder = new ViewHolder();
			holder.name = (TextView)convertView.findViewById(R.id.name);
			holder.appSize = (TextView)convertView.findViewById(R.id.app);
            holder.dataSize = (TextView)convertView.findViewById(R.id.data);
			holder.versionNamem = (TextView)convertView.findViewById(R.id.versionNamem);
            holder.versionName = (TextView)convertView.findViewById(R.id.versionName);
            holder.versionCode = (TextView)convertView.findViewById(R.id.versionCode);
            holder.icon = (ImageView)convertView.findViewById(R.id.icon);
            holder.uninstall = (Button)convertView.findViewById(R.id.uninstall);
            holder.clear = (Button)convertView.findViewById(R.id.clear);
            
            holder.layout_icon = (RelativeLayout)convertView.findViewById(R.id.layout_icon);
			convertView.setTag(holder);
			
			if (ManualClearManager.isAddSystemInfo) {
				holder.versionNamem.setVisibility(View.VISIBLE);
				holder.versionName.setVisibility(View.VISIBLE);
				holder.versionCode.setVisibility(View.VISIBLE);
			}
			
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

 
		
		holder.name.setText(appInfos.get(position).getAppName());
		
	
		
		//uninstall button
		if(appInfos.get(position).isSystemApp()||ManualClearManager.isShowSystemAppList(appInfos.get(position).getPackname())){
//			holder.uninstall.setEnabled(false);
			holder.uninstall.setBackgroundResource(R.drawable.button_disable);
		}else {
//			holder.uninstall.setEnabled(true);
			holder.uninstall.setBackgroundResource(R.drawable.button_unselected);
		}
		
			//clear button
		if(appInfos.get(position).getDataSize()!=null && (appInfos.get(position).getDataSize()==0)){
//			holder.clear.setEnabled(false);
			holder.clear.setBackgroundResource(R.drawable.button_disable);
		}else if(appInfos.get(position).isSystemApp()){
//			holder.clear.setEnabled(false);
			holder.clear.setBackgroundResource(R.drawable.button_disable);
			}else{
//			holder.clear.setEnabled(true);
			holder.clear.setBackgroundResource(R.drawable.button_unselected);
		}
		holder.uninstall.setTextColor(mContext.getResources().getColor(R.color.main_button_unselected));
		holder.clear.setTextColor(mContext.getResources().getColor(R.color.main_button_unselected));
	
	 
//        Utils.print(TAG, "pos===="+position);
		//app data
        if(appInfos.get(position).getApp_size()!=null){
        	holder.appSize.setText(mContext.getResources().getString(R.string.app) + ": " +
		               Utils.getSizeStr(mContext, appInfos.get(position).getApp_size()));
        }else {
        	holder.appSize.setText(mContext.getResources().getString(R.string.app) + ": ");
		}
		
        //cache data
        if(appInfos.get(position).getDataSize()!=null){
        	holder.dataSize.setText(mContext.getResources().getString(R.string.data) + ": " +
		               Utils.getSizeStr(mContext, appInfos.get(position).getDataSize()));
        }else {
        	holder.dataSize.setText(mContext.getResources().getString(R.string.data) + ": ");
		}
		
        holder.versionName.setText(appInfos.get(position).getVersionName());
        
        //cache versionCode
        holder.versionCode.setText(mContext.getResources().getString(R.string.version_code) + ": " +
		             appInfos.get(position).getVersionCode()+"");
        
        //app icon
        holder.layout_icon.setBackgroundResource(appInfos.get(position).getBgId());
		
		
        holder.icon.setTag(appInfos.get(position).getPackname());
		final int pos = position;
		final Drawable drawable = iconAsyncImageLoader.loadDrawable(appInfos.get(position).getPackname(), new ImageCallback() {
			

			public void imageLoaded(Drawable imageDrawable, String imageUrl) {
				// TODO Auto-generated method stub
				ImageView imageView = (ImageView)listView.findViewWithTag(appInfos.get(pos).getPackname());
				if(imageView!=null && imageDrawable!=null)
				  imageView.setBackground(imageDrawable);
			}
		});
		
		if(drawable==null){
//			Utils.print("bitmap null", "bitmap null");
			holder.icon.setBackgroundResource(R.drawable.ic_launcher);
		}
		else {
//			Utils.print("bitmap not null", "bitmap not null");
			holder.icon.setBackground(drawable);
		}
 
		
		
		return convertView;
	}
	
	
	public void removeAppInfo(String packageName){
		iconAsyncImageLoader.removeAppIcon(packageName);
	}
	
	public void removeAllIcon(){
		iconAsyncImageLoader.destroy();
	}
	
}
