package com.pmvb.tektonentry.util;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by pmvb on 17-08-04.
 */

public class CustomMapFragment extends SupportMapFragment {
    private OnTouchListener listener;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View layout = super.onCreateView(layoutInflater, viewGroup, bundle);
        TouchableWrapper frameLayout = new TouchableWrapper(getActivity());
        frameLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));
        ((ViewGroup) layout).addView(
                frameLayout,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
        );
        return layout;
    }

    public void setOnTouchListener(OnTouchListener listener) {
        this.listener = listener;
    }

    public OnTouchListener getOnTouchListener() {
        return listener;
    }

    public interface OnTouchListener {
        public abstract void onTouch();
    }

    public class TouchableWrapper extends FrameLayout {
        public TouchableWrapper(Context context) {
            super(context);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    listener.onTouch();
                    break;
                case MotionEvent.ACTION_UP:
                    listener.onTouch();
                    break;
            }
            return super.dispatchTouchEvent(ev);
        }
    }
}
