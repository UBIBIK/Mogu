package com.example.mogu.share;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mogu.object.GroupInfo;
import com.example.mogu.object.PlaceData;
import com.example.mogu.object.UserInfo;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
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
    public void savePlaces(Map<String, PlaceData> placesMap) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // 기존 장소 제거
        Map<String, ?> allPlaces = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allPlaces.entrySet()) {
            if (entry.getKey().startsWith(KEY_PLACES_MAP)) {
                editor.remove(entry.getKey());
            }
        }

        // 새로운 장소 저장
        for (Map.Entry<String, PlaceData> entry : placesMap.entrySet()) {
            String key = entry.getKey();
            // PlaceData 객체를 JSON 문자열로 변환
            String placeDataJson = gson.toJson(entry.getValue());
            editor.putString(KEY_PLACES_MAP + key, placeDataJson);
        }

        editor.apply();
    }

    public Map<String, List<PlaceData>> getAllPlacesGroupedByDay() {
        Map<String, List<PlaceData>> groupedPlacesMap = new HashMap<>();
        Map<String, ?> allPlaces = sharedPreferences.getAll();

        for (Map.Entry<String, ?> entry : allPlaces.entrySet()) {
            if (entry.getKey().startsWith(KEY_PLACES_MAP)) {
                String key = entry.getKey().substring(KEY_PLACES_MAP.length());
                String placeDataJson = (String) entry.getValue();

                try {
                    // JSON을 PlaceData 객체로 변환
                    Type placeDataType = new TypeToken<PlaceData>() {}.getType();
                    PlaceData placeData = gson.fromJson(placeDataJson, placeDataType);

                    // 해당 DAY에 이미 데이터가 있으면 리스트에 추가하고, 없으면 새 리스트 생성
                    if (!groupedPlacesMap.containsKey(key)) {
                        groupedPlacesMap.put(key, new ArrayList<>());
                    }
                    groupedPlacesMap.get(key).add(placeData);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    // 잘못된 형식의 데이터는 무시하거나 처리 로직 추가
                }
            }
        }

        return groupedPlacesMap;
    }

    public void savePlacesGroupedByDay(Map<String, List<PlaceData>> placesMap) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // 기존 장소 제거
        Map<String, ?> allPlaces = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allPlaces.entrySet()) {
            if (entry.getKey().startsWith(KEY_PLACES_MAP)) {
                editor.remove(entry.getKey());
            }
        }

        // 새로운 장소 저장
        for (Map.Entry<String, List<PlaceData>> entry : placesMap.entrySet()) {
            String key = entry.getKey();
            // List<PlaceData> 객체를 JSON 문자열로 변환
            String placeDataJson = gson.toJson(entry.getValue());
            editor.putString(KEY_PLACES_MAP + key, placeDataJson);
        }

        editor.apply();
    }



    // 장소 불러오기
    public Map<String, PlaceData> getAllPlaces() {
        Map<String, PlaceData> placesMap = new HashMap<>();
        Map<String, ?> allPlaces = sharedPreferences.getAll();

        for (Map.Entry<String, ?> entry : allPlaces.entrySet()) {
            if (entry.getKey().startsWith(KEY_PLACES_MAP)) {
                String key = entry.getKey().substring(KEY_PLACES_MAP.length());
                String placeDataJson = (String) entry.getValue();

                try {
                    // JSON 배열을 처리할 수 있도록 Type을 지정
                    Type placeDataType = new TypeToken<PlaceData>() {}.getType();
                    PlaceData placeData = gson.fromJson(placeDataJson, placeDataType);
                    placesMap.put(key, placeData);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    // 잘못된 형식의 데이터는 무시하거나 처리 로직 추가
                }
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
    }
}
