package com.example.mogu.object;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;

public class PlaceData implements Parcelable, Cloneable {
    private List<String> placeNames;
    private List<LatLng> locations;
    private List<String> notes;
    private List<String> images;

    // 기본 생성자
    public PlaceData() {
        this.placeNames = new ArrayList<>();
        this.locations = new ArrayList<>();
        this.notes = new ArrayList<>();
        this.images = new ArrayList<>();
    }

    // 복사 생성자 (다른 PlaceData 객체로부터 복사)
    public PlaceData(PlaceData other) {
        this.placeNames = new ArrayList<>(other.placeNames);
        this.locations = new ArrayList<>(other.locations);
        this.notes = new ArrayList<>(other.notes);
        this.images = new ArrayList<>(other.images);
    }

    // 전체 필드를 초기화하는 생성자
    public PlaceData(List<String> placeNames, List<LatLng> locations, List<String> notes, List<String> images) {
        this.placeNames = new ArrayList<>(placeNames);
        this.locations = new ArrayList<>(locations);
        this.notes = new ArrayList<>(notes);
        this.images = new ArrayList<>(images);
    }

    // Parcelable 생성자
    protected PlaceData(Parcel in) {
        placeNames = in.createStringArrayList();
        locations = in.createTypedArrayList(LatLng.CREATOR);
        notes = in.createStringArrayList();
        images = in.createStringArrayList();
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
        parcel.writeStringList(images);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // 장소 추가 메서드
    public void addPlace(String placeName, LatLng location, String note, String image) {
        this.placeNames.add(placeName);
        this.locations.add(location);
        this.notes.add(note);
        this.images.add(image);
    }

    // 장소 업데이트 메서드
    public void updatePlace(int index, String placeName, LatLng location, String note, String image) {
        if (index >= 0 && index < placeNames.size()) {
            this.placeNames.set(index, placeName);
            this.locations.set(index, location);
            this.notes.set(index, note);
            this.images.set(index, image);
        }
    }

    // 깊은 복사를 위한 clone 메서드
    @Override
    public PlaceData clone() {
        PlaceData cloned = new PlaceData();
        cloned.placeNames = new ArrayList<>(this.placeNames);
        cloned.locations = new ArrayList<>(this.locations);
        cloned.notes = new ArrayList<>(this.notes);
        cloned.images = new ArrayList<>(this.images);
        return cloned;
    }

    // Getter methods
    public List<String> getPlaceNames() {
        return placeNames;
    }

    public List<LatLng> getLocations() {
        return locations;
    }

    public List<String> getNotes() {
        return notes;
    }

    public List<String> getImages() {
        return images;
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

    public void setImages(List<String> images) {
        this.images = images;
    }

    public void setPlaceNameAt(int index, String placeName) {
        this.placeNames.set(index, placeName);
    }

    public void setImagesAt(int index, String image) {
        this.images.set(index, image);
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
            String image = images.get(i);

            LocationInfo locationInfo = new LocationInfo(
                    placeName,
                    "Address", // 실제 주소를 관리할 경우 이 부분을 수정합니다.
                    location.latitude,
                    location.longitude,
                    note,
                    image
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
        images.clear();

        for (LocationInfo locationInfo : locationInfoList) {
            placeNames.add(locationInfo.getLocationName());
            locations.add(new LatLng(locationInfo.getLatitude(), locationInfo.getLongitude()));
            notes.add(locationInfo.getNote());
            images.add(locationInfo.getImage());
        }
    }

    public List<Double> getLatitudeList() {
        List<Double> latitudeList = new ArrayList<>();
        for (LatLng location : locations) {
            latitudeList.add(location.latitude);
        }
        return latitudeList;
    }

    public List<Double> getLongitudeList() {
        List<Double> longitudeList = new ArrayList<>();
        for (LatLng location : locations) {
            longitudeList.add(location.longitude);
        }
        return longitudeList;
    }

}
