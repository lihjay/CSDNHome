package com.joylee.csdnhome;

import android.app.Activity;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.joylee.business.NewsManager;
import com.joylee.common.NetHelper;
import com.joylee.entity.newsentity;
import com.joylee.entity.Emuns;
import com.joylee.handler.krhandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 13-7-6.
 */
public class KrMainActivity extends Activity {

    private JoyListView newslist;
    private MyHandler myHandler;
    private List<Map<String, String>> list = new ArrayList<Map<String, String>>();
    private List<newsentity> channlist = new ArrayList<newsentity>();
    private boolean isupdate;
    private NewsManager newsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.krmain);

        newslist = (JoyListView) findViewById(R.id.krlist);
        newsManager=new NewsManager(getApplicationContext());

        newslist.setonRefreshListener(new JoyListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                myHandler = new MyHandler(true);
                MyThread m = new MyThread();
                new Thread(m).start();
            }
        });

        NetHelper netHelper = new NetHelper();
        // Toast.makeText(MainActivity.this, String.valueOf(netHelper.isNetworkConnected(this)),200);
        if (netHelper.isNetworkConnected(this)) {
            myHandler = new MyHandler();
            MyThread m = new MyThread();
            new Thread(m).start();
        } else {
            Toast.makeText(getApplicationContext(), "网络故障，请检查您的网络环境", 1000).show();

        }
    }







    class MyHandler extends Handler {
        public MyHandler() {
        }

        public MyHandler(boolean update) {
            if(update==true)
            {
                isupdate=true;
            }
            else {
                isupdate=false;
            }
        }

        public MyHandler(Looper L) {
            super(L);
        }

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            Log.d("MyHandler", "handleMessage......");
            super.handleMessage(msg);
            if(isupdate)
            {
                krhandler krhandler1=new krhandler(getApplicationContext());
                try {
                    krhandler1.Insert("http://www.36kr.com/feed");
                }
                catch (Exception e)
                {
                    Log.v("error",e.toString());
                }
            }

            channlist=newsManager.GetList(String.valueOf(Emuns.newssource.kr.value()));
            if(channlist.size()<=0)
            {
                krhandler krhandler1=new krhandler(getApplicationContext());
                try {
                    krhandler1.Insert("http://www.36kr.com/feed");
                }
                catch (Exception e)
                {
                    Log.v("error",e.toString());
                }
                finally {
                    channlist=newsManager.GetList(String.valueOf(Emuns.newssource.kr.value()));
                }
            }

            for (int i = 0; i < channlist.size(); i++) {
                Map<String, String> map = new HashMap<String, String>();
                newsentity newsinfo = (newsentity) channlist.get(i);
                map.put("title", newsinfo.getTitle());
                //map.put("anthor", newsinfo.getAnthor());
                map.put("newsdatetime", newsinfo.getNewsDatetime());
                map.put("url", newsinfo.getUrl());
                list.add(map);
            }



            findViewById(R.id.kr_progressBar).setVisibility(View.INVISIBLE);
            SimpleAdapter adapter = null;
            if (list.size() != 0) {
                adapter = new SimpleAdapter(getApplicationContext(), list,
                        R.layout.newslistcontent, new String[]{"title","anthor", "newsdatetime", "url"}, new int[]{
                        R.id.titletext, R.id.datetimetext,R.id.anthortext});
                newslist.setAdapter(adapter);
            } else {
                Toast.makeText(getApplicationContext(), "暂无数据", Toast.LENGTH_LONG).show();
            }

            newslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                        long arg3) {
                    String url = list.get(arg2).get("url").toString();
                    String title =list.get(arg2).get("title").toString();
                    Bundle bundle = new Bundle();
                    bundle.putString("url", url);
                    bundle.putString("title", title);
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), NewsdetailActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);

                }
            });
            newslist.onRefreshComplete();
        }


    }

    class MyThread implements Runnable {
        public void run() {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            Log.d("thread.......", "mThread........");
            Message msg = new Message();
            KrMainActivity.this.myHandler.sendMessage(msg);

        }
    }


}
