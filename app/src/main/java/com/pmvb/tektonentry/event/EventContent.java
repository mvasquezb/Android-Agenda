package com.pmvb.tektonentry.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventContent {
    public static final List<EventItem> ITEMS = new ArrayList<>();
    public static final Map<String, EventItem> ITEM_MAP = new HashMap<>();

    public static void addItem(EventItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static class EventItem {
        public final String id;
        public final Event event;

        public EventItem(String id, Event event) {
            this.id = id;
            this.event = event;
        }

        @Override
        public String toString() {
            return "Event " + id;
        }
    }
}
