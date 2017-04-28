package iit.alaurent1.newsgateway;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.support.v7.app.ActionBarDrawerToggle;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static android.R.attr.key;
import static android.R.attr.src;
import android.support.v7.app.AlertDialog;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    static final String ACTION_NEWS_STORY = "ACTION_NEWS_STORY";
    static final String ACTION_MSG_TO_SERVICE = "ACTION_MSG_TO_SERVICE";
    static final String DATA_SOURCE = "DATA_SOURCE";
    static final String DATA_ARTICLE = "DATA_ARTICLE";

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private NewsReceiver newsReceiver;
    private HashMap<String, Source> sourcemap = new HashMap<String, Source>();
    private ArrayList<String> sourcenamelist = new ArrayList<>();
    private ArrayList<String> categorylist = null;
    private ArrayList<Article> articlelist = new ArrayList<>();
    private Menu menu;
    private MyPageAdapter pageAdapter;
    private List<Fragment> fragments;
    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start the service and register the receiver
        Intent intent = new Intent(MainActivity.this, NewsService.class);
        startService(intent);

        newsReceiver = new NewsReceiver();
        IntentFilter filter1 = new IntentFilter(ACTION_NEWS_STORY);
        registerReceiver(newsReceiver, filter1);

        // Set the Drawer  Layout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, sourcenamelist));
        mDrawerList.setOnItemClickListener(
                new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        selectItem(position);
                    }
                }
        );

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        );

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Start the category and source download
        if (networkCheck()) {
            if(savedInstanceState!=null){
                setTitle(savedInstanceState.getCharSequence("title"));
                setSources((ArrayList<Source>)savedInstanceState.getSerializable("sourcelist"), savedInstanceState.getStringArrayList("categorylist"));
            } else {
                NewsSourcesDownloader nsdl = new NewsSourcesDownloader(this, "");
                nsdl.execute();
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No network connection");
            builder.setMessage("News cannot be loaded without a network connection");
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        // Set the page Adapter for fragments
        fragments = getFragments();
        pageAdapter = new MyPageAdapter(getSupportFragmentManager());
        pager = (ViewPager) findViewById(R.id.viewpager);
        pager.setAdapter(pageAdapter);
        pager.setOffscreenPageLimit(10);
        if (savedInstanceState!=null){
            for(int i =0; i<savedInstanceState.getInt("size");i++) {
                fragments.add(getSupportFragmentManager().getFragment(savedInstanceState, "NewsFragment" + Integer.toString(i)));
            }
        } else {
            pager.setBackgroundResource(R.drawable.img_bgnews);
        }
        pageAdapter.notifyDataSetChanged();

    }

    // Drawer handler
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Menu handler
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            Log.d(TAG, "onOptionsItemSelected: mDrawerToggle " + item);
            return true;
        }

        NewsSourcesDownloader nsdl = new NewsSourcesDownloader(this, ""+item);
        nsdl.execute();
        return super.onOptionsItemSelected(item);
    }

    private void selectItem(int position) {
        Toast.makeText(this, sourcenamelist.get(position), Toast.LENGTH_SHORT).show();
        pager.setBackground(null);
        setTitle(sourcenamelist.get(position));
        Intent intent = new Intent(ACTION_MSG_TO_SERVICE);
        intent.putExtra(DATA_SOURCE, sourcemap.get(sourcenamelist.get(position)));
        sendBroadcast(intent);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    // Set the sources and categories after the download
    public void setSources(ArrayList<Source> sourcelist, ArrayList<String> categorylist) {

        sourcemap.clear();
        sourcenamelist.clear();
        Collections.sort(sourcelist);
        for(Source s: sourcelist){
            sourcenamelist.add(s.getName());
            sourcemap.put(s.getName(), s);
        }
        ((ArrayAdapter<String>)mDrawerList.getAdapter()).notifyDataSetChanged();

        if (this.categorylist == null){
            this.categorylist = new ArrayList<>(categorylist);
            if(menu!=null) {
                this.categorylist.add(0, "all");
                for (String c : this.categorylist) {
                    menu.add(c);
                }
            }
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (this.categorylist != null) {
            menu.clear();
            for (String c : this.categorylist) {
                menu.add(c);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    // Fragments handler
    private List<Fragment> getFragments() {
        List<Fragment> fList = new ArrayList<Fragment>();
        return fList;
    }

    private class MyPageAdapter extends FragmentPagerAdapter {
        private long baseId = 0;

        public MyPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }
        @Override
        public int getCount() {
            return fragments.size();
        }
        @Override
        public long getItemId(int position) {
            return baseId + position;
        }
        public void notifyChangeInPosition(int n) {
            baseId += getCount() + n;
        }

    }

    // Receiver
    public class NewsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            switch (intent.getAction()) {
                case ACTION_NEWS_STORY:
                    if (intent.hasExtra(DATA_ARTICLE)) {
                        articlelist = (ArrayList) intent.getSerializableExtra(DATA_ARTICLE);
                        reDoFragments(articlelist);
                    }
                    break;
            }
        }

        private void reDoFragments(List<Article> al) {

            for (int i = 0; i < pageAdapter.getCount(); i++)
                pageAdapter.notifyChangeInPosition(i);

            fragments.clear();

            for (int i = 0; i < al.size(); i++) {
                fragments.add(NewsFragment.newInstance(al.get(i), ""+i, ""+al.size()));
            }

            pageAdapter.notifyDataSetChanged();
            pager.setCurrentItem(0);
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(newsReceiver);
        super.onDestroy();
    }

    public boolean networkCheck() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);
        int compteur = 0;
        for(int i =0; i< fragments.size();i++) {
            if (fragments.get(i).isAdded()) {
                compteur++;
                String temp = "NewsFragment" + Integer.toString(i);
                getSupportFragmentManager()
                        .putFragment(savedInstanceState, temp, fragments.get(i));
                Log.d("FRAGMENTS SAVE :", Integer.toString(i));
            }

        }
        savedInstanceState.putInt("size",compteur);

        savedInstanceState.putStringArrayList("categorylist", categorylist );

        ArrayList<Source> temp = new ArrayList<>();
        for (String key : sourcemap.keySet()) {
           temp.add(sourcemap.get(key));
        }
        savedInstanceState.putSerializable("sourcelist", temp );

        savedInstanceState.putCharSequence("title", getTitle());
    }

}
