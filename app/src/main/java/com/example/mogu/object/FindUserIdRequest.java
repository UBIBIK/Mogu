package com.example.mogu.object;

public class FindUserIdRequest {
    private String username;
    private String phoneNumber;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // 기본 생성자
    public FindUserIdRequest() {}

    // 매개 변수를 받는 생성자
    public FindUserIdRequest(String username, String phoneNumber) {
        this.username = username;
        this.phoneNumber = phoneNumber;
    }
}
