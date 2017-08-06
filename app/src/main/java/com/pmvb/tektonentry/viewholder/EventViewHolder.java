package com.pmvb.tektonentry.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.pmvb.tektonentry.R;
import com.pmvb.tektonentry.event.Event;

/**
 * Created by pmvb on 17-08-05.
 */
public class EventViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final TextView mNameView;
    public final TextView mDateView;

    public EventViewHolder(View view) {
        super(view);
        mView = view;
        mNameView = view.findViewById(R.id.event_item_name);
        mDateView = view.findViewById(R.id.event_item_date);
    }

    public void bindToEvent(Event event) {
        mNameView.setText(event.getName());
        mDateView.setText(event.getDateTimeStr());
    }

    @Override
    public String toString() {
        return super.toString() + " '" + mDateView.getText() + "'";
    }
}
