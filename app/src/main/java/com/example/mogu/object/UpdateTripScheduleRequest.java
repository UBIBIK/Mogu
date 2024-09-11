package com.example.mogu.object;

public class UpdateTripScheduleRequest {
    private UserInfo userInfo;
    private TripScheduleInfo tripScheduleInfo;

    // 기본 생성자
    public UpdateTripScheduleRequest() {}

    // 매개변수를 받는 생성자
    public UpdateTripScheduleRequest(UserInfo userInfo, TripScheduleInfo tripScheduleInfo) {
        this.userInfo = userInfo;
        this.tripScheduleInfo = tripScheduleInfo;
    }

    // Getter 및 Setter 메서드
    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public TripScheduleInfo getTripScheduleInfo() {
        return tripScheduleInfo;
    }

    public void setTripScheduleInfo(TripScheduleInfo tripScheduleInfo) {
        this.tripScheduleInfo = tripScheduleInfo;
    }
}
