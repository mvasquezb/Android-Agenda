package com.pmvb.tektonentry;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.AlarmManagerCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pmvb.tektonentry.db.EventListManager;
import com.pmvb.tektonentry.db.Manager;
import com.pmvb.tektonentry.models.Event;
import com.pmvb.tektonentry.util.CustomMapFragment;
import com.pmvb.tektonentry.util.EventNotificationReceiver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A fragment representing a single Event detail screen.
 * This fragment is either contained in a {@link EventListActivity}
 * in two-pane mode (on tablets) or a {@link EventDetailActivity}
 * on handsets.
 */
public class EventDetailFragment extends Fragment
        implements OnMapReadyCallback {

    private GoogleMap mMap;
    private boolean hasRun;
    private CustomMapFragment mMapFragment;

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_EVENT_ID = "event_id";

    /**
     * The content this fragment is presenting.
     */
    private Event mItem;
    private DatabaseReference mEventRef;
    private DatabaseReference mNotificationRef;

    private CollapsingToolbarLayout mAppBarLayout;
    private View mRootView;
    private ValueEventListener mEventLoadListener;

    @BindView(R.id.event_detail_date)
    TextView dateText;
    @BindView(R.id.event_detail_time)
    TextView timeText;

    // Might be null if fragment is loaded without EventDetailActivity (until this is checked)
    FloatingActionButton notificationToggle;
    private boolean mNotify;
    private Integer mNotificationId;
    private ValueEventListener mNotificationListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EventDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNotify = false;
        if (getArguments().containsKey(ARG_EVENT_ID)) {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            String eventId = getArguments().getString(ARG_EVENT_ID);
            mEventRef = EventListManager.resolveEndpoint(
                    dbRef,
                    "events",
                    eventId
            );
            mNotificationRef = Manager.resolveEndpoint(
                    dbRef,
                    "user-events",
                    FirebaseAuth.getInstance().getCurrentUser().getUid(),
                    eventId,
                    "notification"
            );

            Activity activity = getActivity();
            mAppBarLayout = activity.findViewById(R.id.toolbar_layout);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.event_detail, container, false);
        ButterKnife.bind(this, mRootView);
        notificationToggle = getActivity().findViewById(R.id.btn_toggle_notification);
        if (notificationToggle != null) {
            notificationToggle.setOnClickListener(view -> toggleNotification());
        }

        return mRootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        ValueEventListener eventLoadListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mItem = dataSnapshot.getValue(Event.class);

                if (mAppBarLayout != null && mItem != null) {
                    mAppBarLayout.setTitle(mItem.getName());
                }

                if (mItem != null) {
                    dateText.setText(mItem.getDateStr());
                    timeText.setText(mItem.getTimeStr());

                    mapSetup();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Snackbar.make(dateText, "Failed to load event.", Snackbar.LENGTH_LONG).show();
            }
        };
        mEventRef.addValueEventListener(eventLoadListener);
        mEventLoadListener = eventLoadListener;

        ValueEventListener notificationListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long notificationId = (Long) dataSnapshot.getValue();
                if (notificationId != null) {
                    mNotify = true;
                    mNotificationId = notificationId.intValue();
                }
                if (mItem != null && notificationToggle != null) {
                    toggleNotificationIcon(mNotify);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Snackbar.make(
                        dateText, "Failed to load subscription data.", Snackbar.LENGTH_LONG).show();
            }
        };
        mNotificationRef.addValueEventListener(notificationListener);
        mNotificationListener = notificationListener;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mEventLoadListener != null) {
            mEventRef.removeEventListener(mEventLoadListener);
        }
        if (mNotificationListener != null) {
            mNotificationRef.removeEventListener(mNotificationListener);
        }
    }

    /**
     * Notification methods
     */

    private void toggleNotification() {
        toggleNotification(!mNotify);
    }

    private void toggleNotification(boolean notify) {
        toggleNotification(notify, true);
    }

    private void toggleNotification(boolean notify, boolean send) {
        toggleNotificationIcon(notify);
        if (send && notify != mNotify) {
            if (notify) {
                mNotificationRef.setValue(getNotificationId());
            } else {
                mNotificationRef.setValue(null);
            }
        }
        mNotify = notify;
        if (notify) {
            scheduleNotification();
        } else {
            removeNotification();
        }
    }

    private void toggleNotificationIcon(boolean notify) {
        if (notificationToggle == null) {
            return;
        }
        int drawable;
        if (notify) {
            drawable = R.drawable.ic_notifications_active_white_24dp;
        } else {
            drawable = R.drawable.ic_notifications_none_white_24dp;
        }
        notificationToggle.setImageDrawable(
                ContextCompat.getDrawable(getContext(), drawable)
        );
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getActivity()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(getNotificationId());
    }

    private void scheduleNotification() {
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext())
//                .setSmallIcon(android.R.drawable.ic_dialog_alert)
//                .setContentTitle("A event you're subscribed to is about to start")
//                .setContentText(mItem.getName() + " starts in one hour");
//        // Get notification sound
//        Uri soundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        builder.setSound(soundUri);
//
//        Intent intent = new Intent(getContext(), getActivity());
//        intent.putExtra(ARG_EVENT_ID, getArguments().getString(ARG_EVENT_ID));
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
//        stackBuilder.addParentStack(getActivity());
//        stackBuilder.addNextIntent(intent);
//
//        PendingIntent pendingIntent = stackBuilder.getPendingIntent(
//                0, PendingIntent.FLAG_UPDATE_CURRENT);
//        builder.setContentIntent(pendingIntent);
//        NotificationManager notificationManager = (NotificationManager) getActivity()
//                .getSystemService(Context.NOTIFICATION_SERVICE);
//
//        notificationManager.notify(getNotificationId(), builder.build());

        Intent intent = new Intent(getContext(), EventNotificationReceiver.class);
        intent.putExtra("event_name", mItem.getName());
        intent.putExtra("notification_id", getNotificationId());
        intent.putExtra(ARG_EVENT_ID, getArguments().getString(ARG_EVENT_ID));

        long notificationTime = getTimeToNotification();
        Log.e("EventDetailFragment", "Event date: " + mItem.getDate().getTime().toString());
        Log.e("EventDetailFragment", " Notification date: " + new Date(notificationTime).toString());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 1, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
    }

    public long getTimeToNotification() {
        Calendar eventDate = mItem.getDate(); // DateTime
        eventDate.add(Calendar.HOUR_OF_DAY, -1);

        return eventDate.getTimeInMillis();
    }

    private int getNotificationId() {
        if (mNotificationId == null) {
            mNotificationId = createNotificationId();
        }
        return mNotificationId;
    }

    private int createNotificationId() {
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmssSS",  Locale.US).format(now));
        return id;
    }

    /**
     * Map related methods
     */

    private void mapSetup() {
        mMapFragment = (CustomMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.event_location_map);
        mMapFragment.getMapAsync(this);

        // Display map with equal width and height
        View mapView = mMapFragment.getView();
        ViewTreeObserver observer = mapView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(() -> {
            if (!hasRun) {
                // Run only once
                int dim = mapView.getWidth();
                ViewGroup.LayoutParams params = mapView.getLayoutParams();
                params.height = dim;
                mapView.setLayoutParams(params);
                hasRun = true;
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.addMarker(new MarkerOptions().position(mItem.getLocation()));

        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException ex) {
            ex.printStackTrace();
            Snackbar.make(
                    getActivity().findViewById(R.id.event_detail_time),
                    "Please enable location services for a better experience.",
                    Snackbar.LENGTH_LONG).show();
        }

        ScrollView scrollView = getActivity().findViewById(R.id.event_detail_scroll);
        mMapFragment.setOnTouchListener(() -> scrollView.requestDisallowInterceptTouchEvent(true));

        if (mMap.isMyLocationEnabled()) {
            // Takes long
            Location location = getLastKnownLocation();

            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                LatLng myLocation = new LatLng(latitude, longitude);
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 14));
            }
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mItem.getLocation(), 14));
    }

    private Location getLastKnownLocation() {
        LocationManager locationManager = (LocationManager)
                getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        Location location = locationManager.getLastKnownLocation(
                locationManager.getBestProvider(criteria, false));
        return location;
    }
}
