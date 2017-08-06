package com.pmvb.tektonentry.event;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pmvb on 17-08-04.
 */

public class Event {
    private String name;
    private Calendar date;
    private int hourOfDay;
    private int minute;
    private LatLng location;

    public Event() {
        // Default constructor required for calls to DataSnapshot.getValue(EventDBMapper.class)
    }

    public Event(String name, Date date, int hourOfDay, int minute, LatLng location) {
        this.init(name, date, hourOfDay, minute, location);
    }

    public Event(String name, Date date, int hourOfDay, int minute, double latitude, double longitude) {
        this(name, date, hourOfDay, minute, new LatLng(latitude, longitude));
    }

    public Event(String name, Calendar date, int hourOfDay, int minute, LatLng location) {
        this(name, date.getTime(), hourOfDay, minute, location);
    }

    private void init(String name, Date date, int hourOfDay, int minute, LatLng location) {
        this.setName(name);
        this.setDate(Calendar.getInstance());
        this.getDate().setTime(date);
        this.setHourOfDay(hourOfDay);
        this.setMinute(minute);
        this.setLocation(location);
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

    @Exclude // Exclude from Firebase builder
    public void setDate(Calendar date) {
        this.date = date;
    }

    public void setDate(String dateStr) {
        setDate(getDateFromString(dateStr));
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

    @Exclude
    public LatLng getLocation() {
        return location;
    }

    @Exclude
    public void setLocation(LatLng location) {
        this.location = location;
    }

    public double getLatitude() {
        return location.latitude;
    }

    public double getLongitude() {
        return location.longitude;
    }

    public void setLatitude(double latitude) {
        if (location == null) {
            location = new LatLng(0, 0);
        }
        location = new LatLng(latitude, location.longitude);
    }

    public void setLongitude(double longitude) {
        if (location == null) {
            location = new LatLng(0, 0);
        }
        location = new LatLng(location.latitude, longitude);
    }

    @Exclude
    public String getDateStr() {
        return String.format(
                "%04d-%02d-%02d",
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH)
        );
    }

    @Exclude
    public String getTimeStr() {
        return String.format("%02d:%02d", hourOfDay, minute);
    }

    @Exclude
    public String getDateTimeStr() {
        return getDateStr() + ' ' + getTimeStr();
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();

        data.put("name", getName());
        data.put("date", getDateStr());
        data.put("hour", getHourOfDay());
        data.put("minute", getMinute());
        data.put("latitude", getLocation().latitude);
        data.put("longitude", getLocation().longitude);

        return data;
    }

    private static Calendar getDateFromString(String dateStr) {
        String[] tokens = dateStr.split("-");

        Calendar date = Calendar.getInstance();
        date.set(Calendar.YEAR, Integer.parseInt(tokens[0]));
        date.set(Calendar.MONTH, Integer.parseInt(tokens[1]) - 1);
        date.set(Calendar.DAY_OF_MONTH, Integer.parseInt(tokens[2]));

        return date;
    }

    public String toString() {
        return getName() + ' ' + getDateTimeStr();
    }
}
