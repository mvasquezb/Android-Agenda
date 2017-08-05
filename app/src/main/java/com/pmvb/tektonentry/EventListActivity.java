package com.pmvb.tektonentry;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pmvb.tektonentry.event.EventContent;
import com.pmvb.tektonentry.event.EventParcelable;

import java.util.List;

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
public class EventListActivity extends AppCompatActivity {
    public static final String TAG = "EventListActivity";
    public static final int CREATE_EVENT_REQUEST = 1;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @BindView(R.id.event_list)
    RecyclerView eventListView;
    private SimpleItemAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_event_list);
        ButterKnife.bind(this);
        mAdapter = new SimpleItemAdapter(EventContent.ITEMS);

        initFirebaseAuth();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
//            Snackbar.make(findViewById(R.id.add_event), user.getEmail(), Snackbar.LENGTH_LONG).show();
        }

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
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestLogin() {
        Intent login = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(login);
        finish();
    }

    private void logoutAction() {
        // Launch LoginActivity with logout flag
        Intent logout = new Intent(this, LoginActivity.class);
        logout.putExtra("logout", true);
        startActivity(logout);
        finish();
    }

    private void initFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out");
                requestLogin();
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
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
                EventParcelable evtData = data.getParcelableExtra("agenda_new_event");
                EventContent.addItem(new EventContent.EventItem(evtData.getEvent().getName(), evtData.getEvent()));
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    public void setEventListAdapter(SimpleItemAdapter adapter) {
        mAdapter = adapter;
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        if (mAdapter == null) {
            setEventListAdapter(new SimpleItemAdapter(EventContent.ITEMS));
        }
        recyclerView.setAdapter(mAdapter);
    }

    public class SimpleItemAdapter
            extends RecyclerView.Adapter<SimpleItemAdapter.SimpleViewHolder> {

        private final List<EventContent.EventItem> mItems;

        public SimpleItemAdapter(List<EventContent.EventItem> items) {
            this.mItems = items;
        }

        @Override
        public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.event_list_content, parent, false);
            return new SimpleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final SimpleViewHolder holder, int position) {
            holder.mItem = mItems.get(position);
            holder.mNameView.setText(mItems.get(position).event.getName());
            holder.mDateView.setText(mItems.get(position).event.getDateStr() + ' ' + mItems.get(position).event.getTimeStr());

            holder.mView.setOnClickListener(view -> {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(EventDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        EventDetailFragment fragment = new EventDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.event_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = view.getContext();
                        Intent intent = new Intent(context, EventDetailActivity.class);
                        intent.putExtra(EventDetailFragment.ARG_ITEM_ID, holder.mItem.id);

                        context.startActivity(intent);
                    }
                }
            );
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public class SimpleViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mNameView;
            public final TextView mDateView;
            public EventContent.EventItem mItem;

            public SimpleViewHolder(View view) {
                super(view);
                mView = view;
                mNameView = view.findViewById(R.id.event_item_name);
                mDateView = view.findViewById(R.id.event_item_date);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mDateView.getText() + "'";
            }
        }
    }
}
