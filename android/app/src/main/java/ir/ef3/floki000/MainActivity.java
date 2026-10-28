package ir.ef3.floki000;

import android.app.Activity;
import android.app.Dialog;
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
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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
    private TextView pageTitle;
    private TextView openPanelButton;
    private int selectedSite = 0;
    private Dialog sitePanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableImmersiveMode();
        getWindow().setStatusBarColor(Color.rgb(12, 15, 22));
        getWindow().setNavigationBarColor(Color.rgb(12, 15, 22));
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(12, 15, 22));

        LinearLayout appLayout = new LinearLayout(this);
        appLayout.setOrientation(LinearLayout.VERTICAL);
        appLayout.setBackgroundColor(Color.rgb(12, 15, 22));
        root.addView(appLayout, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // Slim top frame: the web page starts below it, never underneath it.
        LinearLayout topBar = new LinearLayout(this);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(dp(16), 0, dp(16), 0);
        topBar.setBackgroundColor(Color.rgb(12, 15, 22));
        pageTitle = new TextView(this);
        pageTitle.setText("EF3.IR");
        pageTitle.setTextColor(Color.rgb(215, 222, 235));
        pageTitle.setTextSize(13);
        pageTitle.setTypeface(Typeface.DEFAULT_BOLD);
        topBar.addView(pageTitle, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
        appLayout.addView(topBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(34)));

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(10, 12, 18));
        appLayout.addView(webView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        // Slim bottom frame is part of the layout; the web page does not extend behind it.
        FrameLayout bottomBar = new FrameLayout(this);
        bottomBar.setBackgroundColor(Color.rgb(12, 15, 22));
        openPanelButton = new TextView(this);
        openPanelButton.setText("‹");
        openPanelButton.setTextColor(Color.rgb(173, 190, 218));
        openPanelButton.setTextSize(25);
        openPanelButton.setGravity(Gravity.CENTER);
        openPanelButton.setTypeface(Typeface.DEFAULT);
        openPanelButton.setContentDescription("باز کردن فهرست سایت‌ها");
        openPanelButton.setBackground(makeBackground(Color.rgb(22, 28, 40), Color.rgb(39, 49, 68), dp(14)));
        openPanelButton.setOnClickListener(v -> showSitePanel());
        FrameLayout.LayoutParams arrowParams = new FrameLayout.LayoutParams(dp(42), dp(30),
                Gravity.END | Gravity.CENTER_VERTICAL);
        arrowParams.setMargins(0, 0, dp(10), 0);
        bottomBar.addView(openPanelButton, arrowParams);
        appLayout.addView(bottomBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(42)));

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
            // Restored the previous page and navigation history.
        } else {
            webView.loadUrl(HOME_URL);
        }
    }

    private GradientDrawable makeBackground(int fill, int stroke, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(radius);
        drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private void showSitePanel() {
        if (sitePanel != null && sitePanel.isShowing()) {
            sitePanel.dismiss();
            return;
        }

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(16), dp(14), dp(16), dp(12));
        panel.setBackground(makeBackground(Color.rgb(18, 23, 33), Color.rgb(49, 62, 84), dp(20)));

        TextView heading = new TextView(this);
        heading.setText("رفتن به");
        heading.setTextColor(Color.rgb(228, 234, 245));
        heading.setTextSize(14);
        heading.setTypeface(Typeface.DEFAULT_BOLD);
        heading.setGravity(Gravity.END);
        panel.addView(heading, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(30)));

        addSiteRow(panel, "EF3.IR", "سایت اصلی", 0);
        addSiteRow(panel, "PROXY.EF3.IR", "پروکسی", 1);

        TextView close = new TextView(this);
        close.setText("بستن");
        close.setTextColor(Color.rgb(150, 166, 191));
        close.setTextSize(12);
        close.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(34));
        closeParams.topMargin = dp(4);
        panel.addView(close, closeParams);
        close.setOnClickListener(v -> {
            if (sitePanel != null) sitePanel.dismiss();
        });

        sitePanel = new Dialog(this);
        sitePanel.requestWindowFeature(Window.FEATURE_NO_TITLE);
        sitePanel.setContentView(panel);
        Window window = sitePanel.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = getResources().getDisplayMetrics().widthPixels - dp(28);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            lp.dimAmount = 0.28f;
            lp.y = dp(50);
            window.setAttributes(lp);
        }
        sitePanel.setOnDismissListener(d -> enableImmersiveMode());
        sitePanel.show();
        if (window != null) {
            window.setLayout(getResources().getDisplayMetrics().widthPixels - dp(28),
                    WindowManager.LayoutParams.WRAP_CONTENT);
        }
        enableImmersiveMode();
    }

    private void addSiteRow(LinearLayout panel, String name, String subtitle, int index) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(8), dp(12), dp(8));
        row.setBackground(makeBackground(
                selectedSite == index ? Color.rgb(30, 42, 60) : Color.rgb(23, 29, 41),
                Color.rgb(42, 53, 72), dp(12)));

        TextView title = new TextView(this);
        title.setText(name);
        title.setTextColor(Color.rgb(224, 231, 243));
        title.setTextSize(13);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.END);
        row.addView(title, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView detail = new TextView(this);
        detail.setText(subtitle);
        detail.setTextColor(Color.rgb(143, 157, 180));
        detail.setTextSize(11);
        detail.setGravity(Gravity.END);
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        detailParams.topMargin = dp(2);
        row.addView(detail, detailParams);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(58));
        rowParams.topMargin = dp(7);
        panel.addView(row, rowParams);
        row.setOnClickListener(v -> {
            selectedSite = index;
            pageTitle.setText(SITE_NAMES[index]);
            webView.stopLoading();
            webView.loadUrl(SITE_URLS[index]);
            if (sitePanel != null) sitePanel.dismiss();
        });
    }

    private void enableImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(
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
        if (sitePanel != null && sitePanel.isShowing()) {
            sitePanel.dismiss();
        } else if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
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