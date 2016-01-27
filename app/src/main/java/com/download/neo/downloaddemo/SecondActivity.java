package com.download.neo.downloaddemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.download.neo.downloaddemo.DownLoadUtil.DataWatcher;
import com.download.neo.downloaddemo.DownLoadUtil.Db.EntryDao;
import com.download.neo.downloaddemo.DownLoadUtil.DownloadEntry;
import com.download.neo.downloaddemo.DownLoadUtil.DownloadManager;
import com.download.neo.downloaddemo.DownLoadUtil.GlobalConstants;
import com.download.neo.downloaddemo.DownLoadUtil.MyTrace;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SecondActivity extends Activity implements View.OnClickListener {
    TextView tv1;
    Button btn1, btn_resume, btn_pause, btn_cancel, btn_clear, btn_thread;
    DownloadManager mdownloadManager;
    DataWatcher watcher = new DataWatcher() {
        @Override
        public void notifyUpdate(DownloadEntry data) {
            //需要DataWatcher进行数据更新 才能实现断点续传
            entry = data;
        }
    };
    private String url;
    private DownloadEntry entry;
    private final int thread_single = 0;
    private final int thread_mul = 1;


    Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case thread_single:
                    tv1.setText("单线程");
                    break;
                case thread_mul:
                    tv1.setText("多线程");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        mdownloadManager = DownloadManager.getInstance(this);

        initiViews();
    }

    private void initiViews() {
        tv1 = (TextView) findViewById(R.id.tv_1);
        btn1 = (Button) findViewById(R.id.btn1);
        btn_resume = (Button) findViewById(R.id.btn_resume);
        btn_pause = (Button) findViewById(R.id.btn_pause);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        btn_clear = (Button) findViewById(R.id.btn_clear);
        btn_thread = (Button) findViewById(R.id.btn_thread);

        btn1.setOnClickListener(this);
        btn_resume.setOnClickListener(this);
        btn_pause.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        btn_clear.setOnClickListener(this);
        btn_thread.setOnClickListener(this);
        //单线程
        // http://ftp-idc.pconline.com
        // .cn/fd6099c6a62ea4d344307ef9438cfcd8/pub/download/201010/PDFXVwer.zip
//        url="http://gdown.baidu.com/data/wisegame/4a4520f827e73a3b/aiqiyi_80700.apk";
//        url = "http://ftp-idc.pconline.com" +
//                ".cn/fd6099c6a62ea4d344307ef9438cfcd8/pub/download/201010/PDFXVwer.zip";
//      url=   "http://enkj.newhua.com/down/SoftrosLANMessengerSetup.zip";     //9MB+
        url="http://ftp-idc.pconline.com.cn/26de0f0d68d238e998b045b6027dbc92/pub/download/201010/OfflineBaiduPlayer_152.exe";

//       url= "http://skycnxz2.wy119.com/Mouku_fr.zip";
//        url="http://ftp-idc.pconline.com
// .cn/fd6099c6a62ea4d344307ef9438cfcd8/pub/download/201010/PDFXVwer.zip";
//        url="http://img.daimg.com/uploads/allimg/160123/1-160123002004.jpg";
        entry = new DownloadEntry(url, "100");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            开始
            case R.id.btn1:
                mdownloadManager.add(entry);
                break;
//            恢复
            case R.id.btn_resume:
                mdownloadManager.resume(entry);
                break;

//            暂停
            case R.id.btn_pause:
                mdownloadManager.pause(entry);
                break;

//            取消
            case R.id.btn_cancel:
                mdownloadManager.cancel(entry);
                break;
            case R.id.btn_clear:
                new EntryDao(getApplicationContext()).delete(entry);
                break;
            case R.id.btn_thread:
                new ThreadTest().start();
                break;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mdownloadManager.addObserver(watcher);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mdownloadManager.removeObserver(watcher);
    }


    class ThreadTest extends Thread {
        public void run() {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Range", "bytes=0-" + Integer.MAX_VALUE);
                connection.setConnectTimeout(GlobalConstants.CONNECT_TIMEOUT);
                connection.setReadTimeout(GlobalConstants.READ_TIME);
//                connection.connect();
                int responseCode = connection.getResponseCode();
                int contentLength = connection.getContentLength();
                boolean isSupportRange = false;
                if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                    MyTrace.d("connect OK----");
                    isSupportRange = true;
                    Message msg = mhandler.obtainMessage();
                    msg.what = thread_mul;
                    mhandler.sendMessage(msg);
                } else if (responseCode == HttpURLConnection.HTTP_OK) {
                    Message msg = mhandler.obtainMessage();
                    msg.what = thread_single;
                    mhandler.sendMessage(msg);
                }


            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

        }

    }
}
