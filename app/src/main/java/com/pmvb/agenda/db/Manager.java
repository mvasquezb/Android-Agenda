package com.pmvb.agenda.db;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by pmvb on 17-08-05.
 */

public class Manager {
    // Instance fields
    protected DatabaseReference mDatabase;
    protected String mResName;

    public Manager(String endpoint) {
        this(FirebaseDatabase.getInstance().getReference(), endpoint);
    }

    public Manager(DatabaseReference dbRef, String resName) {
        mResName = cleanResourceName(resName);
        mDatabase = dbRef;
    }

    public DatabaseReference getRoot() {
        return mDatabase;
    }

    public DatabaseReference getQuery() {
        return resolveEndpoint(mDatabase, mResName);
    }

    public String getResourceName() {
        return mResName;
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
        if (resources == null) {
            return ref;
        }
        for (String res : resources) {
            ref = ref.child(res);
        }
        return ref;
    }
}
