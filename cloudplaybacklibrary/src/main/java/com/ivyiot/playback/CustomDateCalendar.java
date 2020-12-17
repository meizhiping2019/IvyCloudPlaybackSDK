package com.ivyiot.playback;

import java.io.Serializable;
import java.util.Calendar;

public class CustomDateCalendar implements Serializable {

    private static final long serialVersionUID = 1L;
    public int year;
    public int month;
    public int day;
    public int week;

    public CustomDateCalendar(int year, int month, int day) {
        if (month > 12) {
            month = 1;
            year++;
        } else if (month < 1) {
            month = 12;
            year--;
        }
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public CustomDateCalendar() {
        this.year = DateUtilCalendar.getYear();
        this.month = DateUtilCalendar.getMonth();
        this.day = DateUtilCalendar.getCurrentMonthDay();
    }

    public static CustomDateCalendar modifiDayForObject(CustomDateCalendar date, int day) {
        CustomDateCalendar modifiDate = new CustomDateCalendar(date.year, date.month, day);
        return modifiDate;
    }

    public long getStartTime() {
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.YEAR, year);
        todayStart.set(Calendar.MONTH,month-1);
        todayStart.set(Calendar.DAY_OF_MONTH, day);
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MILLISECOND, 0);
        return todayStart.getTimeInMillis() / 1000;
    }

    @Override
    public String toString() {
        return year + "-" + month + "-" + day;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }
}
