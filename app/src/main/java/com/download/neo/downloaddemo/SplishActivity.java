package com.download.neo.downloaddemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.download.neo.downloaddemo.DownLoadUtil.DownloadManager;
import com.download.neo.downloaddemo.DownLoadUtil.MyTrace;

/**
 * Created by Lenovo on 2016/1/23.
 */
public class SplishActivity extends Activity {

    Handler mhandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            jumpTo();
        }
    };

    private void jumpTo() {
        MyTrace.d("spact---jumpTo");
//        Intent intent = new Intent(this,MainActivity.class);
        Intent intent = new Intent(this,SecondActivity.class);
        startActivity(intent);
        finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spact);
//        DownloadManager.getInstance(getApplicationContext());
        mhandler.sendEmptyMessageDelayed(0, 500);


    }



}
