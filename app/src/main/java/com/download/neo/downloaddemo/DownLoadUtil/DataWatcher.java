package com.download.neo.downloaddemo.DownLoadUtil;

import java.util.Observable;
import java.util.Observer;

/**
 * 观察者
 * Created by Lenovo on 2016/1/13.
 */
public abstract class DataWatcher implements Observer {


    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof DownloadEntry) {
            notifyUpdate((DownloadEntry) data);
        }

    }

    public abstract void notifyUpdate(DownloadEntry data);
}

