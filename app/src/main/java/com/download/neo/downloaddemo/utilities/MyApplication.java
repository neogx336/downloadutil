package com.download.neo.downloaddemo.utilities;

import android.app.Application;

import com.download.neo.downloaddemo.DownLoadUtil.DownloadManager;

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

