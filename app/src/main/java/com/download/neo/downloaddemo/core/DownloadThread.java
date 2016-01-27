package com.download.neo.downloaddemo.core;

import android.os.Environment;

import com.download.neo.downloaddemo.entities.DownloadEntry;
import com.download.neo.downloaddemo.utilities.CreateFileUtil;
import com.download.neo.downloaddemo.utilities.GlobalConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 下载线程
 * Created by Lenovo on 2016/1/13.
 */
public class DownloadThread implements Runnable {
    private final String url;
    private boolean isSingeDownload = false;
    private int index;
    private int startPos;
    private int endPos;
    private String path;
    //    private String pathdir;
//    private String filename;
    DownLoadThreadListener listener;
    //    private volatile boolean isRunning = false;
    private volatile boolean isPaused = false;
    DownloadEntry.DownloadStatus mStatus;
    private volatile boolean isCancelled;
    private boolean isError;

    public DownloadThread(int index, int startPos, int endPos, String url, DownLoadThreadListener
            listener) {
        this.index = index;
        this.startPos = startPos;
        this.endPos = endPos;
        this.url = url;
        this.path = Environment.getExternalStorageDirectory() + File.separator + "neodownload" +
                File.separator + url.substring(url.lastIndexOf("/") + 1);
        this.listener = listener;
        if (startPos == 0 && endPos == 0) {
            isSingeDownload = true;
        }

//        this.pathdir = Environment.getExternalStorageDirectory() + File.separator + "neodownload";
//        this.filename = url.substring(url.lastIndexOf("/") + 1);
    }

    @Override
    public void run() {
        mStatus = DownloadEntry.DownloadStatus.downloading;
        HttpURLConnection connection = null;
//        isRunning = true;

        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
//           设置开始结事位置

            if (!isSingeDownload) {
//          多线程就加一个RANGE头
                connection.setRequestProperty("Range", "bytes=" + startPos + '-' + endPos);
            }
            connection.setConnectTimeout(GlobalConstants.CONNECT_TIMEOUT);
            connection.setReadTimeout(GlobalConstants.READ_TIME);
            int responeCode = connection.getResponseCode();
//            int contentLength = connection.getContentLength();

            File file = new File(path);
            //文件不存在则创建
            if (!file.exists()) {
                CreateFileUtil.createFile(path);
            }
            RandomAccessFile raf = null;
            InputStream is = null;
            FileOutputStream fos = null;
//            多线程下载
            if (responeCode == HttpURLConnection.HTTP_PARTIAL) {
//
                raf = new RandomAccessFile(file, "rw");
                raf.seek(startPos);
                is = connection.getInputStream();
//                BUFFER值不能太大，不然容易OOM
                byte[] buffer = new byte[4096];
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    if (isPaused || isCancelled) {
//                        mStatus= DownloadEntry.DownloadStatus.paused;
//                        listener.onDownloadPaused(index);
                        break;
                    }
                    raf.write(buffer, 0, len);
                    listener.onThreadProgressChanged(index, len);
                }
                raf.close();
                is.close();
//                isRunning = false;

            }
            //       responeCode==200         单线程下载
            else if (responeCode == HttpURLConnection.HTTP_OK) {
                fos = new FileOutputStream(file);
                is = connection.getInputStream();
                byte[] buffer = new byte[4096];
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    if (isPaused || isCancelled) {
//                        mStatus= DownloadEntry.DownloadStatus.paused;
//                        listener.onDownloadPaused(index);
                        break;
                    }
                    fos.write(buffer, 0, len);
//                    加上同步锁 防止并发而造成的数据脏读
                    synchronized (listener) {
                        listener.onThreadProgressChanged(index, len);
                    }
                }
                fos.close();
                is.close();
            }
//
            synchronized (listener) {
                if (isPaused) {
                    mStatus = DownloadEntry.DownloadStatus.paused;
                    listener.onDownloadPaused(index);
                } else if (isCancelled) {
                    mStatus = DownloadEntry.DownloadStatus.cancel;
                    listener.onDownloadCancel(index);
                } else if (isError) {
                    mStatus = DownloadEntry.DownloadStatus.error;
                    listener.onDownloadCancel(index);
                } else {
                    mStatus = DownloadEntry.DownloadStatus.completed;
                    listener.onDownLoadCompleted(index);
                }
            }
        } catch (IOException e) {
            if (isPaused) {
                mStatus = DownloadEntry.DownloadStatus.paused;
                listener.onDownloadPaused(index);
            } else if (isCancelled) {
                mStatus = DownloadEntry.DownloadStatus.cancel;
                listener.onDownloadCancel(index);
            } else {
                mStatus = DownloadEntry.DownloadStatus.error;
                listener.onDownLoadError(index, e.getMessage());
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public boolean isRunning() {
        return mStatus == DownloadEntry.DownloadStatus.downloading;
    }


    /**
     * 供外层调用 停止当前线程
     */
    public void pause() {
        // TODO: 2016/1/25   取消
        isPaused = true;
        Thread.currentThread().interrupt();
        Thread.currentThread().interrupt();
    }

    public boolean isPaused() {
        return mStatus == DownloadEntry.DownloadStatus.paused || mStatus == DownloadEntry
                .DownloadStatus.completed;
    }

    public void cancel() {
        isCancelled = true;
        Thread.currentThread().interrupt();
    }

    public boolean isCancelled() {
        return mStatus == DownloadEntry.DownloadStatus.cancel || mStatus == DownloadEntry
                .DownloadStatus.completed;
    }

    public boolean isError() {
        return mStatus == DownloadEntry.DownloadStatus.error;
    }

    public void cancelByError() {
        isError = true;
        Thread.currentThread().interrupt();
    }

    public boolean isCompleted() {
        return mStatus== DownloadEntry.DownloadStatus.completed;
    }


    interface DownLoadThreadListener {
        void onThreadProgressChanged(int index, int progress);

        void onDownLoadCompleted(int index);

        void onDownLoadError(int index, String message);

        void onDownloadPaused(int index);

        void onDownloadCancel(int index);

//        void OnConnectError(String msg);


    }
}
