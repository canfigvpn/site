package ir.ef3.floki000;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String HOME_URL = "https://www.ef3.ir/";
    private static final String[] SITE_NAMES = {"EF3.IR", "PROXY.EF3.IR"};
    private static final String[] SITE_URLS = {"https://www.ef3.ir/", "https://proxy.ef3.ir/"};
    private static final int FILE_CHOOSER_REQUEST = 1001;

    private FrameLayout root;
    private WebView webView;
    private ValueCallback<Uri[]> fileChooserCallback;
    private TextView siteMenuButton;
    private int selectedSite = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableImmersiveMode();
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(10, 12, 18));

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(10, 12, 18));
        root.addView(webView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        siteMenuButton = new TextView(this);
        siteMenuButton.setText("☰");
        siteMenuButton.setTextColor(Color.WHITE);
        siteMenuButton.setTextSize(23);
        siteMenuButton.setGravity(Gravity.CENTER);
        siteMenuButton.setTypeface(Typeface.DEFAULT_BOLD);
        siteMenuButton.setContentDescription("انتخاب سایت");
        GradientDrawable buttonBackground = new GradientDrawable();
        buttonBackground.setColor(Color.rgb(30, 39, 57));
        buttonBackground.setCornerRadius(dp(18));
        buttonBackground.setStroke(dp(1), Color.rgb(71, 91, 125));
        siteMenuButton.setBackground(buttonBackground);
        siteMenuButton.setElevation(dp(8));
        siteMenuButton.setOnClickListener(v -> showSitePicker());

        FrameLayout.LayoutParams menuParams = new FrameLayout.LayoutParams(dp(50), dp(50), Gravity.TOP | Gravity.END);
        menuParams.setMargins(0, dp(14), dp(14), 0);
        root.addView(siteMenuButton, menuParams);
        setContentView(root);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(false);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUrl(request.getUrl().toString());
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(url);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback,
                                             FileChooserParams params) {
                if (fileChooserCallback != null) fileChooserCallback.onReceiveValue(null);
                fileChooserCallback = callback;
                try {
                    startActivityForResult(params.createIntent(), FILE_CHOOSER_REQUEST);
                    return true;
                } catch (ActivityNotFoundException exception) {
                    fileChooserCallback = null;
                    Toast.makeText(MainActivity.this, "انتخاب فایل در دسترس نیست.", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });

        webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            try {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimeType);
                request.addRequestHeader("User-Agent", userAgent);
                String cookies = CookieManager.getInstance().getCookie(url);
                if (cookies != null) request.addRequestHeader("Cookie", cookies);
                String filename = android.webkit.URLUtil.guessFileName(url, contentDisposition, mimeType);
                request.setTitle(filename);
                request.setDescription("دانلود از EF3");
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                ((DownloadManager) getSystemService(DOWNLOAD_SERVICE)).enqueue(request);
                Toast.makeText(MainActivity.this, "دانلود شروع شد.", Toast.LENGTH_SHORT).show();
            } catch (Exception exception) {
                Toast.makeText(MainActivity.this, "دانلود انجام نشد؛ دوباره تلاش کنید.", Toast.LENGTH_LONG).show();
            }
        });

        if (savedInstanceState != null && webView.restoreState(savedInstanceState) != null) {
            // Restore the previous page and navigation history.
        } else {
            webView.loadUrl(HOME_URL);
        }
    }

    private void enableImmersiveMode() {
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) enableImmersiveMode();
    }

    private void showSitePicker() {
        String[] options = {"EF3.IR  •  سایت اصلی", "PROXY.EF3.IR  •  پروکسی"};
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("انتخاب سایت")
                .setItems(options, (d, which) -> {
                    selectedSite = which;
                    webView.stopLoading();
                    webView.loadUrl(SITE_URLS[which]);
                    Toast.makeText(this, "در حال باز کردن " + SITE_NAMES[which], Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("بستن", null)
                .create();
        dialog.setOnDismissListener(d -> enableImmersiveMode());
        dialog.show();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private boolean handleUrl(String url) {
        if (url == null) return false;
        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();

        if (scheme.equals("http") || scheme.equals("https")) return false;

        if (scheme.equals("tel") || scheme.equals("mailto") || scheme.equals("sms")) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            } catch (ActivityNotFoundException exception) {
                Toast.makeText(this, "برنامه‌ای برای این پیوند پیدا نشد.", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        if (scheme.equals("intent")) {
            try {
                startActivity(Intent.parseUri(url, Intent.URI_INTENT_SCHEME));
            } catch (Exception exception) {
                Toast.makeText(this, "باز کردن پیوند ممکن نیست.", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST && fileChooserCallback != null) {
            Uri[] results = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
            fileChooserCallback.onReceiveValue(results);
            fileChooserCallback = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (webView != null) webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (fileChooserCallback != null) {
            fileChooserCallback.onReceiveValue(null);
            fileChooserCallback = null;
        }
        if (webView != null) {
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.destroy();
        }
        super.onDestroy();
    }
}