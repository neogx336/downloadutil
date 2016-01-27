package com.download.neo.downloaddemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.download.neo.downloaddemo.DownLoadUtil.DataWatcher;
import com.download.neo.downloaddemo.DownLoadUtil.DownloadEntry;
import com.download.neo.downloaddemo.DownLoadUtil.DownloadManager;
import com.download.neo.downloaddemo.DownLoadUtil.MyTrace;
import com.download.neo.downloaddemo.DownLoadUtil.ToastUtil;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements View
        .OnClickListener {

    Button btn_start;
    Button btn_pause;
    Button btn_cancel;
    TextView tv;
    ListView listView;
    /**
     * 请求数组
     */
    ArrayList<DownloadEntry> mDownloadEntries = new ArrayList<DownloadEntry>();

    private DownloadManager mdownloadManager;

    private DownloadEntry entry;
    private DataWatcher watcher = new DataWatcher() {
        /**用于回调后的更新
         * @param data 事件更新后回调的对象
         */
        @Override
        public void notifyUpdate(DownloadEntry data) {
            MyTrace.d(data.toString());
            int index = mDownloadEntries.indexOf(data);
            if (index != -1) {
                mDownloadEntries.remove(index);
                mDownloadEntries.add(index, data);
                adapter.notifyDataSetChanged();
            }
        }
    };
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mdownloadManager = DownloadManager.getInstance(this);
        InitDatas();
        InitViews();
    }

    private void InitDatas() {
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test1.jpg", "1"));
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test2.jpg", "2"));
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test3.jpg", "3"));
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test4.jpg", "4"));
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test5.jpg", "5"));
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test6.jpg", "6"));
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test7.jpg", "7"));
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test8.jpg", "8"));
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test9.jpg", "9"));
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test10.jpg", "10"));
        mDownloadEntries.add(new DownloadEntry("http://api.stay4it.com/uploads/test11.jpg", "11"));

        //同步这些URL


        DownloadEntry realentry=null;
        DownloadEntry entry=null;
        for (int i = 0; i <mDownloadEntries.size() ; i++) {
            entry=mDownloadEntries.get(i);
            realentry = mdownloadManager.queryDownloadEntry(entry.id);
            if (realentry!=null){
                mDownloadEntries.remove(i);
                mDownloadEntries.add(i, realentry);

            }
            
        }


    }

    private void InitViews() {

//        ActionBar actionBar = getActionBar();
//        actionBar.show();

        btn_start = (Button) findViewById(R.id.btn_start);
        btn_pause = (Button) findViewById(R.id.btn_pause);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        tv = (TextView) findViewById(R.id.tv_content);
        btn_start.setOnClickListener(this);
        btn_pause.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);


        /*
        * 新的对象
        *
        * */
        listView = (ListView) findViewById(R.id.lv);
        adapter = new MyAdapter();
        listView.setAdapter(adapter);


    }

    @Override
    public void onClick(View v) {


    }

    @Override
    protected void onResume() {
        super.onResume();
        mdownloadManager.addObserver(watcher);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mdownloadManager.removeObserver(watcher);
        MyTrace.d("onPause");
    }

    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mDownloadEntries.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            if (convertView == null) {
                //必须要新建一个VH
                holder = new ViewHolder();
                convertView = View.inflate(MainActivity.this, R.layout.lv_item, null);
                holder.tv = (TextView) convertView.findViewById(R.id.content);
                holder.btn = (Button) convertView.findViewById(R.id.btn_click);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final DownloadEntry entry = mDownloadEntries.get(position);
            holder.tv.setText(entry.url + " " + " is " + entry.status + " with " + entry
                    .currentLength + "/" + entry
                    .totalLength);
            holder.btn.setText(mDownloadEntries.get(position).status.toString());
            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MyTrace.d("listview");

                    if (entry.status == DownloadEntry.DownloadStatus.idle) {
                        //空闭状态--转为开始下载
                        MyTrace.d(".status== DownloadEntry.DownloadStatus.idle");
                        mdownloadManager.add(entry);
                    } else if (entry.status == DownloadEntry.DownloadStatus.paused) {
//                        暂停状态--恢复下载  paused--resume
                        mdownloadManager.resume(entry);
                    } else if (entry.status == DownloadEntry.DownloadStatus.downloading || entry
                            .status == DownloadEntry.DownloadStatus.waiting) {
//                        正在下载--暂停下载
                        mdownloadManager.pause(entry);
                    }

                }
            });
            return convertView;
        }

        public final class ViewHolder {
            public TextView tv;
            public Button btn;
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to theaction bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item, menu);//获取menu目录下simple.xml的菜单文件
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.refresh:
//                执行恢复全部
//                Utils.showToast(this, "您点击了刷新菜单", Toast.LENGTH_SHORT);
                recoverAllDownloads();
                return true;
            case R.id.cancle:
//                执行暂停全部
                pauseAllDownload();

//                Utils.showToast(this, "您点击了取消菜单", Toast.LENGTH_SHORT);
                return true;
            case R.id.cancle2:
                ToastUtil.ToastSHOW(this,"您点击了删除菜单");
//                Utils.showToast(this, "您点击了删除菜单", Toast.LENGTH_SHORT);
                return true;
            case R.id.setting:
                ToastUtil.ToastSHOW(this,"您点击了设置菜单");
//                Utils.showToast(this, "您点击了设置菜单", Toast.LENGTH_SHORT);
                return true;
            case R.id.play:
                ToastUtil.ToastSHOW(this,"您点击了播放菜单");
//                Utils.showToast(this, "您点击了播放菜单", Toast.LENGTH_SHORT);

                Intent intent = new Intent(MainActivity.this, Threadact.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void pauseAllDownload() {
        ToastUtil.ToastSHOW(this,"暂停全部");
        mdownloadManager.pauseAll();
    }

    private void recoverAllDownloads() {
        ToastUtil.ToastSHOW(this,"恢复全部");
        mdownloadManager.recoverAll();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mdownloadManager.stopService();
        MyTrace.d("onDestroy");

    }



    @Override
    protected void onStop() {
        super.onStop();
        MyTrace.d("onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        MyTrace.d("onRestart");
    }

    @Override
    protected void onStart() {
        super.onStart();

        MyTrace.d("onStart");
    }
}
