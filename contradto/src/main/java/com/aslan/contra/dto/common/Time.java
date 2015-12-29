package com.aslan.contra.dto.common;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * A generic representation of YYY-MM-DD:HH.MM.SS
 * <p>
 * Created by gobinath on 12/22/15.
 */
public class Time implements Serializable {

    private int year;

    @Min(1)
    @Max(12)
    private int month;

    @Min(1)
    @Max(31)
    private int day;

    @Min(0)
    @Max(23)
    private int hour;

    @Min(0)
    @Max(59)
    private int minute;

    @Min(0)
    @Max(59)
    private int second;

    public Time() {

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

    public static Time of(int year, int month, int day) {
        return of(year, month, day, 0, 0, 0);
    }

    public static Time of(int year, int month, int day, int hour, int minute, int second) {
        Time time = new Time();
        time.year = year;
        time.month = month;
        time.day = day;
        time.hour = hour;
        time.minute = minute;
        time.second = second;

        return time;
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
