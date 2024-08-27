package com.example.mogu.screen;

import android.Manifest;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mogu.R;
import com.example.mogu.custom.PlaceListAdapter;
import com.example.mogu.object.PlaceData;
import com.example.mogu.share.SharedPreferencesHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.MapView;
import com.kakao.vectormap.label.Label;
import com.kakao.vectormap.label.LabelLayer;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyle;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HomeMap extends AppCompatActivity {

    // 위치 권한 요청 코드를 정의
    private final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    // 위치 권한 배열 정의 (정밀 위치, 대략적 위치)
    private final String[] locationPermissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    // 위치 서비스 클라이언트 객체
    private FusedLocationProviderClient fusedLocationClient;
    // 시작 위치를 저장할 변수
    private LatLng startPosition = null;
    // 진행 상태를 표시하는 ProgressBar
    private ProgressBar progressBar;
    // 지도의 중앙을 표시하는 Label
    private Label centerLabel;
    // 위치 업데이트 요청 여부를 저장하는 플래그
    private boolean requestingLocationUpdates = false;
    // 위치 요청 설정을 정의하는 객체
    private LocationRequest locationRequest;
    // 위치 업데이트 콜백을 처리하는 객체
    private LocationCallback locationCallback;

    // 날짜 정보와 관련된 UI 요소들
    private LinearLayout dateInfoLayout;
    private Button selectedDayButton = null;
    // 지도 관련 UI 요소들
    private MapView mapView;
    private KakaoMap kakaoMap;

    // 장소 데이터를 저장할 맵 (Day별로 관리)
    private Map<String, PlaceData> placesMap;
    // 기간의 시작과 끝을 저장하는 변수들
    private long startMillis;
    private long endMillis;

    // 하단 시트 동작을 제어하는 객체
    private BottomSheetBehavior<FrameLayout> bottomSheetBehavior;
    // RecyclerView와 어댑터를 정의
    private RecyclerView recyclerViewPlaces;
    private PlaceListAdapter placeListAdapter;
    // 장소 데이터를 리스트로 관리
    private List<PlaceData> placeList;
    private SharedPreferencesHelper sharedPreferencesHelper;

    // KakaoMap 초기화 콜백 객체
    private KakaoMapReadyCallback readyCallback = new KakaoMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull KakaoMap kakaoMap) {
            // 지도 준비가 완료되면 ProgressBar 숨김
            progressBar.setVisibility(View.GONE);
            // 지도에 중앙 레이블 추가
            LabelLayer layer = kakaoMap.getLabelManager().getLayer();
            centerLabel = layer.addLabel(LabelOptions.from("centerLabel", startPosition)
                    .setStyles(LabelStyle.from(R.drawable.red_dot_marker).setAnchorPoint(0.5f, 0.5f))
                    .setRank(1));
        }

        @NonNull
        @Override
        public LatLng getPosition() {
            // 초기 위치 반환
            return startPosition;
        }

        @NonNull
        @Override
        public int getZoomLevel() {
            // 초기 줌 레벨 설정
            return 17;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 레이아웃 설정
        setContentView(R.layout.map);

        // SharedPreferencesHelper를 사용하여 저장된 날짜 데이터를 불러옴
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this);
        SharedPreferencesHelper.DatePeriodData datePeriodData = sharedPreferencesHelper.getDates();
        startMillis = datePeriodData.getStartDateMillis();
        endMillis = datePeriodData.getEndDateMillis();

        // RecyclerView 설정
        recyclerViewPlaces = findViewById(R.id.recyclerViewPlaces);
        recyclerViewPlaces.setLayoutManager(new LinearLayoutManager(this));

        dateInfoLayout = findViewById(R.id.dateInfoLayout);

        // 저장소에서 장소 데이터를 불러옴
        placesMap = sharedPreferencesHelper.getAllPlaces();

        // 불러온 데이터를 로그로 출력
        for (Map.Entry<String, PlaceData> entry : placesMap.entrySet()) {
            String dayKey = entry.getKey();
            PlaceData placeData = entry.getValue();

            Log.d("Day", "Day: " + dayKey);
            Log.d("Place", "Place Name: " + placeData.getPlaceName());
            Log.d("Note", "Note: " + placeData.getNotes());
            Log.d("Lat", "Latitude: " + placeData.getLocations());
        }

        // 지도와 ProgressBar 설정
        mapView = findViewById(R.id.map_view);
        progressBar = findViewById(R.id.progressBar);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // 위치 요청 설정 초기화
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L).build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                // 위치 업데이트 시 중앙 레이블을 이동
                for (Location location : locationResult.getLocations()) {
                    centerLabel.moveTo(LatLng.from(location.getLatitude(), location.getLongitude()));
                }
            }
        };

        // 위치 권한이 있는지 확인 후 위치 가져오기
        if (ContextCompat.checkSelfPermission(this, locationPermissions[0]) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, locationPermissions[1]) == PackageManager.PERMISSION_GRANTED) {
            getStartLocation();
        } else {
            // 권한이 없으면 요청
            ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_PERMISSION_REQUEST_CODE);
        }

        // 하단 시트 초기화
        initializeBottomSheet();

        // 날짜 버튼들 생성
        createDayButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 위치 업데이트가 요청된 경우 재개
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 위치 업데이트 중지
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @SuppressLint("MissingPermission")
    private void getStartLocation() {
        // 현재 위치를 가져오는 메서드
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // 위치가 성공적으로 가져와지면 지도 시작
                        startPosition = LatLng.from(location.getLatitude(), location.getLongitude());
                        mapView.start(readyCallback);
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        // 위치 업데이트 요청을 시작
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
                // 권한 거부 시 알림 대화 상자 표시
                showPermissionDeniedDialog();
            }
        }
    }

    private void showPermissionDeniedDialog() {
        // 위치 권한이 거부된 경우 알림 대화 상자
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("위치 권한 거부시 앱을 사용할 수 없습니다.")
                .setPositiveButton("권한 설정하러 가기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            // 설정 화면으로 이동하여 권한 설정
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                            Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                            startActivity(intent);
                        } finally {
                            finish();
                        }
                    }
                })
                .setNegativeButton("앱 종료하기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void initializeBottomSheet() {
        // 하단 시트 초기화
        CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinatorLayout);
        FrameLayout bottomSheet = findViewById(R.id.bottomSheetContainer);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED); // 기본 상태 설정
    }

    private void createDayButtons() {
        // 기간에 따라 Day 버튼 생성
        Calendar startDate = Calendar.getInstance();
        startDate.setTimeInMillis(startMillis);

        Calendar endDate = Calendar.getInstance();
        endDate.setTimeInMillis(endMillis);

        long numberOfDays = getDateDifference(startDate, endDate) + 1;

        dateInfoLayout.removeAllViews(); // 기존 버튼 제거

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

            dayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectDayButton(dayButton);
                }
            });

            dateInfoLayout.addView(dayButton);
        }

        // 첫 번째 Day 버튼을 기본 선택
        if (dateInfoLayout.getChildCount() > 0) {
            Button firstDayButton = (Button) dateInfoLayout.getChildAt(0);
            selectDayButton(firstDayButton);
        }
    }

    private void selectDayButton(Button button) {
        // 기존 선택된 버튼 상태 복원
        if (selectedDayButton != null) {
            selectedDayButton.setSelected(false);
            selectedDayButton.setBackgroundResource(R.drawable.rounded_button_selected);
            selectedDayButton.setTextColor(getResources().getColorStateList(R.drawable.button_text_selected));
        }

        // 현재 선택된 버튼의 상태 설정
        button.setSelected(true);
        button.setBackgroundResource(R.drawable.rounded_button_selected);
        button.setTextColor(getResources().getColorStateList(R.drawable.button_text_selected));
        selectedDayButton = button;

        // 선택된 Day에 해당하는 장소와 메모를 RecyclerView에 표시
        String day = selectedDayButton.getText().toString();
        PlaceData placeData = placesMap.get(day);

        if (placeData != null) {
            placeListAdapter = new PlaceListAdapter(placeData.getPlaceName(), placeData.getNotes(), day);
            recyclerViewPlaces.setAdapter(placeListAdapter);
        } else {
            placeListAdapter = new PlaceListAdapter(new ArrayList<>(), new ArrayList<>(), day);
            recyclerViewPlaces.setAdapter(placeListAdapter);
        }
    }

    private long getDateDifference(Calendar startDate, Calendar endDate) {
        // 날짜 차이 계산
        long diffInMillis = endDate.getTimeInMillis() - startDate.getTimeInMillis();
        return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
    }
}
