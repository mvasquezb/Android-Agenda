package com.pmvb.tektonentry.db;

import com.google.firebase.database.DatabaseReference;

/**
 * Created by pmvb on 17-08-05.
 */

public abstract class ListManager<T> extends Manager {
    public ListManager(String endpoint) {
        super(endpoint);
    }

    public ListManager(DatabaseReference dbRef, String resName) {
        super(dbRef, resName);
    }

    /**
     * Method to add an element to the resource list
     *
     * @param item  Item to add to the resource list
     * @return      Key for the newly added item
     */
    public abstract String add(T item);
}
