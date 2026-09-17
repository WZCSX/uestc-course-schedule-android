package com.yanyi.courseschedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.widget.RemoteViews;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CourseWidgetProvider extends AppWidgetProvider {
    private static final String PREFS = "course_widget";
    private static final String KEY_SCHEDULE = "schedule_json";
    private static final String KEY_TITLE = "widget_title";
    private static final String KEY_OVERLAY = "widget_overlay";
    private static final String KEY_TEXT_THEME = "widget_text_theme";
    private static final String ACTION_REFRESH = "com.yanyi.courseschedule.WIDGET_REFRESH";

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        updateAll(context);
        scheduleMidnightRefresh(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_REFRESH.equals(intent.getAction()) || Intent.ACTION_DATE_CHANGED.equals(intent.getAction())) {
            updateAll(context);
            scheduleMidnightRefresh(context);
        }
    }

    public static void saveAndUpdate(Context context, String json) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_SCHEDULE, json == null ? "[]" : json).apply();
        updateAll(context);
        scheduleMidnightRefresh(context);
    }

    public static void saveAppearance(Context context, String json) {
        try {
            JSONObject data = new JSONObject(json == null ? "{}" : json);
            int overlay = Math.max(0, Math.min(80, data.optInt("overlay", 35)));
            String textTheme = "dark".equals(data.optString("textTheme")) ? "dark" : "light";
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_TITLE, data.optString("title", ""))
                .putInt(KEY_OVERLAY, overlay)
                .putString(KEY_TEXT_THEME, textTheme)
                .apply();
        } catch (Exception ignored) {}
        updateAll(context);
    }

    public static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName component = new ComponentName(context, CourseWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(component);
        for (int id : ids) {
            try {
                manager.updateAppWidget(id, buildViews(context));
            } catch (Exception ignored) {
                RemoteViews fallback = new RemoteViews(context.getPackageName(), R.layout.course_widget);
                fallback.setTextViewText(R.id.widget_date, "研一课程表");
                fallback.setTextViewText(R.id.widget_count, "请打开一次 App");
                fallback.setTextViewText(R.id.widget_empty, "正在准备今天的课程");
                manager.updateAppWidget(id, fallback);
            }
        }
    }

    private static RemoteViews buildViews(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.course_widget);
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Date now = new Date();
        String dateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(now);
        String dateTitle = new SimpleDateFormat("M月d日 EEEE", Locale.CHINA).format(now);
        String customTitle = preferences.getString(KEY_TITLE, "").trim();
        views.setTextViewText(R.id.widget_date, customTitle.isEmpty() ? dateTitle : customTitle + " · " + dateTitle);

        File backgroundFile = new File(context.getFilesDir(), "widget_background.jpg");
        Bitmap background = backgroundFile.exists() ? BitmapFactory.decodeFile(backgroundFile.getAbsolutePath()) : null;
        if (background != null) {
            views.setImageViewBitmap(R.id.widget_background_image, background);
            views.setViewVisibility(R.id.widget_background_image, android.view.View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.widget_background_image, android.view.View.GONE);
        }
        int overlayPercent = preferences.getInt(KEY_OVERLAY, 35);
        views.setInt(R.id.widget_overlay, "setBackgroundColor", Color.argb(Math.round(255 * overlayPercent / 100f), 0, 0, 0));
        views.setViewVisibility(R.id.widget_overlay, overlayPercent > 0 ? android.view.View.VISIBLE : android.view.View.GONE);

        boolean darkText = "dark".equals(preferences.getString(KEY_TEXT_THEME, "light"));
        int mainText = darkText ? Color.rgb(24, 36, 55) : Color.WHITE;
        int secondaryText = darkText ? Color.argb(210, 24, 36, 55) : Color.argb(220, 255, 255, 255);
        int divider = darkText ? Color.argb(45, 24, 36, 55) : Color.argb(55, 255, 255, 255);
        views.setTextColor(R.id.widget_date, mainText);
        views.setTextColor(R.id.widget_count, secondaryText);
        views.setTextColor(R.id.widget_empty, secondaryText);
        views.setInt(R.id.widget_divider, "setBackgroundColor", divider);
        List<JSONObject> today = new ArrayList<>();
        try {
            String raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_SCHEDULE, "[]");
            JSONArray all = new JSONArray(raw);
            for (int i = 0; i < all.length(); i++) {
                JSONObject item = all.getJSONObject(i);
                if (dateKey.equals(item.optString("date"))) today.add(item);
            }
            Collections.sort(today, Comparator.comparingInt(item -> item.optInt("order", 99)));
        } catch (Exception ignored) {}

        int[] rows = {R.id.widget_course_1, R.id.widget_course_2, R.id.widget_course_3, R.id.widget_course_4};
        for (int i = 0; i < rows.length; i++) {
            if (i < today.size()) {
                JSONObject item = today.get(i);
                String line = item.optString("time") + "  " + item.optString("name") + "\n" + item.optString("location");
                views.setTextViewText(rows[i], line);
                views.setTextColor(rows[i], mainText);
                views.setViewVisibility(rows[i], android.view.View.VISIBLE);
            } else {
                views.setViewVisibility(rows[i], android.view.View.GONE);
            }
        }
        views.setViewVisibility(R.id.widget_empty, today.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
        views.setTextViewText(R.id.widget_count, today.isEmpty() ? "今天无课" : "共 " + today.size() + " 门课");

        Intent open = new Intent(context, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(context, 301, open,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, pending);
        return views;
    }

    private static void scheduleMidnightRefresh(Context context) {
        Calendar next = Calendar.getInstance();
        next.add(Calendar.DAY_OF_YEAR, 1);
        next.set(Calendar.HOUR_OF_DAY, 0);
        next.set(Calendar.MINUTE, 1);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        Intent intent = new Intent(context, CourseWidgetProvider.class).setAction(ACTION_REFRESH);
        PendingIntent pending = PendingIntent.getBroadcast(context, 302, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pending);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pending);
        }
    }
}
