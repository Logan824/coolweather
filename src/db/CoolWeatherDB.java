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
	 * ���ݿ���
	 */
	public static final String DB_NAME = "cool_weather";

	/**
	 * ���ݿ�汾
	 */
	public static final int VERSION = 1;

	private static CoolWeatherDB coolWeatherDB;

	private SQLiteDatabase db;

	/**
	 * �����췽��˽�л�
	 */

	private CoolWeatherDB(Context context) {
		CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context,
				DB_NAME, null, VERSION);
		// �Ӱ������ȡ���ݿ����
		db = dbHelper.getWritableDatabase();
	}

	/**
	 * ��ȡCoolWeatherDB��ʵ��
	 */
	public synchronized static CoolWeatherDB getInstance(Context context) {
		if (coolWeatherDB == null) {
			// ���ù��췽�����ͻ������ݿ����
			coolWeatherDB = new CoolWeatherDB(context);
		}
		return coolWeatherDB;
	}

	/**
	 * ��Provinceʵ���洢�����ݿ��Province����,�൱���ڱ��в���һ������
	 */
	public void saveProvince(Province province) {
		if (province != null) {
			ContentValues values = new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());
			// �����в���һ������
			db.insert("Province", null, values);
		}
	}

	/**
	 * �����ݿ��ȡȫ�����е�ʡ����Ϣ���൱�ڲ�ѯProvince���е���������
	 */

	public List<Province> loadProvinces() {
		// ����һ���б�����Ա�������������ʾ
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
				// ��������ǰѶ�����ӵ��б��У���Ϊ��Ҫ�����б����
				list.add(province);
			} while (cursor.moveToNext());
		}
		if (cursor != null) {
			cursor.close();
		}
		return list;
	}

	/**
	 * ��Cityʵ���������ݿ⡣����һ��City��������City��,�൱���ڱ��в���һ������
	 */

	public void saveCity(City city) {
		if (city != null) {
			ContentValues values = new ContentValues();
			// values.put("id", city.getId());��Ϊid���������ģ����Կ��Բ����������id
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());
			values.put("province_id", city.getProvinceId());
			db.insert("City", null, values);
		}

	}

	/**
	 * �����ݿ��ȡĳʡ�µ����г��е���Ϣ��Ҳ���ǲ�ѯCity����province_id=ĳ��ʡ��id�µ����г���
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
	 * ��Countryʵ���洢�����ݿ��У�����Countryʵ�����뵽Country����
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
	 * �����ݿ��ȡĳ���������е�����Ϣ��Ҳ���ǲ�ѯCountry���е�city_id = ĳ���е��µ�������
	 */

	public List<Country> loadCountries(int cityId) {
		List<Country> list = new ArrayList<Country>();
		// query�в���Ҫдwhere
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
