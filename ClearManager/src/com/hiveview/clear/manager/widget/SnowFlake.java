package com.hiveview.clear.manager.widget;

 
import com.hiveview.clear.manager.R;
import com.hiveview.clear.manager.Utils;

import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;  
import android.graphics.Matrix;
import android.graphics.Paint;  
import android.graphics.Path;
import android.graphics.Point;  
import android.util.Log;
  
  
  
/** 
 * 雪花的类, 移动, 移出屏幕会重新设置位置. 
 * <p/> 
 * Created by wangchenlong on 16/1/24. 
 */  
public class SnowFlake {  
	public final static String TAG = "snow";
    // 雪花的角度  
    private static final float ANGE_RANGE = 0.1f; // 角度范围  
    private static final float HALF_ANGLE_RANGE = ANGE_RANGE / 2f; // 一般的角度  
    private static final float HALF_PI = (float) Math.PI / 2f; // 半PI  
    private static final float ANGLE_SEED = 25f; // 角度随机种子  
    private static final float ANGLE_DIVISOR = 10000f; // 角度的分母  
  
    // 雪花的移动速度  
    private static final float INCREMENT_LOWER = 2f;  
    private static final float INCREMENT_UPPER = 4f;  
  
    // 雪花的大小  
    private static final float FLAKE_SIZE_LOWER = 7f;  
    private static final float FLAKE_SIZE_UPPER = 20f;  
  
    private final RandomGenerator mRandom; // 随机控制器  
    private final Point mPosition; // 雪花位置  
    private float mAngle; // 角度  
    private final float mIncrement; // 雪花的速度  
    private final float mFlakeSize; // 雪花的大小  
    private final Paint mPaint; // 画笔  
    private Context mContext;
    
    private Bitmap bmp;
    private int bmpWidth;
    private int bmpHeight;
  
    private SnowFlake(Context context,RandomGenerator random, Point position, float angle, float increment, float flakeSize, Paint paint) {  
//        Utils.print(TAG, "SnowFlake()");
    	mRandom = random;  
        mPosition = position;  
        mIncrement = increment;  
        mFlakeSize = flakeSize;  
        mPaint = paint;  
        mAngle = angle;  
        mContext = context;
        
        bmp=BitmapFactory.decodeResource(mContext.getResources(),R.drawable.star);
        //获得Bitmap的高和宽
        bmpWidth=bmp.getWidth();
        bmpHeight=bmp.getHeight();
    }  
  
    public static SnowFlake create(Context context,int width, int height, Paint paint) {  
//    	Utils.print(TAG, "create()");
    	RandomGenerator random = new RandomGenerator();  
        int x = random.getRandom(width);  
        int y = random.getRandom(height);  
        Point position = new Point(x, y);  
        float angle = random.getRandom(ANGLE_SEED) / ANGLE_SEED * ANGE_RANGE + HALF_PI - HALF_ANGLE_RANGE;  
        float increment = random.getRandom(INCREMENT_LOWER, INCREMENT_UPPER);  
//        float flakeSize = random.getRandom(FLAKE_SIZE_LOWER, FLAKE_SIZE_UPPER);
        float flakeSize = random.getRandom(0.1f, 0.5f); 
        
        return new SnowFlake(context,random, position, angle, increment, flakeSize, paint);  
    }  
  
    // 绘制雪花  
    public void draw(Canvas canvas) {  
    	
    	
    	try {
    		 int width = canvas.getWidth();  
    	        int height = canvas.getHeight();  
    	        move(width, height);  
    	 
//    	        Log.v("tt", "x=="+mPosition.x+",y=="+mPosition.y);
//    	        Utils.print(TAG,"mFlakeSize=="+mFlakeSize);
    	        
    	        //画圆
//    	        canvas.drawCircle(mPosition.x, mPosition.y, mFlakeSize, mPaint);
    	        
    	        
    	        //设置缩小比例
    	        double scale=mFlakeSize;
//    	        Log.v("test", "size==="+mFlakeSize);
    	        //计算出这次要缩小的比例
    	        float scaleWidth=(float)(1*scale);
    	        float scaleHeight=(float)(1*scale);
    	        //产生resize后的Bitmap对象
    	        Matrix matrix=new Matrix();
    	        
    	        matrix.postScale(scaleWidth, scaleHeight);
    	        //画图片
    	        Bitmap resizeBmp=Bitmap.createBitmap(bmp, 0, 0, bmpWidth, bmpHeight, matrix, true);
    	        canvas.drawBitmap(resizeBmp, mPosition.x,mPosition.y,mPaint);
    	        
    	        recycle(resizeBmp);
    	        
    	        //画多边形         
    	        /*Path path = new Path();
    	        float w = mFlakeSize;
    	        float h = mFlakeSize+8;
    	        float angle;
    	        if(w>=7){
    	        	angle = 3;
    	        }else if(w>=10){
    	        	angle = 4;
    	        }else if(w>=14){
    				angle = 5;
    			}else if(w>=18){
    				angle = 6;
    			}else {
    				angle = 7;
    			}
    	        path.moveTo(mPosition.x, mPosition.y);
    	        path.lineTo(mPosition.x+w,mPosition.y);
    	        path.lineTo(mPosition.x+w-angle, mPosition.y+h);
    	        path.lineTo(mPosition.x-angle, mPosition.y+h);
    	        path.close();
    	        canvas.drawPath(path, mPaint);*/
		} catch (Exception e) {
			// TODO: handle exception
			Utils.print(TAG, "error");
			e.printStackTrace();
		}
    }  
   
    
    
    
    // 移动雪花  
    private void move(int width, int height) {  
        double x = mPosition.x + (mIncrement * Math.cos(mAngle));  
        double y = mPosition.y + (mIncrement * Math.sin(mAngle));  
  
        mAngle += mRandom.getRandom(-ANGLE_SEED, ANGLE_SEED) / ANGLE_DIVISOR; // 随机晃动  
  
        mPosition.set((int) x, (int) y);  
  
        // 移除屏幕, 重新开始  
        if (!isInside(width, height)) {  
            reset(width);  
        }  
    }  
  
    // 判断是否在其中  
    private boolean isInside(int width, int height) {  
        int x = mPosition.x;  
        int y = mPosition.y;  
        return x >= -mFlakeSize - 1 && x + mFlakeSize <= width && y >= -mFlakeSize - 1 && y - mFlakeSize < height;  
    }  
  
    // 重置雪花  
    private void reset(int width) {  
        mPosition.x = mRandom.getRandom(width);  
        mPosition.y = (int) (-mFlakeSize - 1); // 最上面  
        mAngle = mRandom.getRandom(ANGLE_SEED) / ANGLE_SEED * ANGE_RANGE + HALF_PI - HALF_ANGLE_RANGE;  
    }  
    
    private void recycle(Bitmap mBitmap) {  
        if(mBitmap != null)  
            mBitmap.recycle();  
    }  
}  