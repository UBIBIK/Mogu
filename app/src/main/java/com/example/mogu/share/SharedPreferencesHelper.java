package com.example.mogu.share;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mogu.object.UserInfo;

public class SharedPreferencesHelper {
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_PASSWORD = "user_password";
    private static final String KEY_USER_PHONE = "user_phone";

    private SharedPreferences sharedPreferences;

    public SharedPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserInfo(UserInfo userInfo) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_EMAIL, userInfo.getUserEmail());
        editor.putString(KEY_USER_NAME, userInfo.getUserName());
        editor.putString(KEY_USER_PASSWORD, userInfo.getPassword());
        editor.putString(KEY_USER_PHONE, userInfo.getPhoneNumber());
        editor.apply();
    }

    public UserInfo getUserInfo() {
        String email = sharedPreferences.getString(KEY_USER_EMAIL, "");
        String name = sharedPreferences.getString(KEY_USER_NAME, "");
        String password = sharedPreferences.getString(KEY_USER_PASSWORD, "");
        String phone = sharedPreferences.getString(KEY_USER_PHONE, "");
        return new UserInfo(email, name, password, phone);
    }

    public void clearUserInfo() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

}
