package com.example.mogu.screen;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mogu.R;
import com.example.mogu.custom.PlaceListAdapter;
import com.example.mogu.object.PlaceData;
import com.example.mogu.object.UserInfo;
import com.example.mogu.object.GroupInfo;
import com.example.mogu.share.LocationPreference;
import com.example.mogu.share.SharedPreferencesHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MapFragment extends Fragment {

    private LatLng currentLocation; // 사용자의 현재 위치
    private ProgressBar progressBar;
    private MapView mapView;
    private GoogleMap googleMap;

    private LinearLayout dateInfoLayout;
    private Button selectedDayButton = null;

    private Map<String, PlaceData> placesMap;
    private long startMillis;
    private long endMillis;

    private BottomSheetBehavior<FrameLayout> bottomSheetBehavior;
    private RecyclerView recyclerViewPlaces;
    private PlaceListAdapter placeListAdapter;

    private Button selectGroupButton;  // 그룹 선택 버튼

    // 목포의 기본 위치 설정
    private LatLng defaultLocation = new LatLng(34.8118, 126.3922); // 목포의 위도와 경도

    // 장소 마커 리스트
    private List<Marker> placeMarkers = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map, container, false);

        // View 초기화
        dateInfoLayout = view.findViewById(R.id.dateInfoLayout);
        recyclerViewPlaces = view.findViewById(R.id.recyclerViewPlaces);
        recyclerViewPlaces.setLayoutManager(new LinearLayoutManager(getContext()));
        selectGroupButton = view.findViewById(R.id.selectGroupButton);
        mapView = view.findViewById(R.id.map_view);
        progressBar = view.findViewById(R.id.progressBar);

        // 그룹 선택 버튼 리스너 설정
        selectGroupButton.setOnClickListener(v -> openGroupSelectionDialog());

        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(getContext());
        UserInfo userInfo = sharedPreferencesHelper.getUserInfo();

        // 첫 번째 그룹 자동 선택
        if (userInfo != null && userInfo.getGroupList() != null && !userInfo.getGroupList().isEmpty()) {
            GroupInfo firstGroup = userInfo.getGroupList().get(0);
            loadGroupSchedule(firstGroup.getGroupName());
        }

        Bundle extras = getArguments();
        if (extras != null) {
            startMillis = extras.getLong("startDate", -1);
            endMillis = extras.getLong("endDate", -1);
            placesMap = (Map<String, PlaceData>) extras.getSerializable("placesMap");
        }

        if (placesMap == null) {
            placesMap = new HashMap<>();
        }

        initializeBottomSheet(view);
        createDayButtons();

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                googleMap = map;

                // 현재 위치가 있으면 마커 추가, 없으면 기본 위치 설정
                LocationPreference locationPreference = new LocationPreference(getContext());
                double latitude = locationPreference.getLatitude();
                double longitude = locationPreference.getLongitude();

                if (latitude != 0.0 && longitude != 0.0) {
                    currentLocation = new LatLng(latitude, longitude);
                    googleMap.addMarker(new MarkerOptions().position(currentLocation).title("현재 위치"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                } else {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15));
                }

                progressBar.setVisibility(View.GONE);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void initializeBottomSheet(View view) {
        FrameLayout bottomSheet = view.findViewById(R.id.bottomSheetContainer);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @SuppressLint({"ResourceType", "UseCompatLoadingForColorStateLists"})
    private void createDayButtons() {
        Calendar startDate = Calendar.getInstance();
        startDate.setTimeInMillis(startMillis);

        Calendar endDate = Calendar.getInstance();
        endDate.setTimeInMillis(endMillis);

        long numberOfDays = getDateDifference(startDate, endDate) + 1;

        dateInfoLayout.removeAllViews();

        for (int i = 0; i < numberOfDays; i++) {
            final Button dayButton = new Button(getContext());
            dayButton.setText("DAY" + (i + 1));
            dayButton.setBackgroundResource(R.drawable.rounded_button_selected);
            dayButton.setTextColor(getResources().getColorStateList(R.drawable.button_text_selected));
            dayButton.setTextSize(12);
            dayButton.setAllCaps(true);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    160,
                    100
            );
            params.setMargins(24, 0, 24, 0);
            dayButton.setLayoutParams(params);

            dayButton.setOnClickListener(v -> selectDayButton(dayButton));

            dateInfoLayout.addView(dayButton);
        }

        if (dateInfoLayout.getChildCount() > 0) {
            Button firstDayButton = (Button) dateInfoLayout.getChildAt(0);
            selectDayButton(firstDayButton);
        }
    }

    @SuppressLint({"ResourceType", "UseCompatLoadingForColorStateLists"})
    private void selectDayButton(Button button) {
        if (selectedDayButton != null) {
            selectedDayButton.setSelected(false);
            selectedDayButton.setBackgroundResource(R.drawable.rounded_button_selected);
            selectedDayButton.setTextColor(getResources().getColorStateList(R.drawable.button_text_selected));
        }

        button.setSelected(true);
        button.setBackgroundResource(R.drawable.rounded_button_selected);
        button.setTextColor(getResources().getColorStateList(R.drawable.button_text_selected));
        selectedDayButton = button;

        String day = selectedDayButton.getText().toString();
        PlaceData placeData = placesMap.get(day);

        if (placeData != null) {
            // PlaceListAdapter 생성 시 currentLocation 전달 필요 없음
            placeListAdapter = new PlaceListAdapter(getContext(), placeData.getPlaceName(), placeData.getNotes(), day, placeData.getLocations());
            recyclerViewPlaces.setAdapter(placeListAdapter);

            if (googleMap != null) {
                // 장소 마커만 추가, 기존 장소 마커는 제거
                addMarkersToMap(placeData);
            }
        } else {
            // PlaceListAdapter 생성 시 빈 리스트 사용
            placeListAdapter = new PlaceListAdapter(getContext(), new ArrayList<>(), new ArrayList<>(), day, new ArrayList<>());
            recyclerViewPlaces.setAdapter(placeListAdapter);

            if (googleMap != null) {
                // 장소 마커만 제거
                clearPlaceMarkers();
            }
        }
    }


    private void addMarkersToMap(PlaceData placeData) {
        clearPlaceMarkers(); // 기존 장소 마커만 제거

        List<LatLng> locations = placeData.getLocations();
        List<String> placeNames = placeData.getPlaceName();

        for (int i = 0; i < locations.size(); i++) {
            LatLng location = locations.get(i);
            String placeName = placeNames.get(i);

            // 장소 마커 추가 및 리스트에 저장
            Marker marker = googleMap.addMarker(new MarkerOptions().position(location).title(placeName));
            placeMarkers.add(marker); // 장소 마커 리스트에 추가

            if (i == 0) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12));
            }
        }
    }

    private void clearPlaceMarkers() {
        // 기존의 장소 마커만 삭제
        for (Marker marker : placeMarkers) {
            marker.remove();
        }
        placeMarkers.clear(); // 리스트 비우기
    }

    private long getDateDifference(Calendar startDate, Calendar endDate) {
        long diffInMillis = endDate.getTimeInMillis() - startDate.getTimeInMillis();
        return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
    }

    private void openGroupSelectionDialog() {
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(getContext());
        UserInfo userInfo = sharedPreferencesHelper.getUserInfo();
        List<String> groupNames = new ArrayList<>();
        for (GroupInfo group : userInfo.getGroupList()) {
            groupNames.add(group.getGroupName());
        }

        String[] groupArray = groupNames.toArray(new String[0]);

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("그룹 선택")
                .setItems(groupArray, (dialog, which) -> loadGroupSchedule(groupArray[which]))
                .setNegativeButton("취소", null)
                .show();
    }

    private void loadGroupSchedule(String groupName) {
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(getContext());
        UserInfo userInfo = sharedPreferencesHelper.getUserInfo();

        for (GroupInfo group : userInfo.getGroupList()) {
            if (group.getGroupName().equals(groupName)) {
                placesMap = group.getPlacesMap();
                startMillis = group.getStartDateMillis();
                endMillis = group.getEndDateMillis();

                createDayButtons();
                selectDayButton((Button) dateInfoLayout.getChildAt(0));
                break;
            }
        }
    }
}
