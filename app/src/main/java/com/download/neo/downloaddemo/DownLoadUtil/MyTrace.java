package com.download.neo.downloaddemo.DownLoadUtil;

import android.util.Log;

/**
 * Created by Lenovo on 2016/1/13.
 */
public class MyTrace {
    public static String TAG = "NeoDownloadUtil";
    private static final  boolean debug=true;


    public static void d(String msg){
        if (debug)
            Log.d(TAG, msg);
    }

    public static void e(String msg){
        if (debug)
            Log.e(TAG, msg);
    }


}
