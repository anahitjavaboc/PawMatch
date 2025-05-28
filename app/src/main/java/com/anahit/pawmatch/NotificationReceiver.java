package com.anahit.pawmatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.anahit.pawmatch.R;
import com.anahit.pawmatch.NotificationHelper;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String petName = intent.getStringExtra("petName");
        String location = intent.getStringExtra("location");

        // Check for POST_NOTIFICATIONS permission (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, "android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
                Log.e("NotificationReceiver", "Cannot post notification: POST_NOTIFICATIONS permission denied");
                return;  // Exit if permission is not granted
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Vet Appointment Reminder")
                .setContentText("Time for " + petName + "'s appointment at " + location)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(NotificationHelper.NOTIFICATION_ID, builder.build());
        } catch (SecurityException e) {
            Log.e("NotificationReceiver", "Failed to post notification: " + e.getMessage());
        }
    }
}