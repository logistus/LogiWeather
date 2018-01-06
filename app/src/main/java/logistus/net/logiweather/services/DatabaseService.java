package logistus.net.logiweather.services;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import logistus.net.logiweather.models.City;
import logistus.net.logiweather.models.Forecast;

public class DatabaseService extends SQLiteOpenHelper {
    // Database version
    private static final int DATABASE_VERSION = 44;
    // Database name
    private static final String DATABASE_NAME = "logi_weather.db";
    // Table name(s)
    private static final String TABLE_CITIES = "cities";
    private static final String TABLE_FORECASTS = "forecasts";

    // Table columns for cities
    private static final String CITIES_KEY_ID = "_id";
    private static final String CITIES_KEY_FULL_TEXT = "full_name";
    private static final String CITIES_KEY_TIMEZONE = "timezone";
    private static final String CITIES_KEY_QUERYURL = "query_url";
    private static final String CITIES_KEY_LAST_UPDATE = "last_update";

    // Table columns for forecasts
    private static final String FORECASTS_KEY_ID = "_id";
    private static final String FORECASTS_KEY_CITY_ID = "city_id";
    private static final String FORECASTS_KEY_DAY = "day";
    private static final String FORECASTS_KEY_LOW_TEMP_C = "low_temp_c";
    private static final String FORECASTS_KEY_LOW_TEMP_F = "low_temp_f";
    private static final String FORECASTS_KEY_HIGH_TEMP_C = "high_temp_c";
    private static final String FORECASTS_KEY_HIGH_TEMP_F = "high_temp_f";
    private static final String FORECASTS_KEY_CONDITION = "condition";
    private static final String FORECASTS_KEY_ICON_URL = "icon_url";

    public DatabaseService(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_CITIES_TABLE = "CREATE TABLE " + TABLE_CITIES + "(" +
                CITIES_KEY_ID + " INTEGER PRIMARY KEY, " +
                CITIES_KEY_FULL_TEXT + " TEXT, " +
                CITIES_KEY_QUERYURL + " TEXT, " +
                CITIES_KEY_LAST_UPDATE + " TEXT, " +
                CITIES_KEY_TIMEZONE + " TEXT " +
                ")";
        sqLiteDatabase.execSQL(CREATE_CITIES_TABLE);

        String CREATE_FORECASTS_TABLE = "CREATE TABLE " + TABLE_FORECASTS + "(" +
                FORECASTS_KEY_ID + " INTEGER PRIMARY KEY, " +
                FORECASTS_KEY_CITY_ID + " INTEGER, " +
                FORECASTS_KEY_DAY + " TEXT, " +
                FORECASTS_KEY_LOW_TEMP_C + " TEXT, " +
                FORECASTS_KEY_HIGH_TEMP_C + " TEXT, " +
                FORECASTS_KEY_LOW_TEMP_F + " TEXT, " +
                FORECASTS_KEY_HIGH_TEMP_F + " TEXT, " +
                FORECASTS_KEY_CONDITION + " TEXT, " +
                FORECASTS_KEY_ICON_URL + " TEXT " +
                ")";
        sqLiteDatabase.execSQL(CREATE_FORECASTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CITIES);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_FORECASTS);
        onCreate(sqLiteDatabase);
    }

    public long addCity(City city) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CITIES_KEY_FULL_TEXT, city.getCityText());
        values.put(CITIES_KEY_TIMEZONE, city.getTimezone());
        values.put(CITIES_KEY_QUERYURL, city.getQueryUrl());
        values.put(CITIES_KEY_LAST_UPDATE, city.getLastUpdate());
        return db.insert(TABLE_CITIES, null, values);
    }

    public void addForecast(Forecast forecast) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FORECASTS_KEY_CITY_ID, forecast.getCity_id());
        values.put(FORECASTS_KEY_DAY, forecast.getDay());
        values.put(FORECASTS_KEY_LOW_TEMP_C, forecast.getLow_temp_c());
        values.put(FORECASTS_KEY_HIGH_TEMP_C, forecast.getHigh_temp_c());
        values.put(FORECASTS_KEY_LOW_TEMP_F, forecast.getLow_temp_f());
        values.put(FORECASTS_KEY_HIGH_TEMP_F, forecast.getHigh_temp_f());
        values.put(FORECASTS_KEY_CONDITION, forecast.getCondition());
        values.put(FORECASTS_KEY_ICON_URL, forecast.getIcon_url());
        db.insert(TABLE_FORECASTS, null, values);
        db.close();
    }

    public void updateCity(int city_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CITIES_KEY_LAST_UPDATE, String.valueOf(System.currentTimeMillis() / 1000L));
        db.update(TABLE_CITIES, values, CITIES_KEY_ID + "=" + city_id,null);
        db.close();
    }

    public boolean checkCity(String cityText) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] columns = {CITIES_KEY_ID};
        String selection = CITIES_KEY_FULL_TEXT + " =?";
        String[] selectionArgs = { cityText };
        String limit = "1";
        Cursor cursor = db.query(TABLE_CITIES, columns, selection, selectionArgs, null, null, null, limit);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public void deleteCity(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_CITIES, CITIES_KEY_ID + " = ?", new String[] { String.valueOf(id)});
        db.close();
    }

    public void deleteForecasts(int city_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_FORECASTS, FORECASTS_KEY_CITY_ID + " = ?", new String[] {String.valueOf(city_id)});
        db.close();
    }

    public List<City> getAllCities() {
        List<City> cities = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CITIES + " ORDER BY " + CITIES_KEY_ID + " DESC";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            for (int i = 0; i < getCityCount(); i++) {
                City city = new City();
                city.setCityId(cursor.getInt(0));
                city.setCityText(cursor.getString(1));
                city.setQueryUrl(cursor.getString(2));
                city.setLastUpdate(cursor.getString(3));
                city.setTimezone(cursor.getString(4));
                cities.add(city);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return cities;
    }

    public List<Forecast> getAllForecasts(int city_id) {
        List<Forecast> forecasts = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_FORECASTS + " WHERE " + FORECASTS_KEY_CITY_ID + " = " + String.valueOf(city_id);
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                Forecast forecast = new Forecast();
                forecast.setDay(cursor.getString(2));
                forecast.setLow_temp_c(cursor.getString(3));
                forecast.setHigh_temp_c(cursor.getString(4));
                forecast.setLow_temp_f(cursor.getString(5));
                forecast.setHigh_temp_f(cursor.getString(6));
                forecast.setCondition(cursor.getString(7));
                forecast.setIcon_url(cursor.getString(8));
                forecasts.add(forecast);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return forecasts;
    }

    public int getCityCount() {
        int allRecords = 0;
        String countQuery = "SELECT * FROM " + TABLE_CITIES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        allRecords = cursor.getCount();
        cursor.close();
        db.close();
        return allRecords;
    }

    public void deleteAllCities() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_CITIES, null, null);
        db.close();
    }

    public void deleteAllForecasts() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_FORECASTS, null, null);
        db.close();
    }
}
