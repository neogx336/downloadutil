package com.download.neo.downloaddemo.DownLoadUtil;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Lenovo on 2016/1/13.
 */
@DatabaseTable(tableName = "tb_entry")
public class DownloadEntry
        implements Serializable
{

    @DatabaseField(id = true)
    public String id;
    @DatabaseField
    public String name;
    @DatabaseField
    public String url;
    @DatabaseField
    public int currentLength;
    @DatabaseField
    public int totalLength;
    @DatabaseField
    public boolean isSuportRange;
    @DatabaseField
    public DownloadStatus status=DownloadStatus.idle;
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    HashMap<Integer,Integer> ranges;
    @DatabaseField
    public  int percent;

    public DownloadEntry() {
    }

    public void reset()
    {
        currentLength=0;
        percent=0;
        ranges=null;

    }

    public DownloadEntry(String url,String id) {
        this.id=id;
        this.url=url;
        this.status=DownloadStatus.idle;
        this.currentLength=0;
    }




    public enum DownloadStatus {
        waiting, downloading, paused, resumed, cancel,
        completed,idle,connecting,error,network_error,io_error,memory_error
    }

    ;

    @Override
    public String toString() {
        return url +
                "  is " + status +
                " with " + currentLength +
                "/" + totalLength
                ;
    }

    @Override
    public boolean equals(Object o) {
        return o.hashCode()==this.hashCode();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
