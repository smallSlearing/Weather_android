package com.juhe.weather.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.juhe.weather.NewsInfoActivity;
import com.juhe.weather.R;
import com.juhe.weather.bean.NewsBean;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * 新闻的适配器
 */
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    //页面
    private Context mContext;
    //新闻列表
    private List<NewsBean> mNewsList;

    static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        ImageView iv_new_image; //新闻图片
        TextView tv_new_date;   //新闻日期
        TextView tv_new_title;  //新闻标题
        TextView tv_news_category;  //新闻类型

        public ViewHolder(View view){
            super(view);
            cardView =(CardView) view;
            //分别查找到新闻相关的组件分别赋值给各个属性
            iv_new_image = (ImageView)view.findViewById(R.id.new_image);
            tv_new_title =(TextView)view.findViewById(R.id.news_title);
            tv_new_date=(TextView)view.findViewById(R.id.news_date);
            tv_news_category =(TextView)view.findViewById(R.id.news_category);
        }
    }

    public NewsAdapter(List<NewsBean> mNewsList){
        this.mNewsList = mNewsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mContext == null){
            mContext = parent.getContext();
        }

        View view = LayoutInflater.from(mContext).inflate(R.layout.item_news_list,parent,false);
        //创建ViewHolder对象
        final ViewHolder holder=new ViewHolder(view);

        //处理点击某个元素的跳转到详情页面的操作
        holder.cardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                int position=holder. getAdapterPosition();
                NewsBean bean = mNewsList.get(position);
//                Fruit fruit=mFruitlist. get(position);
                Intent intent=new Intent(mContext, NewsInfoActivity.class);
                intent. putExtra("url", bean.getUrl());
//                intent. putExtra(FruitActivity. FRUITIMAGE_ID, fruit. getImageId());
                mContext. startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //获得一个新闻对象
        NewsBean newsBean = mNewsList.get(position);

        //把新闻对象的属性值设置到页面
        holder.tv_new_date.setText(newsBean.getDate());
        holder.tv_news_category.setText(newsBean.getCategory());
        holder.tv_new_title.setText(newsBean.getTitle());
        //加载网络图片
        Glide.with(mContext).load(newsBean.getThumbnailPicS()).into(holder.iv_new_image);

    }

    @Override
    public int getItemCount() {
        return mNewsList.size();
    }
}
