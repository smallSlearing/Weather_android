package com.juhe.weather;

import android.content.Intent;
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
    //当前页数
    private int pageNum = 1;
    //当前新闻类型
    private String type="头条";
    private JSONArray jsonArray;
    //新闻列表
    private List<NewsBean> newList = new ArrayList<>();
    //新闻适配器
    private NewsAdapter newsAdapter;
    //下拉刷新布局对象
    private SwipeRefreshLayout swipeRefresh ;



//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        //设置Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //把toolbar设置顶部
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navView=(NavigationView) findViewById(R. id. nav_view);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        //监听功能菜单
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(MenuItem item){
                switch (item.getItemId()){
                    case R.id.nav_changBackground:
                        break;

                    case R.id.nav_weather:
                        startActivity(new Intent(NewsActivity.this , WeatherActivity.class));
                        break;

                    case R.id.nav_vidoe:
                        startActivity(new Intent(NewsActivity.this , VideoActivity.class));
                        break;

                    case R.id.nav_news:
                        break;
                }

                //关闭滑动菜单栏
                mDrawerLayout.closeDrawers();
                return true;
            }
        });


        swipeRefresh=(SwipeRefreshLayout)findViewById(R. id. swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        //监听下拉刷新
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.
                OnRefreshListener(){
            @Override
            public void onRefresh(){
                //刷新新闻
                refreshNews();
            }
        });

        //从后台请求新闻数据
        getNewData();
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
                        //页数加1，下次想后端请求数据的说好，请求的是第二页的数据
                        pageNum++;
//                        从后端获取新闻信息
                        getNewData();
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
            random.nextInt(newList.size());
        }

        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager=new GridLayoutManager(NewsActivity.this,1);
        recyclerView.setLayoutManager(layoutManager);

        //新建一个新闻适配器
        newsAdapter = new NewsAdapter(newList);
        //newsAdapter作为recyclerView的适配器
        recyclerView.setAdapter(this.newsAdapter);
    }


    /*接受子线程的消息通知*/
    private Handler handler=new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FLAG:
                    //解析新闻
                    parseNews();
                    //初始化新闻页面
                    initNews();
                    break;
                default:
                    break;
            }

        }
    };

    /**
     *请求后端新闻数据
     */
    public void  getNewData(){
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
        //遍历json数组
        for (int i = 0; i < jsonArray.length() ; i++) {
            //NewsBean新闻的实体类
            NewsBean bean = new NewsBean();
            //json对象
            JSONObject jsonObject = null;
            try {
                //把json数组中的下标为i的json对象赋值给jsonObject
                jsonObject = jsonArray.getJSONObject(i);
                //从json对象中获取key为date的值，赋值给bean的date属性
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
                getNewData();
                break;
            case R.id.shehui:
                type="社会";
                pageNum=1;
                newList = new ArrayList<>();
                getNewData();

                break;
            case R.id.guonei:
                type="国内";
                pageNum=1;
                newList = new ArrayList<>();
                getNewData();
                break;
            case R.id.guoji:
                type="国际";
                pageNum=1;
                newList = new ArrayList<>();
                getNewData();
                break;
            case R.id.yule:
                type="娱乐";
                pageNum=1;
                newList = new ArrayList<>();
                getNewData();
                break;
            case R.id.tiyu:
                type="体育";
                pageNum=1;
                newList = new ArrayList<>();
                getNewData();
                break;
            case R.id.junshi:
                type="军事";
                pageNum=1;
                newList = new ArrayList<>();
                getNewData();
                break;
            case R.id.keji:
                type="科技";
                pageNum=1;
                newList = new ArrayList<>();
                getNewData();
                break;
            case R.id.caijing:
                type="财经";
                pageNum=1;
                newList = new ArrayList<>();
                getNewData();
                break;
            case R.id.shishang:
                type="时尚";
                pageNum=1;
                newList = new ArrayList<>();
                getNewData();
                break;

            default:
                break;

        }
        return true;
    }
}




