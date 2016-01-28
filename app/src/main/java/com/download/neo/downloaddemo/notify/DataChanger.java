package com.download.neo.downloaddemo.notify;

import android.content.Context;

import com.download.neo.downloaddemo.entities.DownloadEntry;
import com.download.neo.downloaddemo.db.EntryDao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;

/**
 * 消息通知者，被观察者
 * Created by Lenovo on 2016/1/13.
 */
public class DataChanger extends Observable {
    private static DataChanger mInstance;
    private LinkedHashMap<String, DownloadEntry> mTempEntryList;
    private Context context;

    private DataChanger(Context context) {
        this.context = context;
        mTempEntryList = new LinkedHashMap<>();

    }

    public static DataChanger getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DataChanger(context);
        }
        return mInstance;

    }


    public void postStatus(DownloadEntry entry) {
        mTempEntryList.put(entry.id, entry);
        new EntryDao(context).createOrUpdate(entry);
        setChanged();
        notifyObservers(entry);
    }

    public ArrayList<DownloadEntry> queryAllRecoverableEntries() {
        ArrayList<DownloadEntry> resultList = new ArrayList<>();
        for (Map.Entry<String, DownloadEntry> entry : mTempEntryList.entrySet()) {
            if (entry.getValue().status == DownloadEntry.DownloadStatus.paused) {
                resultList.add(entry.getValue());
            }

        }

        return resultList;
    }

    public DownloadEntry queryDownloadEntryById(String id) {
        return mTempEntryList.get(id);
    }

    public void addToTempEntryMap(String key, DownloadEntry entry) {
        mTempEntryList.put(key, entry);

    }

    public boolean containsDownloadEntry(String id) {
        return mTempEntryList.containsKey(id);
    }

    public void deleDownloadEntryById(String id) {
        mTempEntryList.remove(id);
    }
}
