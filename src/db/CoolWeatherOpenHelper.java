package db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class CoolWeatherOpenHelper extends SQLiteOpenHelper {
	
	/**
	 * Province(省)表建表语句
	 */
	
	public static final String CREATE_PROVINCE = "create table Province ("
			+ "id integer primary key autoincrement,"
			+ "province_name text,"
			+ "province_code text)";
	
	/**
	 * city(市)表建表语句
	 */
	
	public static final String CREATE_CITY = "create table City ("
			+ "id integer primary key autoincrement,"
			+ "city_name text,"
			+ "city_code text,"
			+ "province_id integer)";
	
	/**
	 * Country(县)表建表语句
	 */
	
	public static final String CREATE_COUNTRY = "create table Country ("
			+ "id integer primary key autoincrement,"
			+ "country_name text,"
			+ "country_code text,"
			+ "city_id integer)";
	
	
	
	//创建一个帮助类对象的语句，拿到这个对象才可以创建一个数据库，name代表数据库名
	public CoolWeatherOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	//在帮助类创建的时候，同时实现建表功能
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_PROVINCE); //创建Province表
		db.execSQL(CREATE_CITY);     //创建City表
		db.execSQL(CREATE_COUNTRY);  //创建Country表
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
