package com.pmvb.tektonentry.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.pmvb.tektonentry.EventDetailActivity;

import java.util.Date;

import static com.pmvb.tektonentry.EventDetailFragment.ARG_EVENT_ID;

/**
 * Created by pmvb on 17-08-06.
 */

public class EventNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String eventName = intent.getStringExtra("event_name");
        String eventKey = intent.getStringExtra(ARG_EVENT_ID);
        int mNotificationId = intent.getIntExtra("notification_id", 0);
        Log.e("EventNotificationReceiver", "Called on :" + new Date().toString());
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("An event you're subscribed to is about to start")
                .setContentText(eventName + " starts in one hour");
        // Get notification sound
        Uri soundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(soundUri);

        Intent mIntent = new Intent(context, EventDetailActivity.class);
        intent.putExtra(ARG_EVENT_ID, eventKey);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(mIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(
                0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);

        notificationManager.notify(mNotificationId, builder.build());
    }
}
