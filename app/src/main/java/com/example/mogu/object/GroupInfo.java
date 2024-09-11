package com.example.mogu.object;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupInfo {
    private String groupName;
    private String groupKey;
    private String gmEmail;
    private String gmName;
    private ArrayList<GroupMember> groupMember = new ArrayList<>();
    private ArrayList<TripScheduleInfo> tripScheduleList;
    private boolean expanded;

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public GroupInfo() {}

    public GroupInfo(String groupName, String groupKey, String gmEmail, String gmName, ArrayList<GroupMember> groupMember) {
        this.groupName = groupName;
        this.groupKey = groupKey;
        this.gmEmail = gmEmail;
        this.gmName = gmName;
        this.groupMember = groupMember;
    }

    public ArrayList<TripScheduleInfo> getTripScheduleList() {
        return tripScheduleList;
    }

    public Map<String, PlaceData> getPlacesMap() {
        // TripScheduleInfo에서 장소 데이터를 수집하여 맵으로 반환
        Map<String, PlaceData> placesMap = new HashMap<>();
        if (tripScheduleList != null && !tripScheduleList.isEmpty()) {
            // 첫 번째 일정의 장소를 반환
            TripScheduleInfo tripScheduleInfo = tripScheduleList.get(0);
            for (TripScheduleDetails details : tripScheduleInfo.getTripScheduleDetails()) {
                PlaceData placeData = new PlaceData(); // Create a new PlaceData object
                for (LocationInfo locationInfo : details.getLocationInfo()) {
                    // Add each LocationInfo to PlaceData
                    LatLng latLng = new LatLng(locationInfo.getLatitude(), locationInfo.getLongitude());
                    placeData.addPlace(locationInfo.getLocationName(), latLng, locationInfo.getNote(), locationInfo.getImage()); //Todo :이미지 추가
                }
                placesMap.put(details.getDay(), placeData);
            }
        }
        return placesMap;
    }



    public long getStartDateMillis() {
        if (tripScheduleList != null && !tripScheduleList.isEmpty()) {
            return tripScheduleList.get(0).getStartDateMillis();
        }
        return 0;  // 기본값으로 0 반환
    }

    public long getEndDateMillis() {
        if (tripScheduleList != null && !tripScheduleList.isEmpty()) {
            return tripScheduleList.get(0).getEndDateMillis();
        }
        return 0;  // 기본값으로 0 반환
    }

    public void setTripScheduleList(ArrayList<TripScheduleInfo> tripScheduleList) {
        this.tripScheduleList = tripScheduleList;
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

}
