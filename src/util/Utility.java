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
	 * 解析和处理服务器返回的省级数据。01|北京，02|上海，03|天津，...
	 */
	public synchronized static boolean handleProvincesResponse(
			CoolWeatherDB coolWeatherDB, String response) {
		if (!TextUtils.isEmpty(response)) {
			// 将省级字符串切割，得到字符串数组
			String[] allProvinces = response.split(",");
			if (allProvinces != null && allProvinces.length > 0) {

				for (String p : allProvinces) {
					// 将"01|北京"这种类型的进行切分
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					// 将解析好的数据存储到province表
					coolWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;

	}

	/**
	 * 解析和处理服务器返回的市级数据
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
					// 将解析出来的数据存储到City表
					coolWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * 解析和处理服务器返回的县级数据
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
					// 将解析出来的数据存储到Country表
					coolWeatherDB.saveCountry(country);
				}
				return true;
			}
		}

		return false;

	}

	/**
	 * 解析服务器返回的json数据，并将解析出的数据存储到本地,response是从服务器传来的天气数据的json形式 数据格式为：
	 * {"weatherinfo"
	 * :{"city":"昆山","cityid":"101190404","temp1":"4℃","temp2":"16℃"
	 * ,"weather":"晴转多云","img1":"n0.gif","img2":"d1.gif","ptime":"18:00"}}
	 */
	public static void handleWeatherResponse(Context context, String response) {

		try {
			// 解析服务器返回的json类型的数据
			// {}:代表一个jsonobject,[]:代表一个json数组
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
			String cityName = weatherInfo.getString("city");
			String weatherCode = weatherInfo.getString("cityid");
			String temp1 = weatherInfo.getString("temp1");
			String temp2 = weatherInfo.getString("temp2");
			String weatherDesp = weatherInfo.getString("weather");
			String publishTime = weatherInfo.getString("ptime");

			// 将解析出的数据存储到sharedPreferences文件中
			saveWeatherInfo(context, cityName, weatherCode, temp1, temp2,
					weatherDesp, publishTime);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}
	
/**
 * 将服务器返回的所有天气信息存储到sharedpreferences文件中
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
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putBoolean("city_selected", true);
		editor.putString("city_name", cityName);
		editor.putString("weather_code", weatherCode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desp", weatherDesp);
		editor.putString("publish_time", publishTime);
		editor.putString("current_date", sdf.format(new Date()));
		//sharedpreference.editor最后一定要提交
		editor.commit();
	}

}
