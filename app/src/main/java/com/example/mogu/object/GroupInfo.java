package com.example.mogu.object;

import java.util.ArrayList;

public class GroupInfo {
    private String groupName;
    private String groupKey;
    private String gmEmail;
    private String gmName;
    private ArrayList<GroupMember> groupMember = new ArrayList<>();
    private boolean isExpanded; // 그룹 확장 상태 필드 추가

    public GroupInfo() {}

    public GroupInfo(String groupName, String groupKey, String gmEmail, String gmName, ArrayList<GroupMember> groupMember) {
        this.groupName = groupName;
        this.groupKey = groupKey;
        this.gmEmail = gmEmail;
        this.gmName = gmName;
        this.groupMember = groupMember;
        this.isExpanded = false; // 기본값은 확장되지 않은 상태
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    public String getGmEmail() {
        return gmEmail;
    }

    public void setGmEmail(String gmEmail) {
        this.gmEmail = gmEmail;
    }

    public String getGmName() {
        return gmName;
    }

    public void setGmName(String gmName) {
        this.gmName = gmName;
    }

    public ArrayList<GroupMember> getGroupMember() {
        return groupMember;
    }

    public void setGroupMember(ArrayList<GroupMember> groupMember) {
        this.groupMember = groupMember;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
}
