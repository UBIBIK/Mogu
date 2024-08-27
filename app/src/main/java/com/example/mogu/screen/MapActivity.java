package com.example.mogu.screen;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
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
import android.Manifest;

import androidx.annotation.NonNull;
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
import com.example.mogu.object.PlaceData;
import com.example.mogu.share.SharedPreferencesHelper;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    // 위치 권한 요청 코드
    private final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    // 위치 권한 배열
    private final String[] locationPermissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    // 위치 서비스 제공자
    private FusedLocationProviderClient fusedLocationClient;
    // 초기 위치 (목포)
    private LatLng startPosition = new LatLng(34.8118, 126.3922);
    // 진행 상태 표시바
    private ProgressBar progressBar;
    // 위치 업데이트 플래그
    private boolean requestingLocationUpdates = false;
    // 위치 요청 객체
    private LocationRequest locationRequest;
    // 위치 콜백 객체
    private LocationCallback locationCallback;

    // 구글 지도 관련 객체들
    private MapView mapView;
    private GoogleMap googleMap;
    // 하단 시트 동작 제어 객체
    private BottomSheetBehavior<FrameLayout> bottomSheetBehavior;
    // 날짜 버튼 레이아웃
    private LinearLayout dateInfoLayout;
    // 장소 추가 버튼
    private Button addPlaceButton;
    // 저장 버튼
    private Button saveButton;
    // 선택된 DAY 버튼
    private Button selectedDayButton = null;
    // 장소 표시용 RecyclerView와 어댑터
    private RecyclerView placeRecyclerView;
    private PlaceDataAdapter placeDataAdapter;
    // 저장된 장소 데이터를 저장하는 맵
    private Map<String, PlaceData> placesMap = new HashMap<>();
    // 기간 시작과 끝
    private long startMillis;
    private long endMillis;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 레이아웃 설정
        setContentView(R.layout.activity_map);

        // RecyclerView 설정
        placeRecyclerView = findViewById(R.id.recyclerViewPlaces);
        placeRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // SharedPreferencesHelper를 통해 저장된 날짜 데이터를 가져옴
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this);
        SharedPreferencesHelper.DatePeriodData datePeriodData = sharedPreferencesHelper.getDates();

        // 시작 및 종료 시간을 가져옴
        startMillis = datePeriodData.getStartDateMillis();
        endMillis = datePeriodData.getEndDateMillis();

        // 지도 및 위치 관련 객체들 초기화
        mapView = findViewById(R.id.map_view);
        progressBar = findViewById(R.id.progressBar);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L).build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                // 위치 업데이트 시 지도 이동
                for (Location location : locationResult.getLocations()) {
                    LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(newLocation));
                }
            }
        };

        // 위치 권한이 있으면 시작 위치 가져오기
        if (ContextCompat.checkSelfPermission(this, locationPermissions[0]) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, locationPermissions[1]) == PackageManager.PERMISSION_GRANTED) {
            getStartLocation();
        } else {
            // 위치 권한 요청
            ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_PERMISSION_REQUEST_CODE);
        }

        // 하단 시트 초기화
        initializeBottomSheet();

        // 날짜 버튼들 생성
        createDayButtons();

        // 구글맵 초기화
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.addMarker(new MarkerOptions().position(startPosition).title("목포"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPosition, 15));

        // 로딩이 완료되면 ProgressBar를 숨김
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        // 위치 업데이트 재개
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        // 위치 업데이트 중지
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
        // 현재 위치 가져오기
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
        // 위치 업데이트 시작
        requestingLocationUpdates = true;
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 위치 권한 요청 결과 처리
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getStartLocation();
            } else {
                showPermissionDeniedDialog();
            }
        }
    }

    private void showPermissionDeniedDialog() {
        // 위치 권한이 거부된 경우 알림 대화 상자 표시
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("위치 권한 거부시 앱을 사용할 수 없습니다.")
                .setPositiveButton("권한 설정하러 가기", (dialogInterface, i) -> {
                    try {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
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
        // 하단 시트 초기화
        CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinatorLayout);
        FrameLayout bottomSheet = findViewById(R.id.bottomSheetContainer);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // 날짜 버튼 레이아웃 및 버튼들 초기화
        dateInfoLayout = findViewById(R.id.dateInfoLayout);
        addPlaceButton = findViewById(R.id.addPlaceButton);
        saveButton = findViewById(R.id.saveButton);  // 저장 버튼 찾기

        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this);
        placesMap = sharedPreferencesHelper.getAllPlaces();

        // 장소 추가 버튼 클릭 리스너 설정
        addPlaceButton.setOnClickListener(view -> {
            if (selectedDayButton != null) {
                openPlaceFragment();
            }
        });

        // 저장 버튼 클릭 리스너 설정
        saveButton.setOnClickListener(view -> {
            Intent intent = new Intent(MapActivity.this, HomeMap.class);
            startActivity(intent);
        });
    }

    private void createDayButtons() {
        // 날짜 버튼들 생성
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

            // 날짜 버튼 클릭 리스너 설정
            dayButton.setOnClickListener(view -> selectDayButton(dayButton));
            dateInfoLayout.addView(dayButton);
        }

        // 첫 번째 DAY 버튼을 기본 선택
        if (dateInfoLayout.getChildCount() > 0) {
            Button firstDayButton = (Button) dateInfoLayout.getChildAt(0);
            selectDayButton(firstDayButton);
        }
    }

    private void selectDayButton(Button button) {
        // 선택된 버튼의 스타일 변경
        if (selectedDayButton != null) {
            selectedDayButton.setSelected(false);
            selectedDayButton.setBackgroundResource(R.drawable.rounded_button_selected);
            selectedDayButton.setTextColor(getResources().getColorStateList(R.drawable.button_text_selected));
        }

        button.setSelected(true);
        button.setBackgroundResource(R.drawable.rounded_button_selected);
        button.setTextColor(getResources().getColorStateList(R.drawable.button_text_selected));
        selectedDayButton = button;

        // 선택된 DAY에 해당하는 장소 데이터를 가져옴
        String day = selectedDayButton.getText().toString();
        PlaceData placeData = placesMap.get(day);

        // RecyclerView에 어댑터 설정
        if (placeData != null) {
            placeDataAdapter = new PlaceDataAdapter(placeData, placeData.getPlaceName(), placeData.getNotes(), this, day);
        } else {
            placeDataAdapter = new PlaceDataAdapter(new PlaceData(), new ArrayList<>(), new ArrayList<>(), this, day);
        }

        placeRecyclerView.setAdapter(placeDataAdapter);

        // 장소 편집 리스너 설정
        placeDataAdapter.setOnEditPlaceListener((position, placeDataToEdit) -> {
            openPlaceFragmentForEditing(placeDataToEdit, position);
        });
    }

    private void openPlaceFragmentForEditing(PlaceData placeData, int position) {
        // 장소 편집을 위한 Fragment 열기
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
        // 장소 추가를 위한 Fragment 열기
        Fragment placeFragment = new PlaceFragment();
        Bundle bundle = new Bundle();

        if (selectedDayButton != null) {
            String day = selectedDayButton.getText().toString();
            bundle.putString("selected_day", day);
        }

        ArrayList<String> placesList = new ArrayList<>();
        bundle.putStringArrayList("places_list", placesList);
        placeFragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.mapContainer, placeFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private long getDateDifference(Calendar startDate, Calendar endDate) {
        // 시작일과 종료일의 차이를 계산하여 반환
        long diffInMillis = endDate.getTimeInMillis() - startDate.getTimeInMillis();
        return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
    }
}
