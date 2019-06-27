package com.juhe.weather;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class NewsInfoActivity extends AppCompatActivity {

    public static final String NEWS_URL = "url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_news_info);



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("新闻详情");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //开启返回功能
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
//        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        Intent intent = getIntent();
        //获得上个活动传过来的url
        String newUrl = intent.getStringExtra(NEWS_URL);
        //设置内容
        WebView webView = (WebView) findViewById(R.id.newContent);
        webView.loadUrl(newUrl);


//        collapsingToolbar.setTitle(fruitName);
//        Glide.with(this).load(fruitImageId).into(fruitImageView);
//        String fruitContent = generateFruitContent(fruitName);
//        fruitContentText.setText(fruitContent);
    }

//    private String generateFruitContent(String fruitName) {
//        StringBuilder fruitContent = new StringBuilder();
//        for (int i = 0; i < 500; i++) {
//            fruitContent.append(fruitName);
//        }
//        return fruitContent.toString();
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                finish();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}
