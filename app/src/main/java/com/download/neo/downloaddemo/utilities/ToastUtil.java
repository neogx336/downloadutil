package com.download.neo.downloaddemo.utilities;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Lenovo on 2016/1/14.
 */
public class ToastUtil {
    public static  void ToastSHOW(Context context,String msg)
    {
        Toast.makeText(context,msg,Toast.LENGTH_LONG).show();
    }
}
