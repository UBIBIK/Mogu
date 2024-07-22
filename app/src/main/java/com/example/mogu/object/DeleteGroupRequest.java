package com.example.mogu.object;

public class DeleteGroupRequest {
    private UserInfo userInfo;
    private String groupName;

    public DeleteGroupRequest() {}

    public DeleteGroupRequest(UserInfo userInfo, String groupName) {
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

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
