package com.example.mogu.object;

public class CreateTripScheduleRequest {
    private UserInfo userInfo;
    private TripScheduleInfo tripScheduleInfo;

    public CreateTripScheduleRequest() {}

    public CreateTripScheduleRequest(UserInfo userInfo, TripScheduleInfo tripScheduleInfo) {
        this.userInfo = userInfo;
        this.tripScheduleInfo = tripScheduleInfo;
    }

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