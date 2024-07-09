package com.example.mogu.object;

public class UserInfo {
    private String userEmail;
    private String password;
    private String userName;
    private String phoneNumber;

    public UserInfo(String userEmail, String userName, String password, String phoneNumber) {
        this.userEmail = userEmail;
        this.userName = userName;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
