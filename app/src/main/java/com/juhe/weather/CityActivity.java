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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
	private List<String> list;
	JSONObject json = null;
	private Handler hd = new MyHandler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_city);
		initViews();
		getCities();

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
						JSONArray resultArray = json.getJSONArray("result");
						Set<String> citySet = new HashSet<String>();
						for (int i = 0; i < resultArray.length(); i++) {
							String city = resultArray.getJSONObject(i).getString("city");
							city = city +" "+resultArray.getJSONObject(i).getString("district");
							citySet.add(city);
						}
						list.addAll(citySet);
						CityListAdatper adatper = new CityListAdatper(CityActivity.this, list);
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
