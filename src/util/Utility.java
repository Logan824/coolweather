package util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import model.City;
import model.Country;
import model.Province;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Process;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import db.CoolWeatherDB;

public class Utility {

	/**
	 * �����ʹ������������ص�ʡ�����ݡ�01|������02|�Ϻ���03|���...
	 */
	public synchronized static boolean handleProvincesResponse(
			CoolWeatherDB coolWeatherDB, String response) {
		if (!TextUtils.isEmpty(response)) {
			// ��ʡ���ַ����и�õ��ַ�������
			String[] allProvinces = response.split(",");
			if (allProvinces != null && allProvinces.length > 0) {

				for (String p : allProvinces) {
					// ��"01|����"�������͵Ľ����з�
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					// �������õ����ݴ洢��province��
					coolWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;

	}

	/**
	 * �����ʹ������������ص��м�����
	 */
	public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB,
			String response, int provinceId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCities = response.split(",");
			if (allCities != null && allCities.length > 0) {
				for (String c : allCities) {
					String[] array = c.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					// ���������������ݴ洢��City��
					coolWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * �����ʹ������������ص��ؼ�����
	 */
	public static boolean handleCountriesResponse(CoolWeatherDB coolWeatherDB,
			String response, int cityId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCountries = response.split(",");
			if (allCountries != null && allCountries.length > 0) {
				for (String c : allCountries) {
					String[] array = c.split("\\|");
					Country country = new Country();
					country.setCountryCode(array[0]);
					country.setCountryName(array[1]);
					country.setCityId(cityId);
					// ���������������ݴ洢��Country��
					coolWeatherDB.saveCountry(country);
				}
				return true;
			}
		}

		return false;

	}

	/**
	 * �������������ص�json���ݣ����������������ݴ洢������,response�Ǵӷ������������������ݵ�json��ʽ ���ݸ�ʽΪ��
	 * {"weatherinfo"
	 * :{"city":"��ɽ","cityid":"101190404","temp1":"4��","temp2":"16��"
	 * ,"weather":"��ת����","img1":"n0.gif","img2":"d1.gif","ptime":"18:00"}}
	 */
	public static void handleWeatherResponse(Context context, String response) {

		try {
			// �������������ص�json���͵�����
			// {}:����һ��jsonobject,[]:����һ��json����
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
			String cityName = weatherInfo.getString("city");
			String weatherCode = weatherInfo.getString("cityid");
			String temp1 = weatherInfo.getString("temp1");
			String temp2 = weatherInfo.getString("temp2");
			String weatherDesp = weatherInfo.getString("weather");
			String publishTime = weatherInfo.getString("ptime");

			// �������������ݴ洢��sharedPreferences�ļ���
			saveWeatherInfo(context, cityName, weatherCode, temp1, temp2,
					weatherDesp, publishTime);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}
	
/**
 * �����������ص�����������Ϣ�洢��sharedpreferences�ļ���
 * @param context
 * @param cityName
 * @param weatherCode
 * @param temp1
 * @param temp2
 * @param weatherDesp
 * @param publishTime
 */
	public static void saveWeatherInfo(Context context, String cityName,
			String weatherCode, String temp1, String temp2, String weatherDesp,
			String publishTime) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy��M��d��", Locale.CHINA);
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putBoolean("city_selected", true);
		editor.putString("city_name", cityName);
		editor.putString("weather_code", weatherCode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desp", weatherDesp);
		editor.putString("publish_time", publishTime);
		editor.putString("current_date", sdf.format(new Date()));
		//sharedpreference.editor���һ��Ҫ�ύ
		editor.commit();
	}

}