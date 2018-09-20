package com.hiveview.clear.manager.widget;

import com.hiveview.clear.manager.R;
import com.hiveview.clear.manager.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class RecycleSurfaceView extends SurfaceView implements SurfaceHolder.Callback,Runnable{  
	  
    private String TAG = this.getClass().getSimpleName();  
    private final SurfaceHolder mHolder;  
    private Thread mThread;  
    private long mFrameSpaceTime = 10;  // 每帧图片的间隔时间  
    private boolean mIsDraw = true;     // 画图开关  
    private int mCurrentIndext = 0;     // 当前正在播放的png 
  
    public int mBitmapResourceIds[] = {  
               R.drawable.scan_1,R.drawable.scan_2,R.drawable.scan_3,
               R.drawable.scan_4,R.drawable.scan_5,R.drawable.scan_6,
               R.drawable.scan_7,R.drawable.scan_8,R.drawable.scan_9,
    };  
    private Bitmap mBitmap;  
  
    public RecycleSurfaceView(Context context) {this(context,null);}  
  
    public RecycleSurfaceView(Context context, AttributeSet attrs) {this(context,attrs,0);}  
  
    public RecycleSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {  
        super(context, attrs, defStyleAttr);  
        mHolder = this.getHolder();       // 获得surfaceholder  
        mHolder.addCallback(this);        // 添加回调，这样三个方法才会执行  
  
        setZOrderOnTop(true);  
        mHolder.setFormat(PixelFormat.TRANSLUCENT);// 设置背景透明
//        mHolder.setFixedSize(arg0, arg1);
        
        setLayerType(View.LAYER_TYPE_SOFTWARE, null); 
    }  
  
    /** 
    首先继承SurfaceView，并实现SurfaceHolder.Callback接口，实现它的三个方法： 
    surfaceCreated(SurfaceHolder holder)：surface创建的时候调用，一般在该方法中启动绘图的线程。 
    surfaceChanged(SurfaceHolder holder, int format, int width,int height)：surface尺寸发生改变的时候调用，如横竖屏切换。 
    surfaceDestroyed(SurfaceHolder holder) ：surface被销毁的时候调用，如退出游戏画面，一般在该方法中停止绘图线程。 
    还需要获得SurfaceHolder，并添加回调函数，这样这三个方法才会执行。 
    */  
  
    @Override  
    public void surfaceCreated(SurfaceHolder holder) {  
        Utils.print(TAG, "surfaceCreated: ");  
        if(mBitmapResourceIds == null){  
        	Utils.print(TAG, "surfaceCreated: 图片资源为空");  
            return;  
        } 
 
        drawView();
    }  
    
    public void startRecyle(){
    	 mThread = new Thread(this);  
         mThread.start();  
         mIsDraw = true; 
    }
 
    public void stopRecyle(){
    	mIsDraw = false;
    	setBackgroundResource(R.drawable.center_max_bg);
    }
  
    public void setmBitmapResourceIds(int[] mBitmapResourceIds) {  
        this.mBitmapResourceIds = mBitmapResourceIds;  
    }  
  
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}
  
    @Override  
    public void surfaceDestroyed(SurfaceHolder holder) {  
        mIsDraw = false;  
        try {  
            Thread.sleep(mFrameSpaceTime);  
            Utils.print(TAG, "surfaceDestroyed: Thread " + mThread.getState());  
        } catch (InterruptedException e) {  
            e.printStackTrace();  
        }  
    }  
  
    @Override  
    public void run() {  
        synchronized (mHolder) {         // 这里加锁为了以后控制这个绘制线程的wait与notify  
            while (mIsDraw) {  
                try {  
                	long time1 = SystemClock.currentThreadTimeMillis();
                    drawView();  
                    long time2 = SystemClock.currentThreadTimeMillis();
                    Utils.print(TAG, "div time==="+(time2-time1));
                    Thread.sleep(mFrameSpaceTime);  
                } catch (InterruptedException e) {  
                    e.printStackTrace();  
                }  
            }  
        }  
    }  
  
    private void drawView() {  
    	Utils.print(TAG, "drawView: ");  
        Canvas mCanvas = mHolder.lockCanvas();      // 锁定画布  
        if(mCanvas==null)
        	return;
        try {  
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);        // 清除屏幕  
  
            mBitmap = BitmapFactory.decodeResource(getResources(), mBitmapResourceIds[mCurrentIndext]);  
            Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(mBitmap, 
            		getResources().getDimensionPixelSize(R.dimen.center_cycle_width),
            		getResources().getDimensionPixelSize(R.dimen.center_cycle_height)); 
            
            
            mCanvas.drawBitmap(resizeBmp, 0, 0, null);  
 
          
            if (mCurrentIndext == mBitmapResourceIds.length - 1) {  
                mCurrentIndext = 0;  
            }  
        }  
        catch (Exception e) {  
            e.printStackTrace();  
            Utils.print(TAG, "error");
        }  
        finally {  
            mCurrentIndext++;  
            if (mCanvas != null) {  
                mHolder.unlockCanvasAndPost(mCanvas);       // 提交画布  
            }  
            recycle(mBitmap);  // 这里回收资源非常重要！  
        }  
    }
     
  
    private void recycle(Bitmap mBitmap) {  
        if(mBitmap != null)  
            mBitmap.recycle();  
    }  
}  