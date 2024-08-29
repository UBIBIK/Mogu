package com.example.mogu.share;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mogu.object.GroupInfo;
import com.example.mogu.object.UserInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class SharedPreferencesHelper {
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_PASSWORD = "user_password";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_GROUP_LIST = "group_list";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public SharedPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // 사용자 정보 저장
    public void saveUserInfo(UserInfo userInfo) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_EMAIL, userInfo.getUserEmail());
        editor.putString(KEY_USER_NAME, userInfo.getUserName());
        editor.putString(KEY_USER_PASSWORD, userInfo.getPassword());
        editor.putString(KEY_USER_PHONE, userInfo.getPhoneNumber());

        String groupListJson = gson.toJson(userInfo.getGroupList());
        editor.putString(KEY_GROUP_LIST, groupListJson);

        editor.apply();
    }

    // 사용자 정보 불러오기
    public UserInfo getUserInfo() {
        String email = sharedPreferences.getString(KEY_USER_EMAIL, "");
        String name = sharedPreferences.getString(KEY_USER_NAME, "");
        String password = sharedPreferences.getString(KEY_USER_PASSWORD, "");
        String phone = sharedPreferences.getString(KEY_USER_PHONE, "");

        String groupListJson = sharedPreferences.getString(KEY_GROUP_LIST, null);
        Type groupListType = new TypeToken<ArrayList<GroupInfo>>() {}.getType();
        ArrayList<GroupInfo> groupList = gson.fromJson(groupListJson, groupListType);

        UserInfo userInfo = new UserInfo(email, name, password, phone);
        userInfo.setGroupList(groupList != null ? groupList : new ArrayList<>());

        return userInfo;
    }

    // 사용자 정보 초기화
    public void clearUserInfo() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
