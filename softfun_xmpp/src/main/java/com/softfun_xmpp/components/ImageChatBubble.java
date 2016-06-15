package com.softfun_xmpp.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;
import android.view.View;

/**
 * 废弃
 * Created by 范张 on 2016-06-03.
 */
public class ImageChatBubble extends View {

    private Bitmap mImageSource;
    private Bitmap mOut;
    private View mView;

    public ImageChatBubble(Context context, Bitmap source, View view) {
        super(context);
        this.mImageSource = source;
        mView = view;
        initdata1();
    }


    private void initdata1() {
        setLayerType(LAYER_TYPE_SOFTWARE, null); //禁用硬件加速
        //获取图片的资源文件
        Bitmap original = mImageSource;
        //获取遮罩层图片
        mOut = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
        //将遮罩层的图片放到画布中
        Canvas mCanvas = new Canvas(mOut);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Bitmap mask = getViewBitmap(mView, original.getWidth(), original.getHeight());
        mCanvas.drawBitmap(mask, 0, 0, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));//叠加重复的部分，显示上面的
        mCanvas.drawBitmap(original, 0, 0, paint);
        paint.setXfermode(null);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mImageSource.getWidth(), mImageSource.getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mOut, 0, 0, null);
    }


    @Override
    public void invalidate() {
        //mBufferBitmap = null;
        if (mImageSource != null) {
            mImageSource.recycle();
            mImageSource = null;
        }
        if (mView != null) {
            mView = null;
        }
        super.invalidate();
    }

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

            int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
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
