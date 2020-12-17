package com.ivyiot.playback;

import android.view.View;
import android.widget.TextView;


/**
 * 封装calendar用的一些方法
 */
public class CalendarUtils {

    /**
     * 国内日期的正则表达式 2017-04-19
     */
    public static final String DATE_CHINA = "[0-9]{4}-[0-9]{2}-[0-9]{2}";

    /**
     * 国外日期的正则表达式 04-19-2017
     */
    public static final String DATE_NOT_CHINA = "[0-9]{2}-[0-9]{2}-[0-9]{4}";

    public enum SildeDirection {
        RIGHT, LEFT, NO_SILDE
    }



    /*点击左按钮显示的日期*/
    public static CustomDateCalendar getLeftDate(CustomDateCalendar currDateLive) {
        /***  上个月的天数*/
        int lastMonthDays = DateUtilCalendar.getMonthDays(currDateLive.year, currDateLive.month - 1);
        if (currDateLive.day == 1) {
            if (currDateLive.month == 1) {
                currDateLive.year -= 1;
                currDateLive.month = 12;
            } else {
                currDateLive.month -= 1;
            }
            currDateLive.day = lastMonthDays;
        } else {
            currDateLive.day -= 1;
        }
        return currDateLive;
    }


    /*点击右按钮显示的日期*/
    public static CustomDateCalendar getRightDate(CustomDateCalendar currDateLive) {
        int currentMonthDays = DateUtilCalendar.getMonthDays(currDateLive.year, currDateLive.month);
        if (currDateLive.day == currentMonthDays) {
            if (currDateLive.month == 12) {
                currDateLive.year += 1;
                currDateLive.month = 1;
                currDateLive.day = 1;
            } else {
                currDateLive.month += 1;
                currDateLive.day = 1;
            }
        } else {
            currDateLive.day += 1;
        }
        return currDateLive;
    }

    /*日历滑到最低端*/
    public static void scrollToBottom(final View ly_scrollview, final int day) {
        ly_scrollview.post(new Runnable() {
            public void run() {
                if (day > 15) {
                    ly_scrollview.scrollTo(0, ly_scrollview.getHeight());
                } else {
                    ly_scrollview.scrollTo(0, 0);
                }
            }
        });
    }


    /*设置CHOSE_DAY为外面显示的日期*/
    public static CustomDateCalendar setChoseDayIsOutDate(String chooseDate) {
        CustomDateCalendar date = new CustomDateCalendar();
        if (chooseDate.matches(DATE_CHINA)) {//2017-04-17
            date.year = Integer.parseInt(chooseDate.substring(0, 4));
            date.month = Integer.parseInt(chooseDate.substring(5, 7));
            date.day = Integer.parseInt(chooseDate.substring(8));
        } else if (chooseDate.matches(DATE_NOT_CHINA)) {//04-17-2017
            date.year = Integer.parseInt(chooseDate.substring(6));
            date.month = Integer.parseInt(chooseDate.substring(0, 2));
            date.day = Integer.parseInt(chooseDate.substring(3, 5));
        }
        return date;
    }
    public static CustomDateCalendar getChoseDayIsOutDate() {
        String nowDate = DateAndTimeUtils.getCurrentDate();
        CustomDateCalendar date = new CustomDateCalendar();
        if (nowDate.matches(DATE_CHINA)) {//2017-04-17
            date.year = Integer.parseInt(nowDate.substring(0, 4));
            date.month = Integer.parseInt(nowDate.substring(5, 7));
            date.day = Integer.parseInt(nowDate.substring(8));
        } else if (nowDate.matches(DATE_NOT_CHINA)) {//04-17-2017
            date.year = Integer.parseInt(nowDate.substring(6));
            date.month = Integer.parseInt(nowDate.substring(0, 2));
            date.day = Integer.parseInt(nowDate.substring(3, 5));
        }
        return date;
    }



    /*获取两位数的日期*/
    public static String getTwoDigitDate(int md) {
        return md < 10 ? "0" + md : md + "";
    }


    /*小于等于今天的日期*/
    public static boolean isTodayOrBefore(CustomDateCalendar date) {
        return date.year < DateUtilCalendar.getYear()
                || (date.year == DateUtilCalendar.getYear() && date.month < DateUtilCalendar.getMonth())
                || (date.year == DateUtilCalendar.getYear() && date.month == DateUtilCalendar.getMonth() && date.day <= DateUtilCalendar.getCurrentMonthDay());
    }

    public static boolean isToday(CustomDateCalendar date) {
        return date.year == DateUtilCalendar.getYear()
                && date.month == DateUtilCalendar.getMonth()
                && date.day == DateUtilCalendar.getCurrentMonthDay();
    }

    /*根据获取的日期判断dateList中是否包含此日期*/
    public static String getNewDate(String outDate) {
        String nowDate = outDate.replace("-", "");
        if (outDate.matches(DATE_NOT_CHINA)) {//04172017  20170417
            nowDate = nowDate.substring(4) + nowDate.substring(0, 2) + nowDate.substring(2, 4);
        }
        return nowDate;
    }

}
