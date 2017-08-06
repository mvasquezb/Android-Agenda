package com.pmvb.tektonentry.event;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EventManager {
    // Instance fields
    private DatabaseReference mDatabase;
    private String mResName;

    public EventManager(String endpoint) {
        this(FirebaseDatabase.getInstance().getReference(), endpoint);
    }

    public EventManager(DatabaseReference dbRef, String resName) {
        mResName = cleanResourceName(resName);
        mDatabase = dbRef;
    }

    public String add(Event item) {
        // Save new event
        String key = mDatabase.child(mResName).push().getKey();
        Map<String, Object> evtValues = item.toMap();

        Map<String, Object> updates = new HashMap<>();
        updates.put(getEndpoint(mResName, key), evtValues);
        mDatabase.updateChildren(updates);

        return key;
    }

    public Query getQuery() {
        return mDatabase.child(mResName);
    }

    public String getResourceName() {
        return mResName;
    }

    public DatabaseReference get(String endpoint) {
        return mDatabase.child(endpoint);
    }

    public static String cleanResourceName(String resName) {
        return resName.replace("/", "");
    }

    public static String getEndpoint(String... values) {
        StringBuilder builder = new StringBuilder();
        if (values.length == 0) {
            builder.append('/');
        }
        for (String resName: values) {
            if (!resName.replaceAll("\\s", "").isEmpty()) {
                builder.append('/');
                builder.append(cleanResourceName(resName));
            }
        }
        return builder.toString();
    }

    public ValueEventListener addValueEventListener(ValueEventListener listener) {
        return mDatabase.child(mResName).addValueEventListener(listener);
    }

    public void removeEventListener(ValueEventListener listener) {
        mDatabase.child(mResName).removeEventListener(listener);
    }

    public void addListenerForSingleValueEvent(ValueEventListener listener) {
        mDatabase.child(mResName).addListenerForSingleValueEvent(listener);
    }

    public static DatabaseReference resolveEndpoint(DatabaseReference ref, String endpoint) {
        String[] resources = endpoint.split("/");
        return resolveEndpoint(ref, resources);
    }

    public static DatabaseReference resolveEndpoint(DatabaseReference ref, String... resources) {
        for (String res : resources) {
            ref = ref.child(res);
        }
        return ref;
    }
}
