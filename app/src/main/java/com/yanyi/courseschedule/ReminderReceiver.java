package com.yanyi.courseschedule;

import android.app.NotificationManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ReminderScheduler.createNotificationChannel(context);
        int id = intent.getIntExtra("notificationId", 1);
        String name = intent.getStringExtra("name");
        String location = intent.getStringExtra("location");
        String startTime = intent.getStringExtra("startTime");
        Intent openIntent = new Intent(context, MainActivity.class);
        PendingIntent openPending = PendingIntent.getActivity(
            context, id, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        String detail = (startTime == null ? "" : startTime + " 上课") +
            (location == null || location.isEmpty() ? "" : " · " + location);
        Notification.Builder notification = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ? new Notification.Builder(context, ReminderScheduler.CHANNEL_ID)
            : new Notification.Builder(context);
        notification
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("马上要上课了：" + (name == null ? "课程" : name))
            .setContentText(detail)
            .setStyle(new Notification.BigTextStyle().bigText(detail))
            .setPriority(Notification.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPending);
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(id, notification.build());
    }
}
