package com.moritanian.physicslauncher;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Moritanian on 2018/04/10.
 */

public class LauncherFragment extends Fragment {

    private String TAG = getClass().getSimpleName();
    List<AppData> appList;

    // Fragmentで表示するViewを作成するメソッド
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // 先ほどのレイアウトをここでViewとして作成します
        return inflater.inflate(R.layout.fragment_launcher, container, false);
    }

    // Viewが生成し終わった時に呼ばれるメソッド
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        WebView w = (WebView)getActivity().findViewById(R.id.my_web_view);

        CustomWebView c = new CustomWebView(getActivity(), w){
            //ページの読み込み完了
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                for(AppData d: appList){
                    String base64 = ConvertBase64FromDrawable(d.icon);
                    JsInterface.instance.callJsInterfaceFunction("createIcon", d.id, base64);

                }

            }

        };

        String url = "file:///android_asset/android_home.html";
        //String url = "http://localhost:8080/Androino/server/test/physics/android_home.html";
        c.loadWebView(url);
        //ShowListView();

        appList = GetInstalledAppList();
        JsInterface.instance.setLaunchAppHandler(new JsInterface.LaunchAppHandler() {
            @Override
            public void launch(int id) {
                AppData item = appList.get(id - 1);
                PackageManager pManager = getActivity().getPackageManager();
                Intent intent = pManager.getLaunchIntentForPackage(item.pname);
                startActivity(intent);
            }
        });

    }

    public List<AppData> GetInstalledAppList(){
        // 端末にインストール済のアプリケーション一覧情報を取得
        final PackageManager pm = getActivity().getPackageManager();
        final int flags =  PackageManager.GET_ACTIVITIES | PackageManager.GET_SERVICES;
        final List<ApplicationInfo> installedAppList = pm.getInstalledApplications(0);

        // リストに一覧データを格納する
        final List<AppData> dataList = new ArrayList<AppData>();
        int id = 1;
        for (ApplicationInfo app : installedAppList) {

            // 起動不可能
            if(pm.getLaunchIntentForPackage(app.packageName) == null){
                continue;
            }

            AppData data = new AppData();
            data.id = id;
            data.label = app.loadLabel(pm).toString();
            data.icon = app.loadIcon(pm);
            data.pname = app.packageName;
            dataList.add(data);
            id ++;
            if(id == 20){
                break;
            }
        }

        return dataList;
    }

/*
    public void ShowListView(){
        Activity activity = getActivity();
        // リストビューにアプリケーションの一覧を表示する
        final ListView listView = (ListView) activity.findViewById(R.id.app_list);;

        final List<AppData> dataList = GetInstalledAppList();
        listView.setAdapter(new AppListAdapter(activity, dataList));
        //クリック処理
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AppData item = dataList.get(position);
                PackageManager pManager = getActivity().getPackageManager();
                Intent intent = pManager.getLaunchIntentForPackage(item.pname);
                startActivity(intent);
            }
        });
    }
*/
    // アプリケーションデータ格納クラス
    private static class AppData {
        int id;
        String label;
        Drawable icon;
        String pname;

    }

    // アプリケーションのラベルとアイコンを表示するためのアダプタークラス
    private static class AppListAdapter extends ArrayAdapter<AppData> {

        private final LayoutInflater mInflater;

        public AppListAdapter(Context context, List<AppData> dataList) {
            super(context, R.layout.app_item);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            addAll(dataList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = new ViewHolder();

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.app_item, parent, false);
                holder.textLabel = (TextView) convertView.findViewById(R.id.label);
                holder.imageIcon = (ImageView) convertView.findViewById(R.id.icon);
                holder.packageName = (TextView) convertView.findViewById(R.id.pname);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            // 表示データを取得
            final AppData data = getItem(position);
            // ラベルとアイコンをリストビューに設定
            holder.textLabel.setText(data.label);
            holder.imageIcon.setImageDrawable(data.icon);
            holder.packageName.setText(data.pname);

            return convertView;
        }
    }
    // ビューホルダー
    private static class ViewHolder {
        TextView textLabel;
        ImageView imageIcon;
        TextView packageName;
    }

    private String ConvertBase64FromDrawable(Drawable d){
        Bitmap b =  ((BitmapDrawable)d).getBitmap();
        return ByteArrayToBase64( BitmapToByteArray(b));
    }

    private byte[] BitmapToByteArray(Bitmap bmp){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        return bytes;
    }

    private String ByteArrayToBase64(byte[] bytes){
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }
}
