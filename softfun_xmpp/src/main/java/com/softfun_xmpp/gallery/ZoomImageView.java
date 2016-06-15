package com.softfun_xmpp.gallery;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * Created by 范张 on 2016-02-24.
 * 实现全局布局监听类
 */
public class ZoomImageView extends ImageView implements ViewTreeObserver.OnGlobalLayoutListener, ScaleGestureDetector.OnScaleGestureListener, View.OnTouchListener {

    private boolean mOnce = false;
    /**
     * 初始化时缩放值
     */
    private float mInitScale;
    /**
     * 双击时到达的缩放值
     */
    private float mMidScale;
    /**
     * 最大缩放值
     */
    private float mMaxScale;
    /**
     * 最小缩放值
     */
    private float mMinScale;
    /**
     * 缩放平移
     */
    private Matrix mScaleMatrix;

    /**
     * 多点触控
     */
    private ScaleGestureDetector mScaleGestureDetector;

    //-----------自由移动变量-------------------------------
    /**
     * 记录上一次多点触控的数量
     */
    private int mLastPointerCount;

    private float mLastX;
    private float mLastY;
    private int mTouchSlop;
    private boolean isCanDrag;
    private boolean isCheckLeftAndRight;
    private boolean isCheckTopAndBottom;

    //------------双击放大缩小-------------------------------------
    private GestureDetector mGestureDetector;
    private boolean isAutoScale;

    /**
     * 单参数构造方法调用2参数构造方法
     *
     * @param context
     */
    public ZoomImageView(Context context) {
        this(context, null);
    }

    /**
     * 2参数构造方法调用3参数构造方法
     *
     * @param context
     * @param attrs
     */
    public ZoomImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScaleMatrix = new Matrix();
        setScaleType(ScaleType.MATRIX);
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        //注册触摸事件监听
        setOnTouchListener(this);
        //获得move的参考值
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (isAutoScale) return true;

                float x = e.getX();
                float y = e.getY();
                if (getScale() < mMidScale) {
                    //mScaleMatrix.postScale(mMidScale / getScale(), mMidScale / getScale(), x, y);
                    postDelayed(new AutoScaleRunnable(mMidScale, x, y), 16);
                    isAutoScale = true;
                } else {
                    //mScaleMatrix.postScale(mInitScale / getScale(), mInitScale / getScale(), x, y);
                    postDelayed(new AutoScaleRunnable(mInitScale, x, y), 16);
                    isAutoScale = true;
                }
                setImageMatrix(mScaleMatrix);


                return true;
            }
        });
    }

    /**
     * 缓慢梯度的放大缩小
     */
    public class AutoScaleRunnable implements Runnable {
        //缩放的目标值
        private float mTargetScale;
        //缩放的中心点
        private float x;
        private float y;

        private final float BIGGER = 1.07f;
        private final float SMAILL = 0.93f;

        private float tmpScale;

        public AutoScaleRunnable(float mTargetScale, float x, float y) {
            this.mTargetScale = mTargetScale;
            this.x = x;
            this.y = y;

            if (getScale() < mTargetScale) {
                tmpScale = BIGGER;
            }
            if (getScale() > mTargetScale) {
                tmpScale = SMAILL;
            }
        }

        @Override
        public void run() {
            mScaleMatrix.postScale(tmpScale, tmpScale, x, y);
            checkBorderAndCenterWhenScale();
            setImageMatrix(mScaleMatrix);

            float currentScale = getScale();

            if ((tmpScale > 1.0f && currentScale < mTargetScale) || (tmpScale < 1.0f && currentScale > mTargetScale)) {
                //再次执行run方法，间隔16毫秒，类似递归
                postDelayed(this, 16);
            } else {
                float scale = mTargetScale / currentScale;
                mScaleMatrix.postScale(scale, scale, x, y);
                checkBorderAndCenterWhenScale();
                setImageMatrix(mScaleMatrix);
                isAutoScale = false;
            }
        }
    }


    /**
     * 注册监听
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    /**
     * 移除监听
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeGlobalOnLayoutListener(this);
    }


    /**
     * 获取imageview加载完成的图片
     */
    @Override
    public void onGlobalLayout() {
        if (!mOnce) {
            mOnce = true;
            //得到控件的宽高
            int width = getWidth();
            int height = getHeight();
            //得到图片以及宽高
            Drawable drawable = getDrawable();//得到图片
            if (drawable == null) {
                return;
            }

            int dw = drawable.getIntrinsicWidth();//图片宽度
            int dh = drawable.getIntrinsicHeight();//图片高度

            float scale = 1.f;//缩放值
            //对比宽高
            if (dw > width && dh < height) {//比屏幕宽，比屏幕矮
                scale = width * 1.0f / dw;
            }
            if (dh > height && dw < width) {//比屏幕高，比屏幕窄
                scale = height * 1.0f / dh;
            }
            if ((dw > width && dh > height)) {  //|| (dw < width && dh < height)
                scale = Math.min(width * 1.0f / dw, height * 1.0f / dh);
            }
            if (dw < width && dh < height) {
                scale = 1.0f;
            }
            mInitScale = scale;
            mMaxScale = mInitScale * 4;
            mMidScale = mInitScale * 2;
            mMinScale = scale / 2;


            //移动图片到屏幕中心点
            int dx = width / 2 - dw / 2;
            int dy = height / 2 - dh / 2;

            //设置缩放平移
            mScaleMatrix.postTranslate(dx, dy);
            mScaleMatrix.postScale(mInitScale, mInitScale, width / 2, height / 2);
            setImageMatrix(mScaleMatrix);

        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        //触摸事件传递给ScaleGestureDetector处理
        mScaleGestureDetector.onTouchEvent(event);

        //接触点坐标
        float x = 0;
        float y = 0;
        //获得多点触控的数量
        int pointerCount = event.getPointerCount();
        for (int i = 0; i < pointerCount; i++) {
            x += event.getX(i);
            y += event.getY(i);
        }
        x /= pointerCount;
        y /= pointerCount;
        if (mLastPointerCount != pointerCount) {
            isCanDrag = false;
            mLastX = x;
            mLastY = y;
        }
        mLastPointerCount = pointerCount;

        //得到图片的高宽，如果高宽任意大于屏幕高宽，就要拦截viewpager移动事件，只移动图片不移动viewpager
        RectF rect = getMatrixRectF();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (rect.width() > getWidth() + 0.01 || rect.height() > getHeight() + 0.01) {
                    if(getParent() instanceof ViewPager){
                        //请求 触摸按下事件  不被拦截事件
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (rect.width() > getWidth() + 0.01 || rect.height() > getHeight() + 0.01) {
                    if(getParent() instanceof ViewPager){
                        //请求 触摸移动事件  不被拦截事件
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }

                float dx = x - mLastX;
                float dy = y - mLastY;
                if (!isCanDrag) {
                    isCanDrag = isMoveAction(dx, dy);
                }
                if (isCanDrag) {
                    RectF rectF = getMatrixRectF();
                    if (getDrawable() != null) {
                        isCheckLeftAndRight = isCheckTopAndBottom = true;
                        if (rectF.width() < getWidth()) {
                            isCheckLeftAndRight = false;
                            dx = 0;
                        }
                        if (rectF.height() < getHeight()) {
                            isCheckTopAndBottom = false;
                            dy = 0;
                        }
                        mScaleMatrix.postTranslate(dx, dy);
                        checkBorderWhenTranslate();
                        setImageMatrix(mScaleMatrix);
                    }
                }
                mLastX = x;
                mLastY = y;
                break;
            }
            case MotionEvent.ACTION_UP: {
                mLastPointerCount = 0;
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                mLastPointerCount = 0;
                break;
            }

        }


        return true;//必须true
    }

    /**
     * 判断是否是move
     *
     * @param dx
     * @param dy
     * @return
     */
    private boolean isMoveAction(float dx, float dy) {
        return Math.sqrt(dx * dx + dy * dy) > mTouchSlop;
    }


    /**
     * 获取当前图片的缩放值
     *
     * @return
     */
    public float getScale() {
        float[] values = new float[9];
        mScaleMatrix.getValues(values);
        return values[Matrix.MSCALE_X];
    }

    /**
     * 多点触控缩放方法，其缩放区间是mInitScale ~ mMaxScale
     *
     * @param detector
     * @return
     */
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        //得到当前的缩放值
        float scale = getScale();
        //得到触摸事件后，可以拿到缩放值
        float scaleFactor = detector.getScaleFactor();

        if (getDrawable() == null) {
            return true;
        }
        //缩放范围的控制
//        if ((scale < mMaxScale && scaleFactor > 1.0f) || (scale > mInitScale && scaleFactor < 1.0f)) {
//            if (scale * scaleFactor < mInitScale) {
//                scaleFactor = mInitScale / scale;
//            }
//            if (scale * scaleFactor > mMaxScale) {
//                scaleFactor = mMaxScale / scale;
//            }
//            mScaleMatrix.postScale(scaleFactor,scaleFactor,getWidth()/2,getHeight()/2);
//            setImageMatrix(mScaleMatrix);
//        }
        //最小缩放控制
        if (scale * scaleFactor < mMinScale) {
            scaleFactor = mMinScale / scale;
        }
        //最大缩放控制
        if (scale * scaleFactor > mMaxScale) {
            scaleFactor = mMaxScale / scale;
        }
        mScaleMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
        checkBorderAndCenterWhenScale();
        setImageMatrix(mScaleMatrix);

        return true;
    }

    /**
     * 获得图片缩放后的宽高、4点坐标
     *
     * @return
     */
    private RectF getMatrixRectF() {
        Matrix matrix = mScaleMatrix;
        RectF rectF = new RectF();
        Drawable drawable = getDrawable();
        if (drawable != null) {
            rectF.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            matrix.mapRect(rectF);
        }
        return rectF;
    }

    /**
     * 缩放的时候控制边界
     */
    private void checkBorderAndCenterWhenScale() {
        RectF rect = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;

        int width = getWidth();
        int height = getHeight();
        //缩放时 进行边界检测，防止出现白边
        if (rect.width() >= width) {
            if (rect.left > 0) {
                deltaX = -rect.left;
            }
            if (rect.right < width) {
                deltaX = width - rect.right;
            }
        }

        if (rect.height() >= height) {
            if (rect.top > 0) {
                deltaY = -rect.top;
            }
            if (rect.bottom < height) {
                deltaY = height - rect.bottom;
            }
        }

        //如果rect的高宽小于控件的宽高，就居中
        if (rect.width() < width) {
            deltaX = width / 2f - rect.right + rect.width() / 2f;
        }
        if (rect.height() < height) {
            deltaY = height / 2f - rect.bottom + rect.height() / 2f;
        }
        mScaleMatrix.postTranslate(deltaX, deltaY);

    }

    /**
     * 当移动时进行边界检查
     */
    private void checkBorderWhenTranslate() {
        RectF rectF = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;

        int width = getWidth();
        int height = getHeight();
        if (rectF.top > 0 && isCheckTopAndBottom) {
            deltaY = -rectF.top;
        }

        if (rectF.bottom < height && isCheckTopAndBottom) {
            deltaY = height - rectF.bottom;
        }

        if (rectF.left > 0 && isCheckLeftAndRight) {
            deltaX = -rectF.left;
        }

        if (rectF.right < width && isCheckLeftAndRight) {
            deltaX = width - rectF.right;
        }
        mScaleMatrix.postTranslate(deltaX, deltaY);
    }


    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {

        return true;//必须true
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }


}
