package com.example.mogu.screen;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
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
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mogu.R;
import com.example.mogu.custom.PlaceListAdapter;
import com.example.mogu.object.CreateTripScheduleRequest;
import com.example.mogu.object.GroupInfo;
import com.example.mogu.object.PlaceData;
import com.example.mogu.object.UserInfo;
import com.example.mogu.retrofit.ApiService;
import com.example.mogu.retrofit.RetrofitClient;
import com.example.mogu.share.SharedPreferencesHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment {

    private final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private final String[] locationPermissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private FusedLocationProviderClient fusedLocationClient;
    private LatLng startPosition = null;
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

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private Button selectGroupButton;  // 그룹 선택 버튼

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

        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L).build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(newLocation));
                }
            }
        };

        if (ContextCompat.checkSelfPermission(getContext(), locationPermissions[0]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(), locationPermissions[1]) == PackageManager.PERMISSION_GRANTED) {
            getStartLocation();
        } else {
            requestPermissions(locationPermissions, LOCATION_PERMISSION_REQUEST_CODE);
        }

        initializeBottomSheet(view);
        createDayButtons();

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                googleMap = map;
                if (startPosition != null) {
                    googleMap.addMarker(new MarkerOptions().position(startPosition).title("Start Position"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPosition, 15));
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
        fusedLocationClient.removeLocationUpdates(locationCallback);
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

    @SuppressLint("MissingPermission")
    private void getStartLocation() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        startPosition = new LatLng(location.getLatitude(), location.getLongitude());

                        if (googleMap != null) {
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPosition, 15));
                        } else {
                            Log.e("MapFragment", "GoogleMap is not ready yet.");
                        }
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getStartLocation();
            } else {
                showPermissionDeniedDialog();
            }
        }
    }

    private void showPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("위치 권한 거부 시 앱을 사용할 수 없습니다.")
                .setPositiveButton("권한 설정하러 가기", (dialogInterface, i) -> {
                    try {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + getContext().getPackageName()));
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                        startActivity(intent);
                    } finally {
                        requireActivity().finish();
                    }
                })
                .setNegativeButton("앱 종료하기", (dialogInterface, i) -> requireActivity().finish())
                .setCancelable(false)
                .show();
    }

    private void initializeBottomSheet(View view) {
        CoordinatorLayout coordinatorLayout = view.findViewById(R.id.coordinatorLayout);
        FrameLayout bottomSheet = view.findViewById(R.id.bottomSheetContainer);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

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

    @SuppressLint("MissingPermission")
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
            placeListAdapter = new PlaceListAdapter(placeData.getPlaceName(), placeData.getNotes(), day);
            recyclerViewPlaces.setAdapter(placeListAdapter);

            if (googleMap != null) {
                googleMap.clear(); // 모든 기존 마커를 초기화합니다.
                addMarkersToMap(placeData); // 장소 마커를 추가합니다.
            }
        } else {
            placeListAdapter = new PlaceListAdapter(new ArrayList<>(), new ArrayList<>(), day);
            recyclerViewPlaces.setAdapter(placeListAdapter);

            if (googleMap != null) {
                googleMap.clear(); // 지도에서 모든 마커를 초기화합니다.
            }
        }

        // 내 위치를 비동기적으로 가져오고, 가져온 후에 다른 장소 마커와 함께 표시
        if (fusedLocationClient != null) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null && googleMap != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                            // 내 위치 마커를 지도에 추가합니다.
                            googleMap.addMarker(new MarkerOptions()
                                    .position(currentLocation)
                                    .title("내 위치")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                            // 카메라를 내 위치로 이동합니다.
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));

                            // 장소 마커도 추가합니다.
                            if (placeData != null) {
                                addMarkersToMap(placeData); // 장소 마커를 함께 추가
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MapFragment", "Failed to get current location", e);
                    });
        } else {
            Log.e("MapFragment", "FusedLocationProviderClient is not initialized.");
        }
    }




    private void addMarkersToMap(PlaceData placeData) {
        List<LatLng> locations = placeData.getLocations();
        List<String> placeNames = placeData.getPlaceName();

        for (int i = 0; i < locations.size(); i++) {
            LatLng location = locations.get(i);
            String placeName = placeNames.get(i);

            googleMap.addMarker(new MarkerOptions().position(location).title(placeName));

            if (i == 0) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12));
            }
        }
    }

    private long getDateDifference(Calendar startDate, Calendar endDate) {
        long diffInMillis = endDate.getTimeInMillis() - startDate.getTimeInMillis();
        return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
    }

    private void openGroupSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("그룹 선택");

        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(getContext());
        UserInfo userInfo = sharedPreferencesHelper.getUserInfo();
        List<String> groupNames = new ArrayList<>();
        for (GroupInfo group : userInfo.getGroupList()) {
            groupNames.add(group.getGroupName());
        }

        String[] groupArray = groupNames.toArray(new String[0]);

        builder.setItems(groupArray, (dialog, which) -> {
            String selectedGroupName = groupArray[which];
            loadGroupSchedule(selectedGroupName);
        });

        builder.setNegativeButton("취소", null);
        builder.show();
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

    private void fetchGroupScheduleFromServer(CreateTripScheduleRequest request) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<UserInfo> call = apiService.createTripSchedule(request);
        call.enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserInfo fetchedUserInfo = response.body();
                    if (!fetchedUserInfo.getGroupList().isEmpty()) {
                        GroupInfo fetchedGroup = fetchedUserInfo.getGroupList().get(0);
                        placesMap = fetchedGroup.getPlacesMap();
                        startMillis = fetchedGroup.getStartDateMillis();
                        endMillis = fetchedGroup.getEndDateMillis();

                        createDayButtons();
                        selectDayButton((Button) dateInfoLayout.getChildAt(0));
                    }
                } else {
                    Log.e("MapFragment", "Failed to fetch group schedule: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                Log.e("MapFragment", "Error fetching group schedule", t);
            }
        });
    }
}
