package com.pmvb.tektonentry;

import android.app.DatePickerDialog;

import java.util.Calendar;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.pmvb.tektonentry.db.EventListManager;
import com.pmvb.tektonentry.models.Event;
import com.pmvb.tektonentry.util.CustomMapFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateEventActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    @BindView(R.id.event_input_name)
    TextInputEditText nameField;
    @BindView(R.id.event_input_date)
    TextInputEditText dateField;
    @BindView(R.id.event_input_time)
    TextInputEditText timeField;
    @BindView(R.id.event_input_location)
    TextInputEditText locationField;
    @BindView(R.id.event_input_location_helper)
    TextView locationHelper;

    private Marker eventLocation;

    private GoogleMap mMap;
    private boolean hasRun;
    private EventListManager mEventManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        ButterKnife.bind(this);
        mEventManager = new EventListManager(
                FirebaseDatabase.getInstance().getReference(),
                "events"
        );

        hasRun = false;

        Toolbar toolbar = (Toolbar) findViewById(R.id.event_edit_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.create_event_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupDateField();
        setupTimeField();

        mapSetup();
    }

    private void mapSetup() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.event_location_map);
        mapFragment.getMapAsync(this);

        // Display map with equal width and height
        View mapView = mapFragment.getView();
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

    private void setupTimeField() {
        timeField.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                int selHour;
                int selMinute;
                boolean is24Hour = true;
                String timeStr = timeField.getText().toString();
                if (timeStr.isEmpty()) {
                    final Calendar c = Calendar.getInstance();
                    selHour = c.get(Calendar.HOUR_OF_DAY);
                    selMinute = c.get(Calendar.MINUTE);
                } else {
                    String[] tokens = timeStr.split(":");
                    selHour = Integer.parseInt(tokens[0]);
                    selMinute = Integer.parseInt(tokens[1]);
                }
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        this,
                        (timePicker, hour, minute) -> {
                            timeField.setText(String.format("%02d:%02d", hour, minute));
                            timeField.clearFocus();
                        },
                        selHour, selMinute, is24Hour
                );
                timePickerDialog.show();
            }
        });
    }

    private void setupDateField() {
        dateField.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                // Current date
                int thisYear;
                int thisMonth;
                int thisDay;
                String dateStr = dateField.getText().toString();
                if (dateStr.isEmpty()) {
                    Calendar c = Calendar.getInstance();
                    thisYear = c.get(Calendar.YEAR);
                    thisMonth = c.get(Calendar.MONTH);
                    thisDay = c.get(Calendar.DAY_OF_MONTH);
                } else {
                    String[] tokens = dateStr.split("-");
                    thisYear = Integer.parseInt(tokens[0]);
                    thisMonth = Integer.parseInt(tokens[1]) - 1;
                    thisDay = Integer.parseInt(tokens[2]);
                }
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        this,
                        (picker, year, month, dayOfMonth) -> {
                            dateField.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth));
                            dateField.clearFocus();
                        },
                        thisYear, thisMonth, thisDay
                );
                datePickerDialog.show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.event_save:
                eventFormSubmit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void eventFormSubmit() {
        if (!validateForm()) {
            onSubmitFailed();
            return;
        }
        onSubmitSuccess();
    }

    private void onSubmitSuccess() {
        String[] tokens = dateField.getText().toString().split("-");
        Calendar date = Calendar.getInstance();
        date.set(Calendar.YEAR, Integer.parseInt(tokens[0]));
        date.set(Calendar.MONTH, Integer.parseInt(tokens[1]) - 1);
        date.set(Calendar.DAY_OF_MONTH, Integer.parseInt(tokens[2]));

        tokens = timeField.getText().toString().split(":");
        int selHour = Integer.parseInt(tokens[0]);
        int selMinute = Integer.parseInt(tokens[1]);

        Event evt = new Event(
                nameField.getText().toString(),
                date,
                selHour,
                selMinute,
                eventLocation.getPosition()
        );
        String key = mEventManager.add(evt);

        Intent eventList = new Intent(this, EventListActivity.class);
        eventList.putExtra("agenda_new_event", key);
        setResult(RESULT_OK, eventList);
        finish();
    }

    private void onSubmitFailed() {
        Snackbar.make(nameField, "Please correct the errors shown", Snackbar.LENGTH_LONG).show();
    }

    private boolean validateForm() {
        boolean valid = true;
        valid = valid && basicTextFieldValidation(nameField, "Event must have a valid name");

        // If dateField has content, it's valid (comes from DatePicker)
        valid = basicTextFieldValidation(dateField, "Must select event date") && valid;
        // Same for timeField
        valid = basicTextFieldValidation(timeField, "Must select event time") && valid;

        valid = mapHasMarker() && valid;

        return valid;
    }

    public boolean mapHasMarker() {
        boolean valid = eventLocation != null;
        valid = basicTextFieldValidation(locationField, "Must select event location") && valid;
        if (valid) {
            locationHelper.setTextColor(ActivityCompat.getColor(this, android.R.color.darker_gray));
        } else {
            locationHelper.setTextColor(Color.RED);
        }
        return valid;
    }

    private boolean basicTextFieldValidation(TextInputEditText field, String errorMsg) {
        boolean valid = true;
        if (field.getText().toString().isEmpty()) {
            field.setError(errorMsg);
            valid = false;
        } else {
            field.setError(null);
        }
        return valid;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);

        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException ex) {
            ex.printStackTrace();
            Snackbar.make(
                    nameField,
                    "Please enable location services for a better experience.",
                    Snackbar.LENGTH_LONG).show();
        }

        ScrollView scrollView = (ScrollView) findViewById(R.id.event_form_scroll);
        ((CustomMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.event_location_map))
                .setOnTouchListener(() -> scrollView.requestDisallowInterceptTouchEvent(true));

        if (mMap.isMyLocationEnabled()) {
            // Takes long
            LocationManager locationManager = (LocationManager)
                    getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();

            Location location = locationManager.getLastKnownLocation(
                    locationManager.getBestProvider(criteria, false));
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            LatLng myLocation = new LatLng(latitude, longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 12));
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (eventLocation != null) {
            eventLocation.remove();
        }
        eventLocation = mMap.addMarker(
                new MarkerOptions().position(latLng));
        locationField.setText(String.format("%f; %f", latLng.latitude, latLng.longitude));
    }
}
