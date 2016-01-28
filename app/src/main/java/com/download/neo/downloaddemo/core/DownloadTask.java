package com.download.neo.downloaddemo.core;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.download.neo.downloaddemo.entities.DownloadEntry;
import com.download.neo.downloaddemo.utilities.GlobalConstants;
import com.download.neo.downloaddemo.utilities.MyTrace;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

/**
 * 下载任务
 * Created by Lenovo on 2016/1/13.
 */
public class DownloadTask implements ConnectThread.ConnectListener, DownloadThread
        .DownLoadThreadListener {

    private final DownloadEntry entry;
    private final Handler mhandler;
    private final ExecutorService mExecutor;
    //强制刷新
    private volatile boolean isPause;
    private volatile boolean isCancelled;
    private boolean isRusume;
    private boolean isStart;
    private ConnectThread mconnectThread;
    //    当前线程剩余下载的量
    private int surplusprogress;
    private DownloadThread[] mDownloadThreads;
    private int temDivide = 0;
    private long mLastStamp;
    //    记录每个线程的状态
    private DownloadEntry.DownloadStatus[] mdownloadStatus;

    @Override
    public void onConnected(boolean isSupportRange, int totalLength) {
        entry.isSuportRange = isSupportRange;
        entry.totalLength = totalLength;
        startDownLoad();

    }

    private void startDownLoad() {
//        entry.isSuportRange=false;
        //是否支持多线程
        if (entry.isSuportRange) {
            //支持就开启多线程
            startMultiDownload();
        } else {
//            不支持就单线程
            startSingleDownload();
        }
    }

    @Override
    public synchronized void onError(String message) {
        entry.status = DownloadEntry.DownloadStatus.error;
        notifyUpdate(entry, DownloadService.NOTITY_ERROR);
    }

    //连接时发生的异常
    @Override
    public synchronized void onConnectedError(String msg) {
        if (isPause || isCancelled) {
            entry.status = isPause ? DownloadEntry.DownloadStatus.paused : DownloadEntry
                    .DownloadStatus.cancel;
            notifyUpdate(entry, DownloadService.NOTIFI_PAUSED_OR_CANCELLED);
        } else {
            entry.status = DownloadEntry.DownloadStatus.error;
            notifyUpdate(entry, DownloadService.NOTITY_ERROR);
        }


    }


    /***********
     * 下载线程回调接口**START
     *********/
    @Override
    public synchronized void onDownLoadCompleted(int index) {
        mdownloadStatus[index]= DownloadEntry.DownloadStatus.completed;

        for (int i = 0; i < mdownloadStatus.length; i++) {
            if (mdownloadStatus[i] != DownloadEntry.DownloadStatus.completed) {
                return;
            }

        }
//        for (int i = 0; i < mDownloadThreads.length; i++) {
//            if (mDownloadThreads[i] != null) {
//                if (!mDownloadThreads[i].isCompleted()) {
//                    return;
//                }
//            }
//        }
        //异常情况，把他删掉
        if (entry.totalLength > 0 && entry.currentLength != entry.totalLength) {
            entry.status = DownloadEntry.DownloadStatus.error;
//            删除文件
            entry.reset();
            String url = entry.url;
            String path = Environment.getExternalStorageDirectory() + File.separator +
                    "neodownload" +
                    File.separator + url.substring(url.lastIndexOf("/") + 1);
            File file = new File(path);
//        删除文件
            if (file.exists()) {
                file.delete();
            }
            notifyUpdate(entry, DownloadService.NOTITY_ERROR);
        } else {
            entry.status = DownloadEntry.DownloadStatus.completed;
            MyTrace.d("thread" + index + " is " + entry.status + " with " + entry.currentLength +
                    "/"
                    + entry
                    .totalLength);
            notifyUpdate(entry, DownloadService.NOTIFI_COMPLETED);
        }
    }

    /**
     * 处理线程错误
     *
     * @param index
     * @param message
     */
    @Override
    public synchronized void onDownLoadError(int index, String message) {
        MyTrace.d(message);
        mdownloadStatus[index]= DownloadEntry.DownloadStatus.error;

        for (int i = 0; i < mdownloadStatus.length; i++) {
            if (mdownloadStatus[i] != DownloadEntry.DownloadStatus.error||mdownloadStatus[i]!= DownloadEntry.DownloadStatus.completed) {
                mDownloadThreads[i].cancelByError();
                return;
            }

        }
        entry.status = DownloadEntry.DownloadStatus.error;
        notifyUpdate(entry, DownloadService.NOTITY_ERROR);



//        boolean isAllerror = true;
//        for (int i = 0; i < mDownloadThreads.length; i++) {
//            if (mDownloadThreads[i] != null) {
//                //所有的线程都出错才去发出通知
//                if (!mDownloadThreads[i].isError()) {
//                    isAllerror = false;
//                    mDownloadThreads[i].cancelByError();
//                }
//            }
//        }
//        if (isAllerror) {
//        }
    }

    /**
     * 停止
     *
     * @param index 线程序号
     */
    @Override
    public synchronized void onDownloadPaused(int index) {
        mdownloadStatus[index]= DownloadEntry.DownloadStatus.paused;

        for (int i = 0; i < mdownloadStatus.length; i++) {
            if (mdownloadStatus[i] != DownloadEntry.DownloadStatus.paused||mdownloadStatus[i]!= DownloadEntry.DownloadStatus.completed) {
                return;
            }

        }

//        for (int i = 0; i < mDownloadThreads.length; i++) {
//            if (mDownloadThreads[i] != null) {
//                if (!mDownloadThreads[i].isPaused()) {
//                    return;
//                }
//            }
//        }
        entry.status = DownloadEntry.DownloadStatus.paused;
        MyTrace.d("thread" + index + " is " + entry.status + " with " + entry.currentLength + "/"
                + entry
                .totalLength);
        notifyUpdate(entry, DownloadService.NOTIFI_PAUSED_OR_CANCELLED);
    }


    /**
     * 当线程取消时的操作
     * 1.确认是否所有线程都停止
     * 2.删除已经下载的文件
     *
     * @param index
     */
    @Override
    public void onDownloadCancel(int index) {
        mdownloadStatus[index]= DownloadEntry.DownloadStatus.cancel;

        for (int i = 0; i < mdownloadStatus.length; i++) {
            if (mdownloadStatus[i] != DownloadEntry.DownloadStatus.cancel||mdownloadStatus[i]!= DownloadEntry.DownloadStatus.completed) {
                return;
            }

        }
//        for (int i = 0; i < mDownloadThreads.length; i++) {
//            if (mDownloadThreads[i] != null) {
////                需要全部线程都要停止才算停止
//                if (!mDownloadThreads[i].isCancelled()) {
//                    return;
//                }
//            }
//        }
//
        entry.status = DownloadEntry.DownloadStatus.cancel;
        entry.reset();
        String url = entry.url;
        String path = Environment.getExternalStorageDirectory() + File.separator + "neodownload" +
                File.separator + url.substring(url.lastIndexOf("/") + 1);
        File file = new File(path);
//        删除已取消的文件
        if (file.exists()) {
            file.delete();
        }
        notifyUpdate(entry, DownloadService.NOTIFI_PAUSED_OR_CANCELLED);
    }


    /**
     * @param index    当前的线程
     * @param progress 下载的进度
     */
    @Override
    public void onThreadProgressChanged(int index, int progress) {
//        多线程下载需要用到的MAP，用于记录每天条线程的位置
        if (entry.isSuportRange) {
            int currentprogress = entry.ranges.get(index) + progress;
            entry.ranges.put(index, currentprogress);
        }

//        MyTrace.d(index + "---" + " statue " + entry.status + entry.currentLength + "/" + entry
//                .totalLength + "");
//        累加进度
        entry.currentLength += progress;
        long stamp = System.currentTimeMillis();

//        每隔XX秒更新操作  减少通知的次数
        if (stamp - mLastStamp > 500) {
            mLastStamp = stamp;
            if (entry.totalLength > 0) {
                int percent = (int) (entry.currentLength * 100l / entry.totalLength);
                if (percent > entry.percent) {
                    entry.percent = percent;
//                    MyTrace.d(index + "---" + " statue " + entry.status + entry.currentLength +
//                            "/" +
//                            entry
//                                    .totalLength + "");
                    notifyUpdate(entry, DownloadService.NOTIFI_UPDATING);
                }
            } else {
                int divde = entry.currentLength / (50 * 1024);
                if (divde > temDivide) {
                    temDivide = divde;
                    notifyUpdate(entry, DownloadService.NOTIFI_UPDATING);
                }
            }
        }
    }

    /***********
     * 下载线程回调接口**END
     *********/

    public enum TaskState {
        START, PAUSE, CANCEL, RESUME;
    }


    private TaskState taskState;

    public DownloadTask(DownloadEntry entry, Handler mhandler, ExecutorService mExecutor) {
        this.entry = entry;
        this.mhandler = mhandler;
        this.mExecutor = mExecutor;
    }

    public void pause() {
        isPause = true;
//       在尝试连接时用的
        if (mconnectThread != null && mconnectThread.isRunning()) {
            mconnectThread.cancel();
        }
//        停止多线程
        if (mDownloadThreads != null && mDownloadThreads.length > 0) {
            for (int i = 0; i < mDownloadThreads.length; i++) {
                if (mDownloadThreads[i] != null && mDownloadThreads[i].isRunning()) {
                    if (entry.isSuportRange) {
                        mDownloadThreads[i].pause();
                    } else {
                        mDownloadThreads[i].cancel();
                    }
                }
            }
        }

    }

    public void cancel() {
        isCancelled = true;
        if (mconnectThread != null && mconnectThread.isRunning()) {
            mconnectThread.cancel();
        }

//        停止多线程
        if (mDownloadThreads != null && mDownloadThreads.length > 0) {
            for (int i = 0; i < mDownloadThreads.length; i++) {
                if (mDownloadThreads[i] != null && mDownloadThreads[i].isRunning()) {
                    mDownloadThreads[i].cancel();
                }

            }
        }


    }

    public void resume() {
    }

    /**
     * 启动线程任务
     */
    public void start() {

        //已经有下载记录的就不需要做检查记录
        if (entry.totalLength > 0) {
            startDownLoad();
        } else {
            entry.status = DownloadEntry.DownloadStatus.connecting;
            notifyUpdate(entry, DownloadService.NOTIFY_CONNECTING);
            mconnectThread = new ConnectThread(entry.url, this);
            mExecutor.execute(mconnectThread);

        }
//       改变当然的下载状态

////        DataChanger.getInstance().postStatus(entry);
////        改用handler来发送
//        notifyUpdate(entry, DownloadService.NOTIFI_DOWNLOADING);
////虚拟任务
//        entry.totalLength = 1024 * 50;
//        for (int i = entry.currentLength; i < entry.totalLength; i += 1024) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            if (isPause || isCancelled) {
//                //重置状态
//                entry.status = isPause ? DownloadEntry.DownloadStatus.paused :
//                        DownloadEntry.DownloadStatus.cancel;
//                //更新状态--更新
////                DataChanger.getInstance().postStatus(entry);
////                // TODO: 2016/1/13  if cancelled delete relate file
//                //更新状态
//                notifyUpdate(entry, DownloadService.NOTIFI_PAUSED_OR_CANCELLED);
//                return;
//            }
////            测试数据
//            entry.currentLength += 1024;
////            DataChanger.getInstance().postStatus(entry);
//            //下载更新完毕
//            notifyUpdate(entry, DownloadService.NOTIFI_UPDATING);
//        }
//        entry.status = DownloadEntry.DownloadStatus.completed;
////        DataChanger.getInstance().postStatus(entry);
//        notifyUpdate(entry, DownloadService.NOTIFI_COMPLETED);


    }

    /**
     * 向Handler发送消息
     *
     * @param what
     */
    private void notifyUpdate(DownloadEntry entry, int what) {
//        多线程调用，如果Handler没有及时处理，将会发生消息多次回调
        Message msg = mhandler.obtainMessage();
        msg.what = what;
        msg.obj = entry;
        mhandler.sendMessage(msg);
//     给handler时间处事情
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void startMultiDownload() {
//        设置状态并通知外层
        entry.status = DownloadEntry.DownloadStatus.downloading;
        notifyUpdate(entry, DownloadService.NOTIFI_DOWNLOADING);
        //计算每一个区间
        int block = entry.totalLength / GlobalConstants.MAX_DOWNLOAD_THREADS;
        int startPos = 0;
        int endPos = 0;
        //开启线程数
//       如要第一次下载还没下载线程的队列的话，就要初始化MAP
        if (entry.ranges == null) {
            entry.ranges = new HashMap<>();
            for (int j = 0; j < GlobalConstants.MAX_DOWNLOAD_THREADS; j++) {
                entry.ranges.put(j, 0);
            }
        }

//        若不是，则恢复下载

        mDownloadThreads = new DownloadThread[GlobalConstants.MAX_DOWNLOAD_THREADS];
        mdownloadStatus = new DownloadEntry.DownloadStatus[GlobalConstants.MAX_DOWNLOAD_THREADS];
        for (int i = 0; i < GlobalConstants.MAX_DOWNLOAD_THREADS; i++) {
//            加上剩余的进度
            startPos = i * block + entry.ranges.get(i);
            if (i == GlobalConstants.MAX_DOWNLOAD_THREADS - 1) {
//                最后一块不能整除的用最长的长度来代替
                endPos = entry.totalLength;
            } else {
                endPos = startPos + block - 1;
            }
//
//          可能有的线程已经下载完比，完成后的不用下载，剩下的才要下载
            if (startPos < endPos) {
                mDownloadThreads[i] = new DownloadThread(i, startPos, endPos, entry.url, this);
//                新建线程的状态
                mdownloadStatus[i] = DownloadEntry.DownloadStatus.downloading;
                mExecutor.execute(mDownloadThreads[i]);
            } else {
                mdownloadStatus[i] = DownloadEntry.DownloadStatus.completed;
            }

        }

    }

    /**
     * 单线程下载操作
     */
    private void startSingleDownload() {
        MyTrace.d("单线程下载");
        entry.status = DownloadEntry.DownloadStatus.downloading;
        notifyUpdate(entry, DownloadService.NOTIFI_DOWNLOADING);
        mDownloadThreads = new DownloadThread[1];
        mDownloadThreads[0] = new DownloadThread(0, 0, 0, entry.url, this);
//        执行线程
        mExecutor.execute(mDownloadThreads[0]);
    }
}
