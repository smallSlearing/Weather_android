package com.juhe.weather;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.juhe.weather.bean.FutureWeatherBean;
import com.juhe.weather.bean.HoursWeatherBean;
import com.juhe.weather.bean.NewsBean;
import com.juhe.weather.bean.PMBean;
import com.juhe.weather.bean.WeatherBean;
import com.juhe.weather.service.WeatherService;
import com.juhe.weather.service.WeatherService.OnParserCallBack;
import com.juhe.weather.service.WeatherService.WeatherServiceBinder;
import com.juhe.weather.swiperefresh.PullToRefreshBase;
import com.juhe.weather.swiperefresh.PullToRefreshBase.OnRefreshListener;
import com.juhe.weather.swiperefresh.PullToRefreshScrollView;

public class WeatherActivity extends Activity implements Serializable {

    private Context mContext;
    private PullToRefreshScrollView mPullToRefreshScrollView;
    private ScrollView mScrollView;
    private DrawerLayout mDrawerLayout;
    //当前背景图在列表中的下标
    private int backgroundIndex = 0;
    /*背景图列表*/
    private List<Integer> backgroundList = new ArrayList<>();
    private ImageView im_play_front;
    private ImageView im_play_next;
    private ImageView im_play_play;

    View pull_refresh_scrollview;

    private WeatherService mService;

    private TextView tv_city,// 城市
            tv_release,// 发布时间
            tv_now_weather,// 天气
            tv_today_temp,// 温度
            tv_now_temp,// 当前温度
            tv_aqi,// 空气质量指数
            tv_quality,// 空气质量
            tv_next_three,// 3小时
            tv_next_six,// 6小时
            tv_next_nine,// 9小时
            tv_next_twelve,// 12小时
            tv_next_fifteen,// 15小时
            tv_next_three_temp,// 3小时温度
            tv_next_six_temp,// 6小时温度
            tv_next_nine_temp,// 9小时温度
            tv_next_twelve_temp,// 12小时温度
            tv_next_fifteen_temp,// 15小时温度
            tv_today_temp_a,// 今天温度a
            tv_today_temp_b,// 今天温度b
            tv_tommorrow,// 明天
            tv_tommorrow_temp_a,// 明天温度a
            tv_tommorrow_temp_b,// 明天温度b
            tv_thirdday,// 第三天
            tv_thirdday_temp_a,// 第三天温度a
            tv_thirdday_temp_b,// 第三天温度b
            tv_fourthday,// 第四天
            tv_fourthday_temp_a,// 第四天温度a
            tv_fourthday_temp_b,// 第四天温度b
            tv_humidity,// 湿度
            tv_wind, tv_uv_index,// 紫外线指数
            tv_dressing_index,// 穿衣指数
            tv_news1,  //第一条新闻
            tv_news2,   //第二条新闻
            tv_news3, //第三条新闻
            tv_news4;//第四条新闻

    private ImageView iv_now_weather,// 现在
            iv_next_three,// 3小时
            iv_next_six,// 6小时
            iv_next_nine,// 9小时
            iv_next_twelve,// 12小时
            iv_next_fifteen,// 15小时
            iv_today_weather,// 今天
            iv_tommorrow_weather,// 明天
            iv_thirdday_weather,// 第三天
            iv_fourthday_weather;// 第四天

    private RelativeLayout rl_city;

    private LinearLayout li_news;

    private LinearLayout small_video_ly;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

//        R.drawable.img_2;
        mContext = this;
        init();
        initService();
        findIdByReflect();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout02);
        NavigationView navView=(NavigationView) findViewById(R.id.nav_view02);

        View headerView = navView.inflateHeaderView(R.layout.nav_header);
        /*获得背景音乐的控制按钮*/
        im_play_front = headerView.findViewById(R.id.play_left);
        im_play_play = headerView.findViewById(R.id.play_play);
        im_play_next = headerView.findViewById(R.id.play_right);
        /*给背景音乐的控制按钮设置监听器*/
        initMusicListener();

//        navView.setCheckedItem(R.id.nav_call);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(MenuItem item){

                switch (item.getItemId()){
                    case R.id.nav_changBackground:
                        if (backgroundIndex+1 >= backgroundList.size()) {
                            backgroundIndex = -1;
                        }
                        backgroundIndex++;
                        //更换图片
                        pull_refresh_scrollview.setBackgroundResource(backgroundList.get(backgroundIndex));
                        break;

                    case R.id.nav_weather:
//                        startActivity(new Intent(WeatherActivity.this , WeatherActivity.class));
                        break;

                    case R.id.nav_vidoe:
                        mediaPlayer.release();
                        startActivity(new Intent(WeatherActivity.this , VideoActivity.class));
                        break;

                    case R.id.nav_news:
                        startActivity(new Intent(WeatherActivity.this , NewsActivity.class));
                        break;
                }

                mDrawerLayout.closeDrawers();
                return true;
            }
        });

        pull_refresh_scrollview = findViewById(R.id.pull_refresh_scrollview);
        pull_refresh_scrollview.setBackgroundResource(R.drawable.timg);


    }

    private void initService() {
        Intent intent = new Intent(mContext, WeatherService.class);
//        intent.putExtra("weatherActivity",WeatherActivity.this);
        startService(intent);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            // TODO Auto-generated method stub
            mService.removeCallBack();
        }

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            // TODO Auto-generated method stub
            mService = ((WeatherServiceBinder) arg1).getService();
            mService.setCallBack(new OnParserCallBack() {

                @Override
                public void OnParserComplete(List<HoursWeatherBean> list, PMBean pmBean, WeatherBean weatherBean , List<NewsBean> newsList) {
                    // TODO Auto-generated method stub
                    mPullToRefreshScrollView.onRefreshComplete();
                    if (list != null && list.size() >= 5) {
                        setHourViews(list);
                    }

                    if (pmBean != null) {
                        setPMView(pmBean);
                    }

                    if (weatherBean != null) {
                        setWeatherViews(weatherBean);
                    }

                    if (newsList != null && newsList.size() != 0) {
                        setNewsView(newsList);
                    }
                }
            });

            mService.getCityWeather();
        }
    };

    public void setPMView(PMBean bean) {
        tv_aqi.setText(bean.getAqi());
        tv_quality.setText(bean.getQuality());
    }

    public void setWeatherViews(WeatherBean bean) {

        tv_city.setText(bean.getCity());
        tv_release.setText(bean.getRelease());

        tv_now_weather.setText(bean.getWeather_str());
        String[] tempArr = bean.getTemp().split("~");
        String temp_str_a = tempArr[1].substring(0, tempArr[1].indexOf("℃"));
        String temp_str_b = tempArr[0].substring(0, tempArr[0].indexOf("℃"));
        // 温度 8℃~16℃" ↑ ↓ °
        tv_today_temp.setText("↑ " + temp_str_a + "°   ↓" + temp_str_b + "°");
        tv_now_temp.setText(bean.getNow_temp() + " °");
        iv_today_weather.setImageResource(getResources().getIdentifier("d" + bean.getWeather_id(), "drawable", "com.juhe.weather"));

        tv_today_temp_a.setText(temp_str_a + "°");
        tv_today_temp_b.setText(temp_str_b + "°");
        List<FutureWeatherBean> futureList = bean.getFutureList();
        if (futureList != null && futureList.size() == 3) {
            setFutureData(tv_tommorrow, iv_tommorrow_weather, tv_tommorrow_temp_a, tv_tommorrow_temp_b, futureList.get(0));
            setFutureData(tv_thirdday, iv_thirdday_weather, tv_thirdday_temp_a, tv_thirdday_temp_b, futureList.get(1));
            setFutureData(tv_fourthday, iv_fourthday_weather, tv_fourthday_temp_a, tv_fourthday_temp_b, futureList.get(2));
        }
        Calendar c = Calendar.getInstance();
        int time = c.get(Calendar.HOUR_OF_DAY);
        String prefixStr = null;
        if (time >= 6 && time < 18) {
            prefixStr = "d";
        } else {
            prefixStr = "n";
        }
        iv_now_weather.setImageResource(getResources().getIdentifier(prefixStr + bean.getWeather_id(), "drawable", "com.juhe.weather"));

        tv_humidity.setText(bean.getHumidity());
        tv_dressing_index.setText(bean.getDressing_index());
        tv_uv_index.setText(bean.getUv_index());
        tv_wind.setText(bean.getWind());

    }

    private void setHourViews(List<HoursWeatherBean> list) {

        setHourData(tv_next_three, iv_next_three, tv_next_three_temp, list.get(0));
        setHourData(tv_next_six, iv_next_six, tv_next_six_temp, list.get(1));
        setHourData(tv_next_nine, iv_next_nine, tv_next_nine_temp, list.get(2));
        setHourData(tv_next_twelve, iv_next_twelve, tv_next_twelve_temp, list.get(3));
        setHourData(tv_next_fifteen, iv_next_fifteen, tv_next_fifteen_temp, list.get(4));
    }

    //设置新闻列表
    private void setNewsView(List<NewsBean> newsList) {
        tv_news1.setText(newsList.get(0).getTitle());
        tv_news2.setText(newsList.get(1).getTitle());
        tv_news3.setText(newsList.get(2).getTitle());
        tv_news4.setText(newsList.get(3).getTitle());
    }


    public void setHourData(TextView tv_hour, ImageView iv_weather, TextView tv_temp, HoursWeatherBean bean) {

        String prefixStr = null;
        int time = Integer.valueOf(bean.getTime());

        tv_hour.setText(time + "时");
//        iv_weather.setImageResource(getResources().getIdentifier("cond_icon_"+ bean.getWeather_id() , "drawable-hdpi", "com.juhe.weather"));

        /*通过反射的方式获得资源id*/
        int id = -1;  //资源id
        try {
            Field field = R.drawable.class.getField("cond_icon_"+bean.getWeather_id());
            id = field.getInt(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        iv_weather.setImageResource(id);
        tv_temp.setText(bean.getTemp() + "°");
    }

    public void setFutureData(TextView tv_week, ImageView iv_weather, TextView tv_temp_a, TextView tv_temp_b, FutureWeatherBean bean) {
        tv_week.setText(bean.getWeek());

        /**通过反射的方式获得资源id*/
        int id = -1;  //资源id
        try {
            Field field = R.drawable.class.getField("d" + bean.getWeather_id());
            id = field.getInt(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        iv_weather.setImageResource(id);
        String[] tempArr = bean.getTemp().split("~");
        String temp_str_a = tempArr[1].substring(0, tempArr[1].indexOf("℃"));
        String temp_str_b = tempArr[0].substring(0, tempArr[0].indexOf("℃"));
        tv_temp_a.setText(temp_str_a + "°");
        tv_temp_b.setText(temp_str_b + "°");

    }
//初始化的方法，
    private void init() {
        //下拉属性
        mPullToRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.pull_refresh_scrollview);
        mPullToRefreshScrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
                // TODO Auto-generated method stub
                mService.getCityWeather();
                
            }
            
        });

        mScrollView = mPullToRefreshScrollView.getRefreshableView();

        rl_city = (RelativeLayout) findViewById(R.id.rl_city);
        rl_city.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startActivityForResult(new Intent(mContext, CityActivity.class), 1);

            }
        });

        /*设置今日资讯跳转*/
        li_news = (LinearLayout) findViewById(R.id.li_news);
        li_news.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startActivity(new Intent(mContext, NewsActivity.class));

            }
        });

        /*设置小视频跳转*/
        small_video_ly = (LinearLayout) findViewById(R.id.small_video_ly);
        small_video_ly.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startActivity(new Intent(mContext, VideoActivity.class));

            }
        });


        tv_city = (TextView) findViewById(R.id.tv_city);
        tv_release = (TextView) findViewById(R.id.tv_release);
        tv_now_weather = (TextView) findViewById(R.id.tv_now_weather);
        tv_today_temp = (TextView) findViewById(R.id.tv_today_temp);
        tv_now_temp = (TextView) findViewById(R.id.tv_now_temp);
        tv_aqi = (TextView) findViewById(R.id.tv_aqi);
        tv_quality = (TextView) findViewById(R.id.tv_quality);
        tv_next_three = (TextView) findViewById(R.id.tv_next_three);
        tv_next_six = (TextView) findViewById(R.id.tv_next_six);
        tv_next_nine = (TextView) findViewById(R.id.tv_next_nine);
        tv_next_twelve = (TextView) findViewById(R.id.tv_next_twelve);
        tv_next_fifteen = (TextView) findViewById(R.id.tv_next_fifteen);
        tv_next_three_temp = (TextView) findViewById(R.id.tv_next_three_temp);
        tv_next_six_temp = (TextView) findViewById(R.id.tv_next_six_temp);
        tv_next_nine_temp = (TextView) findViewById(R.id.tv_next_nine_temp);
        tv_next_twelve_temp = (TextView) findViewById(R.id.tv_next_twelve_temp);
        tv_next_fifteen_temp = (TextView) findViewById(R.id.tv_next_fifteen_temp);
        tv_today_temp_a = (TextView) findViewById(R.id.tv_today_temp_a);
        tv_today_temp_b = (TextView) findViewById(R.id.tv_today_temp_b);
        tv_tommorrow = (TextView) findViewById(R.id.tv_tommorrow);
        tv_tommorrow_temp_a = (TextView) findViewById(R.id.tv_tommorrow_temp_a);
        tv_tommorrow_temp_b = (TextView) findViewById(R.id.tv_tommorrow_temp_b);
        tv_thirdday = (TextView) findViewById(R.id.tv_thirdday);
        tv_thirdday_temp_a = (TextView) findViewById(R.id.tv_thirdday_temp_a);
        tv_thirdday_temp_b = (TextView) findViewById(R.id.tv_thirdday_temp_b);
        tv_fourthday = (TextView) findViewById(R.id.tv_fourthday);
        tv_fourthday_temp_a = (TextView) findViewById(R.id.tv_fourthday_temp_a);
        tv_fourthday_temp_b = (TextView) findViewById(R.id.tv_fourthday_temp_b);
        tv_humidity = (TextView) findViewById(R.id.tv_humidity);
        tv_wind = (TextView) findViewById(R.id.tv_wind);
        tv_uv_index = (TextView) findViewById(R.id.tv_uv_index);
        tv_dressing_index = (TextView) findViewById(R.id.tv_dressing_index);
        tv_news1 = (TextView) findViewById(R.id.tv_news1);
        tv_news2 = (TextView) findViewById(R.id.tv_news2);
        tv_news3 = (TextView) findViewById(R.id.tv_news3);
        tv_news4 = (TextView) findViewById(R.id.tv_news4);


        iv_now_weather = (ImageView) findViewById(R.id.iv_now_weather);
        iv_next_three = (ImageView) findViewById(R.id.iv_next_three);
        iv_next_six = (ImageView) findViewById(R.id.iv_next_six);
        iv_next_nine = (ImageView) findViewById(R.id.iv_next_nine);
        iv_next_twelve = (ImageView) findViewById(R.id.iv_next_twelve);
        iv_next_fifteen = (ImageView) findViewById(R.id.iv_next_fifteen);
        iv_today_weather = (ImageView) findViewById(R.id.iv_today_weather);
        iv_tommorrow_weather = (ImageView) findViewById(R.id.iv_tommorrow_weather);
        iv_thirdday_weather = (ImageView) findViewById(R.id.iv_thirdday_weather);
        iv_fourthday_weather = (ImageView) findViewById(R.id.iv_fourthday_weather);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (requestCode == 1 && resultCode == 1) {
            String city = data.getStringExtra("city");
            mService.getCityWeather(city);
        }

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub

        unbindService(conn);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
        }
        return true;
    }


    public void changeBackground(){

    }


    /**
     * 通过反射机制获得所有背景图的id
     * @return
     */
    public void findIdByReflect(){
        Field[] fields = R.drawable.class.getFields();

        for(Field field : fields){
            Log.e("是否包含",(field.getName().indexOf("weather_background_") >= 0)+"");
            if(field.getName().indexOf("weather") >= 0){
                try {
                    backgroundList.add(field.getInt(null));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }




    /**********************音乐模块代码**********************************/

    private MediaPlayer mediaPlayer = new MediaPlayer();//音乐播放器
    String [] musicPathArr = {"http://music.163.com/song/media/outer/url?id=479408999",
            "http://music.163.com/song/media/outer/url?id=516728102",
            "http://music.163.com/song/media/outer/url?id=28310930",
    };

    int musicIndex = 0;


    public void initMusicListener(){

        //给开始暂停按钮设置监听器
        im_play_play.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!mediaPlayer.isPlaying()) {
                    initMediaPlayer();
                    ((ImageView)v).setImageResource(R.drawable.icon_player_play);
                }else{
                    mediaPlayer.pause(); // 开始播放
                    ((ImageView)v).setImageResource(R.drawable.icon_player_puase);
                }
            }
        });

        //给上一首按钮设置监听器
        im_play_front.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.reset();
                mediaPlayer = new MediaPlayer();
                if(musicIndex>0){
                    musicIndex--;
                }else{
                    musicIndex=0;
                }
                initMediaPlayer();

            }
        });


        //给下一首按钮设置监听器
        im_play_next.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.reset();
                mediaPlayer = new MediaPlayer();
                if(musicIndex < musicPathArr.length-1){
                    musicIndex++;
                }else{
                    musicIndex=musicPathArr.length-1;
                }
                initMediaPlayer();
            }
        });
    }

    private void initMediaPlayer() {
        try {
            mediaPlayer.setDataSource(musicPathArr[musicIndex]); // 指定音频文件的路径
            mediaPlayer.prepare(); // 让MediaPlayer进入到准备状态
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (mediaPlayer != null) {
//            mediaPlayer.stop();
//            mediaPlayer.release();
//        }
//    }


}
