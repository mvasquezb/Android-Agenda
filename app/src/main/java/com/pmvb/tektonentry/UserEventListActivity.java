package com.pmvb.tektonentry;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.database.FirebaseDatabase;
import com.pmvb.tektonentry.db.EventListManager;
import com.pmvb.tektonentry.db.Manager;

/**
 * Created by pmvb on 17-08-07.
 */

public class UserEventListActivity extends EventListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("My Events");

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        invalidateOptionsMenu();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.btn_add_event);
        fab.hide();
    }

    @Override
    public EventListManager createEventManager() {
        return new EventListManager(
                Manager.resolveEndpoint(
                        FirebaseDatabase.getInstance().getReference(),
                        "user-events"
                ),
                mAuth.getCurrentUser().getUid()
        );
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Don't create menu
        return true;
    }
}
