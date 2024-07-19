package com.example.mogu.object;

public class CreateGroupRequest {
    private UserInfo userInfo;
    private String groupName;

    public CreateGroupRequest() {}

    public CreateGroupRequest(UserInfo userInfo, String groupName) {
        this.userInfo = userInfo;
        this.groupName = groupName;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) { this.groupName = groupName; }
}
