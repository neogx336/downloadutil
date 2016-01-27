package com.download.neo.downloaddemo.core;

import com.download.neo.downloaddemo.utilities.GlobalConstants;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Lenovo on 2016/1/24.
 */

public class ConnectThread implements Runnable {
    private final String url;
    //    是否在跑
    private volatile boolean isRunning;
    ConnectListener listener = null;

    public ConnectThread(String url, ConnectListener listener) {
        this.url = url;
        this.listener = listener;
    }

    @Override
    public void run() {
        HttpURLConnection connection = null;
        isRunning = true;

        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
//            connection.setRequestProperty("Range", "bytes=0-" + Integer.MAX_VALUE);
            connection.setConnectTimeout(GlobalConstants.CONNECT_TIMEOUT);
            connection.setReadTimeout(GlobalConstants.READ_TIME);
            int responseCode = connection.getResponseCode();
            int contentLength = connection.getContentLength();
            boolean isSupportRange = false;

//            if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
//                isSupportRange = true;
//                listener.onConnected(isSupportRange, contentLength);
//                isRunning = false;
//            }
//            else {
//                isSupportRange = false;
//            }
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String ranges = connection.getHeaderField("Accept-Ranges");
                if ("bytes".equals(ranges)) {
                    isSupportRange = true;
                }
            } else {
                listener.onConnectedError("Don't support Ranges!");
            }
            listener.onConnected(isSupportRange, contentLength);
            isRunning = false;

        } catch (IOException e) {
            isRunning = false;
            listener.onConnectedError(e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void cancel() {
        //中断进程
        Thread.currentThread().interrupt();
    }

    interface ConnectListener {
        void onConnected(boolean isSupportRange, int totalLength);

        void onError(String message);

        void onConnectedError(String msg);
    }
}
