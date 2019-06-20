package com.juhe.weather;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.juhe.weather.adapter.NewsAdapter;
import com.juhe.weather.bean.NewsBean;
import com.juhe.weather.service.LoadUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class NewsActivity extends AppCompatActivity {

    public static final int FLAG = 1;
    private DrawerLayout mDrawerLayout;
    private int pageNum = 1;
    private String type="头条";
    private JSONArray jsonArray;
    private List<NewsBean> newList = new ArrayList<>();
    private NewsAdapter newsAdapter;
    //下拉刷新布局对象
    private SwipeRefreshLayout swipeRefresh ;


    /*接受子线程的消息通知*/
    private Handler handler=new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FLAG:
                    parseNews();
                    initNews();
                    break;
                default:
                    break;
            }

        }
    };
//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        //设置Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navView=(NavigationView) findViewById(R. id. nav_view);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
        navView.setCheckedItem(R.id.nav_call);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(MenuItem item){
                mDrawerLayout.closeDrawers(); return true;
            }
        });

        /*悬浮按钮的处理事件*/
//        FloatingActionButton fab=(FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View. OnClickListener(){
//             @Override
//             public void onClick(View v) {
//                 Snackbar.make(v,"Data deleted", Snackbar. LENGTH_SHORT)
//                         . setAction("Undo", new View. OnClickListener(){
//                             @Override
//                             public void onClick(View v) {
//                                 Toast.makeText(NewsActivity.this, "Data restored", Toast.LENGTH_SHORT).show();
//                             }
//                         }).show();
//             }
//        });


        swipeRefresh=(SwipeRefreshLayout)findViewById(R. id. swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.
                OnRefreshListener(){
            @Override
            public void onRefresh(){
                refreshNews();
            }
        });

        getNewDate();
    }


    /**
     * 下拉刷新的处理函数
     */
    private void refreshNews(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    Thread.sleep(1040);
                } catch(InterruptedException e){
                    e. printStackTrace();
                }
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
//                        initNews();
                        pageNum++;
                        getNewDate();
                        newsAdapter.notifyDataSetChanged();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }). start();
    }


    /**
     * 初始化页面
     */
    private void initNews() {
//        newList.clear();
        for (int i = 0; i < 50; i++) {
            Random random = new Random();
            int index = random.nextInt(newList.size());
        }

        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager=new GridLayoutManager(NewsActivity.this,1);
        recyclerView.setLayoutManager(layoutManager);
        newsAdapter = new NewsAdapter(newList);
        recyclerView.setAdapter(this.newsAdapter);
//        ListView newslistView = (ListView) findViewById(R.id.news_list_view);
//        newsAdapter=new NewsAdapter(NewsActivity.this , R.layout.item_news_list , newList);
//        newslistView.setAdapter(newsAdapter);
    }

    /**
     *请求后端新闻数据
     */
    public void  getNewDate(){
        new Thread() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url("http://139.159.133.43:8080/news/getNewsByType?pageNum=" + pageNum + "&type=" + type).build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();

                    jsonArray = new JSONArray(responseData);

                    Message message=new Message();
                    message.what=FLAG;
                    handler.sendMessage(message);//将 Message对象发送出去

                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }.start();
    }


    /**
     *解析新闻json数据，封装到newsList列表中
     */
    public void parseNews(){

        for (int i = 0; i < jsonArray.length() ; i++) {
            NewsBean bean = new NewsBean();
            JSONObject jsonObject = null;
            try {
                jsonObject = jsonArray.getJSONObject(i);
                bean.setDate(jsonObject.getString("date"));
                bean.setTitle(jsonObject.getString("title"));
                bean.setThumbnailPicS(jsonObject.getString("thumbnailPicS"));
                bean.setCategory(jsonObject.getString("category"));
                bean.setUrl(jsonObject.getString("url"));

            } catch (Exception e) {
                e.printStackTrace();
            }

            newList.add(0,bean);
        }
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }


    /**
     * 点击菜单元素的处理函数
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.toutiao:
                type="头条";
                pageNum=1;
                newList = new ArrayList<>();
                getNewDate();
                break;
            case R.id.shehui:
                type="社会";
                pageNum=1;
                newList = new ArrayList<>();
                getNewDate();

                break;
            case R.id.guonei:
                type="国内";
                pageNum=1;
                newList = new ArrayList<>();
                getNewDate();
                break;
            case R.id.guoji:
                type="国际";
                pageNum=1;
                newList = new ArrayList<>();
                getNewDate();
                break;
            case R.id.yule:
                type="娱乐";
                pageNum=1;
                newList = new ArrayList<>();
                getNewDate();
                break;
            case R.id.tiyu:
                type="体育";
                pageNum=1;
                newList = new ArrayList<>();
                getNewDate();
                break;
            case R.id.junshi:
                type="军事";
                pageNum=1;
                newList = new ArrayList<>();
                getNewDate();
                break;
            case R.id.keji:
                type="科技";
                pageNum=1;
                newList = new ArrayList<>();
                getNewDate();
                break;
            case R.id.caijing:
                type="财经";
                pageNum=1;
                newList = new ArrayList<>();
                getNewDate();
                break;
            case R.id.shishang:
                type="时尚";
                pageNum=1;
                newList = new ArrayList<>();
                getNewDate();
                break;

            default:
                break;

        }
        return true;
    }
}




