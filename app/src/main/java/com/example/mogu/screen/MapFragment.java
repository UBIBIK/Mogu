package com.example.mogu.screen;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.mogu.R;
import com.example.mogu.custom.CircularImageView;
import com.example.mogu.custom.PlaceListAdapter;
import com.example.mogu.object.CreateTripScheduleRequest;
import com.example.mogu.object.GroupInfo;
import com.example.mogu.object.LocationInfo;
import com.example.mogu.object.PlaceData;
import com.example.mogu.object.UserInfo;
import com.example.mogu.retrofit.ApiService;
import com.example.mogu.retrofit.RetrofitClient;
import com.example.mogu.share.LocationPreference;
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
import com.google.android.gms.maps.model.Marker;

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

                // 현재 위치가 있으면 마커 추가, 없으면 기본 위치 설정
                LocationPreference locationPreference = new LocationPreference(getContext());
                double latitude = locationPreference.getLatitude();
                double longitude = locationPreference.getLongitude();

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

    @SuppressLint({"MissingPermission", "ResourceType"})
    private void selectDayButton(Button button) {
        if (selectedDayButton != null) {
            selectedDayButton.setSelected(false);
            selectedDayButton.setBackgroundResource(R.drawable.rounded_button_selected);
        }

        button.setSelected(true);
        button.setBackgroundResource(R.drawable.rounded_button_selected);
        button.setTextColor(getResources().getColorStateList(R.drawable.button_text_selected));
        selectedDayButton = button;

        String day = selectedDayButton.getText().toString();
        PlaceData placeData = placesMap.get(day);

        if (placeData != null) {
            // PlaceListAdapter 생성 시 currentLocation 전달 필요 없음
            placeListAdapter = new PlaceListAdapter(getContext(),placeData.getPlaceNames(), placeData.getNotes(), day, placeData.getLocations());
            recyclerViewPlaces.setAdapter(placeListAdapter);

            if (googleMap != null) {
                addMarkersToMap(placeData); // 장소 마커 추가
            }
        }
        else {
            // PlaceListAdapter 생성 시 빈 리스트 사용
            placeListAdapter = new PlaceListAdapter(getContext(), new ArrayList<>(), new ArrayList<>(), day, new ArrayList<>());
            recyclerViewPlaces.setAdapter(placeListAdapter);

            if (googleMap != null) {
                // 장소 마커만 제거
                clearPlaceMarkers();
            }
        }


        // 내 위치도 함께 표시
        if (fusedLocationClient != null) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null && googleMap != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                            // 내 위치 마커 추가
                            googleMap.addMarker(new MarkerOptions()
                                    .position(currentLocation)
                                    .title("내 위치")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                            // 카메라를 내 위치로 이동
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));
                        }
                    });
        }
    }

    private void addMarkersToMap(PlaceData placeData) {
        clearPlaceMarkers(); // 기존 장소 마커만 제거

        List<LatLng> locations = placeData.getLocations();
        List<String> placeNames = placeData.getPlaceNames();
        List<LocationInfo> locationInfos = placeData.getLocationInfoList(); // 추가

        for (int i = 0; i < locations.size(); i++) {
            LatLng location = locations.get(i);
            String placeName = placeNames.get(i);
            String imageUrl = locationInfos.get(i).getImage();  // 이미지 URL을 LocationInfo에서 가져옴

            // 장소 마커 추가 및 리스트에 저장
            Marker marker = googleMap.addMarker(new MarkerOptions().position(location).title(placeName));
            placeMarkers.add(marker); // 장소 마커 리스트에 추가

            // 커스텀 마커를 원하는 크기로 추가
            addCustomMarkerToMap(location, placeName, imageUrl, 150, 200);  // 이미지 URL 전달

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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("그룹 선택");

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

    private Bitmap getCustomMarkerBitmap(View markerView, int width, int height) {
        // width와 height가 0 이하일 경우 기본값 설정
        if (width <= 0 || height <= 0) {
            width = 150;  // 기본 너비
            height = 150;  // 기본 높이
        }

        // 레이아웃을 Bitmap으로 변환
        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        markerView.layout(0, 0, markerView.getMeasuredWidth(), markerView.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerView.draw(canvas);

        // Bitmap을 원하는 크기로 스케일링
        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }


    private void addCustomMarkerToMap(LatLng location, String placeName, String imageUrl, int markerWidth, int markerHeight) {
        // 커스텀 마커의 레이아웃을 인플레이트
        @SuppressLint("InflateParams")
        View markerView = LayoutInflater.from(getContext()).inflate(R.layout.marker, null);

        CircularImageView markerImage = markerView.findViewById(R.id.marker_image);

        // Glide 설정 최적화: 네트워크 캐시 및 이미지 로딩 최적화
        Glide.with(getContext())
                .load(imageUrl)
                .placeholder(R.drawable.default_image)  // 기본 이미지
                .error(R.drawable.default_image)        // 에러 발생 시 기본 이미지
                .fallback(R.drawable.default_image)     // 이미지 URL이 없을 때 기본 이미지
                .override(150, 150)                     // 크기 최적화
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 자동 캐시 관리 (기본 설정, 필요한 경우 캐시)
                .format(DecodeFormat.PREFER_RGB_565)    // 메모리 최적화 (16비트 이미지 처리)
                .skipMemoryCache(false)                 // 메모리 캐시 활성화 (한번 로드된 이미지는 메모리에 저장)
                .into(markerImage);

        // Bitmap으로 변환된 마커를 Google Map에 추가
        Bitmap customMarkerBitmap = getCustomMarkerBitmap(markerView, markerWidth, markerHeight);

        googleMap.addMarker(new MarkerOptions()
                .position(location)
                .title(placeName)
                .icon(BitmapDescriptorFactory.fromBitmap(customMarkerBitmap)));
    }



}