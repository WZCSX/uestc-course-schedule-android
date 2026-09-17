package com.yanyi.courseschedule;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.content.pm.PackageManager;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashSet;
import java.util.Set;

public final class ReminderScheduler {
    public static final String CHANNEL_ID = "class_reminders";
    private static final String PREFS = "class_reminder_settings";
    private static final String KEY_JSON = "reminders_json";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_IDS = "alarm_ids";
    private static final String KEY_COUNT = "scheduled_count";

    private ReminderScheduler() {}

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "上课提醒", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("在课程开始前提醒上课时间和地点");
            channel.enableVibration(true);
            context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    public static int sync(Context context, String json, boolean enabled) {
        cancelAll(context);
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_JSON, json == null ? "[]" : json)
            .putBoolean(KEY_ENABLED, enabled)
            .apply();
        if (!enabled || json == null) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_COUNT, 0).apply();
            return 0;
        }
        Set<String> ids = new HashSet<>();
        int count = 0;
        try {
            JSONArray items = new JSONArray(json);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                long triggerAt = item.optLong("triggerAt", 0);
                if (triggerAt <= System.currentTimeMillis()) continue;
                String id = item.optString("id", "course-" + i);
                int requestCode = id.hashCode() & 0x7fffffff;
                Intent intent = new Intent(context, ReminderReceiver.class)
                    .putExtra("notificationId", requestCode)
                    .putExtra("name", item.optString("name", "课程提醒"))
                    .putExtra("location", item.optString("location", "地点未设置"))
                    .putExtra("startTime", item.optString("startTime", ""));
                PendingIntent pending = PendingIntent.getBroadcast(
                    context, requestCode, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending);
                    } else {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending);
                    }
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pending);
                }
                ids.add(String.valueOf(requestCode));
                count++;
            }
        } catch (Exception ignored) {}
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putStringSet(KEY_IDS, ids)
            .putInt(KEY_COUNT, count)
            .apply();
        return count;
    }

    private static void cancelAll(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Set<String> ids = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_IDS, new HashSet<>());
        for (String value : ids) {
            try {
                int requestCode = Integer.parseInt(value);
                Intent intent = new Intent(context, ReminderReceiver.class);
                PendingIntent pending = PendingIntent.getBroadcast(
                    context, requestCode, intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
                if (pending != null) {
                    manager.cancel(pending);
                    pending.cancel();
                }
            } catch (Exception ignored) {}
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY_IDS).apply();
    }

    public static void restore(Context context) {
        boolean enabled = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, false);
        String json = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_JSON, "[]");
        sync(context, json, enabled);
    }

    public static String getStatus(Context context) {
        boolean notificationsAllowed = Build.VERSION.SDK_INT < 33 ||
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        boolean exactAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms();
        int count = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_COUNT, 0);
        try {
            return new JSONObject()
                .put("notificationsAllowed", notificationsAllowed)
                .put("exactAlarmsAllowed", exactAllowed)
                .put("scheduledCount", count)
                .toString();
        } catch (Exception ignored) {
            return "{}";
        }
    }
}
