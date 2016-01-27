package com.download.neo.downloaddemo.DownLoadUtil.Db;

import android.content.Context;

import com.download.neo.downloaddemo.DownLoadUtil.DownloadEntry;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Lenovo on 2016/1/23.
 */
public class EntryDao {
    private Context context;
    private Dao<DownloadEntry, Integer> entryDaoOpe;
    private DatabaseHelper helper;

    public EntryDao(Context context) {
        this.context = context;

        try {
            helper = DatabaseHelper.getDbHelper(context);
            entryDaoOpe = helper.getDao(DownloadEntry.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public synchronized void createOrUpdate(DownloadEntry entry) {
        try {
            entryDaoOpe.createOrUpdate(entry);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(DownloadEntry entry) {
        try {
            entryDaoOpe.delete(entry);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<DownloadEntry> queryAll() {
        ArrayList<DownloadEntry> list = null;
        try {
            list = (ArrayList<DownloadEntry>) entryDaoOpe.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;

    }



}
