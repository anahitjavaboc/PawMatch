package com.anahit.pawmatch;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.anahit.pawmatch.R;

public class NotificationHelper {
    public static final String CHANNEL_ID = "VetAppointmentChannel";
    public static final int NOTIFICATION_ID = 1;

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Vet Appointment Reminders";
            String description = "Channel for vet appointment reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static boolean scheduleNotification(Context context, long reminderTimestamp, String petName, String location) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Check if the app can schedule exact alarms (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e("NotificationHelper", "Cannot schedule exact alarms: Permission denied");
                return false;  // Indicate failure
            }
        }

        Intent intent = new Intent(context, com.anahit.pawmatch.NotificationReceiver.class);
        intent.putExtra("petName", petName);
        intent.putExtra("location", location);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        try {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTimestamp, pendingIntent);
            return true;  // Indicate success
        } catch (SecurityException e) {
            Log.e("NotificationHelper", "Failed to schedule alarm: " + e.getMessage());
            return false;  // Indicate failure
        }
    }
}