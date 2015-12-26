package com.aslan.contra.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

/**
 * A generic representation of YYY-MM-DD:HH.MM
 * <p>
 * Created by gobinath on 12/22/15.
 */
public class Time implements Serializable {
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;

    public Time() {

    }

    public Time(int year, int month, int day) {
        this(year, month, day, 0, 0, 0);
    }

    public Time(int year, int month, int day, int hour, int minute, int second) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
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

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public void truncateToMinutes() {
        this.second = 0;
    }

    public long value() {
        return toDate().getTime();
    }

    public Date toDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, hour, minute, second);
        return calendar.getTime();
    }

    public static Time valueOf(long value) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(value);
        Time time = new Time();
        time.year = calendar.get(Calendar.YEAR);
        time.month = calendar.get(Calendar.MONTH) + 1;
        time.day = calendar.get(Calendar.DATE);
        time.hour = calendar.get(Calendar.HOUR_OF_DAY);
        time.minute = calendar.get(Calendar.MINUTE);
        time.second = calendar.get(Calendar.SECOND);

        return time;
    }

    public static Time now() {
        Date date = new Date();

        return fromDate(date);
    }

    public static Time fromDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return fromCalendar(calendar);
    }

    public static Time fromCalendar(Calendar calendar) {
        Time time = new Time();
        time.year = calendar.get(Calendar.YEAR);
        time.month = calendar.get(Calendar.MONTH) + 1;
        time.day = calendar.get(Calendar.DATE);
        time.hour = calendar.get(Calendar.HOUR_OF_DAY);
        time.minute = calendar.get(Calendar.MINUTE);
        time.second = calendar.get(Calendar.SECOND);

        return time;
    }

    @Override
    public String toString() {
        return year + "-" + month + "-" + day + "_" + hour + ":" + minute;
    }
}
