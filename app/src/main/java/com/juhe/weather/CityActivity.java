package com.juhe.weather;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.juhe.weather.adapter.CityListAdatper;
import com.thinkland.juheapi.common.JsonCallBack;
import com.thinkland.juheapi.data.weather.WeatherData;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CityActivity extends Activity {
    private final int UPDATE_VIEW = 5;
    private ListView lv_city;
    private List<String> list;	//具有符合条件的列表
    private List<String> allEleList;	//具有所有数据的列表
    EditText et_content;	//搜索框的对象
    ImageView iv_search,search_clear;     //搜索按钮对象
    CityListAdatper adatper;  //城市适配器
    JSONObject json = null;
    private Handler hd = new MyHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);
        initViews();
        getCities();

        et_content = (EditText) findViewById(R.id.search);
        iv_search = (ImageView)findViewById(R.id.search_button);
		/*处理搜索城市*/
        iv_search.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = et_content.getText().toString().trim();
                list.clear();//清空列表

                for (String s:allEleList) {	//查找所有符合条件的城市
                    if(s.indexOf(content) >= 0){
                        list.add(s);
                    }
                }

                adatper.notifyDataSetChanged();
            }
        });


    }

    private void initViews() {
        findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                finish();
            }
        });

        lv_city = (ListView) findViewById(R.id.lv_city);
        search_clear = findViewById(R.id.search_clear);//清除图片
        search_clear.setOnClickListener(new View.OnClickListener(){//点击删除图片，文本编辑框清空，清空图片隐藏
            @Override
            public void onClick(View v) {
                et_content.setText("");
                //search.setHint("城市选择");
                search_clear.setVisibility(View.GONE);
            }
        });

        et_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()==0){
                    search_clear.setVisibility(View.GONE);
                }else{
                    search_clear.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     *从后台获取城市列表
     */
    private void getCities(){
        new Thread(){
            @Override
            public void run() {
                try {
                    //从后台查询数据
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url("http://139.159.133.43:8080/weather/getCityList").build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();

                    json = new JSONObject(responseData).getJSONObject("cityList");

                    //通知更新ui
                    Message msg = new Message();
                    msg.what = UPDATE_VIEW; // 用户自定义的一个值，用于标识不同类型的消息
                    hd.sendMessage(msg); // 发送消息

                }catch(Exception e){

                }
            }
        }.start();

    }

    /**
     * 解析城市列表的json数据
     */
    private void parseCities() {

        // TODO Auto-generated method stub
        try {

            int code = json.getInt("resultcode");
            int error_code = json.getInt("error_code");
            if (error_code == 0 && code == 200) {

                list = new ArrayList<String>();
                allEleList = new ArrayList<>();

                JSONArray resultArray = json.getJSONArray("result");
                Set<String> citySet = new HashSet<String>();
                for (int i = 0; i < resultArray.length(); i++) {
                    String city = resultArray.getJSONObject(i).getString("city");
                    city = city +" "+resultArray.getJSONObject(i).getString("district");
                    citySet.add(city);
                }

                allEleList.addAll(citySet);
                list.addAll(allEleList);

                adatper = new CityListAdatper(CityActivity.this, list);
                lv_city.setAdapter(adatper);
                lv_city.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    //选择城市后跳回天气页面，并把选择的城市的名字传回去
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        // TODO Auto-generated method stub
                        Intent intent = new Intent();
                        intent.putExtra("city", list.get(arg2));
                        setResult(1, intent);
                        finish();
                    }
                });

            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // 定义一个内部类继承自Handler，并且覆盖handleMessage方法用于处理子线程传过来的消息
    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_VIEW: // 接受到消息之后，对UI控件进行修改
                    parseCities();
                    break;
                default:
                    break;
            }
        }
    }
}
