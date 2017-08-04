package com.pmvb.tektonentry;

import android.app.DatePickerDialog;

import java.util.Calendar;

import android.app.TimePickerDialog;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pmvb.tektonentry.util.CustomMapFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateEventActivity extends AppCompatActivity implements OnMapReadyCallback {

    @BindView(R.id.event_input_name)
    TextInputEditText nameField;
    @BindView(R.id.event_input_date)
    TextInputEditText dateField;
    @BindView(R.id.event_input_time)
    TextInputEditText timeField;

    private GoogleMap mMap;
    private boolean hasRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        ButterKnife.bind(this);

        hasRun = false;

        Toolbar toolbar = (Toolbar) findViewById(R.id.event_edit_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.create_event_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupDateField();
        setupTimeField();

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
                            dateField.setText(String.format("%d-%02d-%02d", year, month + 1, dayOfMonth));
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
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        ScrollView scrollView = (ScrollView) findViewById(R.id.event_form_scroll);
        ((CustomMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.event_location_map))
                .setOnTouchListener(() -> {
                    scrollView.requestDisallowInterceptTouchEvent(true);
                });

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
