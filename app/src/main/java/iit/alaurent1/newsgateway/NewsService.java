package iit.alaurent1.newsgateway;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static iit.alaurent1.newsgateway.MainActivity.ACTION_MSG_TO_SERVICE;
import static iit.alaurent1.newsgateway.MainActivity.ACTION_NEWS_STORY;
import static iit.alaurent1.newsgateway.MainActivity.DATA_ARTICLE;
import static iit.alaurent1.newsgateway.MainActivity.DATA_SOURCE;

public class NewsService extends Service {

    private static final String TAG = "NewsService";
    private boolean running = true;
    private ServiceReceiver serviceReceiver;
    private ArrayList<Article> articleslist = new ArrayList<>();


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        serviceReceiver = new ServiceReceiver();
        IntentFilter filter1 = new IntentFilter(ACTION_MSG_TO_SERVICE);
        registerReceiver(serviceReceiver, filter1);


        new Thread(new Runnable() {
            @Override
            public void run() {

                while(running){
                    while(articleslist.isEmpty()) {
                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Intent intent = new Intent(ACTION_NEWS_STORY);
                    intent.putExtra(DATA_ARTICLE, (Serializable) articleslist);
                    sendBroadcast(intent);
                    articleslist.clear();
                }
            }
        }).start();

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show();
        unregisterReceiver(serviceReceiver);
        running = false;
        super.onDestroy();
    }

    public void setArticles(ArrayList<Article> list) {
        articleslist.clear();
        articleslist = new ArrayList<>(list);
    }


    public class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.e(TAG, "onReceive ");

            switch (intent.getAction()) {
                case ACTION_MSG_TO_SERVICE:
                    if (intent.hasExtra(DATA_SOURCE)) {
                        Source source = (Source) intent.getSerializableExtra(DATA_SOURCE);
                        NewsArticleDownloader nadl = new NewsArticleDownloader(NewsService.this, "" + source.getId());
                        nadl.execute();
                    }
                    break;
            }
        }
    }

}
