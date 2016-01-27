package com.download.neo.downloaddemo.DownLoadUtil;

/**
 * Created by Lenovo on 2016/1/13.
 */
public class GlobalConstants {
    public static final String KEY_DOWNLOAD_ENTRY = "KEY_DOWNLOAD_ENTRY";
    public static final String KEY_DOWNLOAD_ACTION ="KEY_DOWNLOAD_ACTION" ;
    public static final int KEY_DOWNLOAD_ACTION_ADD =1 ;
    public static final int KEY_DOWNLOAD_ACTION_PAUSE =2 ;
    public static final int KEY_DOWNLOAD_ACTION_RESUME =3 ;
    public static final int KEY_DOWNLOAD_ACTION_CANCEL =4 ;
    //最大任务数
    public static final int MAX_DOWNLOAD_TASK = 3;
    public static final int KEY_DOWNLOAD_ACTION_PAUSE_ALL =6 ;
    public static final int KEY_DOWNLOAD_ACTION_RECOVERALL = 7;
    public static final int MIN_OPERATE_INTERVAL = 500*1;
    public static final int MAX_DOWNLOAD_THREADS=3;

    public static final int CONNECT_TIMEOUT = 30000;
    public static final int READ_TIME = 10000;
}
