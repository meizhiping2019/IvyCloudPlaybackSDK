package com.ivyiot.playback;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static java.lang.Long.parseLong;

public class DateAndTimeUtils {
    private static final String TAG = "DateAndTimeUtils";

    /**
     * 根据日期格式，返回手机当前的日期/时间字符串
     *
     * @param dateFormat e.g. yyyy-MM-dd ; yyyyMMdd HH:mm:ss ; etc.
     * @return 当前日期/时间的字符串
     */
    public static String getDateString(String dateFormat) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(dateFormat, Locale.US);
            return format.format(new Date());
        } catch (Exception ex) {
            Log.e(TAG, "getDateString(String) method exception:" + ex.getMessage());
        }
        return "";
    }

    /**
     * 根据日期格式和time(毫秒)，返回日期/时间字符串
     *
     * @param dateFormat e.g. yyyy-MM-dd ; yyyyMMdd HH:mm:ss ; etc.
     * @param timeMillis 时间对应的毫秒数
     * @return 日期/时间的字符串
     */
    public static String getDateString(String dateFormat, long timeMillis) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.US);
            Date date = new Date(timeMillis);
            return formatter.format(date);
        } catch (Exception ex) {
            Log.e(TAG, "getDateString(String,long) method exception:" + ex.getMessage());
        }
        return "";
    }


    /**
     * 获取相册文件时间显示 美国时间
     */
    private static String getUSDateDisplay(String currDate) {
        if (TextUtils.isEmpty(currDate)) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date date;
        try {
            date = sdf.parse(currDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.US);

        return sdf.format(date);
    }

    /**
     * 获取推送年月日
     */
    public static String specificDate(String currDate) {
        if (TextUtils.isEmpty(currDate)) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        Date date;
        try {
            date = sdf.parse(currDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        return sdf.format(date);
    }

    /**
     * 获取推送年月日
     */
    public static String specificDate1(String currDate) {
        if (TextUtils.isEmpty(currDate)) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        Date date;
        try {
            date = sdf.parse(currDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.US);

        return sdf.format(date);
    }

    public static Calendar StingtoCalender(String currDate) {
        if (TextUtils.isEmpty(currDate)) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        Date date;
        try {
            date = sdf.parse(currDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        calendar.setTime(date);
        return calendar;
    }

    /**
     * 十二小时制显示
     */
    public static String getTimeAmPmDisplay(String time) {
        if (TextUtils.isEmpty(time)) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        Date date;
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        sdf = new SimpleDateFormat("hh:mm:ss a", Locale.US);
        return sdf.format(date);
    }

    /**
     * 十二小时制显示
     */
    public static String getTimeAmPmDisplay(String format, String time) {
        if (TextUtils.isEmpty(time)) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        Date date;
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        sdf = new SimpleDateFormat("hh:mm:ss a", Locale.US);
        return sdf.format(date);
    }

    /**
     * 二十四小时制显示
     */
    public static String getTimeNormalDisplay(String format, String time) {
        if (TextUtils.isEmpty(time)) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        Date date;
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);
        return sdf.format(date);
    }

    /**
     * 得到昨天的日期
     */
    private static String getYestoryDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(calendar.getTime());
    }

    /**
     * 获取手机当前时间(ms)
     **/
    public static String getMsgDate(String msgTime) {
        String str = "";
        long time;
        if (!TextUtils.isEmpty(msgTime)) {
            time = parseLong(msgTime);
            if (msgTime.length() == 10) {
                time = time * 1000;
            }
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            Date curDate = new Date(time);
            str = formatter.format(curDate);
        }
        Log.i(TAG, "MsgDate str = " + str);
        return str;
    }

    /**
     * 把long解析为对应的时间 ，总是一个开始时间、一个结束时间，有偶数个元素
     */
    public static ArrayList<String> transformAlertPlanTime(long mplanTime) {
        StringBuilder strPlanTime = new StringBuilder(Long.toBinaryString(mplanTime));
        try {
            // 48位不全，补全位数
            if (strPlanTime.length() < 48) {
                for (int i = 0; i < 48 - strPlanTime.length(); i++) {
                    strPlanTime.insert(0, "0");
                }
            }
            // strPlanTime字符串翻转
            String tempStrPlanTime = strPlanTime.toString();
            char[] arr = tempStrPlanTime.toCharArray();
            int length = tempStrPlanTime.length();
            for (int i = 0; i < length / 2; i++) {
                char temp = arr[i];
                arr[i] = arr[length - i - 1];
                arr[length - i - 1] = temp;
            }
            strPlanTime = new StringBuilder(String.valueOf(arr));

            ArrayList<String> planTime = new ArrayList<>();
            for (int i = 0; i < strPlanTime.length(); i++) {
                int startIndex = i;
                int endIndex = i + 1;
                if (endIndex == strPlanTime.length()) {
                    if (strPlanTime.charAt(startIndex) == '1') {
                        planTime.add("23:30");
                        planTime.add("24:00");
                    }
                }

                if (strPlanTime.charAt(i) == '1') {
                    String str = "00:00";
                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.US);

                    Calendar startCal, endCal;
                    Date myDate = formatter.parse(str);
                    startCal = Calendar.getInstance();
                    startCal.setTime(myDate);
                    endCal = Calendar.getInstance();
                    endCal.setTime(myDate);

                    for (int j = i + 1; j < strPlanTime.length(); j++) {
                        if (j < strPlanTime.length() && strPlanTime.charAt(j) == '0') {
                            endIndex = j;
                            startCal.add(Calendar.MINUTE, 30 * startIndex);
                            endCal.add(Calendar.MINUTE, 30 * endIndex);
                            if (startIndex == 0) {
                                planTime.add("00:00");
                            } else {
                                planTime.add(formatter.format(startCal.getTime()));
                            }
                            if (endIndex == 47) {
                                if (strPlanTime.charAt(47) == '1') {
                                    planTime.add("24:00");
                                } else {
                                    planTime.add("23:30");
                                }
                            } else {
                                planTime.add(formatter.format(endCal.getTime()));
                            }
                            i = endIndex;
                            break;
                        }
                        if (j == (strPlanTime.length() - 1) && (strPlanTime.charAt(j) == '1')) {
                            if (startIndex == 0) {
                                planTime.add("00:00");
                            } else {
                                startCal.add(Calendar.MINUTE, 30 * startIndex);
                                planTime.add(formatter.format(startCal.getTime()));
                            }
                            planTime.add("24:00");
                            i = j;
                            break;
                        }
                    }
                }
            }
            return planTime;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将当前时间转换为UTC时间 精确到秒
     */
    public static long dateToUTC(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();// 取得本地时间
        cal.set(year, month, day, 0, 0, 0); //此处的month会加1,即想要设置为3月，应该填2月
        long time = cal.getTimeInMillis();// cal.getTimeInMillis()方法所取得的即是UTC标准时间
        return time / 1000;
    }

    /***
     * 将20151007T180012 格式 的时间转化为yyyyMMdd
     */
    @SuppressLint("SimpleDateFormat")
    public static String cloudDateToUTC(String time) {
        if (time.contains("T") && time.length() == 15) {
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.US);
                Date curDate = new Date(df.parse(time).getTime());// 获取当前时间
                return formatter.format(curDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static String getUTCTime() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);
        TimeZone pst = TimeZone.getTimeZone("Etc/GMT+0");
        Date curDate = new Date();
        dateFormatter.setTimeZone(pst);
        return dateFormatter.format(curDate);
    }

    /**
     * 解析精确到秒的时间 转换为 XX:XX格式
     */
    public static String secForTime(long mill) {
        int hour = (int) (mill / 3600);
        int minutes = (int) ((mill / 60) % 60);
        return String.format(Locale.US, "%02d:%02d", hour, minutes);
    }


    /**
     * 将 时:分 格式 的时间转化为秒数
     */
    public static int timeToSecond(String time) {
        int second = 0;
        if (time.contains(":") && time.length() == 5) {
            int hour = Integer.parseInt(time.substring(0, 2)) * 3600;
            int min = Integer.parseInt(time.substring(3)) * 60;
            second = hour + min;
        } else if (time.contains(":") && time.length() == 8) {
            int hour = Integer.parseInt(time.substring(0, 2)) * 3600;
            int min = Integer.parseInt(time.substring(3, 5)) * 60;
            int sec = Integer.parseInt(time.substring(6));
            second = hour + min + sec;
        }
        return second;
    }


    /**
     * 将字符串格式转换成毫秒数
     */
    public static long convertStrToTime(String currDate, String format) {
        long time = 0L;
        try {
            time = new SimpleDateFormat(format, Locale.US).parse(currDate).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }
    /**
     * 解析秒级时间为XX:XX格式
     */
    public static String stringForTime(String mill) {
        int millis = Integer.parseInt(mill);
        int hour = millis / 3600;
        int minutes = (millis / 60) % 60;
        return String.format(Locale.US, "%02d:%02d", hour, minutes);
    }

    public static String getCurrentDate() {

        Date date = new Date(); // this object contains the current date value
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(date);
    }
}