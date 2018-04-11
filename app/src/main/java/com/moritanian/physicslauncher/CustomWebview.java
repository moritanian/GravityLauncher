package com.moritanian.physicslauncher;

/**
 * Created by Moritanian on 2018/04/10.
 */

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import java.util.Set;


class CustomWebView extends WebViewClient {

    final String NATIVE_INTERFACE_NAME = "nativeInterface";

    Context mContext;
    WebView webView;
    FrameLayout progressBarBackground;
    String viewUrl;

    public CustomWebView(Context con, WebView webView){
        this(con, webView, null);
    }

    public CustomWebView(Context con,
                         WebView webView,
                         FrameLayout progressBarBackground){
        mContext = con;
        this.webView = webView;
        this.progressBarBackground = progressBarBackground;
        initWebView();
    }

    @Override
    public void onPageStarted (WebView view,
                               String url,
                               Bitmap favicon){

        ((Activity)mContext).setTitle(url);
    }

    //ページの読み込み完了
    @Override
    public void onPageFinished(WebView view, String url) {

        if(progressBarBackground != null){
            progressBarBackground.setVisibility(View.INVISIBLE);
        }


    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        // local server のみssl証明書のないhttps接続許可
        if(viewUrl.startsWith("https://192")){
            handler.proceed();
        } else {
            super.onReceivedSslError(view, handler, error);
        }
    }

    private void initWebView(){
        // for webView debug
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }


        //jacascriptを許可する
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setPluginState(WebSettings.PluginState.ON);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setLoadWithOverviewMode(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);
        settings.setBuiltInZoomControls(true);
        settings.setAllowFileAccess(true);
        settings.setSupportZoom(false);
        settings.setAllowContentAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setWebChromeClient(new WebChromeClient(){
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                request.grant(request.getResources());
            }
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowUniversalAccessFromFileURLs(true);
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setAllowContentAccess(true);
            settings.setAppCacheEnabled(true);
        }

        JsInterface jsInterface = JsInterface.instance;
        if(jsInterface == null){
            jsInterface = new JsInterface (mContext, webView);
        }
        webView.addJavascriptInterface(jsInterface, NATIVE_INTERFACE_NAME);

        // カスタムWebViewを設定する
        webView.setWebViewClient(this);

    }

    public void loadWebView(String url){
        webView.loadUrl(url);
    }
}
