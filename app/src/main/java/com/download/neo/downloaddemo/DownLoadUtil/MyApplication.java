package com.download.neo.downloaddemo.DownLoadUtil;

import android.app.Application;

/**
 * Created by Lenovo on 2016/1/24.
 */
public class MyApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();

        //初始化
        initiAll();



    }

    private void initiAll() {
        DownloadManager.getInstance(getApplicationContext());
    }
}

