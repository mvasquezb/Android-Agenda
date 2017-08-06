package com.pmvb.tektonentry;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.provider.ContactsContract;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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
import com.google.firebase.database.DatabaseReference;
import com.pmvb.tektonentry.event.Event;
import com.pmvb.tektonentry.event.EventManager;
import com.pmvb.tektonentry.util.CustomMapFragment;

/**
 * A fragment representing a single EventManager detail screen.
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
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The content this fragment is presenting.
     */
    private Event mItem;
    private DatabaseReference mEventRef;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EventDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
//            mItem = new EventManager("events").get(getArguments().getString(ARG_ITEM_ID));

            Activity activity = getActivity();
            CollapsingToolbarLayout appBarLayout = activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.getName());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.event_detail, container, false);

        if (mItem != null) {
            TextView dateText = rootView.findViewById(R.id.event_detail_date);
            dateText.setText(mItem.getDateStr());

            TextView timeText = rootView.findViewById(R.id.event_detail_time);
            timeText.setText(mItem.getTimeStr());

            mapSetup();
        }
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

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
            LocationManager locationManager = (LocationManager)
                    getActivity().getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();

            Location location = locationManager.getLastKnownLocation(
                    locationManager.getBestProvider(criteria, false));
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            LatLng myLocation = new LatLng(latitude, longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 12));
        }
    }
}
