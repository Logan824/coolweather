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

	// �������Ի�������ס���һ��Ҫshow()������Ч������˾һ��
	private ProgressDialog progressDialog;
	private TextView titleText;
	// ������ʾʡ�����б�
	private ListView listView;
	// ��������������ʡ���ص������б����
	private ArrayAdapter<String> adapter;
	// ���ڶ����ݿ������ɾ�Ĳ�
	private CoolWeatherDB coolWeatherDB;
	// һ��ʼ��һ���յ��ַ����б�������listview������ʾ
	private List<String> dataList = new ArrayList<String>();

	/**
	 * ʡ�б�Ŀ����Ϊ�˻������ʡ����
	 */
	private List<Province> provinceList;

	/**
	 * ���б�Ŀ����Ϊ�˻��ĳ��ʡ�µ������ж���
	 */
	private List<City> cityList;

	/**
	 * ���б�Ŀ����Ϊ�˻��ĳ�����µ������ض���
	 */
	private List<Country> countryList;

	/**
	 * ѡ�е�ʡ��
	 */
	private Province selectedProvince;

	/**
	 * ѡ�еĳ���
	 */
	private City SelectedCity;

	/**
	 * ��ǰѡ�еļ���
	 */
	private int currentLevel;
	
	/**
	 * �Ƿ��weatherActivity����ת����
	 */
	private boolean isFromWeatherActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		
		//�Ѿ�ѡ���˳����Ҳ��Ǵ�WeatherActivity��ת�������Ż�ֱ����ת��WeatherActivity
		/**
		 * ����ӵ�ǰӦ�õ�sharedpreferences�ļ��ж�ȡcity_selected��־λΪture,˵��
		 * ��ǰ�Ѿ�ѡ������У�ֱ����ת��weatherActivity����
		 */
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (prefs.getBoolean("city_selected", false) && !isFromWeatherActivity) {
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;  //��������Ͳ�ִ����
		}
		
		// û�б�����
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		// �ҵ��ؼ���listviewֻ��һ��
		listView = (ListView) findViewById(R.id.list_view);
		// ������ʾʡ�����Ϸ����ı�
		titleText = (TextView) findViewById(R.id.title_text);
		// ����һ�����������������װ�������б���ÿ����Ŀ����ʽ
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, dataList);
		// ��listview��������������
		listView.setAdapter(adapter);
		// ��ȡCoolWeatherDB��ʵ������������Ϳ��Զ����ݿ���в���������Ҳ��Ҫ�ڻ������ʱ���ʼ��
		coolWeatherDB = CoolWeatherDB.getInstance(this);

		// Ϊlistview���õ���¼�
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				// �����ǰѡ�е���ʡ��
				if (currentLevel == LEVEL_PROVINCE) {
					// ���Ȼ�ȡ�������ʡ�ݶ���
					selectedProvince = provinceList.get(position);
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					// �����ǰ��ѡ�е����м������ȡ��������ж���
					SelectedCity = cityList.get(position);
					queryCountries();
				} else if (currentLevel == LEVEL_COUNTRY) {
					//�����ǰ�Ĳ�����ؼ���
					String countryCode = countryList.get(position).getCountryCode();
					Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					intent.putExtra("country_code", countryCode);
					startActivity(intent);
					finish();
				}

			}
		});

		// ����ʡ������
		queryProvinces();
	}

	/**
	 * ��ѯȫ�����е�ʡ�����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ
	 */
	private void queryProvinces() {
		// �����ݿ�������ʡ������ʡ�б���
		provinceList = coolWeatherDB.loadProvinces();
		// ������ݿ�����ʡ����Ϣ����ֱ��������ʾ��listview��
		if (provinceList.size() > 0) {
			// �������datalist�������
			dataList.clear();
			// ���ӷ������˻�ȡ��ʡ�б��е����ݸ��������б�
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			// ֪ͨ��Ҫ��ʾ�ļ��Ϸ����˱仯
			adapter.notifyDataSetChanged();

			listView.setSelection(0);
			titleText.setText("�й�");
			currentLevel = LEVEL_PROVINCE;
		} else {
			// �����ݿ���û�У���ӷ�������ȡ����
			queryFromServer(null, "province");
		}
	}

	/**
	 * ��ѯѡ��ʡ�����е��У����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ
	 */
	private void queryCities() {
		cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		// ������ݿ�����ĳ��ʡ���µ����г��У�������ݿ��ж�ȡ�����б�����ʾ��listview��
		if (cityList.size() > 0) {
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			// ����������ʾΪ��ѡ�е�ʡ��
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		} else {
			// ������ݿ���û�����ݣ���ӷ������˻�ȡ������ʾ��listview��
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}

	/**
	 * ��ѯѡ���������е��أ����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ
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
	 * ���ݴ���Ĵ��ź����ʹӷ������ϲ�ѯʡ��������
	 */
	private void queryFromServer(final String code, final String type) {
		String address;
		// ��������Ĵ��Ų�Ϊ��
		if (!TextUtils.isEmpty(code)) {
			// ���ַҪô�ǲ��У�Ҫô�ǲ���
			address = "http://www.weather.com.cn/data/list3/city" + code
					+ ".xml";
		} else {
			// ���ַ�ǲ�ʡ��
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		// ����������н�HttpCallbackListener�ӿ���ĳ��󷽷�������д��������������Ὺ�����̷߳������磬����ȡ����response
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinish(String response) {
				// һ��
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
					// ͨ��runOnUiThread���������ص����̴߳����߼�
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)) {
								//��ʱ�Ѿ������ݴ��뵽���ݿ��У����Կ���ֱ�Ӵ����ݿ��в�ѯ
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
				//ͨ��runOnUiThread���������ص����̴߳����߼�
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {

						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});

	}

	
	/**
	 * ��ʾ�������Ի���
	 */
	private void showProgressDialog() {

		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	/**
	 * �رս������Ի���
	 */
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	
	/**
	 * ����back�������ݵ�ǰ�ļ������жϣ���ʱӦ�÷������б�ʡ�б�����ֱ���˳�
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
