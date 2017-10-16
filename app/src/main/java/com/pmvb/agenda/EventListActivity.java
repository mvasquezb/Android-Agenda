package com.pmvb.agenda;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.pmvb.agenda.db.EventListManager;
import com.pmvb.agenda.models.Event;
import com.pmvb.agenda.viewholder.EventViewHolder;


import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;

/**
 * An activity representing a list of Events. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link EventDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class EventListActivity extends LoginProtectedActivity {
    public static final String TAG = "EventListActivity";
    public static final int CREATE_EVENT_REQUEST = 1;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private EventListManager mEventManager;

    @BindView(R.id.event_list)
    RecyclerView eventListView;
    private FirebaseEventAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_event_list);
        ButterKnife.bind(this);

        mEventManager = createEventManager();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.btn_add_event);
        fab.setOnClickListener(view -> {
            redirectEventCreate();
        });

        assert eventListView != null;
        setupRecyclerView(eventListView);

        if (findViewById(R.id.event_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add items to the action bar if present
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item click
        int id = item.getItemId();

        if (id == R.id.menu_action_logout) {
            logoutAction();
            return true;
        } else if (id == R.id.menu_action_my_events) {
            showUserEvents();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUserEvents() {
        Intent userEvents = new Intent(this, UserEventListActivity.class);
        startActivity(userEvents);
//        finish();
    }

    private void logoutAction() {
        // Launch LoginActivity with logout flag
        Intent logout = new Intent(this, LoginActivity.class);
        logout.putExtra("logout", true);
        startActivity(logout);
        finish();
    }

    private void redirectEventCreate() {
        Intent addEvent = new Intent(getApplicationContext(), CreateEventActivity.class);
        startActivityForResult(addEvent, CREATE_EVENT_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_EVENT_REQUEST) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(
                        findViewById(R.id.btn_add_event),
                        R.string.event_submit_success,
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        mAdapter = new FirebaseEventAdapter(
                Event.class,
                R.layout.event_list_content,
                EventViewHolder.class,
                mEventManager.getQuery()
        );
        recyclerView.setAdapter(mAdapter);
    }

    public EventListManager createEventManager() {
        return new EventListManager(
                FirebaseDatabase.getInstance().getReference(),
                "events"
        );
    }

    public class FirebaseEventAdapter extends FirebaseRecyclerAdapter<Event, EventViewHolder> {

        /**
         * @param modelClass      Firebase will marshall the data at a location into
         *                        an instance of a class that you provide
         * @param modelLayout     This is the layout used to represent a single item in the list.
         *                        You will be responsible for populating an instance of the corresponding
         *                        view with the data from an instance of modelClass.
         * @param viewHolderClass The class that hold references to all sub-views in an instance modelLayout.
         * @param ref             The Firebase location to watch for data changes. Can also be a slice of a location,
         *                        using some combination of {@code limit()}, {@code startAt()}, and {@code endAt()}.
         */
        public FirebaseEventAdapter(Class<Event> modelClass, int modelLayout, Class<EventViewHolder> viewHolderClass, Query ref) {
            super(modelClass, modelLayout, viewHolderClass, ref);
        }

        @Override
        protected void populateViewHolder(EventViewHolder viewHolder, Event model, int position) {
            DatabaseReference eventRef = getRef(position);
            String key = eventRef.getKey();

            viewHolder.bindToEvent(model);
            viewHolder.mView.setOnClickListener(new EventDetailLauncher(key));
        }
    }

    public class EventDetailLauncher implements View.OnClickListener, Runnable {
        private String mKey;
        private Context mContext;

        public EventDetailLauncher(String key) {
            mKey = key;
        }

        @Override
        public void onClick(View view) {
            Log.d(TAG, "RUN onClick");
            run(view.getContext());
        }

        public void run(Context context) {
            if (mContext == null) {
                setContext(context);
            }
            run();
        }

        @Override
        public void run() {
            if (mContext == null) {
                throw new NullPointerException("Context is not valid");
            }
            Log.d(TAG, "RUN with context: " + mContext.toString());
            if (mTwoPane) {
                showEventInFragment(mKey);
            } else {
                Intent intent = new Intent(mContext, EventDetailActivity.class);
                intent.putExtra(EventDetailFragment.ARG_EVENT_ID, mKey);

                mContext.startActivity(intent);
            }
        }

        public String getKey(){
            return mKey;
        }

        public void setKey(String key) {
            mKey = key;
        }

        public void setContext(Context context) {
            mContext = context;
        }
    }

    private void showEventInFragment(String eventKey) {
        Bundle arguments = new Bundle();
        arguments.putString(EventDetailFragment.ARG_EVENT_ID, eventKey);
        EventDetailFragment fragment = new EventDetailFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.event_detail_container, fragment)
                .commit();
    }
}
