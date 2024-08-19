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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
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

import com.example.mogu.R;
import com.example.mogu.share.SharedPreferencesHelper;
import com.example.mogu.screen.PlaceFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.MapView;
import com.kakao.vectormap.label.Label;
import com.kakao.vectormap.label.LabelLayer;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyle;
import com.kakao.vectormap.label.TrackingManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MapActivity extends AppCompatActivity {

    private TextView textAddedPlaceName;

    private final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private final String[] locationPermissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng startPosition = null;
    private ProgressBar progressBar;
    private Label centerLabel;
    private boolean requestingLocationUpdates = false;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private MapView mapView;
    private KakaoMap kakaoMap;
    private BottomSheetBehavior<FrameLayout> bottomSheetBehavior;
    private LinearLayout dateInfoLayout;
    private Button addPlaceButton;
    private Button addNoteButton;
    private Button selectedDayButton = null;
    private TextView noteTextView;
    private LinearLayout notePopupLayout;
    private EditText popupNoteEditText;
    private Button popupSaveButton;
    private Button toggleBottomSheetButton;

    private Map<String, String> dayNotesMap = new HashMap<>();
    private Map<String, String> placesMap = new HashMap<>();
    private long startMillis;
    private long endMillis;

    private KakaoMapReadyCallback readyCallback = new KakaoMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull KakaoMap kakaoMap) {
            progressBar.setVisibility(View.GONE);
            LabelLayer layer = kakaoMap.getLabelManager().getLayer();
            centerLabel = layer.addLabel(LabelOptions.from("centerLabel", startPosition)
                    .setStyles(LabelStyle.from(R.drawable.red_dot_marker).setAnchorPoint(0.5f, 0.5f))
                    .setRank(1));
            TrackingManager trackingManager = kakaoMap.getTrackingManager();
            trackingManager.startTracking(centerLabel);
            startLocationUpdates();
        }

        @NonNull
        @Override
        public LatLng getPosition() {
            return startPosition;
        }

        @NonNull
        @Override
        public int getZoomLevel() {
            return 17;
        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this);
        SharedPreferencesHelper.DatePeriodData datePeriodData = sharedPreferencesHelper.getDates();
        startMillis = datePeriodData.getStartDateMillis();
        endMillis = datePeriodData.getEndDateMillis();

        mapView = findViewById(R.id.map_view);
        progressBar = findViewById(R.id.progressBar);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L).build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    centerLabel.moveTo(LatLng.from(location.getLatitude(), location.getLongitude()));
                }
            }
        };

        if (ContextCompat.checkSelfPermission(this, locationPermissions[0]) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, locationPermissions[1]) == PackageManager.PERMISSION_GRANTED) {
            getStartLocation();
        } else {
            ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_PERMISSION_REQUEST_CODE);
        }

        // 하단 시트 초기화
        initializeBottomSheet();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @SuppressLint("MissingPermission")
    private void getStartLocation() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        startPosition = LatLng.from(location.getLatitude(), location.getLongitude());
                        mapView.start(readyCallback);
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
        CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinatorLayout);
        FrameLayout bottomSheet = findViewById(R.id.bottomSheetContainer);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        dateInfoLayout = findViewById(R.id.dateInfoLayout);
        addPlaceButton = findViewById(R.id.addPlaceButton);
        addNoteButton = findViewById(R.id.addNoteButton);
        noteTextView = findViewById(R.id.noteTextView);
        notePopupLayout = findViewById(R.id.notePopupLayout);
        popupNoteEditText = findViewById(R.id.popupNoteEditText);
        popupSaveButton = findViewById(R.id.popupSaveButton);
        toggleBottomSheetButton = findViewById(R.id.toggleBottomSheetButton);

        textAddedPlaceName = findViewById(R.id.textAddedPlaceName);

        // 인텐트에서 장소 이름 가져오기
        Intent intent = getIntent();
        String placeName = intent.getStringExtra("PLACE_NAME");
        if (placeName != null) {
            textAddedPlaceName.setText(placeName);
        }

        // SharedPreferences에서 메모와 장소 불러오기
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this);
        dayNotesMap = sharedPreferencesHelper.getAllNotes();
        placesMap = sharedPreferencesHelper.getAllPlaces();

        // 장소 추가 버튼 클릭 리스너
        addPlaceButton.setOnClickListener(view -> {
            if (selectedDayButton != null) {
                toggleBottomSheetButton.setVisibility(View.GONE);
                openPlaceFragment();
            }
        });

        // 메모 추가 버튼 클릭 리스너
        addNoteButton.setOnClickListener(view -> {
            if (selectedDayButton != null) {
                showNotePopup();
            }
        });

        // 메모 팝업 창의 저장 버튼 클릭 리스너
        popupSaveButton.setOnClickListener(view -> saveNote());

        // 하단 시트 열기/닫기 버튼 클릭 리스너
        toggleBottomSheetButton.setOnClickListener(v -> {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                toggleBottomSheetButton.setText("하단 시트 닫기");
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                toggleBottomSheetButton.setText("하단 시트 열기");
            }
        });

        // 날짜 버튼들 생성
        createDayButtons();
    }

    private void createDayButtons() {
        Calendar startDate = Calendar.getInstance();
        startDate.setTimeInMillis(startMillis);

        Calendar endDate = Calendar.getInstance();
        endDate.setTimeInMillis(endMillis);

        long numberOfDays = getDateDifference(startDate, endDate) + 1;

        dateInfoLayout.removeAllViews(); // 기존 버튼 삭제

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

        // DAY1 버튼을 기본적으로 클릭된 상태로 설정
        if (dateInfoLayout.getChildCount() > 0) {
            Button firstDayButton = (Button) dateInfoLayout.getChildAt(0);
            selectDayButton(firstDayButton);
        }
    }

    private void selectDayButton(Button button) {
        // 기존 선택된 버튼의 상태를 복원
        if (selectedDayButton != null) {
            selectedDayButton.setSelected(false);
            selectedDayButton.setBackgroundResource(R.drawable.rounded_button_selected);
            selectedDayButton.setTextColor(getResources().getColorStateList(R.drawable.button_text_selected));
        }

        // 현재 버튼을 선택 상태로 설정
        button.setSelected(true);
        button.setBackgroundResource(R.drawable.rounded_button_selected);
        button.setTextColor(getResources().getColorStateList(R.drawable.button_text_selected));
        selectedDayButton = button;

        // 선택된 DAY 버튼에 해당하는 메모를 TextView에 표시
        String day = selectedDayButton.getText().toString();
        String note = dayNotesMap.get(day);
        noteTextView.setText(note != null ? note : "");

        // 하단 시트가 닫히지 않도록 설정
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void showNotePopup() {
        String day = selectedDayButton.getText().toString();
        String note = dayNotesMap.get(day);
        popupNoteEditText.setText(note != null ? note : "");
        notePopupLayout.setVisibility(View.VISIBLE);
    }

    private void saveNote() {
        String day = selectedDayButton.getText().toString();
        String note = popupNoteEditText.getText().toString();
        dayNotesMap.put(day, note);
        noteTextView.setText(note);
        notePopupLayout.setVisibility(View.GONE);

        // 메모를 SharedPreferences에 저장
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this);
        sharedPreferencesHelper.saveNotes(dayNotesMap);

        // 하단 시트 상태 유지
        bottomSheetBehavior.setState(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED
                ? BottomSheetBehavior.STATE_EXPANDED : BottomSheetBehavior.STATE_COLLAPSED);
    }

    private long getDateDifference(Calendar startDate, Calendar endDate) {
        long diffInMillis = endDate.getTimeInMillis() - startDate.getTimeInMillis();
        return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
    }

    private void openPlaceFragment() {
        Fragment placeFragment = new PlaceFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("places_list", new ArrayList<>(placesMap.values())); // 장소 이름 리스트로 변경
        placeFragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.mapContainer, placeFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    // 장소를 저장하는 메소드
    public void savePlace(String placeName) {
        placesMap.put(placeName, placeName); // 장소 이름만 저장

        // SharedPreferences에 장소 저장
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this);
        sharedPreferencesHelper.savePlaces(placesMap);
    }
}

