package com.example.mogu.object;

public class JoinGroupRequest {
    private UserInfo userInfo;
    private String groupKey;

    public JoinGroupRequest(UserInfo userInfo, String groupKey) {
        this.userInfo = userInfo;
        this.groupKey = groupKey;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }
}
