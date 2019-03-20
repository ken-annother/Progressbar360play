package com.nicekun.progreebarlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.Interpolator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class WaterProgressBar extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private static final int DEFAULT_BAR_FORGROUND_COLOR = 0xFFFF3366;
    private static final int DEFAULT_BAR_BACKROUND_COLOR = 0xFFFFCCFF;
    private static final int DEFAULT_BAR_INDICATOR_COLOR = 0xFFFFFFFF;
    private static final String DEFAULT_PREPARE_TEXT = "下载";

    private int mBarForgroundColor = DEFAULT_BAR_FORGROUND_COLOR;
    private int mBarBackgroundColor = DEFAULT_BAR_BACKROUND_COLOR;
    private int mIndicatorColor = DEFAULT_BAR_INDICATOR_COLOR;

    private String mPrepareText = DEFAULT_PREPARE_TEXT;

    private ExecutorService mExecutorService = Executors.newFixedThreadPool(1);

    private SurfaceHolder mHolder;
    private boolean surfaceAvailable;

    private int mWidth;
    private int mHeight;
    private Paint mBgPaint;
    private Paint mFgPaint;
    private Paint mIdPaint;

    private int mSplashWatersStepInterval;

    /**
     * 每一帧旋转多少度
     * 这个会影响右边旋转水滴的转速
     */
    private int mRotateDegreePerFrame = 9;

    /**
     * 动画一帧多少毫秒
     */
    private long mTimeInFrame = 30;

    /**
     * 甩出来的水滴每一帧移动的像素数
     */
    private int mWaterMovePerPiexlFrame = 12;

    private ReentrantLock mLock = new ReentrantLock();

    public static final int STATUS_PREPARE = 0;
    public static final int STATUS_PREPARING_FOLD = 10;
    public static final int STATUS_PREPARING_SPREAD = 11;
    public static final int STATUS_RUNNING = 1;
    public static final int STATUS_PAUSE = 2;

    private int mStatus = STATUS_RUNNING;

    public WaterProgressBar(Context context) {
        super(context);
        initView();
    }

    public WaterProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaterProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WaterProgressBar);
        if (typedArray != null) {
            mIndicatorColor = typedArray.getColor(R.styleable.WaterProgressBar_barIndicatorColor, DEFAULT_BAR_INDICATOR_COLOR);
            mBarForgroundColor = typedArray.getColor(R.styleable.WaterProgressBar_barForgroundColor, DEFAULT_BAR_FORGROUND_COLOR);
            mBarBackgroundColor = typedArray.getColor(R.styleable.WaterProgressBar_barBackgroundColor, DEFAULT_BAR_BACKROUND_COLOR);
            typedArray.recycle();
        }

        initView();
    }

    private void initView() {
        mHolder = getHolder();
        mHolder.addCallback(this);


        mBgPaint = new Paint();
        mBgPaint.setColor(mBarBackgroundColor);
        mBgPaint.setAntiAlias(true);
        mBgPaint.setStyle(Paint.Style.FILL);

        mFgPaint = new Paint();
        mFgPaint.setColor(mBarForgroundColor);
        mFgPaint.setAntiAlias(true);
        mFgPaint.setStyle(Paint.Style.FILL);

        mIdPaint = new Paint();
        mIdPaint.setColor(mIndicatorColor);
        mIdPaint.setAntiAlias(true);
        mIdPaint.setTextAlign(Paint.Align.CENTER);
        mIdPaint.setStyle(Paint.Style.FILL);
        mIdPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));

        setFocusable(true);
        setZOrderOnTop(true);
        mHolder.setFormat(PixelFormat.TRANSLUCENT);
        setFocusableInTouchMode(true);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceAvailable = true;
        mExecutorService.execute(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (width < mHeight * 2) {
            mHeight = width / 4;
            mWidth = width;
        } else {
            mHeight = height;
            mWidth = width;
        }

        mSplashWatersStepInterval = 2 * mHeight;
        mIdPaint.setTextSize(mHeight / 3f);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceAvailable = false;
    }

    @Override
    public void run() {
        while (surfaceAvailable) {
            long startTime = System.currentTimeMillis();
            mLock.lock();
            try {
                Canvas canvas = mHolder.lockCanvas(new Rect(0, 0, mWidth, mHeight));
                if (canvas != null) {
                    draw(canvas);
                    mHolder.unlockCanvasAndPost(canvas);
                }
            } finally {
                mLock.unlock();
            }
            long diffTime = System.currentTimeMillis() - startTime;
            while (diffTime <= mTimeInFrame) {
                diffTime = System.currentTimeMillis() - startTime;
                Thread.yield();
            }
        }
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        drawProgressbar(canvas);
    }

    private int mPrePrepareCount = 0;
    private int mPrePrepareScalePiexlPerFrame = 10;

    private void drawProgressbar(Canvas canvas) {
        if (mStatus == STATUS_PREPARE) {
            RectF leftCircleRect = new RectF(0, 0, mHeight, mHeight);
            canvas.drawArc(leftCircleRect, 90, 180, true, mFgPaint);

            RectF centerRect = new RectF(mHeight / 2f, 0, mWidth - mHeight / 2f, mHeight);
            canvas.drawRect(centerRect, mFgPaint);

            RectF rightCircleRect = new RectF(mWidth - mHeight, 0, mWidth, mHeight);
            canvas.drawArc(rightCircleRect, -90, 180, true, mFgPaint);

            drawProgressTextLable(canvas);

        } else if (mStatus == STATUS_PREPARING_SPREAD) {
            if (mPrePrepareCount * mPrePrepareScalePiexlPerFrame >= mWidth - mHeight) {

            } else {
                mPrePrepareCount++;

            }

        } else if (mStatus == STATUS_RUNNING) {
            RectF leftCircleRect = new RectF(0, 0, mHeight, mHeight);
            canvas.drawArc(leftCircleRect, 90, 180, true, mBgPaint);

            RectF centerRect = new RectF(mHeight / 2f, 0, mWidth - mHeight / 2f, mHeight);
            canvas.drawRect(centerRect, mBgPaint);

            RectF rightCircleRect = new RectF(mWidth - mHeight, 0, mWidth, mHeight);
            canvas.drawArc(rightCircleRect, -90, 180, true, mBgPaint);

            canvas.drawOval(rightCircleRect, mFgPaint);

            drawSplashWaters(canvas);
            drawFinshedBar(canvas);
            drawRotateCircles(canvas);
            drawProgressTextLable(canvas);
        }

    }

    private void drawProgressTextLable(Canvas canvas) {
        canvas.save();
        canvas.translate(mWidth / 2f, mHeight / 2f + mHeight / 7f);

        if (mStatus == STATUS_PREPARE) {
            canvas.drawText(mPrepareText, 0, 0, mIdPaint);
        } else if (mStatus == STATUS_RUNNING) {
            canvas.drawText(mProgress + "%", 0, 0, mIdPaint);
        }
        canvas.restore();
    }

    private int mSplashWatersRunningFrameCount = 0;

    private void drawSplashWaters(Canvas canvas) {
        canvas.save();
        canvas.translate(0, mHeight / 2f);

        boolean allOut = false;

        for (int i = 0; i < 4; i++) {
            float ratio = (float) (Math.sin(mSplashWatersRunningFrameCount * mWaterMovePerPiexlFrame * 2f / mSplashWatersStepInterval * Math.PI) + 1) / 2;
            float baseMoveX = mSplashWatersRunningFrameCount * mWaterMovePerPiexlFrame - (mHeight / 3.6f - ratio * 4) * i;

            if (baseMoveX < 0) {
                continue;
            }

            float moveX = mWidth - mHeight - baseMoveX;
            float moveY = (float) Math.sin(baseMoveX * 2f / mSplashWatersStepInterval * Math.PI) * (mHeight / 2f - mHeight / 8f);
            boolean isIn = checkInBarScope(moveX, moveY, mHeight / 8f);
            allOut = allOut || isIn;
            if (isIn) {
                float radius = mHeight / 8f;
                if (i > 0) {
                    radius = radius - (4 - i) * radius / 6;
                }
                canvas.drawCircle(moveX, moveY, radius, mFgPaint);
            }
        }

        canvas.restore();

        mSplashWatersRunningFrameCount++;
        if (!allOut) {
            mSplashWatersRunningFrameCount = 0;
        }
    }


    /**
     * 检查水滴是否在进度条范围内
     *
     * @param moveX
     * @param moveY
     * @param watersRadius
     * @return
     */
    private boolean checkInBarScope(float moveX, float moveY, float watersRadius) {
        if (moveX < 0 || Math.abs(moveY) > mHeight / 2f) {
            return false;
        }

        if (moveX > mHeight / 2f) {
            return true;
        }

        return Math.sqrt((mHeight / 2f - moveX) * (mHeight / 2f - moveX) + moveY * moveY) + watersRadius <= mHeight / 2f;
    }

    private void drawFinshedBar(Canvas canvas) {
        if (mProgress == 0) {
            return;
        }

        float shouldDrawBarWidth = mWidth / 100f * mProgress;
        if (shouldDrawBarWidth <= mHeight / 2) {
            RectF leftCircleRect = new RectF(0, 0, mHeight, mHeight);
            float v = (float) Math.toDegrees(Math.acos((mHeight / 2f - shouldDrawBarWidth) / (mHeight / 2f)));
            canvas.drawArc(leftCircleRect, 90 + 90 - v, 2 * v, false, mFgPaint);
        } else if (shouldDrawBarWidth <= (mWidth - mHeight / 2)) {
            RectF leftCircleRect = new RectF(0, 0, mHeight, mHeight);
            canvas.drawArc(leftCircleRect, 90, 180, true, mFgPaint);

            RectF centerRect = new RectF(mHeight / 2f, 0, shouldDrawBarWidth, mHeight);
            canvas.drawRect(centerRect, mFgPaint);
        } else {
            RectF leftCircleRect = new RectF(0, 0, mHeight, mHeight);
            canvas.drawArc(leftCircleRect, 90, 180, true, mFgPaint);

            RectF centerRect = new RectF(mHeight / 2f, 0, mWidth - mHeight / 2f, mHeight);
            canvas.drawRect(centerRect, mFgPaint);
        }
    }

    private int mRotateCounter = 0;

    private Interpolator mRotateInterpolator = new Interpolator() {
        @Override
        public float getInterpolation(float input) {
            return input * input * (2 - 1 * input);
        }
    };

    private void drawRotateCircles(Canvas canvas) {
        mRotateCounter++;

        for (int i = 0; i < 4; i++) {
            drawRotateCircle(canvas, i, 100 + 360 * mRotateInterpolator.getInterpolation(mRotateCounter * mRotateDegreePerFrame % 360f / 360));
        }

        if (mRotateCounter * mRotateDegreePerFrame % 360 == 0) {
            mRotateCounter = 0;
        }
    }

    private void drawRotateCircle(Canvas canvas, int index, float initialDegree) {
        canvas.save();
        canvas.translate(mWidth - mHeight / 2f, mHeight / 2f);
        canvas.rotate(index == 0 ? initialDegree : initialDegree - 70 * index + 25 * (index - 1));
        canvas.drawCircle(0, mHeight / 4f, (float) (mHeight / 8f * (Math.pow(0.7, index))), mIdPaint);
        canvas.restore();
    }


    private int mProgress = 10;

    /**
     * @param progress from 0~100
     */
    public void setProgress(int progress) {
        if (progress >= 0 && progress <= 100) {
            mProgress = progress;
            if (mStatus == STATUS_PREPARING_FOLD) {
                mStatus = STATUS_PREPARING_SPREAD;
            }
        }
    }

    /**
     * 预备
     */
    public void prepare() {
//        mStatus = STATUS_PREPARE;

    }

    /**
     * 开始显示进度条
     */
    public void start() {
//        if (mStatus == STATUS_PREPARE || mStatus == STATUS_PAUSE) {
//            mStatus = STATUS_PREPARING_FOLD;
//        }
    }

    /**
     * 暂停
     */
    public void pause() {
        mStatus = STATUS_PAUSE;
    }
}
