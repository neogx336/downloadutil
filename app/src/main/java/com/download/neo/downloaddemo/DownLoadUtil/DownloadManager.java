package com.download.neo.downloaddemo.DownLoadUtil;

import android.content.Context;
import android.content.Intent;

import com.download.neo.downloaddemo.core.DownloadService;
import com.download.neo.downloaddemo.entities.DownloadEntry;
import com.download.neo.downloaddemo.notify.DataChanger;
import com.download.neo.downloaddemo.notify.DataWatcher;
import com.download.neo.downloaddemo.utilities.GlobalConstants;
import com.download.neo.downloaddemo.utilities.MyTrace;

/**
 * Created by Lenovo on 2016/1/13.
 */
public class DownloadManager {
    private static DownloadManager mInstance;
    private final Context context;
    //两次重复执行的时间
    private final int MIN_OPERATE_INTERVAL = GlobalConstants.MIN_OPERATE_INTERVAL;
    private long mLastoperratetime = 0;

    private DownloadManager(Context context) {
        this.context = context;
        context.startService(new Intent(context,DownloadService.class));
    }

    public static DownloadManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DownloadManager(context);
        }
        return mInstance;
    }

    /**
     * 添加任务
     */
    public void add(DownloadEntry entry) {


        //如果不可执行直接Return
        if (!checkIfExcuteable())
            return;
        MyTrace.d("manager-add");
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(GlobalConstants.KEY_DOWNLOAD_ENTRY, entry);
        intent.putExtra(GlobalConstants.KEY_DOWNLOAD_ACTION, GlobalConstants
                .KEY_DOWNLOAD_ACTION_ADD);
        context.startService(intent);
    }

    ;

    /**
     * 暂停
     */
    public void pause(DownloadEntry entry) {
        if (!checkIfExcuteable())
            return;
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(GlobalConstants.KEY_DOWNLOAD_ENTRY, entry);
        intent.putExtra(GlobalConstants.KEY_DOWNLOAD_ACTION, GlobalConstants
                .KEY_DOWNLOAD_ACTION_PAUSE);
        MyTrace.d("paused-service start");
        context.startService(intent);

    }

    ;

    /**
     * 恢复
     *
     * @param entry
     */
    public void resume(DownloadEntry entry) {
        if (!checkIfExcuteable())
            return;
        MyTrace.d("downloadermanager----resumed");
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(GlobalConstants.KEY_DOWNLOAD_ENTRY, entry);
        intent.putExtra(GlobalConstants.KEY_DOWNLOAD_ACTION, GlobalConstants
                .KEY_DOWNLOAD_ACTION_RESUME);
        MyTrace.d("resumed-service ");
        context.startService(intent);
    }

    private boolean checkIfExcuteable() {
        long currenttime = System.currentTimeMillis();
        if (currenttime - mLastoperratetime > MIN_OPERATE_INTERVAL) {
            mLastoperratetime = currenttime;
            return true;
        }
        return false;
    }


    /**
     * 取消下载
     *
     * @param entry
     */
    public void cancel(DownloadEntry entry) {
        if (!checkIfExcuteable())
            return;
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(GlobalConstants.KEY_DOWNLOAD_ENTRY, entry);
        intent.putExtra(GlobalConstants.KEY_DOWNLOAD_ACTION, GlobalConstants
                .KEY_DOWNLOAD_ACTION_CANCEL);
        context.startService(intent);
    }

    public void pauseAll() {
        if (!checkIfExcuteable())
            return;
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(GlobalConstants.KEY_DOWNLOAD_ACTION, GlobalConstants
                .KEY_DOWNLOAD_ACTION_PAUSE_ALL);
        context.startService(intent);
    }

    public void recoverAll() {
        if (!checkIfExcuteable())
            return;
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(GlobalConstants.KEY_DOWNLOAD_ACTION, GlobalConstants
                .KEY_DOWNLOAD_ACTION_RECOVERALL);
        context.startService(intent);
    }


    /**
     * @param watcher
     */
    public void addObserver(DataWatcher watcher) {
        DataChanger.getInstance(context).addObserver(watcher);
    }

    public void removeObserver(DataWatcher watcher) {
        DataChanger.getInstance(context).deleteObserver(watcher);
    }

    public DownloadEntry queryDownloadEntry(String id) {
       return DataChanger.getInstance(context).queryDownloadEntryById(id);
    }

    public void stopService()
    {
        Intent intent = new Intent(context, DownloadService.class);
        context.stopService(intent);
    }
}
