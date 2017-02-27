package activity;

import service.AutoUpdateService;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;

import com.coolweather.app.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity implements OnClickListener{

	// 这个布局控件里包含了天气信息
	private LinearLayout weatherInfoLayout;

	/**
	 * 用于显示城市名
	 */
	private TextView cityNameText;

	/**
	 * 用于显示发布时间
	 */
	private TextView publishText;

	/**
	 * 用于显示天气描述信息
	 */
	private TextView weatherDespText;

	/**
	 * 用于显示气温1
	 */
	private TextView temp1Text;

	/**
	 * 用于显示气温2
	 */
	private TextView temp2Text;

	/**
	 * 用于显示当前日期
	 */
	private TextView currentDateText;
	
	/**
	 * 切换城市按钮
	 */
	private Button switchCity;
	
	/**
	 * 更新天气
	 */
	private Button refreshWeather;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		// 初始化各控件
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text = (TextView) findViewById(R.id.temp2);
		currentDateText = (TextView) findViewById(R.id.current_date);
		
		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);

		String countryCode = getIntent().getStringExtra("country_code");

		if (!TextUtils.isEmpty(countryCode)) {
			// 有县级代号时就去查询天气
			publishText.setText("同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			//
			queryWeatherCode(countryCode);
		} else {
			// 没有县级代号时就直接显示本地天气
			showWeather();
		}
	}

	/**
	 * 从服务器查询县级代号所对应的天气代号
	 */
	private void queryWeatherCode(String countryCode) {

		String address = "http://www.weather.com.cn/data/list3/city"
				+ countryCode + ".xml";
		// 这里的queryFromServer是WeatherActivity里的，和ChooseActivity里的不一样，
		// 这里是无法调用到ChooseActivity里的queryFromServer
		// 这里查询的是县级代号对应的天气代号
		queryFromServer(address, "countryCode");
	}

	/**
	 * 从服务器查询天气代号所对应的天气情况
	 */
	private void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/"
				+ weatherCode + ".html";
		// 这里查询的是天气代号对应的天气情况
		queryFromServer(address, "weatherCode");
	}

	/**
	 * 根据传入的地址和类型去向服务器查询天气代号或者天气信息
	 */
	private void queryFromServer(final String address, final String type) {
		// 相当与new出来一个对象，并实现了接口里的方法，供自己用
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinish(final String response) {
				// TODO Auto-generated method stub，重写接口里的方法
				if ("countryCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						// 从服务器返回的数据中解析出天气代号
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);

						}

					}
				} else if ("weatherCode".equals(type)) {
					// 将服务器返回的数据利用json解析存入sharedpreference中
					Utility.handleWeatherResponse(WeatherActivity.this,
							response);
					// 回到主线程更新UI
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							showWeather();
						}
					});
				}
			}

			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub，重写接口里的方法
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						// 如果服务器未能成功返回数据，发布时间那块显示同步失败
						publishText.setText("同步失败");
					}
				});
			}
		});

	}
	
	/**
	 * 从sharedPreferences文件中读取存储的天气信息，并显示到界面上
	 */
	
	private void showWeather() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		temp2Text.setText(prefs.getString("temp2", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		publishText.setText("今天" + prefs.getString("publish_time", "") + "发布");
		currentDateText.setText(prefs.getString("current_date", ""));
		//由于在前面把这俩个控件设为不可见，因此现在要设为可见
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		
		Intent intent = new Intent(this, AutoUpdateService.class);
		startService(intent);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.switch_city:

			Intent intent = new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			
			break;
		
		case R.id.refresh_weather:
			
			publishText.setText("同步中...");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			//从SharedPreferences文件中读取天气代号
			String weatherCode = prefs.getString("weather_code", "");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
			}
			
			break;

		default:
			break;
		}
	}

}
