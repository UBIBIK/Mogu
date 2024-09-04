package com.example.mogu.object;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class PlaceData implements Parcelable {
    private List<String> placeNames;
    private List<LatLng> locations;
    private List<String> notes;

    public PlaceData() {
        this.placeNames = new ArrayList<>();
        this.locations = new ArrayList<>();
        this.notes = new ArrayList<>();
    }

    protected PlaceData(Parcel in) {
        placeNames = in.createStringArrayList();
        locations = in.createTypedArrayList(LatLng.CREATOR);
        notes = in.createStringArrayList();
    }

    public static final Creator<PlaceData> CREATOR = new Creator<PlaceData>() {
        @Override
        public PlaceData createFromParcel(Parcel in) {
            return new PlaceData(in);
        }

        @Override
        public PlaceData[] newArray(int size) {
            return new PlaceData[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeStringList(placeNames);
        parcel.writeTypedList(locations);
        parcel.writeStringList(notes);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void addPlace(String placeName, LatLng location, String note) {
        this.placeNames.add(placeName);
        this.locations.add(location);
        this.notes.add(note);
    }

    public void updatePlace(int index, String placeName, LatLng location, String note) {
        if (index >= 0 && index < placeNames.size()) {
            this.placeNames.set(index, placeName);
            this.locations.set(index, location);
            this.notes.set(index, note);
        }
    }

    // Getter methods
    public List<String> getPlaceName() {
        return placeNames;
    }

    public List<LatLng> getLocations() {
        return locations;
    }

    public List<String> getNotes() {
        return notes;
    }

    // Setter methods
    public void setPlaceName(List<String> placeNames) {
        this.placeNames = placeNames;
    }

    public void setLocations(List<LatLng> locations) {
        this.locations = locations;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }

    public void setPlaceNameAt(int index, String placeName) {
        this.placeNames.set(index, placeName);
    }

    public void setLocationAt(int index, LatLng location) {
        this.locations.set(index, location);
    }

    public void setNoteAt(int index, String note) {
        this.notes.set(index, note);
    }

    // LocationInfo 리스트를 반환하는 메서드
    public List<LocationInfo> getLocationInfoList() {
        List<LocationInfo> locationInfoList = new ArrayList<>();

        for (int i = 0; i < placeNames.size(); i++) {
            String placeName = placeNames.get(i);
            LatLng location = locations.get(i);
            String note = notes.get(i);

            LocationInfo locationInfo = new LocationInfo(
                    placeName,
                    "Address", // 실제 주소를 관리할 경우 이 부분을 수정합니다.
                    location.latitude,
                    location.longitude,
                    note
            );
            locationInfoList.add(locationInfo);
        }

        return locationInfoList;
    }

    // LocationInfo 리스트를 받아 PlaceData를 설정하는 메서드
    public void setLocationInfoList(List<LocationInfo> locationInfoList) {
        placeNames.clear();
        locations.clear();
        notes.clear();

        for (LocationInfo locationInfo : locationInfoList) {
            placeNames.add(locationInfo.getLocationName());
            locations.add(new LatLng(locationInfo.getLatitude(), locationInfo.getLongitude()));
            notes.add(locationInfo.getNote());
        }
    }
}
