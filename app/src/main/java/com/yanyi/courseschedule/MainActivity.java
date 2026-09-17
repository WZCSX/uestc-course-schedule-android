package com.yanyi.courseschedule;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.graphics.Color;
import android.view.Window;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private WebView webView;
    private static final String LOCAL_APP = "file:///android_asset/index.html";
    private static final int NOTIFICATION_PERMISSION_REQUEST = 201;
    private static final int EXPORT_SCHEDULE_REQUEST = 301;
    private static final int IMPORT_SCHEDULE_REQUEST = 302;
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
            });
        }
    }

    public class WidgetBridge {
        @JavascriptInterface
        public void updateSchedule(String scheduleJson) {
            CourseWidgetProvider.saveAndUpdate(MainActivity.this, scheduleJson);
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
