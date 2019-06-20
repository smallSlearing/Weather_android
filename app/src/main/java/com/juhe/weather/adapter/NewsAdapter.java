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
    private Context mContext;
    private List<NewsBean> mNewsList;

    static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        ImageView iv_new_image;
        TextView tv_new_date;
        TextView tv_new_title;
        TextView tv_news_category;

        public ViewHolder(View view){
            super(view);
            cardView =(CardView) view;
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
        NewsBean newsBean = mNewsList.get(position);
        holder.tv_new_date.setText(newsBean.getDate());
        holder.tv_news_category.setText(newsBean.getCategory());
        holder.tv_new_title.setText(newsBean.getTitle());
        Glide.with(mContext).load(newsBean.getThumbnailPicS()).into(holder.iv_new_image);

    }

    @Override
    public int getItemCount() {
        return mNewsList.size();
    }
}
//
//
//public class NewsAdapter extends ArrayAdapter<NewsBean> {
//    private int resourceId;
//    ImageView iv_new_image;
//    TextView tv_new_date;
//    TextView tv_new_title;
//    TextView tv_news_category;
//
//    public NewsAdapter(Context context, int textViewResourceId, List<NewsBean> objects) {
//        super(context, textViewResourceId, objects);
//        resourceId = textViewResourceId;
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        NewsBean newsBean = getItem(position);
//        //获取当前项的Fruit 实例
//        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
//        iv_new_image = (ImageView) view.findViewById(R.id.new_image);
//        tv_new_title = (TextView) view.findViewById(R.id.news_date);
//        tv_new_date = (TextView) view.findViewById(R.id.news_date);
//        tv_news_category = (TextView) view.findViewById(R.id.news_category);
//
//        tv_new_date.setText(newsBean.getDate());
//        tv_news_category.setText(newsBean.getCategory());
//        tv_new_title.setText(newsBean.getTitle());
//
//        //加载网络图片
//        Bitmap urLimage = getURLimage(newsBean.getThumbnailPicS());
//        iv_new_image.setImageBitmap(urLimage);
//
//        return view;
//    }
//
//
//    /**
//     * 加载网络图片
//     *
//     * @param url
//     * @return
//     */
//    public Bitmap getURLimage(String url) {
//        Bitmap bmp = null;
//        try {
//            URL myurl = new URL(url);
//            // 获得连接
//            HttpURLConnection conn = (HttpURLConnection) myurl.openConnection();
//            conn.setConnectTimeout(6000);//设置超时
//            conn.setDoInput(true);
//            conn.setUseCaches(false);//不缓存
//            conn.connect();
//            InputStream is = conn.getInputStream();//获得图片的数据流
//            bmp = BitmapFactory.decodeStream(is);
//            is.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return bmp;
//    }
//
//}
