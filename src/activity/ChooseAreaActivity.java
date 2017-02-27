package activity;

import java.util.ArrayList;
import java.util.List;

import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;

import com.coolweather.app.R;

import model.City;
import model.Country;
import model.Province;

import db.CoolWeatherDB;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.DownloadManager.Query;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {

	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTRY = 2;

	// 进度条对话框，最后记住最后一定要show()才能生效，和土司一样
	private ProgressDialog progressDialog;
	private TextView titleText;
	// 用于显示省市县列表
	private ListView listView;
	// 数据适配器，将省市县的数组列表放入
	private ArrayAdapter<String> adapter;
	// 用于对数据库进行增删改查
	private CoolWeatherDB coolWeatherDB;
	// 一开始是一个空的字符串列表，用于在listview上做显示
	private List<String> dataList = new ArrayList<String>();

	/**
	 * 省列表，目的是为了获得所有省对象
	 */
	private List<Province> provinceList;

	/**
	 * 市列表，目的是为了获得某个省下的所有市对象
	 */
	private List<City> cityList;

	/**
	 * 县列表，目的是为了获得某个市下的所有县对象
	 */
	private List<Country> countryList;

	/**
	 * 选中的省份
	 */
	private Province selectedProvince;

	/**
	 * 选中的城市
	 */
	private City SelectedCity;

	/**
	 * 当前选中的级别
	 */
	private int currentLevel;
	
	/**
	 * 是否从weatherActivity中跳转过来
	 */
	private boolean isFromWeatherActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		
		//已经选择了城市且不是从WeatherActivity跳转过来，才会直接跳转到WeatherActivity
		/**
		 * 如果从当前应用的sharedpreferences文件中读取city_selected标志位为ture,说明
		 * 当前已经选择过城市，直接跳转到weatherActivity即可
		 */
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (prefs.getBoolean("city_selected", false) && !isFromWeatherActivity) {
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;  //后面的语句就不执行了
		}
		
		// 没有标题栏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		// 找到控件，listview只有一个
		listView = (ListView) findViewById(R.id.list_view);
		// 用于显示省市县上方的文本
		titleText = (TextView) findViewById(R.id.title_text);
		// 创建一个适配器对象，里面封装了数据列表，和每个条目的样式
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, dataList);
		// 给listview设置数据适配器
		listView.setAdapter(adapter);
		// 获取CoolWeatherDB的实例，有了这个就可以对数据库进行操作，所以也需要在活动创建的时候初始化
		coolWeatherDB = CoolWeatherDB.getInstance(this);

		// 为listview设置点击事件
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				// 如果当前选中的是省级
				if (currentLevel == LEVEL_PROVINCE) {
					// 首先获取被点击的省份对象
					selectedProvince = provinceList.get(position);
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					// 如果当前被选中的是市级，则获取被点击的市对象
					SelectedCity = cityList.get(position);
					queryCountries();
				} else if (currentLevel == LEVEL_COUNTRY) {
					//如果当前的层次是县级别
					String countryCode = countryList.get(position).getCountryCode();
					Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					intent.putExtra("country_code", countryCode);
					startActivity(intent);
					finish();
				}

			}
		});

		// 加载省级数据
		queryProvinces();
	}

	/**
	 * 查询全国所有的省，优先从数据库查询，如果没有查询到在去服务器上查询
	 */
	private void queryProvinces() {
		// 从数据库中所有省，放入省列表中
		provinceList = coolWeatherDB.loadProvinces();
		// 如果数据库中有省份信息，最直接拿来显示在listview上
		if (provinceList.size() > 0) {
			// 首先清除datalist里的内容
			dataList.clear();
			// 将从服务器端获取的省列表中的数据赋给数据列表
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			// 通知需要显示的集合发生了变化
			adapter.notifyDataSetChanged();

			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		} else {
			// 若数据库中没有，则从服务器获取数据
			queryFromServer(null, "province");
		}
	}

	/**
	 * 查询选中省内所有的市，优先从数据库查询，如果没有查询到在去服务器上查询
	 */
	private void queryCities() {
		cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		// 如果数据库中有某个省份下的所有城市，则从数据库中读取数据列表来显示到listview上
		if (cityList.size() > 0) {
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			// 将标题栏显示为被选中的省份
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		} else {
			// 如果数据库中没有数据，则从服务器端获取数据显示到listview上
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}

	/**
	 * 查询选中市内所有的县，优先从数据库查询，如果没有查询到在去服务器上查询
	 */
	private void queryCountries() {
		countryList = coolWeatherDB.loadCountries(SelectedCity.getId());
		if (countryList.size() > 0) {
			dataList.clear();
			for (Country country : countryList) {
				dataList.add(country.getCountryName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(SelectedCity.getCityName());
			currentLevel = LEVEL_COUNTRY;
		} else {
			queryFromServer(SelectedCity.getCityCode(), "country");
		}
	}

	/**
	 * 根据传入的代号和类型从服务器上查询省市县数据
	 */
	private void queryFromServer(final String code, final String type) {
		String address;
		// 如果传来的代号不为空
		if (!TextUtils.isEmpty(code)) {
			// 则地址要么是查市，要么是查县
			address = "http://www.weather.com.cn/data/list3/city" + code
					+ ".xml";
		} else {
			// 则地址是查省的
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		// 在这个方法中将HttpCallbackListener接口里的抽象方法进行重写，调用这个方法会开启子线程访问网络，并获取数据response
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinish(String response) {
				// 一旦
				boolean result = false;
				if ("province".equals(type)) {
					result = Utility.handleProvincesResponse(coolWeatherDB,
							response);
				} else if ("city".equals(type)) {
					result = Utility.handleCitiesResponse(coolWeatherDB,
							response, selectedProvince.getId());
				} else if ("country".equals(type)) {
					result = Utility.handleCountriesResponse(coolWeatherDB,
							response, SelectedCity.getId());
				}
				if (result) {
					// 通过runOnUiThread（）方法回到主线程处理逻辑
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)) {
								//此时已经将数据存入到数据库中，所以可以直接从数据库中查询
								queryProvinces();
							} else if ("city".equals(type)) {
								queryCities();
							} else if ("country".equals(type)) {
								queryCountries();
							}
						}
					});
				}

			}

			@Override
			public void onError(Exception e) {
				//通过runOnUiThread（）方法回到主线程处理逻辑
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {

						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});

	}

	
	/**
	 * 显示进度条对话框
	 */
	private void showProgressDialog() {

		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	/**
	 * 关闭进度条对话框
	 */
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	
	/**
	 * 捕获back键，根据当前的级别来判断，此时应该返回市列表、省列表、还是直接退出
	 */
	@Override
	public void onBackPressed() {

		if (currentLevel == LEVEL_COUNTRY) {
			queryCities();
		} else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		} else {
			if (isFromWeatherActivity) {
				Intent intent = new Intent(this, WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}

}
