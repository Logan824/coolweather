package db;

import java.util.ArrayList;
import java.util.List;

import model.City;
import model.Country;
import model.Province;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CoolWeatherDB {

	/**
	 * 数据库名
	 */
	public static final String DB_NAME = "cool_weather";

	/**
	 * 数据库版本
	 */
	public static final int VERSION = 1;

	private static CoolWeatherDB coolWeatherDB;

	private SQLiteDatabase db;

	/**
	 * 将构造方法私有化
	 */

	private CoolWeatherDB(Context context) {
		CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context,
				DB_NAME, null, VERSION);
		// 从帮助类获取数据库对象
		db = dbHelper.getWritableDatabase();
	}

	/**
	 * 获取CoolWeatherDB的实例
	 */
	public synchronized static CoolWeatherDB getInstance(Context context) {
		if (coolWeatherDB == null) {
			// 调用构造方法，就会获得数据库对象
			coolWeatherDB = new CoolWeatherDB(context);
		}
		return coolWeatherDB;
	}

	/**
	 * 将Province实例存储到数据库的Province表中,相当于在表中插入一条数据
	 */
	public void saveProvince(Province province) {
		if (province != null) {
			ContentValues values = new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());
			// 往表中插入一条数据
			db.insert("Province", null, values);
		}
	}

	/**
	 * 从数据库读取全国所有的省份信息，相当于查询Province表中的所有数据
	 */

	public List<Province> loadProvinces() {
		// 返回一个列表对象，以便适配器进行显示
		List<Province> list = new ArrayList<Province>();
		Cursor cursor = db
				.query("Province", null, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				Province province = new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));
				province.setProvinceName(cursor.getString(cursor
						.getColumnIndex("province_name")));
				province.setProvinceCode(cursor.getString(cursor
						.getColumnIndex("province_code")));
				// 最后不能忘记把对象添加到列表中，因为需要返回列表对象
				list.add(province);
			} while (cursor.moveToNext());
		}
		if (cursor != null) {
			cursor.close();
		}
		return list;
	}

	/**
	 * 将City实例存入数据库。即将一个City对象插入表City中,相当于在表中插入一条数据
	 */

	public void saveCity(City city) {
		if (city != null) {
			ContentValues values = new ContentValues();
			// values.put("id", city.getId());因为id是自增长的，所以可以不往里面插入id
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());
			values.put("province_id", city.getProvinceId());
			db.insert("City", null, values);
		}

	}

	/**
	 * 从数据库读取某省下的所有城市的信息，也就是查询City表中province_id=某个省的id下的所有城市
	 */

	public List<City> loadCities(int provinceId) {
		List<City> list = new ArrayList<City>();
		Cursor cursor = db.query("City", null, "province_id = ?",
				new String[] { String.valueOf(provinceId) }, null, null, null);

		while (cursor.moveToNext()) {
			City city = new City();
			city.setId(cursor.getInt(cursor.getColumnIndex("id")));
			city.setCityName(cursor.getString(cursor
					.getColumnIndex("city_name ")));
			city.setCityCode(cursor.getString(cursor
					.getColumnIndex("city_code")));
			city.setProvinceId(provinceId);
			list.add(city);
		}

		if (cursor != null) {
			cursor.close();
		}

		return list;
	}

	/**
	 * 将Country实例存储到数据库中，即将Country实例插入到Country表中
	 */

	public void saveCountry(Country country) {
		if (country != null) {
			ContentValues values = new ContentValues();
			values.put("country_name", country.getCountryName());
			values.put("country_code", country.getCountryCode());
			values.put("city_id", country.getCityId());
			db.insert("Country", null, values);
		}
	}

	/**
	 * 从数据库读取某城市下所有的县信息，也就是查询Country表中当city_id = 某个市的下的所有县
	 */

	public List<Country> loadCountries(int cityId) {
		List<Country> list = new ArrayList<Country>();
		// query中不需要写where
		Cursor cursor = db.query("Country", null, "city_id = ?",
				new String[] { String.valueOf(cityId) }, null, null, null);
		while (cursor.moveToNext()) {
			Country country = new Country();
			country.setId(cursor.getInt(cursor.getColumnIndex("id")));
			country.setCountryName(cursor.getString(cursor.getColumnIndex("country_name")));
			country.setCountryCode(cursor.getString(cursor.getColumnIndex("country_code")));
			country.setCityId(cityId);
			list.add(country);
		}
		if (cursor != null) {
			cursor.close();
		}
		return list;
	}

}
