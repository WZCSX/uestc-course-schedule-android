package com.yanyi.courseschedule;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.provider.Settings;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.view.Window;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;
    private static final int FILE_PICKER_REQUEST = 101;
    private static final String LOCAL_APP = "file:///android_asset/index.html";
    private static final String PORTAL_URL = "https://eportal.uestc.edu.cn/";
    private static final String PREFS = "school_sync";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setStatusBarColor(Color.rgb(19, 95, 202));
        window.setNavigationBarColor(Color.WHITE);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        CookieManager.getInstance().setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }

        webView.addJavascriptInterface(new SchoolBridge(), "SchoolBridge");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url.startsWith("file:///android_asset/")) {
                    deliverPendingSchedule();
                } else if (isSchoolUrl(url)) {
                    injectScheduleDetector();
                }
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                try {
                    startActivityForResult(params.createIntent(), FILE_PICKER_REQUEST);
                    return true;
                } catch (Exception exception) {
                    fileCallback = null;
                    return false;
                }
            }
        });
        String courseUrl = getSharedPreferences(PREFS, MODE_PRIVATE).getString("course_url", "");
        webView.loadUrl(courseUrl.isEmpty() ? LOCAL_APP : courseUrl);
    }

    private boolean isSchoolUrl(String url) {
        try {
            String host = Uri.parse(url).getHost();
            return host != null && (host.equals("uestc.edu.cn") || host.endsWith(".uestc.edu.cn"));
        } catch (Exception ignored) { return false; }
    }

    private void injectScheduleDetector() {
        String script = "(function(){try{" +
            "var body=(document.body&&document.body.innerText)||'';" +
            "if(!/(节次|1-2节|3-4节)/.test(body)||!/(星期一|周一)/.test(body))return;" +
            "var tables=[].slice.call(document.querySelectorAll('table')).map(function(t){return {rows:[].slice.call(t.querySelectorAll('tr')).map(function(r){return {cells:[].slice.call(r.querySelectorAll('th,td')).map(function(c){return {text:c.innerText||c.textContent||''};})};})};});" +
            "if(tables.length)SchoolBridge.captureSchedule(JSON.stringify({url:location.href,title:document.title,tables:tables}),location.href);" +
            "}catch(e){}})();";
        webView.evaluateJavascript(script, null);
    }

    private void deliverPendingSchedule() {
        String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString("pending_schedule", "");
        if (raw.isEmpty()) return;
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().remove("pending_schedule").apply();
        webView.evaluateJavascript("window.handleSchoolSchedule(" + JSONObject.quote(raw) + ");", null);
    }

    public class SchoolBridge {
        @JavascriptInterface
        public void openPortal() {
            runOnUiThread(() -> {
                String saved = getSharedPreferences(PREFS, MODE_PRIVATE).getString("course_url", PORTAL_URL);
                webView.loadUrl(saved);
            });
        }

        @JavascriptInterface
        public void captureSchedule(String raw, String pageUrl) {
            String current = webView.getUrl();
            if (!isSchoolUrl(current) || raw == null || raw.length() < 50) return;
            getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putString("pending_schedule", raw)
                .putString("course_url", pageUrl)
                .apply();
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "已识别学校课表，正在导入", Toast.LENGTH_SHORT).show();
                webView.loadUrl(LOCAL_APP);
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKER_REQUEST && fileCallback != null) {
            Uri[] result = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
            fileCallback.onReceiveValue(result);
            fileCallback = null;
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
