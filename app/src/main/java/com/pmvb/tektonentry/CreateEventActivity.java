package com.pmvb.tektonentry;

import android.app.DatePickerDialog;

import java.util.Calendar;

import android.app.IntentService;
import android.app.TimePickerDialog;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TimePicker;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateEventActivity extends AppCompatActivity {

    @BindView(R.id.event_input_name)
    TextInputEditText nameField;
    @BindView(R.id.event_input_date)
    TextInputEditText dateField;
    @BindView(R.id.event_input_time)
    TextInputEditText timeField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.event_edit_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.create_event_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupDateField();
        setupTimeField();
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


}
