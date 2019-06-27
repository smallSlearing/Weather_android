package com.juhe.weather;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.juhe.weather.bean.Video;
import com.juhe.weather.util.MyLayoutManager;
import com.juhe.weather.util.MyVideoView;
import com.juhe.weather.util.OnViewPagerListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class VideoActivity extends AppCompatActivity {
    private final int SUCCESS= 1;  //后端获得数据成功的标志
    private static final String TAG = "videoActivity";
    private RecyclerView mRecyclerView;
    //是否已经点赞过的标志
    private boolean isAlreadStar = false;
    //被点赞视频的id
    private int currentVideoId = -1;
    //后台操作点赞成功
    private final int ADD_STAR_SUCCESS = 1;
    //后台操作取消点赞成功
    private final int REMOVE_STAR_SUCCESS = 2;
    //标记是点赞或者是取消点赞的标志，true  为点赞   false  取消点赞
    private boolean  isAdd = true;

    private MyAdapter mAdapter;
    //点赞按钮的图片对象
    private ImageView starIV = null;
    //点赞数量的文本对象
    private TextView starCountTV = null;

    List<Video> videoList = new ArrayList<>();
    MyLayoutManager myLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        getDate();
//        initView();
//        initListener();
    }


    Handler findVideosHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SUCCESS:
                    initView();
                    initListener();
            }
        }
    };


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    /**
     * 初始化页面
     */
    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        myLayoutManager = new MyLayoutManager(this,
                OrientationHelper.VERTICAL, false);

        mAdapter = new MyAdapter(this,videoList);
        mRecyclerView.setLayoutManager(myLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * 从后台获取数据
     */
    public void getDate(){
        new Thread() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url("http://139.159.133.43:8080/video/findVideos").build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();

                    JSONArray jsonArray = new JSONArray(responseData);

                    parseVideo(jsonArray);


                    if(videoList !=null && videoList.size() != 0){
                        /*获取数据成功后发送消息*/
                        Message message = new Message();
                        message.what=SUCCESS;
                        findVideosHandler.sendMessage(message);
                    }



                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 解析视频信息
     *
     */

    public void parseVideo(JSONArray jsonArray){

        for (int i = 0; i < jsonArray.length() ; i++) {
            JSONObject jsonObject = null;
            try {
                jsonObject = jsonArray.getJSONObject(i);

                Video video = new Video();
                video.setId(jsonObject.getInt("id"));
                video.setImgUrl(jsonObject.getString("imgUrl"));
                video.setStar(jsonObject.getInt("star"));

                /*通过反射的方式获得视屏的id*/
                int id = -1;  //资源id
                Field field =  R.raw.class.getField(jsonObject.getString("videoUrl"));
                id = field.getInt(null);

                video.setVideoUrl(id);

                videoList.add(video);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

//    /**
//     * 把点赞的请求发送给后台
//     */
//    public void sendAddStar(int id){
//
//    }

    /**
     * 给返回按钮设置监听器
     */
    private void initListener() {
        View goBack = findViewById(R.id.go_back);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到天气activity
                Intent intent = new Intent(VideoActivity.this , WeatherActivity.class);
                startActivity(intent);
            }
        });
        myLayoutManager.setOnViewPagerListener(new OnViewPagerListener() {
            @Override
            public void onInitComplete() {

            }

            /**
             *判断需要释放的view
             * @param isNext
             * @param position
             */
            @Override
            public void onPageRelease(boolean isNext, int position) {
                Log.e(TAG, "释放位置:" + position + " 下一页:" + isNext);
                int index = 0;
                if (isNext) {
                    index = 0;
                } else {
                    index = 1;
                }
                releaseVideo(index);
            }

            /**
             * 当选中view(RelativeLayout的子元素)的时候触发
             * @param position
             * @param bottom
             */
            @Override
            public void onPageSelected(int position, boolean bottom) {
                currentVideoId = videoList.get(position).getId();
                isAlreadStar = false;
                Log.e(TAG, "选择位置:" + position + " 下一页:" + bottom);
                playVideo(0);
            }
        });
    }

    /**
     * 自定义的适配器
     */
    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {


        private List<Video> dataList = null;
        private int index = 0;
        private Context mContext;

        public MyAdapter(Context context , List<Video> dataList) {
            this.mContext = context;
            this.dataList = dataList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_view_pager, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
//            holder.img_thumb.setImageResource(imgs[index]);
//            holder.videoView.setVideoURI(Uri.parse("android.resource://"
//                    + getPackageName() + "/" + videos[index]));

            Glide.with(mContext).load(dataList.get(index).getImgUrl()).into(holder.img_thumb);
            holder.videoView.setVideoURI(Uri.parse("android.resource://"
                    + getPackageName() + "/" + dataList.get(index).getVideoUrl()));String s = dataList.get(index).getStar() + "";
            holder.starCount.setText(s);

            index++;
            if (index >= 7) {
                index = 0;
            }
        }

        @Override
        public int getItemCount() {
            return 88;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView img_thumb;
            VideoView videoView;
            ImageView img_play;
            RelativeLayout rootView;
            TextView starCount;

            public ViewHolder(View itemView) {
                super(itemView);
                img_thumb = (ImageView) itemView.findViewById(R.id.img_thumb);
                videoView = (VideoView) itemView.findViewById(R.id.video_view);
                img_play = (ImageView) itemView.findViewById(R.id.img_play);
                rootView = (RelativeLayout) itemView.findViewById(R.id.root_view);
                starCount = (TextView) itemView.findViewById(R.id.star_count);
                Log.e("videoActivity" , starCount.getText().toString());
            }
        }
    }

    /**
     * 释放View
     * @param index
     */
    private void releaseVideo(int index) {
        /*注：getChildAt()这个方法,只能get到屏幕显示的部分.*/
        /*获得即将移出屏幕的view*/
        View itemView = mRecyclerView.getChildAt(index);
        final VideoView videoView = (VideoView) itemView.findViewById(R.id.video_view);
        final ImageView imgThumb = (ImageView) itemView.findViewById(R.id.img_thumb);
        final ImageView imgPlay = (ImageView) itemView.findViewById(R.id.img_play);
        videoView.stopPlayback();
        imgThumb.animate().alpha(1).start();
        imgPlay.animate().alpha(0f).start();
    }

    /**
     * 播放视频
     * @param position
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void playVideo(int position) {
        //获得指定位置的子元素
        View itemView = mRecyclerView.getChildAt(position);

        //查找子元素各元素
        final MyVideoView videoView = (MyVideoView) itemView.findViewById(R.id.video_view);
        final ImageView imgPlay = (ImageView) itemView.findViewById(R.id.img_play);
        final ImageView imgThumb = (ImageView) itemView.findViewById(R.id.img_thumb);
        final RelativeLayout rootView = (RelativeLayout) itemView.findViewById(R.id.root_view);
        starIV = itemView.findViewById(R.id.star);
        starCountTV = itemView.findViewById(R.id.star_count);

        final MediaPlayer[] mediaPlayer = new MediaPlayer[1];

        /**
         * 给爱心设置监听器，处理点赞
         */
        starIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isAlreadStar) {
                    addStar(true);
                }else{
                    addStar(false);
                }
            }
        });
        //OnPreparedListener视频状态的监听器
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

            }
        });


        videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            /**
             *当有信息或者警告触发
             * @param mp    信息所属的媒体播放器。
             * @param what  信息
             * @param extra
             * @return
             */
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                mediaPlayer[0] = mp;
                //设置视频循环
                mp.setLooping(true);
                imgThumb.animate().alpha(0).setDuration(200).start();
                return false;
            }
        });


        videoView.start();

        /**
         * 给视频设置点击监听器
         */
        imgPlay.setOnClickListener(new View.OnClickListener() {
            boolean isPlaying = true;

            @Override
            public void onClick(View v) {

                if (videoView.isPlaying()) {    //视频处于播放状态
                    imgPlay.animate().alpha(0.7f).start();
                    videoView.pause();  //暂停视频
                    isPlaying = false;
                } else {    //视频处于暂停状态
                    imgPlay.animate().alpha(0f).start();
                    videoView.start();
                    isPlaying = true;
                }
            }
        });
    }



    Handler addStarHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                /*点赞或者取消点赞成功，执行前端页面的数据的刷新*/
                case ADD_STAR_SUCCESS:
//                    Log.e("addStarHandler","进入addStarHandler");
                    isAlreadStar = true;
                    //换成红色
                    starIV.setImageResource(R.mipmap.icon_alrea_star);
                    int oldValue = Integer.parseInt(starCountTV.getText().toString());
                    Log.e("oldValue","oldValue");
                    starCountTV.setText(oldValue+1+"");
                    for(Video v: videoList){
                        if(currentVideoId == v.getId()){
                            v.setStar(v.getStar()+1);
                        }
                    }

                    break;
                case REMOVE_STAR_SUCCESS:
                    isAlreadStar = false;
                    //换成白色
                    starIV.setImageResource(R.mipmap.heart_icon);
                    oldValue = Integer.parseInt(starCountTV.getText().toString());
                    Log.e("oldValue","oldValue");
                    starCountTV.setText(oldValue-1+"");
                    for(Video v: videoList){
                        if(currentVideoId == v.getId()){
                            v.setStar(v.getStar()-1);
                        }
                    }

                    break;
            }
        }
    };

    /**
     * 向后台发送增加点赞数量的请求
     */
    public void addStar(boolean flag){
        isAdd = flag;
        new Thread() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url("http://139.159.133.43:8080/video/addStar?id="+currentVideoId+"&isAdd="+isAdd).build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Log.e("currentVideoId",currentVideoId+"");
                    Log.e("responseData",responseData);

                    if("true".equals(responseData)){

                        /*后端点赞成功后发送信息*/
                        Message message = new Message();
                        if(isAdd) {
                            message.what = ADD_STAR_SUCCESS;
                        }else{
                            message.what = REMOVE_STAR_SUCCESS;
                        }
                        addStarHandler.sendMessage(message);
                    }


                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }.start();
    }

}
