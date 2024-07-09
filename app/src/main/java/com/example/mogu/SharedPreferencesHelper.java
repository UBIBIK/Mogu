package com.example.mogu;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {
    private static final String PREFERENCES_NAME = "mogu_prefs";
    private static final String KEY_EMAIL = "key_email";
    private static final String KEY_USERNAME = "key_username";
    private static final String KEY_PHONE = "key_phone";

    private SharedPreferences sharedPreferences;

    public SharedPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserInfo(UserInfo userInfo) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EMAIL, userInfo.getUserEmail());
        editor.putString(KEY_USERNAME, userInfo.getUserName());
        editor.putString(KEY_PHONE, userInfo.getPhoneNumber());
        editor.apply();
    }

    public UserInfo getUserInfo() {
        String email = sharedPreferences.getString(KEY_EMAIL, "");
        String username = sharedPreferences.getString(KEY_USERNAME, "");
        String phone = sharedPreferences.getString(KEY_PHONE, "");
        return new UserInfo(email, username, "", phone);
    }

    public void clearUserInfo() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_PHONE);
        editor.apply();
    }
}
