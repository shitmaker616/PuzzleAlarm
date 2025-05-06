package com.example.puzzlealarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class WakeUpReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "alarm_notifications";
    private static final int NOTIFICATION_ID = 1111;

    @Override
    public void onReceive(Context context, Intent intent) {
        int alarmId = intent.getIntExtra("id", -1);

        //TODO: для android 8+ создавать канал уведомлений

        Intent wakeUpActivity = new Intent(context, WakeUpActivity.class);
        wakeUpActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pi = PendingIntent.getActivity(context, 0, wakeUpActivity,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        //сбор уведомлений
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Задачевый будильник").setContentText("ПОДЪЁМ!!!")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build();

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, notification);
    }
}


