package com.ivyiot.playback;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * created by yeyewu on 2019/10/15
 */
public class DateLine extends View {
    private static final String TAG = "DateLine";
    /**
     * 手势滑动控制器
     */
    private Scroller mScroller;

    private float mDensity;
    private int mMinVelocity;
    /**
     * 文字画笔 灰色#646464
     */
    private TextPaint mTextPaint;
    /**
     * 有录像的标识
     */
    private Paint mDotPaint;

    /**
     * 控件宽度
     */
    private int mWidth;

    /**
     * 控件高度
     */
    private int mHeight;

    /**
     * 字体大小(dp)
     */
    private int mTextSize = 12;

    /**
     * 日期字符串的宽度
     */
    private int mStrWith;
    /**
     * 日期字符串的高度
     */
    private int mStrHeigh;

    /**
     * 文字底部位置
     */
    private int mTextBottom;

    /**
     * 文字间距
     */
    private int mTextMargin;

    /**
     * 日期字符串个数
     */
    private int mShowedCount = 7;

    /**
     * 初始日期
     */
    private CustomDateCalendar mShowDate;
    /**
     * 当前日期
     */
    private CustomDateCalendar mChosenDate;

    /**
     * 套餐时长(天)
     */
    private int mTotalCount = 30;


    /**
     * 当前指示的日期角标（30天中的第几天）
     */
    private int chosenIndex = mTotalCount - 1;


    private String[] dateStrs;
    private SimpleDateFormat mFormatter;

    private int mDevide;


    /**
     * 速度追踪器
     */
    private VelocityTracker mVelocityTracker;

    private int mLineOffest;

    private List mDataList = new ArrayList();

    /**
     * 日期改变监听
     */
    private OnDateChangeListener mOnDateChangeListener;

    private int touchSlop;
    /**
     * 一天的秒数
     */
    private static final long MILLISECONDS_PER_DAY = 3600*24*1000;

    public DateLine(Context context) {
        this(context, null);
    }

    public DateLine(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        getAttrs(context, attrs);
        init(context);
    }

    public DateLine(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // TODO:获取自定义属性
        getAttrs(context, attrs);
        init(context);
    }
    /**
     * 得到属性值
     *
     * @param context context
     * @param attrs   attrs
     */
    int dateTextNorColor,dateTextSelectColor,dateDotColor;
    private void getAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DateLine);
            dateTextNorColor = ta.getColor(R.styleable.DateLine_dateTextNorColor, 0);
            dateTextSelectColor = ta.getColor(R.styleable.DateLine_dateTextSelectColor, 0);
            dateDotColor = ta.getColor(R.styleable.DateLine_dateDotColor, 0);
            ta.recycle();
        }

    }
    /**
     *
     */
    private void init(Context context) {
        mScroller = new Scroller(getContext());
        mDensity = getContext().getResources().getDisplayMetrics().density;
        mMinVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();

        mTextPaint = new TextPaint();
        mTextPaint.setTextSize(mTextSize * mDensity);
        mTextPaint.setColor(dateTextNorColor);

        mDotPaint = new Paint();
        mDotPaint.setStrokeWidth(1 * mDensity);
        mDotPaint.setColor(dateDotColor);

        mShowDate = new CustomDateCalendar();

        mChosenDate = new CustomDateCalendar();
        dateStrs = new String[mTotalCount];
        mFormatter = new SimpleDateFormat("MM/dd", Locale.US);
        getDateStrs2(dateStrs);

        Rect rect = new Rect();
        mTextPaint.getTextBounds(dateStrs[0], 0, dateStrs[0].length(), rect);

        mStrWith = rect.width();
        mStrHeigh = rect.height();
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    private void getDateStrs2(String[] dateStrs) {
        Date date = new Date();
        for (int i = mTotalCount - 1; i >= 0; i--) {
            dateStrs[i] = mFormatter.format(date);
            date.setTime(date.getTime() - MILLISECONDS_PER_DAY);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mWidth = getWidth();
        mHeight = getHeight();
        mTextMargin = (mWidth - mShowedCount * mStrWith) / mShowedCount;
        mDevide = mTextMargin + mStrWith;
        mTextBottom = mHeight / 2 + mStrHeigh / 2;
        super.onLayout(changed, left, top, right, bottom);
    }

    private RectF mRectF = new RectF();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mTextPaint.setColor(dateTextSelectColor);
        int drawOffset = 0, xPosition = 0;

        for (int i = 0; drawOffset <= mWidth / 2; i++) {
            xPosition = mWidth / 2 + i * mDevide - mLineOffest;
            if (xPosition + getPaddingRight() < mWidth && chosenIndex + i < mTotalCount) {
                canvas.drawText(dateStrs[chosenIndex + i], xPosition - mStrWith / 2, mTextBottom, mTextPaint);
                int year = dateStrs[chosenIndex + i].startsWith("12") && dateStrs[mTotalCount - 1].startsWith("01") ? mShowDate.year - 1 : mShowDate.year;
                String[] split = dateStrs[chosenIndex + i].split("/");
                String dateStr = year + split[0] + split[1];
                Log.d(TAG, "dateStr=" + dateStr);
                if (mDataList.contains(dateStr)) {
                    float radius = mDensity * 1;
                    mRectF.set(xPosition - radius, mTextBottom + mDensity * 9,
                            xPosition + radius, mTextBottom + mDensity * 11);
                    canvas.drawOval(mRectF, mDotPaint);
                }
                if (i == 0) {
                    mTextPaint.setColor(dateTextNorColor);
                }
            }
            if (i != 0) {
                xPosition = mWidth / 2 - i * mDevide - mLineOffest;
                if (xPosition - getPaddingLeft() >= 0 && chosenIndex - i >= 0) {
                    canvas.drawText(dateStrs[chosenIndex - i], xPosition - mStrWith / 2, mTextBottom, mTextPaint);
                    int year = dateStrs[chosenIndex - i].startsWith("12") && dateStrs[mTotalCount - 1].startsWith("01") ? mShowDate.year - 1 : mShowDate.year;
                    String[] split = dateStrs[chosenIndex - i].split("/");
                    String dateStr = year + split[0] + split[1];
                    if (mDataList.contains(dateStr)) {
                        float radius = mDensity * 1;
                        mRectF.set(xPosition - radius, mTextBottom + mDensity * 9,
                                xPosition + radius, mTextBottom + mDensity * 11);
                        canvas.drawOval(mRectF, mDotPaint);
                    }
                }

            }
            drawOffset += mDevide;
        }

    }

    int lastPosition;
    float clickDown = 0;
    long downStartTime;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int xPosition = (int) event.getX();

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downStartTime = System.currentTimeMillis();
                clickDown = event.getX();
                mScroller.forceFinished(true);
                mLineOffest = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                mLineOffest += (lastPosition - xPosition);
                offsetToValue();

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                float disX = event.getX() - clickDown;
                long downPeriod = System.currentTimeMillis() - downStartTime;
                downStartTime = 0;
                if (Math.abs(disX) < touchSlop && downPeriod < 100) {
                    performClick(event.getX());
                    return true;
                }
                if (countVelocityTracker(event)) {
                    mLineOffest = 0;
                    return false;
                }
                offsetEndValue();
                if (mOnDateChangeListener != null) {
                    notifyDateChange();
                }
                break;

            default:
                break;
        }
        lastPosition = xPosition;
        return true;
    }

    private void performClick(float x) {
        int index = (int) (x / mDevide + 1);
        int offset = index - 4;
        int tempChosen = chosenIndex + offset;
        lastPosition = 0;
        mLineOffest = 0;
        if (tempChosen >= 0 && tempChosen < mTotalCount) {
            //  mScroller.startScroll(0,0,-offset*mDevide+mDevide/4,0);
            chosenIndex = tempChosen;
            postInvalidate();
            if (mOnDateChangeListener != null)
                notifyDateChange();
        }
    }

    private void offsetToValue() {

        int tValue = mLineOffest / mDevide;
        //滑动的距离超过一个格子
        if (Math.abs(tValue) > 0) {
            chosenIndex += tValue;
            mLineOffest -= tValue * mDevide;
            if (chosenIndex < 0) {
                chosenIndex = 0;
                if (tValue < 0) {
                    mScroller.forceFinished(true);
                    if (null != mOnDateChangeListener)
                        notifyDateChange();
                }
                mLineOffest = 0;
            }
            if (chosenIndex > mTotalCount - 1) {
                chosenIndex = mTotalCount - 1;
                if (tValue > 0) {
                    mScroller.forceFinished(true);
                    if (null != mOnDateChangeListener)
                        notifyDateChange();
                }
                mLineOffest = 0;
            }
        }
        postInvalidate();
    }

    private void offsetEndValue() {
        int roundMove;
        roundMove = mLineOffest / mDevide;
        chosenIndex += roundMove;
        lastPosition = 0;
        mLineOffest = 0;
        chosenIndex = chosenIndex < 0 ? 0 : chosenIndex;
        chosenIndex = chosenIndex > mTotalCount - 1 ? mTotalCount - 1 : chosenIndex;
        postInvalidate();
    }

    private boolean countVelocityTracker(MotionEvent event) {
        //1s内运动了多少个像素
        mVelocityTracker.computeCurrentVelocity(1000);
        float xVelocity = mVelocityTracker.getXVelocity();
        if (Math.abs(xVelocity) > mMinVelocity) {
            //转入computeScroll计算时 mScroller.getCurrX()初始值为0
            lastPosition = 0;
            mScroller.fling(0, 0, (int) xVelocity, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
            return true;
        }
        return false;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        //动画执行完成后会返回true
        if (mScroller.computeScrollOffset()) {
            //over
            if (mScroller.getCurrX() == mScroller.getFinalX()) {
                offsetEndValue();
                if (mOnDateChangeListener != null) {
                    notifyDateChange();
                }
            } else {
                int xPosition = mScroller.getCurrX();
                mLineOffest += (lastPosition - xPosition);
                offsetToValue();
                lastPosition = xPosition;
            }
        }
    }

    private void notifyDateChange() {
        int year = dateStrs[chosenIndex].startsWith("12") && dateStrs[mTotalCount - 1].startsWith("01") ? mShowDate.year - 1 : mShowDate.year;
        String[] splits = dateStrs[chosenIndex].split("/");
        if (mChosenDate.day != Integer.parseInt(splits[1]) || mChosenDate.month != Integer.parseInt(splits[0]) ||
                mChosenDate.year != year) {
            mChosenDate.setYear(year);
            mChosenDate.setMonth(Integer.parseInt(splits[0]));
            mChosenDate.setDay(Integer.parseInt(splits[1]));
            mOnDateChangeListener.onDateChange(mChosenDate);
        }
    }

    public void setDataList(List<String> arrayList) {
        if (arrayList == null) {
            return;
        }
        this.mDataList = arrayList;
        invalidate();
    }

    public void setOnDateChangeListener(OnDateChangeListener listener) {
        if (null != listener) {
            this.mOnDateChangeListener = listener;
        }
    }

    public void setChosenDay(CustomDateCalendar currDate) {
        int i = mTotalCount - 1;
        while (i >= 0) {
            if (currDate.month == Integer.parseInt(dateStrs[i].split("/")[0])
                    && currDate.day == Integer.parseInt(dateStrs[i].split("/")[1])) {
                chosenIndex = i;
                mChosenDate.setYear(currDate.year);
                mChosenDate.setMonth(currDate.month);
                mChosenDate.setDay(currDate.day);
                invalidate();
                return;
            }
            i--;
        }
    }

    public interface OnDateChangeListener {

        void onDateChange(CustomDateCalendar dateCalendar);
    }

    /**
     * 更新日期栏
     *
     * @param dateJson
     */
    public void updateDateLine(String dateJson, String chooseDate) {
        List<String> datelist = analyseCloudDateData(dateJson);
        setDataList(datelist);
        setChosenDay(CalendarUtils.setChoseDayIsOutDate(chooseDate));
    }
    /**
     * 云录像日期列表
     */
    public static final String DATE_LIST = "dateList";

    //解析日期
    public List<String> analyseCloudDateData(String dateJson) {
        List<String> dateList = new ArrayList<>();
        try {
            JSONObject resultJson = new JSONObject(dateJson);
            if (isResultCodeCorrect(resultJson)) {

                if (!resultJson.isNull(DATE_LIST)) {
                    String dateListResult = resultJson.getString(DATE_LIST);
                    if (!TextUtils.isEmpty(dateListResult)) {
                        JSONArray msgArr = resultJson.getJSONArray(DATE_LIST);
                        if (msgArr.length() > 0) {
                            for (int i = 0; i < msgArr.length(); i++) {
                                String getDate = (String) msgArr.get(i);
                                if (getDate.length() == 8) {
                                    if (!dateList.contains(getDate)) {
                                        dateList.add(getDate);
                                    }
                                } else if (getDate.length() > 8) {
                                    String date = DateAndTimeUtils.cloudDateToUTC(getDate);
                                    if (!TextUtils.isEmpty(date) && !dateList.contains(date)) {
                                        dateList.add(date);
                                    }
                                }
                            }
                        }
                    }
                }
                if (dateList.size() > 0) {
                    for (int i = 0; i < dateList.size(); i++) {
                        // 20150801
                        String date_now = dateList.get(i);
                        Log.d(TAG, "最后解析得到的时间为:  " + date_now);
                        if (!dateList.contains(date_now)) {
                            dateList.add(date_now);
                        }
                    }
                }

            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return dateList;
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

}
