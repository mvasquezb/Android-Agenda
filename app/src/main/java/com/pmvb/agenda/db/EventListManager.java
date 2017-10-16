package com.pmvb.agenda.db;

import com.google.firebase.database.DatabaseReference;
import com.pmvb.agenda.models.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pmvb on 17-08-05.
 */
public class EventListManager extends ListManager<Event> {

    public EventListManager(String endpoint) {
        super(endpoint);
    }

    public EventListManager(DatabaseReference dbRef, String resName) {
        super(dbRef, resName);
    }

    @Override
    public String add(Event item) {
        // Save new event
        String key = getQuery().push().getKey();
        Map<String, Object> evtValues = item.toMap();

//        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put(getEndpoint(getResourceName(), key), evtValues);
//        updates.put(getEndpoint("user-events", uid, key), evtValues);
        getRoot().updateChildren(updates);

        return key;
    }
}
