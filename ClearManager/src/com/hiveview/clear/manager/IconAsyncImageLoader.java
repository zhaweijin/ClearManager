package com.hiveview.clear.manager;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;

public class IconAsyncImageLoader {

	private BlockingQueue queue;
	private ThreadPoolExecutor executor;
	private HashMap<String, SoftReference<Drawable>> imageCache;
	private PackageManager packageManager;
	private Context mContext;

	public IconAsyncImageLoader(Context context) {
		imageCache = new HashMap<String, SoftReference<Drawable>>();
		queue = new LinkedBlockingQueue();
		mContext = context;
		executor = new ThreadPoolExecutor(1, 50, 180, TimeUnit.SECONDS, queue);
		packageManager = mContext.getPackageManager();
	}

	public Drawable loadDrawable(final String packageName,
			final ImageCallback imageCallback) {

		if (imageCache.containsKey(packageName)) {
			SoftReference<Drawable> softReference = imageCache.get(packageName);
			Drawable drawable = softReference.get();
			if (drawable != null) {
				return drawable;
			}
		}

		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				imageCallback.imageLoaded((Drawable) message.obj, packageName);
			}
		};

		executor.execute(new Runnable() {
			public void run() {
				Drawable drawable = loadImageFromPackage(mContext, packageName);
				imageCache.put(packageName, new SoftReference<Drawable>(drawable));
				Message message = handler.obtainMessage(0, drawable);
				handler.sendMessage(message);
			}
		});

		return null;
	}

	public synchronized Drawable loadImageFromPackage(Context context,
			String packageName) {
		Drawable drawable = null;
		if (packageName == null)
			return null;

		drawable = getAppIcon(packageName);
		return drawable;
	}

	public interface ImageCallback {
		public void imageLoaded(Drawable imageBitmap, String packageName);
	}

	/*
	 * 获取程序 图标
	 */
	public Drawable getAppIcon(String packname) {
		try {
			ApplicationInfo info = packageManager.getApplicationInfo(packname, 0);
			return info.loadIcon(packageManager);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	public void removeAppIcon(String packname){
		imageCache.remove(packname);
	}
	
	public void destroy(){
		imageCache.clear();
	}

	
}
