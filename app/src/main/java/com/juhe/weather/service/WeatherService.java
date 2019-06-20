package com.juhe.weather.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.juhe.weather.WeatherActivity;
import com.juhe.weather.bean.FutureWeatherBean;
import com.juhe.weather.bean.HoursWeatherBean;
import com.juhe.weather.bean.NewsBean;
import com.juhe.weather.bean.PMBean;
import com.juhe.weather.bean.WeatherBean;
import com.thinkland.juheapi.common.JsonCallBack;
import com.thinkland.juheapi.data.weather.WeatherData;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class WeatherService extends Service {

	private String city;
	private final String tag = "WeatherService";
	private WeatherServiceBinder binder = new WeatherServiceBinder();
	private boolean isRunning = false;
	private List<HoursWeatherBean> list;
	private List<NewsBean> newsList;
	private PMBean pmBean;
	private WeatherBean weatherBean;
	private OnParserCallBack callBack;

	private final int REPEAT_MSG = 0x01;
	private final int CALLBACK_OK = 0x02;
	private final int CALLBACK_ERROR = 0x04;

	private WeatherActivity weatherActivity;

	public interface OnParserCallBack {
		public void OnParserComplete(List<HoursWeatherBean> list, PMBean pmBean, WeatherBean weatherBean , List<NewsBean> list2);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		//获取WeatherActivity传过来的参数
		weatherActivity = (WeatherActivity)arg0.getSerializableExtra("weatherActivity");
		return binder;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		city = "广州 增城";
		mHandler.sendEmptyMessage(REPEAT_MSG);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.v(tag, "onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub

			switch (msg.what) {
			case REPEAT_MSG:

				getCityWeather();
				sendEmptyMessageDelayed(REPEAT_MSG, 30 * 60 * 1000);
				break;
			case CALLBACK_OK:
				if (callBack != null) {
					callBack.OnParserComplete(list, pmBean, weatherBean , newsList);
				}
				isRunning = false;
				break;
			case CALLBACK_ERROR:
				Toast.makeText(getApplicationContext(), "loading error", Toast.LENGTH_SHORT).show();
				break;

			default:
				break;
			}
		}

	};

	// 解析pm
	private PMBean parserPM(JSONObject json) {
		PMBean bean = null;
		try {
			int code = json.getInt("resultcode");
			int error_code = json.getInt("error_code");
			if (error_code == 0 && code == 200) {
				bean = new PMBean();
				JSONObject pmJSON = json.getJSONArray("result").getJSONObject(0);
				bean.setAqi(pmJSON.getString("AQI"));
				bean.setQuality(pmJSON.getString("quality"));

			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bean;

	}

	public void setCallBack(OnParserCallBack callback) {
		this.callBack = callback;
	}

	public void removeCallBack() {
		callBack = null;
	}

	public void getCityWeather(String city) {
		this.city = city;
		getCityWeather();
	}

	public void getCityWeather() {
		if (isRunning) {
			return;
		}
		isRunning = true;
		final CountDownLatch countDownLatch = new CountDownLatch(3);

		new Thread(){
			@Override
			public void run() {
				try{
					if(city == null){
						city = "广州 增城";
					}
					OkHttpClient client = new OkHttpClient();
					Request request = new Request.Builder().url("http://139.159.133.43:8080/weather/cityWeather?cityName="+city).build();
					Response response = client.newCall(request).execute();
					String responseData = response.body().string();

					JSONObject jsonObject = new JSONObject(responseData);

					JSONObject cityWeather = jsonObject.getJSONObject("cityWeather");
					weatherBean = parserWeather(cityWeather);
					countDownLatch.countDown();


					JSONObject hoursWeather = jsonObject.getJSONObject("hoursWeather").getJSONArray("HeWeather6").getJSONObject(0);
					list = parserForecast3h(hoursWeather);
					countDownLatch.countDown();


					JSONObject pm = jsonObject.getJSONObject("pm");
					countDownLatch.countDown();
					pmBean = parserPM(pm);
//					weatherActivity.setPMView(pmBean);
					System.out.println("pm="+pm);

					JSONArray jsonArray = jsonObject.getJSONArray("news");
					newsList = parserNews(jsonArray);

				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}.start();


		new Thread() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {

					countDownLatch.await();
					mHandler.sendEmptyMessage(CALLBACK_OK);
					System.out.println("CALLBACK_OK");
				} catch (Exception ex) {
					mHandler.sendEmptyMessage(CALLBACK_ERROR);
					System.out.println("CALLBACK_ERROR");
					return;
				}
			}

		}.start();
	}

	// 解析城市天气查询接口
	private WeatherBean parserWeather(JSONObject json) {

		WeatherBean bean = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

		try {
			int code = json.getInt("resultcode");
			int error_code = json.getInt("error_code");
			if (error_code == 0 && code == 200) {
				JSONObject resultJson = json.getJSONObject("result");
				bean = new WeatherBean();

				// toady
				JSONObject todayJson = resultJson.getJSONObject("today");
				bean.setCity(todayJson.getString("city"));
				bean.setUv_index(todayJson.getString("uv_index"));
				bean.setTemp(todayJson.getString("temperature"));
				bean.setWeather_str(todayJson.getString("weather"));
				bean.setWeather_id(todayJson.getJSONObject("weather_id").getString("fa"));
				bean.setDressing_index(todayJson.getString("dressing_index"));

				// sk
				JSONObject skJson = resultJson.getJSONObject("sk");
				bean.setWind(skJson.getString("wind_direction") + skJson.getString("wind_strength"));
				bean.setNow_temp(skJson.getString("temp"));
				bean.setRelease(skJson.getString("time"));
				bean.setHumidity(skJson.getString("humidity"));

				// future

				Date date = new Date(System.currentTimeMillis());
				JSONArray futureArray = resultJson.getJSONArray("future");
				List<FutureWeatherBean> futureList = new ArrayList<FutureWeatherBean>();
				for (int i = 0; i < futureArray.length(); i++) {
					JSONObject futureJson = futureArray.getJSONObject(i);
					FutureWeatherBean futureBean = new FutureWeatherBean();
					Date datef = sdf.parse(futureJson.getString("date"));
					if (!datef.after(date)) {
						continue;
					}
					futureBean.setTemp(futureJson.getString("temperature"));
					futureBean.setWeek(futureJson.getString("week"));
					futureBean.setWeather_id(futureJson.getJSONObject("weather_id").getString("fa"));
					futureList.add(futureBean);
					if (futureList.size() == 3) {
						break;
					}
				}
				bean.setFutureList(futureList);

			} else {
				Toast.makeText(getApplicationContext(), "WEATHER_ERROR", Toast.LENGTH_SHORT).show();
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bean;

	}

	// 解析新闻列表
	private List<NewsBean> parserNews(JSONArray jsonArr) {
		List<NewsBean> newsBeanList2 = null;

		try {
			newsBeanList2 = new ArrayList<>();
			for (int i = 0; i < jsonArr.length(); i++) {
				NewsBean newsBean = new NewsBean();
				JSONObject newsJson = jsonArr.getJSONObject(i);
				newsBean.setTitle(newsJson.getString("title"));

				newsBeanList2.add(newsBean);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return newsBeanList2;

	}


	// 解析3小时预报
	private List<HoursWeatherBean> parserForecast3h(JSONObject json) {
		List<HoursWeatherBean> list = null;
		//定义日期转化的格式
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		Date date = new Date(System.currentTimeMillis());

		try {
			String status = json.getString("status");	//获得查询结果状态,ok为成功

			if ("ok".equals(status)) {
				list = new ArrayList<HoursWeatherBean>();
				JSONArray resultArray = json.getJSONArray("hourly");
				for (int i = 0; i < resultArray.length(); i++) {
					JSONObject hourJson = resultArray.getJSONObject(i);
					Date hDate = sdf.parse(hourJson.getString("time"));
					if (!hDate.after(date)) {
						continue;
					}
					HoursWeatherBean bean = new HoursWeatherBean();
					bean.setWeather_id(hourJson.getString("cond_code"));
					bean.setTemp(hourJson.getString("tmp"));
					Calendar c = Calendar.getInstance();
					c.setTime(hDate);
					bean.setTime(c.get(Calendar.HOUR_OF_DAY) + "");
					list.add(bean);
					if (list.size() == 5) {
						break;
					}
				}

			} else {
				Toast.makeText(getApplicationContext(), "HOURS_ERROR", Toast.LENGTH_SHORT).show();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.v(tag, "onDestroy");
	}

	public class WeatherServiceBinder extends Binder {

		public WeatherService getService() {
			return WeatherService.this;
		}

	}

}
