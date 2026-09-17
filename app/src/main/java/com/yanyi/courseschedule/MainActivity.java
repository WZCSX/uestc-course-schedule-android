package com.yanyi.courseschedule;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;
import android.graphics.Color;
import android.view.Window;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private WebView webView;
    private static final String LOCAL_APP = "file:///android_asset/index.html";
    private static final int NOTIFICATION_PERMISSION_REQUEST = 201;
    private static final int EXPORT_SCHEDULE_REQUEST = 301;
    private static final int IMPORT_SCHEDULE_REQUEST = 302;
    private static final int WIDGET_BACKGROUND_REQUEST = 303;
    private String pendingExportJson = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setStatusBarColor(Color.rgb(19, 95, 202));
        window.setNavigationBarColor(Color.WHITE);
        ReminderScheduler.createNotificationChannel(this);

        webView = new WebView(this);
        setContentView(webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        webView.addJavascriptInterface(new ReminderBridge(), "ReminderBridge");
        webView.addJavascriptInterface(new WidgetBridge(), "WidgetBridge");
        webView.addJavascriptInterface(new ScheduleFileBridge(), "ScheduleFileBridge");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("请确认")
                    .setMessage(message)
                    .setPositiveButton("确定", (dialog, which) -> result.confirm())
                    .setNegativeButton("取消", (dialog, which) -> result.cancel())
                    .setOnCancelListener(dialog -> result.cancel())
                    .show();
                return true;
            }
        });
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(LOCAL_APP);
    }

    public class ReminderBridge {
        @JavascriptInterface
        public void syncReminders(String remindersJson, boolean enabled) {
            runOnUiThread(() -> {
                if (enabled && Build.VERSION.SDK_INT >= 33 &&
                    checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST);
                }
                int count = ReminderScheduler.sync(MainActivity.this, remindersJson, enabled);
                String message = enabled ? "已安排 " + count + " 个上课提醒" : "上课提醒已关闭";
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                notifyReminderStatusChanged();
            });
        }

        @JavascriptInterface
        public String getStatus() {
            return ReminderScheduler.getStatus(MainActivity.this);
        }

        @JavascriptInterface
        public void openSettings() {
            runOnUiThread(() -> openRequiredReminderPermission());
        }

        @JavascriptInterface
        public void sendTestNotification() {
            runOnUiThread(() -> {
                if (Build.VERSION.SDK_INT >= 33 &&
                    checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "请先允许通知权限", Toast.LENGTH_LONG).show();
                    requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST);
                    return;
                }
                Intent test = new Intent(MainActivity.this, ReminderReceiver.class)
                    .putExtra("notificationId", 99001)
                    .putExtra("name", "测试课程")
                    .putExtra("location", "通知功能正常")
                    .putExtra("startTime", "现在");
                sendBroadcast(test);
                Toast.makeText(MainActivity.this, "测试通知已发送", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void openRequiredReminderPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST);
            return;
        }
        android.app.AlarmManager manager = (android.app.AlarmManager) getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !manager.canScheduleExactAlarms()) {
            try {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } catch (Exception exception) {
                startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName())));
            }
            return;
        }
        Toast.makeText(this, "提醒所需权限均已开启", Toast.LENGTH_SHORT).show();
        ReminderScheduler.restore(this);
        notifyReminderStatusChanged();
    }

    private void notifyReminderStatusChanged() {
        if (webView != null) webView.evaluateJavascript("window.onReminderStatusChanged && window.onReminderStatusChanged();", null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                openRequiredReminderPermission();
            } else {
                Toast.makeText(this, "未允许通知权限，上课提醒无法显示", Toast.LENGTH_LONG).show();
            }
            notifyReminderStatusChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            ReminderScheduler.restore(this);
            notifyReminderStatusChanged();
            CourseWidgetProvider.updateAll(this);
        }
    }

    public class WidgetBridge {
        @JavascriptInterface
        public void updateSchedule(String scheduleJson) {
            CourseWidgetProvider.saveAndUpdate(MainActivity.this, scheduleJson);
        }

        @JavascriptInterface
        public void pickBackground() {
            runOnUiThread(() -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, WIDGET_BACKGROUND_REQUEST);
            });
        }

        @JavascriptInterface
        public void clearBackground() {
            File background = new File(getFilesDir(), "widget_background.jpg");
            if (background.exists()) background.delete();
            CourseWidgetProvider.updateAll(MainActivity.this);
        }

        @JavascriptInterface
        public boolean hasBackground() {
            return new File(getFilesDir(), "widget_background.jpg").exists();
        }

        @JavascriptInterface
        public void updateAppearance(String appearanceJson) {
            CourseWidgetProvider.saveAppearance(MainActivity.this, appearanceJson);
        }
    }

    public class ScheduleFileBridge {
        @JavascriptInterface
        public void exportSchedule(String json) {
            pendingExportJson = json == null ? "" : json;
            runOnUiThread(() -> {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/json");
                intent.putExtra(Intent.EXTRA_TITLE, "研一课程表数据.json");
                startActivityForResult(intent, EXPORT_SCHEDULE_REQUEST);
            });
        }

        @JavascriptInterface
        public void importSchedule() {
            runOnUiThread(() -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/json", "text/plain", "application/octet-stream"});
                startActivityForResult(intent, IMPORT_SCHEDULE_REQUEST);
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        if (requestCode == EXPORT_SCHEDULE_REQUEST) {
            try (OutputStream output = getContentResolver().openOutputStream(uri, "wt")) {
                if (output == null) throw new IllegalStateException("无法打开保存位置");
                output.write(pendingExportJson.getBytes(StandardCharsets.UTF_8));
                output.flush();
                Toast.makeText(this, "课程表已导出", Toast.LENGTH_SHORT).show();
            } catch (Exception exception) {
                Toast.makeText(this, "导出失败，请重新选择保存位置", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == IMPORT_SCHEDULE_REQUEST) {
            try (InputStream input = getContentResolver().openInputStream(uri);
                 ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                if (input == null) throw new IllegalStateException("无法读取文件");
                byte[] buffer = new byte[4096];
                int read;
                while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
                String raw = output.toString(StandardCharsets.UTF_8.name());
                webView.evaluateJavascript("window.handleImportedSchedule(" + JSONObject.quote(raw) + ");", null);
            } catch (Exception exception) {
                Toast.makeText(this, "读取失败，请选择正确的课表文件", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == WIDGET_BACKGROUND_REQUEST) {
            try (InputStream input = getContentResolver().openInputStream(uri)) {
                if (input == null) throw new IllegalStateException("无法读取图片");
                Bitmap source = BitmapFactory.decodeStream(input);
                if (source == null) throw new IllegalStateException("图片格式不支持");
                final int targetWidth = 320;
                final int targetHeight = 190;
                float targetRatio = (float) targetWidth / targetHeight;
                int cropWidth = source.getWidth();
                int cropHeight = source.getHeight();
                if ((float) cropWidth / cropHeight > targetRatio) {
                    cropWidth = Math.round(cropHeight * targetRatio);
                } else {
                    cropHeight = Math.round(cropWidth / targetRatio);
                }
                int left = (source.getWidth() - cropWidth) / 2;
                int top = (source.getHeight() - cropHeight) / 2;
                Bitmap cropped = Bitmap.createBitmap(source, left, top, cropWidth, cropHeight);
                Bitmap scaled = Bitmap.createScaledBitmap(cropped, targetWidth, targetHeight, true);
                File background = new File(getFilesDir(), "widget_background.jpg");
                try (FileOutputStream output = new FileOutputStream(background)) {
                    scaled.compress(Bitmap.CompressFormat.JPEG, 90, output);
                }
                if (scaled != cropped) scaled.recycle();
                if (cropped != source) cropped.recycle();
                source.recycle();
                CourseWidgetProvider.updateAll(this);
                webView.evaluateJavascript("window.onWidgetBackgroundChanged(true);", null);
            } catch (Exception exception) {
                Toast.makeText(this, "背景图片设置失败，请换一张图片重试", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
