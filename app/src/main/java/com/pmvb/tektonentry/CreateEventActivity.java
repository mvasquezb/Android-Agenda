package com.pmvb.tektonentry;

import android.app.DatePickerDialog;

import java.util.Calendar;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

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
    }

    private void setupDateField() {
        dateField.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                // Current date
                int thisYear;
                int thisMonth;
                int thisDay;
                if (dateField.getText().toString().isEmpty()) {
                    Calendar c = Calendar.getInstance();
                    thisYear = c.get(Calendar.YEAR);
                    thisMonth = c.get(Calendar.MONTH);
                    thisDay = c.get(Calendar.DAY_OF_MONTH);
                } else {
                    String[] tokens = dateField.getText().toString().split("-");
                    thisYear = Integer.parseInt(tokens[0]);
                    thisMonth = Integer.parseInt(tokens[1]) - 1;
                    thisDay = Integer.parseInt(tokens[2]);
                }
                DatePickerDialog pickerDialog = new DatePickerDialog(
                        this,
                        (picker, year, month, dayOfMonth) -> {
                            dateField.setText(String.format("%d-%02d-%02d", year, month + 1, dayOfMonth));
                            nameField.requestFocus();
                        },
                        thisYear, thisMonth, thisDay
                );
                pickerDialog.show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


}
