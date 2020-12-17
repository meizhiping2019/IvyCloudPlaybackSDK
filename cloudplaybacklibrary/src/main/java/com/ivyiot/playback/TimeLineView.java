package com.ivyiot.playback;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import static com.ivyiot.playback.DateAndTimeUtils.stringForTime;
import static com.ivyiot.playback.TimeLineView.ScaleMode.MODE_12_SEC;
import static com.ivyiot.playback.TimeLineView.ScaleMode.MODE_6_MIN;

/**
 * 云录像回放时间轴
 */
public class TimeLineView extends View {

    private static final String TAG = "TimeLineView";
    /**
     * 每一小格的时间间隔(s)
     */
    public static final int NOR_SCALE_TIME_INTERVAL = 60;
    public static final int MID_SCALE_TIME_INTERVAL = 10;
    public static final int MAX_SCALE_TIME_INTERVAL = 2;
    /**
     * 每一小格的距离
     */
    private float mNorLineDivider = 0;
    /**
     * 当前使用的时间间隔
     */
    private int mCurrentTimeInterval = NOR_SCALE_TIME_INTERVAL;
    /**
     * 当前使用的格间距
     */
    private float mCurrentLineDivider;

    /***
     * 时间刻度画笔
     */
    private Paint longShortPaint;
    /***
     * 透明画笔
     */
    private Paint transPaint;
    /***
     * 文字画笔
     */
    private Paint textPaint;
    /***
     * 有云录像的画笔
     */
    private Paint videoPaint;
    /***
     * 中间白线的画笔
     */
    private Paint whitePaint;

    private float mDensity;
    /***
     * 当前时间 精确到秒
     */
    private long mTimeValue;

    /**
     * 一天最大秒数
     */
    private int mMaxValue = 24 * 3600;


    private int mLastTimeBarX, mTimeBarMove;
    private int mWidth;

    private int mMinVelocity;
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    private ITimeLineListener scru_listener;

    /***
     * 云录像列表
     */
    private HashMap<String, List<IvyVideo>> cloudListMap;
    /***
     * timeline最左边的value值
     */
    private int leftestValue = 0;
    /***
     * timeline最左边的position
     */
    private float leftestPosition = 0;

    private float longStart;
    private float longEnd;
    private float shortStart;
    private float shortEnd;
    private float textHeight;
    /***
     * 保留精确时间
     */
    private long mAccurateTime = 0L;
    /***
     * 当前播放进度
     */
    private float mProgressDis;
    /**
     * 当前播放录像需要渲染的格数
     */
    private float mCurrentDuration;
    /**
     * 当前日期起始时间
     */
    private long mCurrentTimeStart;
    /**
     * 当前时间轴渲染总格数
     */
    private float mTotalDiv;
    /**
     * 时间轴右边可见最大时间点
     */
    private long mVisibleMaxTimeRight;
    /**
     * 时间轴左边可见最小时间点
     */
    private long mVisibleMinTimeLeft;
    private SimpleDateFormat mFormatter;
    private Date mDate;

    /**
     * 时间轴状态
     */
    private enum State {
        START, MOVE, END
    }

    /**
     * 当前的伸缩模式
     */
    private int mScaleMode = MODE_6_MIN;


    /**
     * 最后一个云录像结束时间
     */

    private long mLastestVideoEndTime;



    public TimeLineView(Context context) {
        super(context);
        initPaint();
    }

    public TimeLineView(Context context, AttributeSet attr) {
        super(context, attr, 0);
        getAttrs(context, attr);
        initPaint();

    }

    public TimeLineView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        // TODO:获取自定义属性
        getAttrs(context, attr);
        initPaint();
    }

    public void setTimeLineListener(ITimeLineListener listen) {
        this.scru_listener = listen;
    }

    /**
     * 得到属性值
     *
     * @param context context
     * @param attrs   attrs
     */
    int timeScaleColor,timeTextColor,cloudVideoNorColor,middleLineColor;
    private void getAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TimeLineView);
        timeScaleColor = ta.getColor(R.styleable.TimeLineView_timeScaleColor, 0);
        timeTextColor = ta.getColor(R.styleable.TimeLineView_timeTextColor, 0);
        cloudVideoNorColor = ta.getColor(R.styleable.TimeLineView_cloudVideoNorColor, 0);
        middleLineColor = ta.getColor(R.styleable.TimeLineView_middleLineColor, 0);
        ta.recycle();
    }


    private void initPaint() {
        mScroller = new Scroller(getContext());
        mDensity = getContext().getResources().getDisplayMetrics().density;
        mMinVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();


        //画竖线的画笔 灰色#05afeb
        longShortPaint = new Paint();
        longShortPaint.setStrokeWidth(1 * mDensity);
        longShortPaint.setColor(timeScaleColor);


        //文字画笔 灰色#646464
        textPaint = new TextPaint();
        textPaint.setTextSize(11 * mDensity);
        textPaint.setColor(timeTextColor);

        //中间小刻度 透明
        transPaint = new TextPaint();
        transPaint.setTextSize(16 * mDensity);
        transPaint.setColor(Color.TRANSPARENT);

        //有录像的标识
        videoPaint = new Paint();
        videoPaint.setStrokeWidth(1 * mDensity);
        videoPaint.setColor(cloudVideoNorColor);

        whitePaint = new Paint();
        whitePaint.setStrokeWidth(1 * mDensity);
        whitePaint.setColor(middleLineColor);

        mFormatter = new SimpleDateFormat("HH:mm", Locale.US);
        mDate = new Date(0);
        setPortraitScreen();
    }

    /**
     * @param defaultValue 初始值
     */
    public void initViewParam(int screenWidth, long defaultValue) {
        mNorLineDivider = screenWidth / 6 / 20;//一屏幕120分钟 120个格子(60s/格)
        mCurrentLineDivider = mNorLineDivider;
        mTimeValue = defaultValue;
        invalidate();
        mLastTimeBarX = 0;
        mTimeBarMove = 0;
        mProgressDis = 0;
        notifyScrubState(State.MOVE);
    }

    /***
     * 更新时间轴位置
     */
    public void updatePosition(long defaultValue) {
        if (withProgress()) {
            mAccurateTime = defaultValue;
            mTimeValue = mAccurateTime;
            mProgressDis = 0;

        } else {
            mAccurateTime = defaultValue;
            int tempValue = DateAndTimeUtils.timeToSecond(getDateString(defaultValue));
            if (mScaleMode == ScaleMode.MODE_72_MIN) {
                mTimeValue = tempValue / 12;
            } else {
                mTimeValue = tempValue;
            }
        }
        mCurrentTimeStart = mAccurateTime - DateAndTimeUtils.timeToSecond(DateAndTimeUtils.getDateString("HH:mm:ss", 1000 * mAccurateTime));
        invalidate();
    }

    /***
     * 获取当前值
     */
    public long getValue() {
        return mTimeValue;
    }

    public void setValue(long timeValue) {
        mTimeValue = timeValue;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mWidth = getWidth();
        super.onLayout(changed, left, top, right, bottom);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mScaleMode != ScaleMode.MODE_72_MIN) {
            drawNorScalePart(canvas);
        } else {
            drawMinestScaleLine(canvas);
        }
    }


    /***
     * 从中间往两边开始画刻度线
     */
    private void drawNorScalePart(Canvas canvas) {
        canvas.save();
        int drawCount = 0;
        mTotalDiv = 0;
        mTimeValue -= mTimeValue % mCurrentTimeInterval;
        mVisibleMaxTimeRight = mTimeValue + (int) ((mWidth / 2 + mTimeBarMove + mProgressDis) / mCurrentLineDivider * mCurrentTimeInterval);
        mVisibleMinTimeLeft = mTimeValue - (int) ((mWidth / 2 - mTimeBarMove - mProgressDis) / mCurrentLineDivider * mCurrentTimeInterval);

        for (int i = 0; drawCount <= mWidth / 2 + mProgressDis; i++) {
            float xPosition = (mWidth / 2 - mTimeBarMove - mProgressDis) + i * mCurrentLineDivider;
            if (xPosition + getPaddingRight() < mWidth) {
                //画右边
                long resultRight = mTimeValue + i * mCurrentTimeInterval;
                float div;
                long rightMax;
                if (withProgress()) {
                    div = calculateDrawProgressDiv(resultRight);
                    rightMax = mMaxValue + mCurrentTimeStart;
                } else {
                    div = calculateDrawDiv((int) resultRight);
                    rightMax = mMaxValue;
                }
                if (div > 0) {
                    mTotalDiv += div;
                    canvas.drawRect(xPosition, 0, xPosition + div * mCurrentLineDivider, getHeight(), videoPaint);
                }
                if (resultRight <= rightMax) {
                    drawDivideLine(canvas, xPosition, resultRight);
                }
            }
            //画左边
            if (i != 0) {//防止重绘
                xPosition = (mWidth / 2 - mTimeBarMove - mProgressDis) - i * mCurrentLineDivider;
                if (xPosition >= getPaddingLeft()) {
                    float div;
                    long leftMin;
                    long resultLeft = (int) (mTimeValue - i * mCurrentTimeInterval);
                    if (withProgress()) {
                        leftestValue = (int) mTimeValue - (int) ((mWidth / 2 - mTimeBarMove - mProgressDis) / mCurrentLineDivider * mCurrentTimeInterval);
                        div = calculateDrawProgressDiv(resultLeft);
                        leftMin = mCurrentTimeStart;
                    } else {
                        leftestValue = (int) resultLeft;
                        div = calculateDrawDiv(leftestValue);
                        leftMin = 0;
                    }
                    leftestPosition = xPosition;
                    if (div > 0) {
                        mTotalDiv += div;
                        canvas.drawRect(xPosition, 0, xPosition + div * mCurrentLineDivider, getHeight(), videoPaint);
                    }
                    if (resultLeft >= leftMin) {
                        drawDivideLine(canvas, xPosition, resultLeft);
                    }
                }
            }
            drawCount += mCurrentLineDivider;
        }
        canvas.drawLine(mWidth / 2, 0, mWidth / 2, getHeight(), whitePaint);

        float div = calculateInvisibleDiv();
        if (div > 0) {
            mTotalDiv += div;
            if (withProgress()) {
                canvas.drawRect(0, 0, div * mCurrentLineDivider, getHeight(), videoPaint);
            } else {
                canvas.drawRect(leftestPosition, 0, leftestPosition + div * mCurrentLineDivider, getHeight(), videoPaint);
            }
        }
        canvas.restore();
    }

    private float calculateInvisibleDiv() {
        float skip = 0;
        if (null != cloudListMap) {
            if (withProgress()) {
                for (Entry<String, List<IvyVideo>> entry : cloudListMap.entrySet()) {

                    for (IvyVideo cloudVideo : entry.getValue()) {
                        long startleft = cloudVideo.getStartTime();
                        long endleft = cloudVideo.getEndTime();
                        if (leftestValue >= startleft && leftestValue <= endleft) {
                            if (0 != mVisibleMaxTimeRight && endleft > mVisibleMaxTimeRight) {
                                skip = (float) (mVisibleMaxTimeRight - leftestValue) / mCurrentTimeInterval;
                            } else
                                skip = (float) (endleft - leftestValue) / mCurrentTimeInterval;

                            changeVideoBg(cloudVideo.getVideoType());
                            return skip;
                        }
                    }
                }
            } else {
                for (Entry<String, List<IvyVideo>> entry : cloudListMap.entrySet()) {
                    if (leftestValue < DateAndTimeUtils.timeToSecond(entry.getKey())) {
                        continue;
                    }
                    for (IvyVideo cloudVideo : entry.getValue()) {
                        int startleft = DateAndTimeUtils.timeToSecond(getDateString(cloudVideo.getStartTime()));
                        int endleft = DateAndTimeUtils.timeToSecond(getDateString(cloudVideo.getEndTime()));
                        if (leftestValue > startleft && leftestValue < endleft) {
                            long time = endleft - leftestValue;
                            skip = time / mCurrentTimeInterval;
                            if (cloudListMap.entrySet().size() == 1) {
                                if (skip == 0)
                                    skip = 1;
                            }
                            changeVideoBg(entry.getValue().get(0).getVideoType());
                            return skip;
                        }
                    }

                }
            }

        }
        return skip;
    }

    private void drawDivideLine(Canvas canvas, float xPosition, long timeValue) {
        String key;
        if (timeValue > mMaxValue) {
            key = getDateString(timeValue);
        } else {
            key = stringForTime(String.valueOf(timeValue));
        }
        if (isDrawLongLine(timeValue)) {//画竖长线
            canvas.drawLine(xPosition, longStart, xPosition, longEnd, longShortPaint);
            canvas.drawText(key, xPosition + 5, textHeight, textPaint);
        } else if (isDrawShortLine(timeValue)) {//画竖短线
            canvas.drawLine(xPosition, shortStart, xPosition, shortEnd, longShortPaint);
        } else {
            // canvas.drawLine(xPosition, 0, xPosition, 2, transPaint);
        }
    }

    private float calculateDrawDiv(int timeValue) {
        String key = stringForTime(String.valueOf(timeValue));
        float skip = 0;
        if (null != cloudListMap && cloudListMap.containsKey(key)) {
            if (null != cloudListMap.get(key)) {
                int minLeftStart = 0;
                int maxLeftEnd = 0;
                long lastDuration = 0;
                EVideoType type = null;
                for (IvyVideo cloud : cloudListMap.get(key)) {
                    int leftstart = DateAndTimeUtils.timeToSecond(getDateString(cloud.getStartTime()));
                    long duration = cloud.getEndTime() - cloud.getStartTime();
                    if (lastDuration == 0) {
                        lastDuration = duration;
                        type = cloud.getVideoType();
                    } else if (duration > lastDuration) {
                        lastDuration = duration;
                        type = cloud.getVideoType();
                    }
                    if (0 == minLeftStart) {
                        minLeftStart = leftstart;
                    } else {
                        if (minLeftStart > leftstart) {
                            minLeftStart = leftstart;
                        }
                    }
                    int leftend = DateAndTimeUtils.timeToSecond(getDateString(cloud.getEndTime()));
                    if (0 == maxLeftEnd) {
                        maxLeftEnd = leftend;
                    } else {
                        if (maxLeftEnd < leftend) {
                            maxLeftEnd = leftend;
                        }
                        if (0 != mVisibleMaxTimeRight && mVisibleMaxTimeRight < maxLeftEnd) {
                            maxLeftEnd = (int) mVisibleMaxTimeRight;
                        }
                    }
                }
                changeVideoBg(type);
                int time = maxLeftEnd - minLeftStart;
                skip = time / mCurrentTimeInterval;
                if (skip == 0)
                    skip = 1;
            }
        }
        return skip;
    }

    private float calculateDrawProgressDiv(long timeValue) {
        String key = getDateString(timeValue);
        float skip = 0;
        if (null != cloudListMap && cloudListMap.containsKey(key)) {
            if (null != cloudListMap.get(key)) {
                boolean shouldDraw = false;
                for (IvyVideo cloud : cloudListMap.get(key)) {
                    if (cloud.getStartTime() >= timeValue && cloud.getStartTime() < timeValue + mCurrentTimeInterval) {
                        shouldDraw = true;
                        if (0 != mVisibleMaxTimeRight && mVisibleMaxTimeRight < cloud.getEndTime()) {
                            skip += (float) (mVisibleMaxTimeRight - cloud.getStartTime()) / mCurrentTimeInterval;
                        } else
                            skip += (float) (cloud.getEndTime() - cloud.getStartTime()) / mCurrentTimeInterval;
                        changeVideoBg(cloud.getVideoType());
                    }
                }
                if (skip == 0 && shouldDraw) {
                    skip = 1;
                }
            }
        }
        return skip;
    }

    /**
     * 修改录像画笔颜色
     */
    private void changeVideoBg(EVideoType type) {
        if(null != scru_listener){
            scru_listener.onSetCloudVideoColor(type);
        }
    }


    /***
     * 画缩放到最小的刻度线
     */
    private void drawMinestScaleLine(Canvas canvas) {
        canvas.save();
        int drawCount = 0;
        videoPaint.setColor(cloudVideoNorColor);
        for (int i = 0; drawCount <= mWidth / 2; i++) {
            float xPosition = (mWidth / 2 - mTimeBarMove) + i * mNorLineDivider;
            if (xPosition + getPaddingRight() < mWidth) { //画右边
                int resultRight = (int) (mTimeValue + i * NOR_SCALE_TIME_INTERVAL);

                int newResultRight = resultRight * 12;

                String newResultKey = stringForTime(String.valueOf(newResultRight)).substring(0, 4);

                if (null != cloudListMap) {
                    for (Object o : cloudListMap.entrySet()) {
                        Entry entry = (Entry) o;
                        String key = (String) entry.getKey();
                        if (key.substring(0, 4).equals(newResultKey)) {
                            List<IvyVideo> cloud = (List<IvyVideo>) entry.getValue();
                            long sss = DateAndTimeUtils.timeToSecond(getDateString(cloud.get(0).getStartTime()));
                            long eee = DateAndTimeUtils.timeToSecond(getDateString(cloud.get(0).getEndTime()));
                            long time = eee - sss;
                            float skip = time / NOR_SCALE_TIME_INTERVAL;
                            if (skip == 0)
                                skip = 1;
                            canvas.drawRect(xPosition, 0, xPosition + skip * mNorLineDivider / 6, getHeight(), videoPaint);
                            break;
                        }
                    }
                }

                if (mTimeValue + i <= mMaxValue && resultRight <= 2 * 3600 + 300) {
                    if (isDrawLongLine(resultRight)) {//画竖长线
                        canvas.drawLine(xPosition, longStart, xPosition, longEnd, longShortPaint);
                        if (resultRight <= 2 * 3600) {
                            String newtime = stringForTime(String.valueOf(resultRight * 12));
                            canvas.drawText(newtime, xPosition + 5, textHeight, textPaint);
                        }
                    } else if (isDrawShortLine(resultRight)) {//画竖短线
                        canvas.drawLine(xPosition, shortStart, xPosition, shortEnd, longShortPaint);
                    } else {
                        canvas.drawLine(xPosition, 0, xPosition, 2, transPaint);
                    }
                }
            }
            //画左边
            if (i != 0) {//防止重绘
                xPosition = (mWidth / 2 - mTimeBarMove) - i * mNorLineDivider;
                if (xPosition > getPaddingLeft()) {
                    mTimeValue -= mTimeValue % 60;
                    leftestValue = (int) (mTimeValue - i * NOR_SCALE_TIME_INTERVAL);

                    int newResultRight = leftestValue * 12;

                    String newResultKey = stringForTime(String.valueOf(newResultRight)).substring(0, 4);

                    if (null != cloudListMap && newResultRight > 0) {
                        for (Object o : cloudListMap.entrySet()) {
                            Entry entry = (Entry) o;
                            String key = (String) entry.getKey();
                            if (key.substring(0, 4).equals(newResultKey)) {
                                List<IvyVideo> cloud = (List<IvyVideo>) entry.getValue();
                                long sss = DateAndTimeUtils.timeToSecond(getDateString(cloud.get(0).getStartTime()));
                                long eee = DateAndTimeUtils.timeToSecond(getDateString(cloud.get(0).getEndTime()));
                                long time = eee - sss;
                                float skip = time / NOR_SCALE_TIME_INTERVAL;
                                if (skip == 0)
                                    skip = 1;
                                canvas.drawRect(xPosition, 0, xPosition + skip * mNorLineDivider / 6, getHeight(), videoPaint);
                                break;
                            }
                        }
                    }

                    if (mTimeValue - i >= 0 && newResultKey.length() < 6 && !newResultKey.contains("-")) {
                        if (isDrawLongLine(leftestValue)) {//画竖长线
                            canvas.drawLine(xPosition, longStart, xPosition, longEnd, longShortPaint);
                            if (leftestValue < 2 * 3600) {
                                String newtime = stringForTime(String.valueOf(leftestValue * 12));
                                canvas.drawText(newtime, xPosition + 5, textHeight, textPaint);
                            }
                        } else if (isDrawShortLine(leftestValue)) {//画竖短线
                            canvas.drawLine(xPosition, shortStart, xPosition, shortEnd, longShortPaint);
                        } else {
                            canvas.drawLine(xPosition, 0, xPosition, 2, transPaint);
                        }
                    }
                }
            }
            drawCount += mNorLineDivider;
        }
        canvas.drawLine(mWidth / 2, 0, mWidth / 2, getHeight(), whitePaint);
        canvas.restore();
    }

    /***
     * 画竖长线
     */
    private boolean isDrawLongLine(long currTime) {
        if (mScaleMode == MODE_12_SEC) {
            return currTime % 60 == 0;
        } else if (mScaleMode == ScaleMode.MODE_72_MIN) {
            return currTime % (30 * 60) == 0;
        } else if (mScaleMode == MODE_6_MIN) {

            return currTime % (30 * 60) == 0;
        } else if (mScaleMode == ScaleMode.MODE_1_MIN) {
            return currTime % (60 * 60) == 0 || currTime % (5 * 60) == 0;
        }


        return false;
    }

    /***
     * 画竖短线
     */
    private boolean isDrawShortLine(long currTime) {
        if (mScaleMode == MODE_12_SEC) {
            return currTime % (12) == 0;
        } else if (mScaleMode == ScaleMode.MODE_72_MIN) {
            return currTime % (6 * 60) == 0;
        } else if (mScaleMode == MODE_6_MIN) {
            return currTime % (6 * 60) == 0;
        } else if (mScaleMode == ScaleMode.MODE_1_MIN) {
            return currTime % (60) == 0;
        }

        return false;
    }


    //双指刚刚按下的距离
    float startDis = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int xPosition = (int) event.getX();
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        //将事件加入到VelocityTracker类实例中
        mVelocityTracker.addMovement(event);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN://5
                mScroller.forceFinished(true);
                startDis = getSpace(event);
                break;
            case MotionEvent.ACTION_POINTER_UP://6
                mCurrentLineDivider = mNorLineDivider;
                float result = getSpace(event);
                if (result < startDis) {
                    zoomIn();
                } else {
                    zoomOut();
                }
                break;
            case MotionEvent.ACTION_DOWN://0
                mScroller.forceFinished(true);
                mLastTimeBarX = xPosition;
                mTimeBarMove = 0;
                notifyScrubState(State.START);
                break;
            case MotionEvent.ACTION_MOVE://2
                if (event.getPointerCount() > 1) {
                    //时间轴的缩放处理
                    float ratio = getSpace(event) / startDis;
                    if (ratio > 1 && mScaleMode == MODE_12_SEC) {
                        return true;
                    }
                    mCurrentLineDivider = mNorLineDivider * ratio;
                    postInvalidate();
                    return true;
                }
                mTimeBarMove += (mLastTimeBarX - xPosition);
                changeMoveAndValue();
                notifyScrubState(State.MOVE);
                break;
            case MotionEvent.ACTION_UP://1
            case MotionEvent.ACTION_CANCEL://3
                if (countVelocityTracker(event)) {
                    mLastTimeBarX = 0;
                    return false;
                }
                countMoveEnd();
                notifyScrubState(State.END);
                return false;
            default:
                break;
        }
        mLastTimeBarX = xPosition;
        return true;
    }

    /***
     * 获取手指两点之间的距离
     */
    private float getSpace(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /***
     * 缩小
     */
    private void zoomIn() {
        if (mScaleMode == ScaleMode.MODE_72_MIN) {
            return;
        } else if (mScaleMode == MODE_12_SEC) {
            mAccurateTime = mTimeValue;
            modifyModeParams(ScaleMode.MODE_1_MIN);
        } else if (mScaleMode == ScaleMode.MODE_1_MIN) {
            modifyModeParams(MODE_6_MIN);
            mAccurateTime = mTimeValue;
            mTimeValue = DateAndTimeUtils.timeToSecond(getDateString(mTimeValue));

        } else if (mScaleMode == MODE_6_MIN) {
            mTimeValue = mTimeValue / 12;
            modifyModeParams(ScaleMode.MODE_72_MIN);
        }
        mLastTimeBarX = 0;
        mTimeBarMove = 0;
        mProgressDis = 0;
        postInvalidate();
    }

    /***
     * 放大
     */
    private void zoomOut() {
        if (mScaleMode == MODE_12_SEC) {
            return;
        }
        if (mScaleMode == ScaleMode.MODE_1_MIN) {
            mTimeValue = mAccurateTime;
            modifyModeParams(MODE_12_SEC);
        } else if (mScaleMode == ScaleMode.MODE_72_MIN) {
            mTimeValue = mTimeValue * 12;
            modifyModeParams(MODE_6_MIN);
        } else if (mScaleMode == MODE_6_MIN) {
            mTimeValue = mAccurateTime;
            modifyModeParams(ScaleMode.MODE_1_MIN);
        }
        mLastTimeBarX = 0;
        mTimeBarMove = 0;
        mProgressDis = 0;
        postInvalidate();
    }

    /***
     * 如果是最小刻度，开始播放时要恢复到中间刻度
     */
    public void minToMiddle(long playTime) {
        if (mScaleMode == ScaleMode.MODE_72_MIN) {
            mAccurateTime = playTime;
            mTimeValue = DateAndTimeUtils.timeToSecond(getDateString(playTime));
            modifyModeParams(MODE_6_MIN);
            postInvalidate();
        }
    }

    private boolean countVelocityTracker(MotionEvent event) {
        //1s内运动了多少个像素
        mVelocityTracker.computeCurrentVelocity(1000);
        float xVelocity = mVelocityTracker.getXVelocity();
        if (Math.abs(xVelocity) > mMinVelocity) {
            mScroller.fling(0, 0, (int) xVelocity, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
            return true;
        }
        return false;
    }

    /***
     * 计算滑动的时间
     */
    private void changeMoveAndValue() {

        int tValue = (int) (mTimeBarMove / (mCurrentLineDivider));
        //滑动的距离超过一个格子
        if (Math.abs(tValue) > 0) {
            mTimeValue += tValue * mCurrentTimeInterval;
            mTimeBarMove -= tValue * mCurrentLineDivider;
            long temValue;
            if (withProgress()) {
                temValue = mTimeValue - mCurrentTimeStart;
            } else {
                temValue = mTimeValue;
            }
            if (temValue <= 0 || temValue > mMaxValue) {
                if (withProgress()) {
                    mTimeValue = temValue <= 0 ? mCurrentTimeStart : (mCurrentTimeStart + mMaxValue);
                } else if (mScaleMode == ScaleMode.MODE_72_MIN) {
                    mTimeValue = mTimeValue <= 0 ? 0 : 2 * 3600;
                } else {
                    mTimeValue = mTimeValue <= 0 ? 0 : mMaxValue;
                }
                mTimeBarMove = 0;
                mScroller.forceFinished(true);
            }
        }
        postInvalidate();
    }


    private void countMoveEnd() {
        int roundMove;
        if (withProgress()) {
            roundMove = Math.round(mTimeBarMove / (mCurrentLineDivider));
            int shortTime = DateAndTimeUtils.timeToSecond(getDateString(mTimeValue));
            mTimeValue += roundMove * mCurrentTimeInterval;
            mLastTimeBarX = 0;
            mTimeBarMove = 0;
            mTimeValue = mTimeValue <= 0 ? 0 : mTimeValue;
            mTimeValue = shortTime > mMaxValue ? (mTimeValue - (shortTime - mMaxValue)) : mTimeValue;
        } else {
            roundMove = Math.round(mTimeBarMove / (mNorLineDivider));
            mTimeValue = mTimeValue + roundMove;
            mTimeValue = mTimeValue <= 0 ? 0 : mTimeValue;
            mTimeValue = mTimeValue > mMaxValue ? mMaxValue : mTimeValue;
            mLastTimeBarX = 0;
            mTimeBarMove = 0;
        }
        postInvalidate();
    }


    private void notifyScrubState(State state) {
        if (null == scru_listener)
            return;
        switch (state) {
            case START:
                scru_listener.onTimeScrollStart();
                break;
            case MOVE:
                scru_listener.onTimeScrollMove(mTimeValue);
                break;
            case END:
                if (mScaleMode == ScaleMode.MODE_72_MIN) {
                    mTimeValue = mTimeValue * 12;
                }
                if (mScaleMode == MODE_12_SEC || mScaleMode == ScaleMode.MODE_1_MIN) {
                    Log.e(TAG, "onScrubbingEnd------------>" + mTimeValue);
                    scru_listener.onTimeScrollEnd(DateAndTimeUtils.timeToSecond(getDateString(mTimeValue)));
                } else {
                    Log.e(TAG, "onScrubbingEnd------------>" + mTimeValue);
                    scru_listener.onTimeScrollEnd(mTimeValue);
                }
                break;
        }

    }

    /**
     * 获取当前时间轴位置对于的秒数 用于录像列表与时间轴切换
     *
     * @return
     */
    public long getCurrentTimeValue() {
        if (mScaleMode == ScaleMode.MODE_72_MIN) {
            return mTimeValue * 12;
        }
        if (mScaleMode == MODE_12_SEC || mScaleMode == ScaleMode.MODE_1_MIN) {
            return DateAndTimeUtils.timeToSecond(getDateString(mTimeValue));
        }
        return mTimeValue;
    }

    /**
     * 执行ontouch或invalidate(）或postInvalidate()都会导致这个方法的执行
     * 手快速滑动后，抬起手不会调用MotionEvent.ACTION_UP，滑动过程computeScrollOffset返回false，滑动结束后返回true
     * 慢滑抬手时，会调用MotionEvent.ACTION_UP，滑动过程中及结束后computeScrollOffset返回false
     * 即onTouchEvent和该方法不会起冲突
     */
    @Override
    public void computeScroll() {
        super.computeScroll();
        //动画执行完成后会返回true
        if (mScroller.computeScrollOffset()) {
            //over
            if (mScroller.getCurrX() == mScroller.getFinalX()) {
                countMoveEnd();
                notifyScrubState(State.END);
            } else {
                int xPosition = mScroller.getCurrX();
                mTimeBarMove += (mLastTimeBarX - xPosition);
                changeMoveAndValue();
                mLastTimeBarX = xPosition;
                notifyScrubState(State.MOVE);
            }
        }
    }

    /***
     * 设置录像列表
     */
    public void setCloudVideoMap(HashMap<String, List<IvyVideo>> videoMap) {
        this.cloudListMap = videoMap;
        modifyModeParams(ScaleMode.MODE_1_MIN);
        invalidate();
    }


    /*72分钟的时间间隔*/
    //	private void setSevTwoSnap(){
    //		mNorLineDivider = Global.screenWidth/1440;//1440个格子
    //	}

    /*竖屏的线间距*/
    public void setPortraitScreen() {
        longStart = 15 * mDensity;
        longEnd = 60 * mDensity;
        shortStart = 25 * mDensity;
        shortEnd = 50 * mDensity;
        textHeight = 70 * mDensity;
    }

    /*横屏的线间距*/
    public void setLandscapeScreen() {
        longStart = 10 * mDensity;
        longEnd = 40 * mDensity;
        shortStart = 16 * mDensity;
        shortEnd = 37 * mDensity;
        textHeight = 46 * mDensity;
    }

    /**
     * @return 是否处于最大刻度显示模式
     */
    public boolean isMaxScaleMode() {
        return mScaleMode == MODE_12_SEC;
    }

    /**
     * @param duration 当前播放录像所占据的格数(格/2s)
     *                 只在最大刻度时生效
     */
    public void setDuration(float duration) {
        mCurrentDuration = duration;
    }

    /**
     * @param progress 当前播放的录像进度
     *                 时间轴会根据进度和长度向左进行偏移
     */
    public void setProgress(float progress) {
        if (withProgress()) {
            mProgressDis = progress * mCurrentDuration * mCurrentLineDivider;
            invalidate();
        } else {
            mProgressDis = 0;
        }
    }

    public interface ScaleMode {
        int MODE_72_MIN = 0;
        int MODE_6_MIN = 1;
        int MODE_1_MIN = 2;
        int MODE_12_SEC = 3;
    }

    public int getScaleMode() {
        return mScaleMode;
    }

    /**
     * @param mode 当前时间轴模式
     */
    private void modifyModeParams(int mode) {
        switch (mode) {
            case ScaleMode.MODE_1_MIN:
                mScaleMode = ScaleMode.MODE_1_MIN;
                mCurrentTimeInterval = MID_SCALE_TIME_INTERVAL;
                break;
            case MODE_6_MIN:
                mScaleMode = MODE_6_MIN;
                mCurrentTimeInterval = NOR_SCALE_TIME_INTERVAL;
                break;
            case MODE_12_SEC:
                mScaleMode = MODE_12_SEC;
                mCurrentTimeInterval = MAX_SCALE_TIME_INTERVAL;
                break;
            case ScaleMode.MODE_72_MIN:
                mScaleMode = ScaleMode.MODE_72_MIN;
                mCurrentLineDivider = mNorLineDivider;
                mCurrentTimeInterval = NOR_SCALE_TIME_INTERVAL;
                break;

            default:
                break;
        }
    }

    /**
     * @return 是否实时更新时间轴(目前只有MODE_12_SEC, MODE_1_MIN两种模式下会实时更新)
     */
    public boolean withProgress() {
        return mScaleMode == MODE_12_SEC || mScaleMode == ScaleMode.MODE_1_MIN;
    }

    /**
     * @param startTime 录像起始时间
     * @param endTime   录像结束时间
     *                  设置时间轴当前录像长度,设置播放录像段时必须重新设置
     * @see #updatePosition
     */
    public void calculateDuration(long startTime, long endTime) {

        if (withProgress()) {
            mCurrentDuration = (float) (endTime - startTime) / mCurrentTimeInterval;
        }
    }

    /**
     * 设置最后一个录像的结束时间
     */
    public void setLastestVideoEndTime(long lastestVideoEndTime) {
        mLastestVideoEndTime = lastestVideoEndTime;
    }

    /**
     * 判断时间轴空隙是否超过5%
     */
    public boolean invalidDuration() {
        float invalid = 0f;
        String playDate = DateAndTimeUtils.getDateString("yyyy-MM-dd", 1000 * mAccurateTime);
        String currentDate = DateAndTimeUtils.getDateString("yyyy-MM-dd");
        if (!withProgress()) {
            mVisibleMaxTimeRight += mCurrentTimeStart;
            mVisibleMinTimeLeft += mCurrentTimeStart;
        }
        //非当天录像，有效时间段为0-24小时,当天录像，有效时间段为0到最后一个录像的结束时间
        if (!playDate.equals(currentDate)) {
            if (mCurrentTimeStart > mVisibleMinTimeLeft) {
                invalid = (mCurrentTimeStart - mVisibleMinTimeLeft) / mCurrentTimeInterval;

            } else if (mVisibleMaxTimeRight > (mCurrentTimeStart + 3600 * 24)) {
                invalid = (mVisibleMaxTimeRight - mCurrentTimeStart + 3600 * 24) / mCurrentTimeInterval;

            }
        } else {
            if (mCurrentTimeStart > mVisibleMinTimeLeft && mLastestVideoEndTime < mVisibleMaxTimeRight) {
                invalid = (mCurrentTimeStart - mVisibleMinTimeLeft + mVisibleMaxTimeRight - mLastestVideoEndTime) / mCurrentTimeInterval;

            } else if (mCurrentTimeStart > mVisibleMinTimeLeft) {
                invalid = (mCurrentTimeStart - mVisibleMinTimeLeft) / mCurrentTimeInterval;

            } else if (mLastestVideoEndTime < mVisibleMaxTimeRight) {
                invalid = (mVisibleMaxTimeRight - mLastestVideoEndTime) / mCurrentTimeInterval;

            }
        }
        return invalid + mTotalDiv < 120 * 0.95;
    }

    private String getDateString(long timeMillis) {
        try {
            if (null != mDate && null != mFormatter) {
                mDate.setTime(timeMillis * 1000);
                return mFormatter.format(mDate);
            }
        } catch (Exception ex) {
            Log.e(TAG, "getDateString(String,long) method exception:" + ex.getMessage());
        }
        return "";
    }

    /**
     * 设置云视频颜色
     * @param resId
     */
    public void setCloudVideoColor(int resId){
        if(null != videoPaint){
            videoPaint.setColor(getResources().getColor(resId));
        }
    }



    /**
     * 返回码
     */
    private static final String RETURN_CODE = "errorCode";
    /**
     * 返回码描述
     */
    private static final String RETURN_DESC = "failureDetails";

    /**
     * 云平台返回值returnCode是不是为空
     */
    private static boolean isResultCodeCorrect(JSONObject resultJson) {
        try {
            if (null != resultJson && !TextUtils.isEmpty(resultJson.toString())) {
                String returnCode = resultJson.getString(RETURN_CODE);
                if (TextUtils.isEmpty(returnCode)) {
                    return true;
                } else {
                    Log.e(TAG, resultJson.getString(RETURN_DESC));
                    return false;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 云录像列表
     */
    public static final String RECORD_LIST = "recordList";
    //解析云录像时间段
    public List<IvyVideo> analyseCloudTimeLineData(String timeJson, long mStartTime) {
        List<IvyVideo> cloudList = new ArrayList<>();
        try {
            JSONObject resultJson = new JSONObject(timeJson);
            if (isResultCodeCorrect(resultJson)) {
                if (!resultJson.isNull(RECORD_LIST)) {
                    JSONArray msgArr = resultJson.getJSONArray(RECORD_LIST);
                    if (msgArr.length() > 0) {
                        for (int i = 0; i < msgArr.length(); i++) {
                            IvyVideo cloud = new IvyVideo();
                            if (msgArr.getJSONArray(i).length() == 3) {
                                //剔除无效数据
                                if (msgArr.getJSONArray(i).getLong(0) < mStartTime) {
                                    continue;
                                }
                                cloud.setStartTime(msgArr.getJSONArray(i).getLong(0));
                                cloud.setEndTime(msgArr.getJSONArray(i).getLong(1));
                                cloud.setVideoType(EVideoType.getCloudVideoType(msgArr.getJSONArray(i).getInt(2)));
                            }
                            if (cloudList.size() == 0) {
                                cloudList.add(cloud);
                            } else {
                                IvyVideo lastCloud = cloudList.get(cloudList.size() - 1);
                                if (cloud.getStartTime() >= lastCloud.getStartTime() && cloud.getStartTime() < lastCloud.getEndTime()) {
                                    lastCloud.setStartTime(lastCloud.getStartTime());
                                    lastCloud.setEndTime(cloud.getEndTime() >= lastCloud.getEndTime() ? cloud.getEndTime() : lastCloud.getEndTime());
                                } else if (cloud.getStartTime() >= lastCloud.getStartTime() && cloud.getEndTime() < lastCloud.getEndTime()) {
                                    lastCloud.setStartTime(cloud.getStartTime());
                                    lastCloud.setEndTime(lastCloud.getEndTime());
                                    //continue;
                                } else if (cloud.getStartTime() == lastCloud.getStartTime() && cloud.getEndTime() > lastCloud.getEndTime()) {
                                    lastCloud.setStartTime(cloud.getStartTime());
                                    lastCloud.setEndTime(cloud.getEndTime());
                                    //continue;
                                } else if (cloud.getStartTime() > lastCloud.getStartTime() && cloud.getStartTime() < lastCloud.getEndTime() && cloud.getEndTime() > lastCloud.getEndTime()) {
                                    lastCloud.setStartTime(lastCloud.getStartTime());
                                    lastCloud.setEndTime(cloud.getEndTime());
                                    //continue;
                                } else {
                                    cloudList.add(cloud);
                                }
                            }
                        }
                    }
                    Log.d(TAG, "cloudlist-----size------->" + cloudList.size());

                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return cloudList;
    }
    /**
     * 将云录像列表整合到HashMap中，用于时间轴匹配
     */

    public HashMap<String, List<IvyVideo>> analyseRecodeList(List<IvyVideo> cloud_list) {
        HashMap<String, List<IvyVideo>> recodeMap = new HashMap<>();
        for (int i = 0; i < cloud_list.size(); i++) {
            IvyVideo cloud = cloud_list.get(i);
            String key = DateAndTimeUtils.getDateString("HH:mm", 1000 * cloud.getStartTime());
            if (!TextUtils.isEmpty(key)) {
                if (recodeMap.containsKey(key)) {
                    if (null != recodeMap.get(key)) {
                        recodeMap.get(key).add(cloud);
                    }
                } else {
                    ArrayList<IvyVideo> cloudll = new ArrayList<IvyVideo>();
                    cloudll.add(cloud);
                    recodeMap.put(key, cloudll);
                }
            }
        }
        return recodeMap;
    }
    /**
     * 更新时间栏
     */
    private List<IvyVideo> cloudList;
    public void setTimeLineData(HashMap<String, List<IvyVideo>> cloudListMap, List<IvyVideo> cloudList) {
        Collections.sort(cloudList, new IvyVideo());
        this.cloudList = cloudList;
        if (cloudList.size() > 0) {
            setLastestVideoEndTime(cloudList.get(cloudList.size() - 1).getEndTime());
            calculateDuration(cloudList.get(cloudList.size() - 1).getStartTime(), cloudList.get(cloudList.size() - 1).getEndTime());
            updatePosition(cloudList.get(cloudList.size() - 1).getStartTime());
            postInvalidate();
        }
        if (null != cloudListMap) {
            setCloudVideoMap(cloudListMap);
        }
    }

    /**
     * 设置时间栏位置
     * @param current_time
     * @param currDate
     */
    public IvyVideo convertToIvyVideo(long current_time, CustomDateCalendar currDate) {//cloudList.get(cloudList.size() - 1).getEndTime()
        IvyVideo currCloudVideo = null;
        boolean hasTime = false;
        long playTime = dateToSecond(DateAndTimeUtils.secForTime(current_time), currDate);
        if (null != cloudListMap && null != cloudList) {
            String getKey = DateAndTimeUtils.secForTime(current_time);
            if (cloudListMap.containsKey(getKey)) {
                hasTime = true;
                List<IvyVideo> list = cloudListMap.get(getKey);
                if (null != list && list.size() > 0) {
                    if (list.size() > 1) {
                        long timeOne = list.get(0).getEndTime() - list.get(0).getStartTime();
                        long timeTwo = list.get(1).getEndTime() - list.get(1).getStartTime();
                        playTime = timeTwo > timeOne ? list.get(1).getStartTime() : list.get(0).getStartTime();
                        long endTime = timeTwo > timeOne ? list.get(1).getEndTime() : list.get(0).getEndTime();
                        calculateDuration(playTime, endTime);
                        currCloudVideo = timeTwo > timeOne ? list.get(1) : list.get(0);
                    } else if (list.size() == 1) {
                        playTime = list.get(0).getStartTime();
                        calculateDuration(playTime, list.get(0).getEndTime());
                        currCloudVideo = list.get(0);
                    }
                }
            } else {
                for (Map.Entry<String, List<IvyVideo>> entry : cloudListMap.entrySet()) {
                    if (entry.getValue().size() == 1 && current_time >= DateAndTimeUtils.timeToSecond(entry.getKey()) && current_time <= DateAndTimeUtils.timeToSecond(DateAndTimeUtils.getDateString("HH:mm", 1000 * entry.getValue().get(0).getEndTime()))) {
                        hasTime = true;
                        calculateDuration(playTime, entry.getValue().get(0).getEndTime());
                        currCloudVideo = entry.getValue().get(0);
                        break;
                    } else if (entry.getValue().size() > 1 && current_time >= DateAndTimeUtils.timeToSecond(entry.getKey())) {
                        for (IvyVideo cloud : entry.getValue()) {
                            if (current_time <= DateAndTimeUtils.timeToSecond(DateAndTimeUtils.getDateString("HH:mm", 1000 * cloud.getEndTime()))) {
                                hasTime = true;
                                calculateDuration(playTime, cloud.getEndTime());
                                currCloudVideo = cloud;
                                break;
                            }
                        }
                        if (hasTime) {
                            break;
                        }
                    }
                }
            }
            // 就近播放
            if (cloudList.size() > 0 && !hasTime) {
                // 往前播放
                if (cloudList.get(cloudList.size() - 1).getStartTime() < playTime) {
                    // 时间轴最后面
                    hasTime = true;
                    playTime = cloudList.get(cloudList.size() - 1).getStartTime();
                    calculateDuration(playTime, cloudList.get(cloudList.size() - 1).getEndTime());
                    currCloudVideo = cloudList.get(cloudList.size() - 1);
                } else if (cloudList.get(0).getStartTime() > playTime) {
                    // 时间轴最前面
                    hasTime = true;
                    playTime = cloudList.get(0).getStartTime();
                    calculateDuration(playTime, cloudList.get(0).getEndTime());
                    currCloudVideo = cloudList.get(0);
                } else {
                    // 就近播放
                    for (int i = 0; i < cloudList.size(); i++) {
                        long nearbytime = DateAndTimeUtils.timeToSecond(DateAndTimeUtils.getDateString("HH:mm", 1000 * cloudList.get(i).getStartTime()));
                        if (nearbytime > current_time) {
                            hasTime = true;
                            long tempTimeNext = cloudList.get(i).getStartTime();
                            if (i > 1) {
                                long tempTime = cloudList.get(i - 1).getStartTime();
                                playTime = Math.abs(tempTimeNext - playTime) > Math.abs(tempTime - playTime) ? tempTime : tempTimeNext;
                                currCloudVideo = Math.abs(tempTimeNext - playTime) > Math.abs(tempTime - playTime) ? cloudList.get(i - 1) : cloudList.get(i);
                                calculateDuration(playTime, Math.abs(tempTimeNext - playTime) > Math.abs(tempTime - playTime) ? cloudList.get(i - 1).getEndTime() : cloudList.get(i).getEndTime());
                            } else {
                                playTime = tempTimeNext;
                                calculateDuration(playTime, cloudList.get(i).getEndTime());
                                currCloudVideo = cloudList.get(i);
                            }
                            break;
                        }
                    }
                }
            }
            if (hasTime && playTime != 0){
                updatePosition(playTime);
            }
        }
        return currCloudVideo;
    }




    /**
     * 将日期转换为UTC秒数
     */
    private long dateToSecond(String dateFormat, CustomDateCalendar currDate) {
        StringBuilder sb = new StringBuilder();
        sb.append(currDate.year);
        sb.append("-");
        int month = currDate.month;
        if (month < 10) {
            sb.append("0");
        }
        sb.append(month);
        sb.append("-");
        if (currDate.day < 10) {
            sb.append("0");
        }
        sb.append(currDate.day);
        sb.append(" ");
        sb.append(dateFormat);
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
        Date date = null;
        try {
            date = sf.parse(sb.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (null != date) {
            return date.getTime() / 1000;
        }
        return 0;
    }

    /**
     * 更新时间轴
     */
    public void updateTimeLine(int position, int duration) {
        // 如果播放时滑动时间轴，则不刷新时间轴
        float progress = (float) position / duration;
        Log.d(TAG, "currentPosition----------->>" + position + ",,during------------------>>>" + duration + ",,prpgress------------->>" + progress);
        setProgress(progress);
        if (!isMaxScaleMode() && getScaleMode() != TimeLineView.ScaleMode.MODE_1_MIN) {
            final long mValue = 1605603660 + position / 1000;
            String time = DateAndTimeUtils.getDateString("yyyy-MM-dd HH:mm:ss", 1000 * mValue);
            //iv_pause.setBackground(getResources().getDrawable(R.drawable.a_sel_cloud_video_play));
            if (time.endsWith("00")) {
                updatePosition(mValue);
            }
        }
    }

}
