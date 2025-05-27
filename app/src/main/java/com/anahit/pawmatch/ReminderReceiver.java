package com.anahit.pawmatch;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.anahit.pawmatch.R;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "vet_reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        createNotificationChannel(context);

        String petName = intent.getStringExtra("pet_name");
        String apptDate = intent.getStringExtra("appt_date");
        String apptKey = intent.getStringExtra("appt_key");

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.putExtra("fragment_to_load", "health");
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                apptKey.hashCode(),
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Vet Appointment Reminder")
                .setContentText("Appointment for " + petName + " on " + apptDate)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(apptKey.hashCode(), builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Vet Reminders";
            String description = "Channel for vet appointment reminders";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}