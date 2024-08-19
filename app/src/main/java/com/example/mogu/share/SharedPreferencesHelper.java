package com.example.mogu.share;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mogu.object.GroupInfo;
import com.example.mogu.object.UserInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SharedPreferencesHelper {
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_PASSWORD = "user_password";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_GROUP_LIST = "group_list";
    private static final String KEY_START_DATE = "start_date";
    private static final String KEY_END_DATE = "end_date";
    private static final String KEY_PERIOD = "period";
    private static final String KEY_NOTES_MAP = "notes_map";
    private static final String KEY_PLACES_MAP = "places_map"; // 장소 저장을 위한 키

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public SharedPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveUserInfo(UserInfo userInfo) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_EMAIL, userInfo.getUserEmail());
        editor.putString(KEY_USER_NAME, userInfo.getUserName());
        editor.putString(KEY_USER_PASSWORD, userInfo.getPassword());
        editor.putString(KEY_USER_PHONE, userInfo.getPhoneNumber());

        // Group list 저장
        String groupListJson = gson.toJson(userInfo.getGroupList());
        editor.putString(KEY_GROUP_LIST, groupListJson);

        editor.apply();
    }

    public UserInfo getUserInfo() {
        String email = sharedPreferences.getString(KEY_USER_EMAIL, "");
        String name = sharedPreferences.getString(KEY_USER_NAME, "");
        String password = sharedPreferences.getString(KEY_USER_PASSWORD, "");
        String phone = sharedPreferences.getString(KEY_USER_PHONE, "");

        // Group list 불러오기
        String groupListJson = sharedPreferences.getString(KEY_GROUP_LIST, null);
        Type groupListType = new TypeToken<ArrayList<GroupInfo>>() {}.getType();
        ArrayList<GroupInfo> groupList = gson.fromJson(groupListJson, groupListType);

        UserInfo userInfo = new UserInfo(email, name, password, phone);
        userInfo.setGroupList(groupList != null ? groupList : new ArrayList<>());

        return userInfo;
    }

    public void clearUserInfo() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    // 날짜와 기간 저장
    public void saveCalendarData(String startDate, String endDate, int period) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_START_DATE, startDate);
        editor.putString(KEY_END_DATE, endDate);
        editor.putInt(KEY_PERIOD, period);
        editor.apply();
    }

    // 날짜와 기간 불러오기
    public CalendarData getCalendarData() {
        String startDate = sharedPreferences.getString(KEY_START_DATE, null);
        String endDate = sharedPreferences.getString(KEY_END_DATE, null);
        int period = sharedPreferences.getInt(KEY_PERIOD, 0);

        return new CalendarData(startDate, endDate, period);
    }

    // 밀리초 단위 날짜 및 기간 저장
    public void saveDates(long startDateMillis, long endDateMillis) {
        // 기간 계산
        long diffInMillis = endDateMillis - startDateMillis;
        int period = (int) TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_START_DATE, startDateMillis);
        editor.putLong(KEY_END_DATE, endDateMillis);
        editor.putInt(KEY_PERIOD, period);
        editor.apply();
    }

    // 밀리초 단위 날짜 및 기간 불러오기
    public DatePeriodData getDates() {
        long startDateMillis = sharedPreferences.getLong(KEY_START_DATE, -1);
        long endDateMillis = sharedPreferences.getLong(KEY_END_DATE, -1);
        int period = sharedPreferences.getInt(KEY_PERIOD, 0);

        return new DatePeriodData(startDateMillis, endDateMillis, period);
    }

    // 메모 저장
    public void saveNotes(Map<String, String> notesMap) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // 기존 메모 제거
        Map<String, ?> allNotes = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allNotes.entrySet()) {
            if (entry.getKey().startsWith(KEY_NOTES_MAP)) {
                editor.remove(entry.getKey());
            }
        }

        // 새로운 메모 저장
        for (Map.Entry<String, String> entry : notesMap.entrySet()) {
            editor.putString(KEY_NOTES_MAP + entry.getKey(), entry.getValue());
        }

        editor.apply();
    }

    // 메모 저장
    public void saveNoteForDay(String day, String note) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Map<String, ?> allNotes = sharedPreferences.getAll();
        Map<String, String> notesMap = new HashMap<>();

        for (Map.Entry<String, ?> entry : allNotes.entrySet()) {
            if (entry.getKey().startsWith(KEY_NOTES_MAP)) {
                String key = entry.getKey().substring(KEY_NOTES_MAP.length());
                notesMap.put(key, (String) entry.getValue());
            }
        }

        notesMap.put(day, note);
        for (Map.Entry<String, String> entry : notesMap.entrySet()) {
            editor.putString(KEY_NOTES_MAP + entry.getKey(), entry.getValue());
        }

        editor.apply();
    }

    // 메모 불러오기
    public Map<String, String> getAllNotes() {
        Map<String, String> notesMap = new HashMap<>();
        Map<String, ?> allNotes = sharedPreferences.getAll();

        for (Map.Entry<String, ?> entry : allNotes.entrySet()) {
            if (entry.getKey().startsWith(KEY_NOTES_MAP)) {
                String key = entry.getKey().substring(KEY_NOTES_MAP.length());
                notesMap.put(key, (String) entry.getValue());
            }
        }

        return notesMap;
    }

    // 장소 저장
    public void savePlaces(Map<String, String> placesMap) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // 기존 장소 제거
        Map<String, ?> allPlaces = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allPlaces.entrySet()) {
            if (entry.getKey().startsWith(KEY_PLACES_MAP)) {
                editor.remove(entry.getKey());
            }
        }

        // 새로운 장소 저장
        for (Map.Entry<String, String> entry : placesMap.entrySet()) {
            String key = entry.getKey();
            String placeName = entry.getValue();
            editor.putString(KEY_PLACES_MAP + key, placeName);
        }

        editor.apply();
    }

    // 장소 저장
    public void savePlaceForDay(String day, String place) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Map<String, ?> allPlaces = sharedPreferences.getAll();
        Map<String, String> placesMap = new HashMap<>();

        for (Map.Entry<String, ?> entry : allPlaces.entrySet()) {
            if (entry.getKey().startsWith(KEY_PLACES_MAP)) {
                String key = entry.getKey().substring(KEY_PLACES_MAP.length());
                placesMap.put(key, (String) entry.getValue());
            }
        }

        placesMap.put(day, place);
        for (Map.Entry<String, String> entry : placesMap.entrySet()) {
            editor.putString(KEY_PLACES_MAP + entry.getKey(), entry.getValue());
        }

        editor.apply();
    }

    // 장소 불러오기
    public Map<String, String> getAllPlaces() {
        Map<String, String> placesMap = new HashMap<>();
        Map<String, ?> allPlaces = sharedPreferences.getAll();

        for (Map.Entry<String, ?> entry : allPlaces.entrySet()) {
            if (entry.getKey().startsWith(KEY_PLACES_MAP)) {
                String key = entry.getKey().substring(KEY_PLACES_MAP.length());
                String placeName = (String) entry.getValue();
                placesMap.put(key, placeName);
            }
        }

        return placesMap;
    }

    // 날짜와 기간을 포함한 데이터 클래스
    public static class DatePeriodData {
        private long startDateMillis;
        private long endDateMillis;
        private int period;

        public DatePeriodData(long startDateMillis, long endDateMillis, int period) {
            this.startDateMillis = startDateMillis;
            this.endDateMillis = endDateMillis;
            this.period = period;
        }

        public long getStartDateMillis() {
            return startDateMillis;
        }

        public long getEndDateMillis() {
            return endDateMillis;
        }

        public int getPeriod() {
            return period;
        }
    }

    public static class CalendarData {
        private String startDate;
        private String endDate;
        private int period;

        public CalendarData(String startDate, String endDate, int period) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.period = period;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public int getPeriod() {
            return period;
        }
    }
}
