package com.example.mogu.object;

public class DeleteGroupMemberRequest {
    private UserInfo userInfo;
    private String groupName;
    private String deleteMemberEmail;

    public DeleteGroupMemberRequest() {}

    public DeleteGroupMemberRequest(UserInfo userInfo, String groupName, String deleteMemberEmail) {
        this.userInfo = userInfo;
        this.groupName = groupName;
        this.deleteMemberEmail = deleteMemberEmail;
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

    public String getDeleteMemberEmail() {
        return deleteMemberEmail;
    }

    public void setDeleteMemberEmail(String deleteMemberEmail) {
        this.deleteMemberEmail = deleteMemberEmail;
    }
}
