package com.softfun_xmpp.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class ImageChatBubbleEx extends ImageView {

    private Bitmap mImageSource;
    private Bitmap mOut;
    private View mView;
    private int mWidth;
    private int mHeight;
    private boolean b;

    public boolean isB() {
        return b;
    }

    public void setB(boolean b) {
        this.b = b;
    }

    public int getmWidth() {
        return mWidth;
    }

    public void setmWidth(int mWidth) {
        this.mWidth = mWidth;
    }

    public int getmHeight() {
        return mHeight;
    }

    public void setmHeight(int mHeight) {
        this.mHeight = mHeight;
    }

    public Bitmap getmImageSource() {
        return mImageSource;
    }

    public void setmImageSource(Bitmap imageSource) {
        this.mImageSource = imageSource;
    }

    public View getmView() {
        return mView;
    }

    public void setmView(View view) {
        this.mView = view;
    }


    /**
     * 构造
     *
     * @param context -
     */
    public ImageChatBubbleEx(Context context) {
        this(context, null);
    }

    /**
     * 构造
     *
     * @param context -
     * @param attrs   -
     */
    public ImageChatBubbleEx(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 构造
     *
     * @param context      -
     * @param attrs        -
     * @param defStyleAttr -
     */
    public ImageChatBubbleEx(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     * 计算
     */
    public void initdata1() {
        //System.out.println("====================  合并  =====================");
        //setLayerType(LAYER_TYPE_SOFTWARE, null); //禁用硬件加速
        //获取图片的资源文件 mImageSource
        //获取遮罩层图片
        mOut = Bitmap.createBitmap(mImageSource.getWidth(), mImageSource.getHeight(), Bitmap.Config.ARGB_8888);
        //将遮罩层的图片放到画布中
        Canvas mCanvas = new Canvas(mOut);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Bitmap mask = getViewBitmap(mView, mWidth, mHeight);
        mCanvas.drawBitmap(mask, 0, 0, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));//叠加重复的部分，显示上面的
        mCanvas.drawBitmap(mImageSource, 0, 0, paint);
        paint.setXfermode(null);
        if(!b)
            setImageBitmap(mOut);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        canvas.drawBitmap(mOut, 0, 0, null);
        //System.out.println("====================  onDraw  ===================== ");
        if (mImageSource != null && mView != null && !b) {
            initdata1();
            b = true;
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        if (bm != null) {
            mImageSource = bm;
            mWidth = bm.getWidth();
            mHeight = bm.getHeight();
            //System.out.println("====================  setImageBitmap  =====================");
            setImageDrawable(new BitmapDrawable(getResources(), mImageSource));
        }
    }

    //// TODO: 2016-06-05  测量自身图片的高宽，得到高宽，在onDraw的时候
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //System.out.println("====================  onMeasure  ===================== :" + widthMeasureSpec + " " + heightMeasureSpec + " " + mWidth + " " + mHeight);
        b = false;
        //setMeasuredDimension(mWidth, mHeight);
    }


    //    @Override
//    public void invalidate() {
//        System.out.println("====================  invalidate  =====================");
//        if (mImageSource != null) {
//            mImageSource.recycle();
//            mImageSource = null;
//        }
//        if (mView != null) {
//            mView = null;
//        }
//        super.invalidate();
//    }

    /**
     * 将View转化成Bitmap
     *
     * @param comBitmap-
     * @param width-
     * @param height-
     * @return -
     */
    public Bitmap getViewBitmap(View comBitmap, int width, int height) {
        Bitmap bitmap = null;
        if (comBitmap != null) {
            comBitmap.clearFocus();
            comBitmap.setPressed(false);

            boolean willNotCache = comBitmap.willNotCacheDrawing();
            comBitmap.setWillNotCacheDrawing(false);

            // Reset the drawing cache background color to fully transparent
            // for the duration of this operation
            int color = comBitmap.getDrawingCacheBackgroundColor();
            comBitmap.setDrawingCacheBackgroundColor(0);
            float alpha = comBitmap.getAlpha();
            comBitmap.setAlpha(1.0f);

            if (color != 0) {
                comBitmap.destroyDrawingCache();
            }

            int widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            int heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            comBitmap.measure(widthSpec, heightSpec);
            comBitmap.layout(0, 0, width, height);

            comBitmap.buildDrawingCache();
            Bitmap cacheBitmap = comBitmap.getDrawingCache();
            if (cacheBitmap == null) {
                Log.e("view.ProcessImageToBlur", "failed getViewBitmap(" + comBitmap + ")",
                        new RuntimeException());
                return null;
            }
            bitmap = Bitmap.createBitmap(cacheBitmap);
            // Restore the view
            comBitmap.setAlpha(alpha);
            comBitmap.destroyDrawingCache();
            comBitmap.setWillNotCacheDrawing(willNotCache);
            comBitmap.setDrawingCacheBackgroundColor(color);
        }
        return bitmap;
    }


}
