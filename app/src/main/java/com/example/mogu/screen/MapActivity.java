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
import com.example.mogu.object.LocationInfo;
import com.example.mogu.object.PlaceData;
import com.example.mogu.object.TripScheduleDetails;
import com.example.mogu.object.TripScheduleInfo;
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

    // 상수 정의
    private static final int PLACE_SELECTION_REQUEST_CODE = 1002; // 장소 선택 요청 코드
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001; // 위치 권한 요청 코드
    private static final String TAG = "MapActivity"; // 로그 태그

    // 위치 권한 배열
    private final String[] locationPermissions = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};

    // 위치 서비스 클라이언트
    private FusedLocationProviderClient fusedLocationClient;

    // 지도 초기 시작 위치 (목포의 위도와 경도)
    private LatLng startPosition = new LatLng(34.8118, 126.3922);

    // UI 요소
    private ProgressBar progressBar;
    private boolean requestingLocationUpdates = false;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private MapView mapView;
    private GoogleMap googleMap;
    private BottomSheetBehavior<FrameLayout> bottomSheetBehavior;
    private LinearLayout dateInfoLayout;
    private Button addPlaceButton;
    private Button saveButton;
    private Button selectedDayButton = null;
    private RecyclerView placeRecyclerView;
    private PlaceDataAdapter placeDataAdapter;

    // 장소 데이터 맵
    private Map<String, PlaceData> placesMap = new HashMap<>();
    private long startMillis;
    private long endMillis;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Intent에서 시작일과 종료일을 가져옴
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            startMillis = extras.getLong("startDate", -1);
            endMillis = extras.getLong("endDate", -1);
            long duration = extras.getLong("duration", -1);

            // 로그에 시작일, 종료일 및 기간을 출력
            String formattedStartDate = extras.getString("formattedStartDate");
            String formattedEndDate = extras.getString("formattedEndDate");

            Log.d(TAG, "Received Start Date: " + formattedStartDate);
            Log.d(TAG, "Received End Date: " + formattedEndDate);
            Log.d(TAG, "Duration: " + duration + " days");

            placesMap = (Map<String, PlaceData>) extras.getSerializable("placesMap");
        }

        // placesMap이 null인 경우 빈 해시맵으로 초기화
        if (placesMap == null) {
            placesMap = new HashMap<>();
        }

        // RecyclerView 초기화 및 레이아웃 설정
        placeRecyclerView = findViewById(R.id.recyclerViewPlaces);
        placeRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 지도 뷰 초기화 및 위치 서비스 클라이언트 설정
        mapView = findViewById(R.id.map_view);
        progressBar = findViewById(R.id.progressBar);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 위치 요청 설정 (고정밀도, 2초 간격)
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L).build();

        // 위치 콜백 설정
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(newLocation));
                }
            }
        };

        // 위치 권한 확인 및 요청
        if (ContextCompat.checkSelfPermission(this, locationPermissions[0]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, locationPermissions[1]) == PackageManager.PERMISSION_GRANTED) {
            getStartLocation(); // 권한이 허용된 경우 시작 위치 가져오기
        } else {
            ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_PERMISSION_REQUEST_CODE); // 권한 요청
        }

        // BottomSheet 초기화 및 날짜 버튼 생성
        initializeBottomSheet();
        createDayButtons();

        // 지도 뷰 초기화
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.addMarker(new MarkerOptions().position(startPosition).title("목포"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPosition, 15));
        progressBar.setVisibility(View.GONE); // 로딩 스피너 숨기기
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume(); // 지도 뷰 복구
        if (requestingLocationUpdates) {
            startLocationUpdates(); // 위치 업데이트 재개
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause(); // 지도 뷰 일시 중지
        fusedLocationClient.removeLocationUpdates(locationCallback); // 위치 업데이트 중지
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy(); // 지도 뷰 소멸
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory(); // 메모리 부족 시 처리
    }

    @SuppressLint("MissingPermission")
    private void getStartLocation() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
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
                getStartLocation(); // 권한이 허용된 경우 시작 위치 가져오기
            } else {
                showPermissionDeniedDialog(); // 권한이 거부된 경우 다이얼로그 표시
            }
        }
    }

    // 위치 권한 거부 시 경고 다이얼로그 표시
    private void showPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("위치 권한 거부시 앱을 사용할 수 없습니다.")
                .setPositiveButton("권한 설정하러 가기", (dialogInterface, i) -> {
                    try {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                        startActivity(intent);
                    } finally {
                        finish(); // 설정 화면으로 이동 후 앱 종료
                    }
                })
                .setNegativeButton("앱 종료하기", (dialogInterface, i) -> finish()) // 앱 종료 선택 시 처리
                .setCancelable(false)
                .show();
    }

    // BottomSheet 초기화 및 설정
    private void initializeBottomSheet() {
        CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinatorLayout);
        FrameLayout bottomSheet = findViewById(R.id.bottomSheetContainer);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED); // 기본으로 BottomSheet를 닫힌 상태로 설정

        dateInfoLayout = findViewById(R.id.dateInfoLayout);
        addPlaceButton = findViewById(R.id.addPlaceButton);
        saveButton = findViewById(R.id.saveButton);

        // 장소 추가 버튼 클릭 리스너 설정
        addPlaceButton.setOnClickListener(view -> {
            if (selectedDayButton != null) {
                openPlaceFragment(); // 장소 선택 프래그먼트 열기
            }
        });

        // 일정 저장 버튼 클릭 리스너 설정
        saveButton.setOnClickListener(view -> {
            saveTripSchedule(); // 일정 저장 메서드 호출
        });
    }

    // DAY 버튼 생성 메서드
    private void createDayButtons() {
        Calendar startDate = Calendar.getInstance();
        startDate.setTimeInMillis(startMillis);
        Calendar endDate = Calendar.getInstance();
        endDate.setTimeInMillis(endMillis);

        // 날짜 차이 계산 (총 날짜 수)
        long numberOfDays = getDateDifference(startDate, endDate) + 1;

        dateInfoLayout.removeAllViews(); // 이전에 추가된 모든 뷰 제거

        // 각 DAY에 대해 버튼을 생성
        for (int i = 0; i < numberOfDays; i++) {
            final Button dayButton = new Button(this);
            dayButton.setText("DAY" + (i + 1)); // DAY1, DAY2, DAY3... 등의 텍스트 설정
            dayButton.setBackgroundResource(R.drawable.rounded_button_selected);
            dayButton.setTextColor(getResources().getColorStateList(R.drawable.button_text_selected));
            dayButton.setTextSize(12);
            dayButton.setAllCaps(true);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    160,
                    100
            );
            params.setMargins(24, 0, 24, 0); // 여백 설정
            dayButton.setLayoutParams(params);

            // DAY 버튼 클릭 리스너 설정
            dayButton.setOnClickListener(view -> selectDayButton(dayButton));
            dateInfoLayout.addView(dayButton); // 버튼을 레이아웃에 추가

            // 각 버튼이 생성될 때마다 로그 출력
            Log.d(TAG, "DAY Button Created: " + dayButton.getText());
        }

        // 기본으로 첫 번째 DAY를 선택
        if (dateInfoLayout.getChildCount() > 0) {
            Button firstDayButton = (Button) dateInfoLayout.getChildAt(0);
            selectDayButton(firstDayButton); // 첫 번째 DAY를 선택하도록 설정
        }
    }

    // 선택된 DAY 버튼을 처리하는 메서드
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

        // 로그 추가
        Log.d(TAG, "Selected Day: " + day);
        Log.d(TAG, "PlaceData for Selected Day: " + (placeData != null ? placeData.toString() : "No data"));

        if (placeData != null) {
            placeDataAdapter = new PlaceDataAdapter(placeData, placeData.getPlaceName(), placeData.getNotes(), this, day);
        } else {
            placeDataAdapter = new PlaceDataAdapter(new PlaceData(), new ArrayList<>(), new ArrayList<>(), this, day);
        }

        placeRecyclerView.setAdapter(placeDataAdapter);

        // 장소 수정 리스너 설정
        placeDataAdapter.setOnEditPlaceListener((position, placeDataToEdit) -> {
            openPlaceFragmentForEditing(placeDataToEdit, position);
        });
    }

    // 장소 수정 프래그먼트를 여는 메서드
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

    // 장소 선택 프래그먼트를 여는 메서드
    private void openPlaceFragment() {
        Fragment placeFragment = new PlaceFragment();
        Bundle bundle = new Bundle();

        if (selectedDayButton != null) {
            String day = selectedDayButton.getText().toString();
            bundle.putString("selected_day", day);
            bundle.putSerializable("placesMap", new HashMap<>(placesMap)); // 현재의 장소 맵을 번들에 추가
        }

        placeFragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.mapContainer, placeFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    // 날짜 차이를 계산하는 메서드
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

            if (placeLat != 0 && placeLng != 0) {
                LatLng placeLatLng = new LatLng(placeLat, placeLng);

                if (googleMap != null) {
                    googleMap.addMarker(new MarkerOptions().position(placeLatLng).title("Selected Place"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng, 15));
                }

                // PlaceData에 추가 또는 수정
                String day = selectedDayButton.getText().toString();
                PlaceData placeData = placesMap.get(day);

                if (placeData == null) {
                    placeData = new PlaceData();
                }

                int editPosition = data.getIntExtra("edit_position", -1);
                if (editPosition >= 0) {
                    // 기존 장소를 수정
                    placeData.updatePlace(editPosition, "Selected Place", placeLatLng, "");
                } else {
                    // 새로운 장소 추가
                    placeData.addPlace("Selected Place", placeLatLng, "");
                }
                placesMap.put(day, placeData);

                // RecyclerView를 갱신
                placeDataAdapter.updateData(placeData, placeData.getPlaceName(), placeData.getNotes());
            }
        }
    }

    // 지도에 장소를 추가하는 메서드
    public void addPlaceToMap(String placeName, LatLng placeLatLng, int editPosition) {
        if (googleMap != null) {
            googleMap.addMarker(new MarkerOptions().position(placeLatLng).title(placeName));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng, 15));

            String day = selectedDayButton.getText().toString();
            PlaceData placeData = placesMap.get(day);

            if (placeData == null) {
                placeData = new PlaceData();
            }

            if (editPosition >= 0) {
                placeData.updatePlace(editPosition, placeName, placeLatLng, "");
            } else {
                placeData.addPlace(placeName, placeLatLng, "");
            }
            placesMap.put(day, placeData);

            // 데이터 갱신
            placeDataAdapter.updateData(placeData, placeData.getPlaceName(), placeData.getNotes());
        }
    }

    // 여행 일정을 저장하는 메서드
    private void saveTripSchedule() {
        // TripScheduleDetails 생성
        List<TripScheduleDetails> tripScheduleDetailsList = createTripScheduleDetails();

        // TripScheduleInfo 생성
        LocalDate startDate = Instant.ofEpochMilli(startMillis).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = Instant.ofEpochMilli(endMillis).atZone(ZoneId.systemDefault()).toLocalDate();
        TripScheduleInfo tripScheduleInfo = new TripScheduleInfo("groupKey", startDate, endDate);

        // List를 ArrayList로 변환하여 설정
        tripScheduleInfo.setTripScheduleDetails(new ArrayList<>(tripScheduleDetailsList));

        // CreateTripScheduleRequest 생성
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this);
        UserInfo userInfo = sharedPreferencesHelper.getUserInfo();
        CreateTripScheduleRequest request = new CreateTripScheduleRequest(userInfo, tripScheduleInfo);

        // 로그로 요청 데이터 확인
        Log.d(TAG, "UserInfo: " + userInfo.getUserEmail());
        Log.d(TAG, "StartDate: " + startDate.toString());
        Log.d(TAG, "EndDate: " + endDate.toString());
        for (TripScheduleDetails details : tripScheduleDetailsList) {
            Log.d(TAG, "Day: " + details.getDay());
            for (LocationInfo location : details.getLocationInfo()) {
                Log.d(TAG, "Location: " + location.getLocationName() + " at " + location.getLatitude() + ", " + location.getLongitude());
            }
        }

        // 서버로 요청 보내기
        sendTripScheduleRequestToServer(request);
    }

    // 여행 일정 세부 정보를 생성하는 메서드
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

            // 로그 추가
            Log.d(TAG, "Creating TripScheduleDetails for " + dayKey + ": " + (placeData != null ? placeData.toString() : "No data"));

            if (placeData != null) {
                details.setLocationInfo(new ArrayList<>(placeData.getLocationInfoList()));
            }
            details.setDay(dayKey); // Day 정보를 설정
            tripScheduleDetailsList.add(details);
        }

        return tripScheduleDetailsList;
    }

    // 서버로 여행 일정 요청을 보내는 메서드
    private void sendTripScheduleRequestToServer(CreateTripScheduleRequest request) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<UserInfo> call = apiService.createTripSchedule(request);
        call.enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                if (response.isSuccessful()) {
                    // 서버 요청 성공 시 처리
                    Log.d(TAG, "Trip schedule saved successfully.");
                } else {
                    // 서버 요청 실패 시 처리
                    Log.e(TAG, "Failed to save trip schedule: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                // 네트워크 오류 처리
                Log.e(TAG, "Error saving trip schedule", t);
            }
        });
    }
}
