package com.moritanian.physicslauncher;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;


/**
 * Created by Moritanian on 2018/04/10.
 */

public class JsInterface {

    Context con;
    WebView webView;
    public static JsInterface instance;

    private String TAG = getClass().getSimpleName();

    public interface LaunchAppHandler {
        void launch(int id);
    }
    private LaunchAppHandler mLaunchAppHandler;

    public void setLaunchAppHandler(LaunchAppHandler handler){
        mLaunchAppHandler = handler;
    }



    public JsInterface(Context c, WebView webView)
    {
        this.con = c;
        this.webView = webView;
        instance = this;
    }

    @SuppressWarnings("UnusedDeclaration")
    @JavascriptInterface
    public void LogNative(String log){
        Log.i(TAG, log);
    }

    @SuppressWarnings("UnusedDeclaration")
    @JavascriptInterface
    public void launchApp(int id){
        if(mLaunchAppHandler != null){
            mLaunchAppHandler.launch(id);
        }
    }

    public void callJsLog(final String log){
        callJsFunction(String.format("console.log('%s')", log));
    }

    public void dispatchNativeInterfaceEvent(final String eventName, byte[] bytes){

        // byte[] -> jsonString
        StringBuilder bytesJson = new StringBuilder();
        bytesJson.append("[");
        for(byte b : bytes){
            int unsignedVal = b & 0xff;
            bytesJson.append(String.format("%d,", unsignedVal));
        }
        int len = bytesJson.length();
        bytesJson.replace(len - 1, len ,"]"); // 最後の , を　] に置き換え
        String funcString = String.format("if(nativeInterface.emit)nativeInterface.emit(\"%s\", \"%s\");", eventName, bytesJson.toString());
        callJsFunction(funcString);
    }

    public void dispatchNativeInterfaceEvent(final String eventName) {
        String funcString = String.format("if(nativeInterface.emit)nativeInterface.emit(\"%s\");", eventName);
        callJsFunction(funcString);
    }

    public void dispatchJsEvent(final String eventName, String value){
        String funcStr = "var event = new Event(\"%s\");" +
                "event.value = %s;" +
                "window.dispatchEvent(event);";
        callJsFunction(String.format(funcStr, eventName, value));
    }

    public void dispatchJsEvent(final String eventName, float x, float y, float z){
        String funcStr = "var event = new Event(\"%s\");" +
                "event.x = %f;" +
                "event.y = %f;" +
                "event.z = %f;" +
                "window.dispatchEvent(event);";
        callJsFunction(String.format(funcStr, eventName, x, y ,z));
    }

    public void dispatchJsEvent(final String eventName, float x, float y, float z, float w){
        String funcStr = "var event = new Event(\"%s\");" +
                "event.x = %f;" +
                "event.y = %f;" +
                "event.z = %f;" +
                "event.w = %f;" +
                "window.dispatchEvent(event);";
        callJsFunction(String.format(funcStr, eventName, x, y ,z, w));
    }

    public void callJsInterfaceFunction(String func, int id, String s1){
        String funcStr = "nativeInterface.%s( %d, '%s');";
        callJsFunction(String.format(funcStr, func, id, s1));
    }

    public void callJsInterfaceFunction(String func, int id, int x, int y, int z){
        String funcStr = "nativeInterface.%s( %d, %d, %d, %d);";
        callJsFunction(String.format(funcStr, func, id, x, y, z));
    }

    public void callJsFunction(final String func) {
        ((Activity)con).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript(func, null);
                } else {
                    webView.loadUrl("javascript:" + func);
                }
            }
        });
    }

}
