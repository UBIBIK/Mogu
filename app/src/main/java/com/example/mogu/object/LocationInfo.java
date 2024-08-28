package com.example.mogu.object;

public class LocationInfo {
    private String locationName; // 장소 이름
    private String address; // 도로명 주소
    private Double latitude; // 장소 위도 추후 수정 가능
    private Double longitude; // 장소 경도 추후 수정 가능
    private String note; // 메모

    // Default constructor
    public LocationInfo() {}

    // Parameterized constructor
    public LocationInfo(String locationName, String address, Double latitude, Double longitude, String note) {
        this.locationName = locationName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.note = note;
    }

    // Getters and Setters
    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
