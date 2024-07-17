package com.example.mogu.object;

import java.util.ArrayList;

public class GroupInfo {
    private String GroupName;
    private String GroupKey;
    private String GM_Email;
    private String GM_Name;
    private ArrayList<GroupMember> GroupMember;

    public String getGroupName() {
        return GroupName;
    }

    public void setGroupName(String groupName) {
        GroupName = groupName;
    }

    public String getGroupKey() {
        return GroupKey;
    }

    public void setGroupKey(String groupKey) {
        GroupKey = groupKey;
    }

    public String getGM_Email() {
        return GM_Email;
    }

    public void setGM_Email(String GM_Email) {
        this.GM_Email = GM_Email;
    }

    public String getGM_Name() {
        return GM_Name;
    }

    public void setGM_Name(String GM_Name) {
        this.GM_Name = GM_Name;
    }

    public ArrayList<com.example.mogu.object.GroupMember> getGroupMember() {
        return GroupMember;
    }

    public void setGroupMember(ArrayList<com.example.mogu.object.GroupMember> groupMember) {
        GroupMember = groupMember;
    }
}
