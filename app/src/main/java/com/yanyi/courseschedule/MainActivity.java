package com.yanyi.courseschedule;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.graphics.Color;
import android.view.Window;
import android.widget.Toast;

public class MainActivity extends Activity {
    private WebView webView;
    private static final String LOCAL_APP = "file:///android_asset/index.html";
    private static final int NOTIFICATION_PERMISSION_REQUEST = 201;

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
