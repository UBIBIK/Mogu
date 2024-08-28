package com.example.mogu.object;

import java.util.ArrayList;

public class TripScheduleDetails {
    private String date; // 날짜
    private String day; // x일차(1일차, 2일차...)
    private ArrayList<LocationInfo> locationInfo = new ArrayList<>(); // 장소 정보

    // 기본 생성자
    public TripScheduleDetails() {}

    // 매개변수를 받는 생성자
    public TripScheduleDetails(String date, String day) {
        this.date = date;
        this.day = day;
    }

    // 모든 필드를 초기화하는 생성자
    public TripScheduleDetails(String date, String day, ArrayList<LocationInfo> locationInfo) {
        this.date = date;
        this.day = day;
        this.locationInfo = locationInfo;
    }

    // Getter 및 Setter 메서드
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public ArrayList<LocationInfo> getLocationInfo() {
        return locationInfo;
    }

    public void setLocationInfo(ArrayList<LocationInfo> locationInfo) {
        this.locationInfo = locationInfo;
    }
}
