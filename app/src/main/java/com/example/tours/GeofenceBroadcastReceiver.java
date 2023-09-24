package com.example.tours;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceBroadcastRcvr";
    private static final String CHANNEL_ID = "FENCE_CHANNEL";

    public static NotificationManager notificationManager;

    @Override
    public void onReceive(Context context, Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent == null) {
            Log.d(TAG, "onReceive: NULL GeofencingEvent received");
            return;
        }

        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Error: " + geofencingEvent.getErrorCode());
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            if (triggeringGeofences != null) {
                for (Geofence g : triggeringGeofences) {
                    sendNotification(context, g.getRequestId(), geofenceTransition);
                }
            }
        }
    }

    public static void doClearAll() {
        notificationManager.cancelAll();
    }
    public void sendNotification(Context context, String id, int transitionType) {

        notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) return;

        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {

            Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notif_sound);
            AudioAttributes att = new AudioAttributes.Builder().
                    setUsage(AudioAttributes.USAGE_NOTIFICATION).build();

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
            mChannel.setSound(soundUri, att);
            mChannel.setLightColor(Color.RED);
            mChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            mChannel.setShowBadge(true);

            notificationManager.createNotificationChannel(mChannel);

        }

        ////
        String transitionString = transitionType == Geofence.GEOFENCE_TRANSITION_ENTER
                ? "Welcome!" : "Goodbye!";

        Intent resultIntent = new Intent(context.getApplicationContext(), BuildingInfoActivity.class);
        resultIntent.putExtra("FENCE_ID", id);
        resultIntent.putExtra("FENCE_TRANS", transitionString);

        PendingIntent pi = PendingIntent.getActivity(
                context.getApplicationContext(), 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);


        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentIntent(pi)
                .setSmallIcon(R.drawable.walker_right)
                .setContentTitle(id + " (" + "Tap to see details" + ")") // Bold title
                .setContentText(MapsActivity.buildingMap.get(id).getAddress()) // Detail info
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.fence_notif)
                .build();

        notificationManager.notify(getUniqueId(), notification);
    }

    private static int getUniqueId() {
        return(int) (System.currentTimeMillis() % 10000);
    }

}
