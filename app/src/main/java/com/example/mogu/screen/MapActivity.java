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

    private static final int PLACE_SELECTION_REQUEST_CODE = 1002;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private final String[] locationPermissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng startPosition = new LatLng(34.8118, 126.3922);
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
    private Map<String, PlaceData> placesMap = new HashMap<>();
    private long startMillis;
    private long endMillis;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        placeRecyclerView = findViewById(R.id.recyclerViewPlaces);
        placeRecyclerView.setLayoutManager(new LinearLayoutManager(this));

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
                    LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(newLocation));
                }
            }
        };

        if (ContextCompat.checkSelfPermission(this, locationPermissions[0]) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, locationPermissions[1]) == PackageManager.PERMISSION_GRANTED) {
            getStartLocation();
        } else {
            ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_PERMISSION_REQUEST_CODE);
        }

        initializeBottomSheet();
        createDayButtons();

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.addMarker(new MarkerOptions().position(startPosition).title("목포"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPosition, 15));
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
        saveButton = findViewById(R.id.saveButton);

        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this);
        placesMap = sharedPreferencesHelper.getAllPlaces();

        addPlaceButton.setOnClickListener(view -> {
            if (selectedDayButton != null) {
                openPlaceFragment();
            }
        });

        saveButton.setOnClickListener(view -> {
            Intent intent = new Intent(MapActivity.this, HomeMap.class);
            startActivity(intent);
        });
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
            placeDataAdapter = new PlaceDataAdapter(placeData, placeData.getPlaceName(), placeData.getNotes(), this, day);
        } else {
            placeDataAdapter = new PlaceDataAdapter(new PlaceData(), new ArrayList<>(), new ArrayList<>(), this, day);
        }

        placeRecyclerView.setAdapter(placeDataAdapter);

        placeDataAdapter.setOnEditPlaceListener((position, placeDataToEdit) -> {
            openPlaceFragmentForEditing(placeDataToEdit, position);
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

            if (placeLat != 0 && placeLng != 0) {
                LatLng placeLatLng = new LatLng(placeLat, placeLng);

                if (googleMap != null) {
                    googleMap.addMarker(new MarkerOptions().position(placeLatLng).title("Selected Place"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng, 15));
                }

                // PlaceData에 추가
                String day = selectedDayButton.getText().toString();
                PlaceData placeData = placesMap.get(day);

                if (placeData == null) {
                    placeData = new PlaceData();
                }

                placeData.addPlace("Selected Place", placeLatLng, "");
                placesMap.put(day, placeData);

                // RecyclerView를 갱신
                placeDataAdapter.notifyDataSetChanged();
            }
        }
    }

    public void addPlaceToMap(String placeName, LatLng placeLatLng) {
        if (googleMap != null) {
            // 지도에 마커 추가
            googleMap.addMarker(new MarkerOptions().position(placeLatLng).title(placeName));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng, 15));

            // PlaceData에 추가
            String day = selectedDayButton.getText().toString();
            PlaceData placeData = placesMap.get(day);

            if (placeData == null) {
                placeData = new PlaceData();
            }

            placeData.addPlace(placeName, placeLatLng, "");
            placesMap.put(day, placeData);

            // RecyclerView를 갱신
            placeDataAdapter.notifyDataSetChanged();
        }
    }
}
