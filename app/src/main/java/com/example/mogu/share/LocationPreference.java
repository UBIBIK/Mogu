package com.example.mogu.share;

import android.content.Context;
import android.content.SharedPreferences;

public class LocationPreference {

    private static final String PREF_NAME = "location_pref";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public LocationPreference(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // 위치 저장
    public void saveLocation(double latitude, double longitude) {
        editor.putString(KEY_LATITUDE, String.valueOf(latitude));
        editor.putString(KEY_LONGITUDE, String.valueOf(longitude));
        editor.apply(); // 비동기적으로 저장
    }

    // 위도 불러오기
    public double getLatitude() {
        String latitude = sharedPreferences.getString(KEY_LATITUDE, "0.0");
        return Double.parseDouble(latitude);
    }

    // 경도 불러오기
    public double getLongitude() {
        String longitude = sharedPreferences.getString(KEY_LONGITUDE, "0.0");
        return Double.parseDouble(longitude);
    }

    // 위치 초기화
    public void clearLocation() {
        editor.remove(KEY_LATITUDE);
        editor.remove(KEY_LONGITUDE);
        editor.apply();
    }
}