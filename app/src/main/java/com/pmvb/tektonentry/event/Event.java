package com.pmvb.tektonentry.event;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by pmvb on 17-08-04.
 */

public class Event {
    private String name;
    private Calendar date;
    private int hourOfDay;
    private int minute;
    private LatLng location;

    public Event(String name, Date date, int hourOfDay, int minute, LatLng location) {
        this.setName(name);
        this.setDate(Calendar.getInstance());
        this.getDate().setTime(date);
        this.setHourOfDay(hourOfDay);
        this.setMinute(minute);
        this.setLocation(location);
    }

    public Event(String name, Date date, int hourOfDay, int minute, double latitude, double longitude) {
        this(name, date, hourOfDay, minute, new LatLng(latitude, longitude));
    }

    public Event(String name, Calendar date, int hourOfDay, int minute, LatLng location) {
        this(name, date.getTime(), hourOfDay, minute, location);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public int getHourOfDay() {
        return hourOfDay;
    }

    public void setHourOfDay(int hourOfDay) {
        this.hourOfDay = hourOfDay;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getDateStr() {
        return String.format(
                "%04d-%02d-%02d",
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH)
        );
    }

    public String getTimeStr() {
        return String.format("%02d:%02d", hourOfDay, minute);
    }
}
