package com.pmvb.tektonentry.event;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;

/**
 * Created by pmvb on 17-08-03.
 */

public class EventParcelable implements Parcelable {
    private Event event;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getEvent().getName());
        parcel.writeIntArray(new int[] {
                getEvent().getDate().get(Calendar.YEAR),
                getEvent().getDate().get(Calendar.MONTH) + 1,
                getEvent().getDate().get(Calendar.DAY_OF_MONTH)
        });
        parcel.writeIntArray(new int[] {
                getEvent().getHourOfDay(),
                getEvent().getMinute()
        });
        parcel.writeDouble(getEvent().getLocation().latitude);
        parcel.writeDouble(getEvent().getLocation().longitude);
    }

    public static final Parcelable.Creator<EventParcelable> CREATOR = new Parcelable.Creator<EventParcelable>() {
        public EventParcelable createFromParcel(Parcel parcel) {
            return new EventParcelable(parcel);
        }

        @Override
        public EventParcelable[] newArray(int i) {
            return new EventParcelable[i];
        }
    };

    private EventParcelable(Parcel parcel) {
        String name = parcel.readString();

        int[] dateValues = new int[3];
        parcel.readIntArray(dateValues);
        Calendar date = Calendar.getInstance();
        date.set(Calendar.YEAR, dateValues[0]);
        date.set(Calendar.MONTH, dateValues[1] - 1);
        date.set(Calendar.DAY_OF_MONTH, dateValues[2]);

        int[] timeValues = new int[2];
        parcel.readIntArray(timeValues);
        int hourOfDay = timeValues[0];
        int minute = timeValues[1];

        LatLng location = new LatLng(parcel.readDouble(), parcel.readDouble());

        setEvent(new Event(name, date, hourOfDay, minute, location));
    }

    public EventParcelable(Event evt) {
        setEvent(evt);
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
