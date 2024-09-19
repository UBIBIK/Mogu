package com.example.mogu.screen;

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
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mogu.R;
import com.example.mogu.custom.PlaceDataAdapter;
import com.example.mogu.object.CreateTripScheduleRequest;
import com.example.mogu.object.GroupInfo;
import com.example.mogu.object.PlaceData;
import com.example.mogu.object.TripScheduleDetails;
import com.example.mogu.object.TripScheduleInfo;
import com.example.mogu.object.UpdateTripScheduleRequest;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";

    private static final int PLACE_SELECTION_REQUEST_CODE = 1002;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private final String[] locationPermissions = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng startPosition = new LatLng(34.8118, 126.3922);

    private ProgressBar progressBar;
    private boolean requestingLocationUpdates = false;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private String groupKey;
    private MapView mapView;
    private GoogleMap googleMap;
    private BottomSheetBehavior<FrameLayout> bottomSheetBehavior;
    private LinearLayout dateInfoLayout;
    private Button addPlaceButton;
    private Button saveButton;
    private Button cancelButton;
    private Button selectedDayButton = null;
    private RecyclerView placeRecyclerView;
    private PlaceDataAdapter placeDataAdapter;

    private Map<String, PlaceData> originalPlacesMap = new HashMap<>(); // 취소를 위한 원본 데이터 저장
    private Map<String, PlaceData> placesMap = new HashMap<>();
    private long startMillis;
    private long endMillis;
    private SharedPreferencesHelper sharedPreferencesHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        sharedPreferencesHelper = new SharedPreferencesHelper(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            startMillis = extras.getLong("startDate", -1);
            endMillis = extras.getLong("endDate", -1);
            placesMap = (Map<String, PlaceData>) extras.getSerializable("placesMap");
        }

        if (placesMap == null) {
            placesMap = new HashMap<>();
        }

        placeRecyclerView = findViewById(R.id.recyclerViewPlaces);
        placeRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mapView = findViewById(R.id.map_view);
        progressBar = findViewById(R.id.progressBar);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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

        dateInfoLayout = findViewById(R.id.dateInfoLayout);

        if (ContextCompat.checkSelfPermission(this, locationPermissions[0]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, locationPermissions[1]) == PackageManager.PERMISSION_GRANTED) {
            getStartLocation();
        } else {
            ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_PERMISSION_REQUEST_CODE);
        }

        loadExistingPlaceData(groupKey);
        initializeBottomSheet();
        createDayButtons();
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    private void loadExistingPlaceData(String groupKey) {
        UserInfo userInfo = sharedPreferencesHelper.getUserInfo();

        if (userInfo != null) {
            // 그룹 키를 기반으로 해당 그룹의 일정만 불러옴
            for (GroupInfo group : userInfo.getGroupList()) {
                if (group.getGroupKey().equals(groupKey) && group.getTripScheduleList() != null && !group.getTripScheduleList().isEmpty()) {
                    TripScheduleInfo tripSchedule = group.getTripScheduleList().get(0);
                    for (TripScheduleDetails details : tripSchedule.getTripScheduleDetails()) {
                        PlaceData placeData = new PlaceData();
                        placeData.setLocationInfoList(details.getLocationInfo());

                        placesMap.put(details.getDay(), placeData);
                    }
                }
            }
        }

        Log.d(TAG, "loadExistingPlaceData - placesMap: " + placesMap.toString());

        if (dateInfoLayout.getChildCount() > 0) {
            Button firstDayButton = (Button) dateInfoLayout.getChildAt(0);
            selectDayButton(firstDayButton);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPosition, 15));

        // 지도가 준비된 후에만 getStartLocation 호출
        getStartLocation();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                        // GoogleMap 객체가 초기화 되었는지 확인
                        if (googleMap != null) {
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        } else {
                            Log.e("MapActivity", "GoogleMap is not initialized yet.");
                        }
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        requestingLocationUpdates = true;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("위치 권한 거부시 앱을 사용할 수 없습니다.")
                .setPositiveButton("권한 설정하러 가기", (dialogInterface, i) -> {
                    try {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                        startActivity(intent);
                    } finally {
                        finish();
                    }
                })
                .setNegativeButton("앱 종료하기", (dialogInterface, i) -> finish())
                .setCancelable(false)
                .show();
    }

    private void initializeBottomSheet() {

        // 원본 데이터를 깊은 복사하여 저장
        Log.d(TAG, "원본 데이터 복사 전: " + placesMap.toString()); // 복사 전 로그
        copyPlacesMap(placesMap, originalPlacesMap);
        Log.d(TAG, "원본 데이터 복사 후: " + originalPlacesMap.toString()); // 복사 후 로그

        CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinatorLayout);
        FrameLayout bottomSheet = findViewById(R.id.bottomSheetContainer);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        dateInfoLayout = findViewById(R.id.dateInfoLayout);
        addPlaceButton = findViewById(R.id.addPlaceButton);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);



        cancelButton.setOnClickListener(v -> {
            // 1. placesMap 복원
            Log.d(TAG, "취소 버튼 클릭 - 원본 데이터 복원 전 placesMap: " + placesMap.toString()); // 복원 전 로그
            copyPlacesMap(originalPlacesMap, placesMap);
            Log.d(TAG, "취소 버튼 클릭 - 원본 데이터 복원 후 placesMap: " + placesMap.toString()); // 복원 후 로그

            // 2. UserInfo 복원
            updateUserInfoFromPlacesMap(placesMap);

            // 3. 선택된 날짜의 데이터를 복원 및 RecyclerView 업데이트
            String selectedDay = selectedDayButton != null ? selectedDayButton.getText().toString() : null;
            if (selectedDay != null && originalPlacesMap.containsKey(selectedDay)) {
                PlaceData originalPlaceData = originalPlacesMap.get(selectedDay);

                placeDataAdapter.updateData(
                        originalPlaceData,
                        originalPlaceData.getPlaceNames(),
                        originalPlaceData.getNotes()
                );
                placeDataAdapter.notifyDataSetChanged();

                Log.d(TAG, "취소 버튼 클릭 - 선택된 날짜: " + selectedDay + ", 복원된 PlaceData: " + originalPlaceData.toString()); // 복원된 데이터 로그
            } else {
                Toast.makeText(this, "원본 데이터를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "취소 버튼 클릭 - 선택된 날짜의 원본 데이터를 찾을 수 없음: " + selectedDay); // 에러 로그
            }

            // 4. 변경 사항이 취소되었음을 알리고 액티비티를 종료
            Toast.makeText(this, "변경 사항이 취소되었습니다.", Toast.LENGTH_SHORT).show();
            finish();
        });





        addPlaceButton.setOnClickListener(view -> {
            if (selectedDayButton != null) {
                openPlaceFragment();
            }
        });

        saveButton.setOnClickListener(view -> {
            saveTripSchedule();
            moveToHomeMap();
        });
    }

    private void copyPlacesMap(Map<String, PlaceData> source, Map<String, PlaceData> destination) {
        destination.clear(); // 기존 데이터를 지움
        for (Map.Entry<String, PlaceData> entry : source.entrySet()) {
            PlaceData clonedData = entry.getValue() != null ? entry.getValue().clone() : null;
            destination.put(entry.getKey(), clonedData);
        }
        Log.d(TAG, "copyPlacesMap - 복사된 데이터: " + destination.toString());

        // 복사된 placesMap을 기반으로 UserInfo를 업데이트하는 로직 추가
        updateUserInfoFromPlacesMap(destination);
    }

    private void updateUserInfoFromPlacesMap(Map<String, PlaceData> placesMap) {
        UserInfo userInfo = sharedPreferencesHelper.getUserInfo();
        if (userInfo != null) {
            for (GroupInfo group : userInfo.getGroupList()) {
                if (group.getTripScheduleList() != null && !group.getTripScheduleList().isEmpty()) {
                    TripScheduleInfo tripSchedule = group.getTripScheduleList().get(0);
                    for (TripScheduleDetails details : tripSchedule.getTripScheduleDetails()) {
                        String day = details.getDay();
                        PlaceData placeData = placesMap.get(day);
                        if (placeData != null) {
                            details.setLocationInfo(new ArrayList<>(placeData.getLocationInfoList()));
                        }
                    }
                }
            }
            sharedPreferencesHelper.saveUserInfo(userInfo); // 수정된 UserInfo를 저장
            Log.d(TAG, "UserInfo updated from placesMap.");
        }
    }




    private void createDayButtons() {
        Calendar startDate = Calendar.getInstance();
        startDate.setTimeInMillis(startMillis);
        Calendar endDate = Calendar.getInstance();
        endDate.setTimeInMillis(endMillis);

        long numberOfDays = getDateDifference(startDate, endDate) + 1;

        dateInfoLayout.removeAllViews();

        for (int i = 0; i < numberOfDays; i++) {
            final Button dayButton = new Button(this);
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

            dayButton.setOnClickListener(view -> selectDayButton(dayButton));
            dateInfoLayout.addView(dayButton);
        }

        if (dateInfoLayout.getChildCount() > 0) {
            Button firstDayButton = (Button) dateInfoLayout.getChildAt(0);
            selectDayButton(firstDayButton);
        }
    }

    // MapActivity에서 콜백 설정
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

        String groupKey = getIntent().getStringExtra("group_key");
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this);
        UserInfo userInfo = sharedPreferencesHelper.getUserInfo();

        if (placeData != null) {
            placeDataAdapter = new PlaceDataAdapter(
                    placeData,
                    placeData.getPlaceNames(),           // 장소 이름 리스트
                    placeData.getNotes(),                // 노트 리스트
                    placeData.getImages(),            // 이미지 리스트
                    placeData.getLatitudeList(),         // 위도 리스트
                    placeData.getLongitudeList(),        // 경도 리스트
                    this,                                // Context
                    day,                                 // 선택된 날짜
                    userInfo                             // 사용자 정보
            );
        } else {
            placeDataAdapter = new PlaceDataAdapter(
                    new PlaceData(),
                    new ArrayList<>(),                   // 장소 이름 리스트
                    new ArrayList<>(),                   // 노트 리스트
                    new ArrayList<>(),                   // 이미지 리스트
                    new ArrayList<>(),                   // 위도 리스트
                    new ArrayList<>(),                   // 경도 리스트
                    this,                                // Context
                    day,                                 // 선택된 날짜
                    userInfo                             // 사용자 정보
            );
        }


        placeRecyclerView.setAdapter(placeDataAdapter);

        // 장소 수정 콜백
        placeDataAdapter.setOnEditPlaceListener((position, placeDataToEdit) -> {
            openPlaceFragmentForEditing(placeDataToEdit, position);
        });

        // 장소 삭제 콜백
        placeDataAdapter.setOnPlaceDeletedListener((position, updatedPlaceData) -> {
            placesMap.put(day, updatedPlaceData);  // placesMap 갱신
            updateUserInfoForDay(day, updatedPlaceData);  // UserInfo 갱신
            placeDataAdapter.updateData(updatedPlaceData, updatedPlaceData.getPlaceNames(), updatedPlaceData.getNotes());  // RecyclerView 갱신
            placeDataAdapter.notifyDataSetChanged();
        });
    }



    private void openPlaceFragmentForEditing(PlaceData placeData, int position) {
        Fragment placeFragment = new PlaceFragment();
        Bundle bundle = new Bundle();

        if (selectedDayButton != null) {
            String day = selectedDayButton.getText().toString();
            bundle.putString("selected_day", day);
            bundle.putParcelable("edit_place_data", placeData);
            bundle.putInt("edit_position", position);
        }

        placeFragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.mapContainer, placeFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void openPlaceFragment() {
        Fragment placeFragment = new PlaceFragment();
        Bundle bundle = new Bundle();

        if (selectedDayButton != null) {
            String day = selectedDayButton.getText().toString();
            bundle.putString("selected_day", day);
            bundle.putSerializable("placesMap", new HashMap<>(placesMap));
        }

        placeFragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.mapContainer, placeFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private long getDateDifference(Calendar startDate, Calendar endDate) {
        long diffInMillis = endDate.getTimeInMillis() - startDate.getTimeInMillis();
        return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLACE_SELECTION_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            double placeLat = data.getDoubleExtra("place_lat", 0);
            double placeLng = data.getDoubleExtra("place_lng", 0);
            String imageUrl = data.getStringExtra("place_image");

            if (placeLat != 0 && placeLng != 0) {
                LatLng placeLatLng = new LatLng(placeLat, placeLng);

                if (googleMap != null) {
                    googleMap.addMarker(new MarkerOptions().position(placeLatLng).title("Selected Place"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng, 15));
                }

                String day = selectedDayButton.getText().toString();
                PlaceData placeData = placesMap.get(day);

                if (placeData == null) {
                    placeData = new PlaceData();
                }

                int editPosition = data.getIntExtra("edit_position", -1);
                if (editPosition >= 0) {
                    placeData.updatePlace(editPosition, "Selected Place", placeLatLng, "", imageUrl);
                } else {
                    placeData.addPlace("Selected Place", placeLatLng, "", imageUrl);
                }

                placesMap.put(day, placeData);
                placeDataAdapter.updateData(placeData, placeData.getPlaceNames(), placeData.getNotes());
                updateUserInfoForDay(day, placeData);
            }
        }
    }

    public void addPlaceToMap(String placeName, LatLng placeLatLng, int editPosition, String image) {
        if (googleMap != null) {
            googleMap.addMarker(new MarkerOptions().position(placeLatLng).title(placeName));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng, 15));

            String day = selectedDayButton.getText().toString();
            PlaceData placeData = placesMap.get(day);

            if (placeData == null) {
                placeData = new PlaceData();
            }

            if (editPosition >= 0) {
                placeData.updatePlace(editPosition, placeName, placeLatLng, "", image);
            } else {
                placeData.addPlace(placeName, placeLatLng, "", image);
            }

            placesMap.put(day, placeData);
            updateUserInfoForDay(day, placeData);
            placeDataAdapter.updateData(placeData, placeData.getPlaceNames(), placeData.getNotes());
        }
    }

    private void updateUserInfoForDay(String day, PlaceData placeData) {
        UserInfo userInfo = sharedPreferencesHelper.getUserInfo();
        if (userInfo != null) {
            for (GroupInfo group : userInfo.getGroupList()) {
                if (group.getGroupKey().equals(groupKey)) {  // 그룹 키로 일정 확인
                    if (group.getTripScheduleList() != null && !group.getTripScheduleList().isEmpty()) {
                        TripScheduleInfo tripSchedule = group.getTripScheduleList().get(0);
                        for (TripScheduleDetails details : tripSchedule.getTripScheduleDetails()) {
                            if (details.getDay().equals(day)) {
                                details.setLocationInfo(new ArrayList<>(placeData.getLocationInfoList()));
                                return;
                            }
                        }
                        // 기존 일정이 없는 경우 새로운 일정 추가
                        TripScheduleDetails newDetails = new TripScheduleDetails();
                        newDetails.setDay(day);
                        newDetails.setLocationInfo(new ArrayList<>(placeData.getLocationInfoList()));
                        tripSchedule.getTripScheduleDetails().add(newDetails);
                    }
                }
            }
            sharedPreferencesHelper.saveUserInfo(userInfo);
        }
    }

    // 여행 일정 저장 또는 수정
    private void saveTripSchedule() {
        String groupKey = getIntent().getStringExtra("group_key");

        List<TripScheduleDetails> tripScheduleDetailsList = createTripScheduleDetails();

        LocalDate startDate = Instant.ofEpochMilli(startMillis).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = Instant.ofEpochMilli(endMillis).atZone(ZoneId.systemDefault()).toLocalDate();
        TripScheduleInfo tripScheduleInfo = new TripScheduleInfo(groupKey, startDate, endDate);
        tripScheduleInfo.setTripScheduleDetails(new ArrayList<>(tripScheduleDetailsList));

        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this);
        UserInfo userInfo = sharedPreferencesHelper.getUserInfo();

        // 일정 수정인지 여부 확인
        if (tripScheduleExists(userInfo, groupKey)) {
            UpdateTripScheduleRequest updateRequest = new UpdateTripScheduleRequest(userInfo, tripScheduleInfo);
            sendTripScheduleUpdateRequestToServer(updateRequest);
        } else {
            CreateTripScheduleRequest createRequest = new CreateTripScheduleRequest(userInfo, tripScheduleInfo);
            sendTripScheduleCreateRequestToServer(createRequest);
        }
    }

    private boolean tripScheduleExists(UserInfo userInfo, String groupKey) {
        if (userInfo != null) {
            for (GroupInfo group : userInfo.getGroupList()) {
                if (group.getGroupKey().equals(groupKey) && group.getTripScheduleList() != null && !group.getTripScheduleList().isEmpty()) {
                    return true; // 기존 일정이 존재
                }
            }
        }
        return false; // 기존 일정이 없음
    }

    private void sendTripScheduleUpdateRequestToServer(UpdateTripScheduleRequest request) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<UserInfo> call = apiService.updateTripSchedule(request);
        call.enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Trip schedule updated successfully.");

                    UserInfo updatedUserInfo = response.body();
                    if (updatedUserInfo != null) {
                        sharedPreferencesHelper.saveUserInfo(updatedUserInfo);
                    }
                } else {
                    Log.e(TAG, "Failed to update trip schedule: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                Log.e(TAG, "Error updating trip schedule", t);
            }
        });
    }

    private void sendTripScheduleCreateRequestToServer(CreateTripScheduleRequest request) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<UserInfo> call = apiService.createTripSchedule(request);
        call.enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Trip schedule created successfully.");

                    UserInfo updatedUserInfo = response.body();
                    if (updatedUserInfo != null) {
                        sharedPreferencesHelper.saveUserInfo(updatedUserInfo);
                    }
                } else {
                    Log.e(TAG, "Failed to create trip schedule: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                Log.e(TAG, "Error creating trip schedule", t);
            }
        });
    }

    private List<TripScheduleDetails> createTripScheduleDetails() {
        List<TripScheduleDetails> tripScheduleDetailsList = new ArrayList<>();
        Calendar startDate = Calendar.getInstance();
        startDate.setTimeInMillis(startMillis);
        Calendar endDate = Calendar.getInstance();
        endDate.setTimeInMillis(endMillis);

        long numberOfDays = getDateDifference(startDate, endDate) + 1;

        for (int i = 0; i < numberOfDays; i++) {
            TripScheduleDetails details = new TripScheduleDetails();
            String dayKey = "DAY" + (i + 1);
            PlaceData placeData = placesMap.get(dayKey);

            if (placeData != null) {
                details.setLocationInfo(new ArrayList<>(placeData.getLocationInfoList()));
            }
            details.setDay(dayKey);
            tripScheduleDetailsList.add(details);
        }

        return tripScheduleDetailsList;
    }

    private void moveToHomeMap() {
        finish();
    }
}
