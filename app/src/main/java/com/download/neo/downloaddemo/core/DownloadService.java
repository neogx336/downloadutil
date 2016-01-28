package com.download.neo.downloaddemo.core;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.download.neo.downloaddemo.entities.DownloadEntry;
import com.download.neo.downloaddemo.db.EntryDao;
import com.download.neo.downloaddemo.notify.DataChanger;
import com.download.neo.downloaddemo.utilities.GlobalConstants;
import com.download.neo.downloaddemo.utilities.MyTrace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;


/**
 * 下载服务
 * Created by Lenovo on 2016/1/13.
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class DownloadService extends Service {
    public static final int NOTIFI_DOWNLOADING = 1;
    public static final int NOTIFI_UPDATING = 2;
    public static final int NOTIFI_PAUSED_OR_CANCELLED = 3;
    public static final int NOTIFI_COMPLETED = 4;
    public static final int NOTIFY_CONNECTING = 5;
    public static final int NOTITY_ERROR = 6;
    public static final int NOTITY_NETWORK_ERROR = 7;

    private Context context;
//    各种异常  容错能力
    /**
     * 1.网络异常
     * 2.SD卡无法访问
     * 3.磁盘空间不足
     *
     * 有可能出现错误的类
     * 1.DownloadThread
     * 2.ConnetThread
     */

    //    任务队列
    private HashMap<String, DownloadTask> mDownloadingTasks = new HashMap<String, DownloadTask>();
    ArrayList<String> lists = new ArrayList<>();
    private ExecutorService mExecutors;
    private DataChanger mdataChager;

    /**
     * 等待队列
     */
    private LinkedBlockingDeque<DownloadEntry> mWaittingQueue = new
            LinkedBlockingDeque<DownloadEntry>();

    /**
     * Handler用于连接UI线程与子线程  他们进行发消息进行UI的更新
     */
    private Handler mhandler = new Handler() {
        @Override

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case NOTIFI_COMPLETED:
                case NOTIFI_PAUSED_OR_CANCELLED:
                    checkNext((DownloadEntry)msg.obj);
                    break;
                //网络异常
                case NOTITY_NETWORK_ERROR:
                    break;
            }
            DataChanger.getInstance(context).postStatus((DownloadEntry) msg.obj);
        }
    };

    private void checkNext(DownloadEntry obj) {
        mDownloadingTasks.remove(obj);
        //检查队  下下一个，从等待队列中拿出另一个出来
        DownloadEntry newentry = mWaittingQueue.poll();
        if (newentry != null) {
            startDownload(newentry);
        }

    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        MyTrace.d("DownloadService-onCreate");
        super.onCreate();
        mExecutors = Executors.newCachedThreadPool();
        this.context = getApplicationContext();
        mdataChager = DataChanger.getInstance(context);
        ArrayList<DownloadEntry> mDownloadEntries = new EntryDao(context).queryAll();
        if (mDownloadEntries != null) {
            for (DownloadEntry entry : mDownloadEntries) {
                if (entry.status == DownloadEntry.DownloadStatus.downloading || entry.status ==
                        DownloadEntry.DownloadStatus.waiting) {
                    entry.status = DownloadEntry.DownloadStatus.paused;
                    addDownload(entry);
                }
                mdataChager.addToTempEntryMap(entry.id, entry);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyTrace.d("nloadService-onStartCommand");
//        Service 从Intent中拿到 Entry中的实体与标志来进行处理
        DownloadEntry entry = (DownloadEntry) intent.getSerializableExtra(GlobalConstants
                .KEY_DOWNLOAD_ENTRY);
        //查询对象是否存在，如果有就复用
//        if (entry!=null)
//        {
//            if (mdataChager.containsDownloadEntry(entry.id)) {
//                entry = mdataChager.queryDownloadEntryById(entry.id);
//            }
//        }


        int action = intent.getIntExtra(GlobalConstants.KEY_DOWNLOAD_ACTION, -1);
//       分送到对应的Action
        doActioin(action, entry);
        return super.onStartCommand(intent, flags, startId);
    }

    private void doActioin(int action, DownloadEntry entry) {
        MyTrace.d("doActioin--" + action);
//        实现各个Action
        switch (action) {
//            添加
            case GlobalConstants.KEY_DOWNLOAD_ACTION_ADD:
                MyTrace.d("doActioin--" + GlobalConstants.KEY_DOWNLOAD_ACTION_ADD);
//                startDownload(entry);
                addDownload(entry);
                break;
//            暂停
            case GlobalConstants.KEY_DOWNLOAD_ACTION_PAUSE:
                MyTrace.d("doActioin--" + GlobalConstants.KEY_DOWNLOAD_ACTION_PAUSE);
                pauseDownload(entry);
                break;
//            恢复
            case GlobalConstants.KEY_DOWNLOAD_ACTION_RESUME:
                MyTrace.d("doActioin--" + GlobalConstants.KEY_DOWNLOAD_ACTION_RESUME);
                resumeDownload(entry);
                break;
//            取消
            case GlobalConstants.KEY_DOWNLOAD_ACTION_CANCEL:
                MyTrace.d("doActioin--" + GlobalConstants.KEY_DOWNLOAD_ACTION_CANCEL);
                cancelDownload(entry);
                break;
            case GlobalConstants.KEY_DOWNLOAD_ACTION_PAUSE_ALL:
                pause_all();
                break;
            case GlobalConstants.KEY_DOWNLOAD_ACTION_RECOVERALL:
                recover_all();
                break;
            default:
                MyTrace.d("NO MATCH");
                break;
        }
    }

    private void recover_all() {
//        恢复全部
        ArrayList<DownloadEntry> recoverList = DataChanger.getInstance(context)
                .queryAllRecoverableEntries();
        if (recoverList != null) {
            for (DownloadEntry entry : recoverList) {
                addDownload(entry);
            }
        }

    }

    private void pause_all() {
        //暂停所有任务
//        MyTrace.d("download -pause_all");
//        DownloadTask task=  mDownloadingTasks.remove(entry.id);
//        if (task!=null){
//            MyTrace.d("pauseDownload");
//            //在任务队列中就暂停
//            task.pause();
//        }
//        else {
//            //不在队列中
//            mWaittingQueue.remove(entry);
//            entry.status= DownloadEntry.DownloadStatus.paused;
//            DataChanger.getInstance().postStatus(entry);
//        }

/*    停止所有任务的流程
       1.从等待任务队列里面拿 出所有正在运行的把他们停卡掉
       2.停止正在执行的任务队列里面的任务
       3.保存队列  （DB/其它方式）
       3.最后清空任务队列
       */


// 1.从等待任务队列里面拿 出所有正在运行的把他们停卡掉
        while (mWaittingQueue.iterator().hasNext()) {
            DownloadEntry entry = mWaittingQueue.poll();
            entry.status = DownloadEntry.DownloadStatus.paused;
            //此处的性能不高，需要优化
            DataChanger.getInstance(context).postStatus(entry);

        }
//        2.停止正在执行的任务队列里面的任务
//        private HashMap<String,DownloadTask> mDownloadingTasks=new HashMap<String,DownloadTask>();
//        HASHMAP的遍历
        for (Map.Entry<String, DownloadTask> entry : mDownloadingTasks.entrySet()) {
//            Map.Entry  Entry 为MAP的内存类，用于封装key-value对
            entry.getValue().pause();
        }

        //3.最后清空任务队列
        mDownloadingTasks.clear();


    }

    private void cancelDownload(DownloadEntry entry) {
        DownloadTask task = mDownloadingTasks.remove(entry.id);
        if (task != null) {
            task.cancel();
        } else {
            mWaittingQueue.remove(entry);
            entry.status = DownloadEntry.DownloadStatus.cancel;
            DataChanger.getInstance(context).postStatus(entry);
        }
    }

    private void resumeDownload(DownloadEntry entry) {
//        DownloadTask task=  mDownloadingTasks.remove(entry.id);
//        if (task!=null)
//        {
//            MyTrace.d("resumed---Download");
//            task.resumed();
//
//        }

//         之前完成执行的代码
//        startDownload(entry);
        addDownload(entry);

    }

    private void pauseDownload(DownloadEntry entry) {
        MyTrace.d("download -services pauseDownload");
        DownloadTask task = mDownloadingTasks.remove(entry.id);
        if (task != null) {
            MyTrace.d("pauseDownload");
            //在任务队列中就暂停
            task.pause();
        } else {
            //不在队列中
            mWaittingQueue.remove(entry);
            entry.status = DownloadEntry.DownloadStatus.paused;
            DataChanger.getInstance(context).postStatus(entry);
        }
    }

    /**
     * 开始下载，需要加入Entry
     *
     * @param entry
     */
    private void startDownload(DownloadEntry entry) {
        MyTrace.d("startDownload");
        DownloadTask task = new DownloadTask(entry, mhandler, mExecutors);
        mDownloadingTasks.put(entry.id, task);
        task.start();
        //使用子线程执行
//        mExecutors.execute(task);
    }


    private void addDownload(DownloadEntry entry) {
        //如果队满了，加入队列
        if (mDownloadingTasks.size() >= GlobalConstants.MAX_DOWNLOAD_TASK) {
            mWaittingQueue.offer(entry);
            entry.status = DownloadEntry.DownloadStatus.waiting;
            DataChanger.getInstance(context).postStatus(entry);

        } else {
//            队列未满时即时执行任务
            startDownload(entry);
        }

    }
}
