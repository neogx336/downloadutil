package com.download.neo.downloaddemo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.download.neo.downloaddemo.entities.DownloadEntry;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;

/**
 * 数据库操作类
 * Created by Lenovo on 2016/1/23.
 */
public class DatabaseHelper  extends OrmLiteSqliteOpenHelper{
    private static final String TABLE_NAME="sqlite-test.db";
    private Map<String, Dao> daos = new Hashtable<String,Dao>();
    private static  DatabaseHelper instance;


    private DatabaseHelper(Context context)
    {
        super(context,TABLE_NAME,null,4);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            //创建表格
            TableUtils.createTable(connectionSource, DownloadEntry.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int
            oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, DownloadEntry.class,true);
            onCreate(database,connectionSource);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static  synchronized  DatabaseHelper getDbHelper(Context context)
    {
        context=context.getApplicationContext();
        if (instance==null)
        {
            synchronized (DatabaseHelper.class)
            {
                instance =new  DatabaseHelper(context);
            }
        }
        return  instance;
    }

    public synchronized  Dao getDao(Class clz) throws SQLException
    {
        Dao dao=null;
        String className = clz.getSimpleName();
        if (daos.containsKey(className)){
            dao = daos.get(className);
        }
        if (dao==null){
            dao = super.getDao(clz);
            daos.put(className, dao);
        }
        return dao;
    }


    @Override
    public void close() {
        super.close();
        for (String key:daos.keySet()){
            Dao dao = daos.get(key);
            dao = null;
        }
    }
}
