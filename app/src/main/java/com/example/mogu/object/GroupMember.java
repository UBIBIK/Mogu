package com.example.mogu.object;

public class GroupMember {
    private String memberEmail;
    private String memberName;
    private Double memberLongitude;
    private Double memberLatitude;

    public GroupMember() {}

    public GroupMember(String memberEmail, String memberName, Double memberLongitude, Double memberLatitude) {
        this.memberEmail = memberEmail;
        this.memberName = memberName;
        this.memberLongitude = memberLongitude;
        this.memberLatitude = memberLatitude;
    }

    public String getMemberEmail() {
        return memberEmail;
    }

    public void setMemberEmail(String memberEmail) {
        this.memberEmail = memberEmail;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public double getMemberLongitude() {
        return memberLongitude;
    }

    public void setMemberLongitude(double memberLongitude) {
        this.memberLongitude = memberLongitude;
    }

    public double getMemberLatitude() {
        return memberLatitude;
    }

    public void setMemberLatitude(double memberLatitude) {
        this.memberLatitude = memberLatitude;
    }
}
