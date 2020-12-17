package com.ivyiot.playback;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;



/**
 * @author yeyewu
 * @time 2018/5/4 11:02
 */
public class DownloadButton extends LinearLayout {

    private ImageView mIvIcon;
    private Paint mProgressPaint;
    private boolean isProgressEnable = true;
    private long mProgress = 0;
    private long mMax = 100;
    private String TAG = "DownloadButton";
    private int mThick = 2;

    private int progressColor;
    private int progressBackground;

    public DownloadButton(Context context) {
        super(context);
    }

    @SuppressLint("ResourceAsColor")
    public DownloadButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view;
        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.DownloadButton);
            progressColor = ta.getColor(R.styleable.DownloadButton_progressColor, R.color.light_have_cloud_dot);
            progressBackground = ta.getResourceId(R.styleable.DownloadButton_downloadIcon, R.drawable.a_sel_cloud_video_download_light);
            ta.recycle();
        }
        view = View.inflate(context, R.layout.download_progress_view, this);
        mIvIcon = view.findViewById(R.id.progressView_iv_icon);
        mIvIcon.setBackgroundResource(progressBackground);
        init();
    }

    public void init() {
        mProgressPaint = new Paint();
        mProgressPaint.setStyle(Paint.Style.STROKE);// 绘制边框的形式
        mProgressPaint.setStrokeWidth(mThick);
        mProgressPaint.setColor(progressColor);
        mProgressPaint.setAntiAlias(true);//消除锯齿
    }

    public DownloadButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }

    /**
     * 设置是否允许进度
     */
    public void setIsProgressEnable(boolean isProgressEnable) {
        this.isProgressEnable = isProgressEnable;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);//绘制图标+文本
        if (isProgressEnable) {
            float left = mIvIcon.getLeft() + mThick;
            float top = mIvIcon.getTop() + mThick;
            float right = mIvIcon.getRight() - mThick;
            float bottom = mIvIcon.getBottom() - mThick;
            RectF oval = new RectF(left, top, right, bottom);
            float startAngle = -90;//开始角度
            float sweepAngle = mProgress * 1.0f / mMax * 360;
            boolean useCenter = false;
            canvas.drawArc(oval, startAngle, sweepAngle, useCenter, mProgressPaint);
        }
    }

    /**
     * 设置进度的当前值
     */
    public void setProgress(long progress) {
        mProgress = progress;
        //重绘
        invalidate();
    }

    /**
     * 设置进度的最大值
     */
    public void setMax(long max) {
        mMax = max;
    }


    public void setImgSelected(boolean isSelect){
        mIvIcon.setSelected(isSelect);
    }
}
