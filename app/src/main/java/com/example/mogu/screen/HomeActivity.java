package com.example.mogu.screen;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.mogu.R;
import com.example.mogu.share.LocationPreference;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity"; // 로그 태그 설정

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationPreference locationPreference; // LocationPreference 객체 선언

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int LOCATION_SETTINGS_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationPreference = new LocationPreference(this); // LocationPreference 초기화

        // 버튼 클릭 시 프래그먼트 교체
        findViewById(R.id.HomeButton).setOnClickListener(v -> replaceFragment(new HomeFragment()));
        findViewById(R.id.GroupButton).setOnClickListener(v -> replaceFragment(new GroupFragment()));
        findViewById(R.id.MapButton).setOnClickListener(v -> replaceFragment(new MapFragment()));

        // 초기 프래그먼트 설정
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
        }

        // 위치 권한이 있는지 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // 권한 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // 권한이 있으면 위치 서비스 설정 확인 및 요청
            checkLocationSettingsAndRequestLocation();
        }
    }

    // 프래그먼트 교체 메서드
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // 위치 설정 확인 및 요청
    private void checkLocationSettingsAndRequestLocation() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // 위치 설정이 올바른 경우 위치 업데이트 요청
                requestLocationUpdates();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(HomeActivity.this, LOCATION_SETTINGS_REQUEST_CODE);
                    } catch (IntentSender.SendIntentException sendEx) {
                        Log.e(TAG, "Error enabling location services", sendEx);
                    }
                } else {
                    Log.w(TAG, "Location settings are inadequate and cannot be resolved.");
                }
            }
        });
    }

    // 위치 업데이트 요청
    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.w(TAG, "Unable to get current location");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        // 위치 정보를 LocationPreference에 저장
                        locationPreference.saveLocation(latitude, longitude);

                        // 저장 완료 로그 출력
                        Log.d(TAG, "Location saved: Lat=" + latitude + ", Lon=" + longitude);
                    } else {
                        Log.w(TAG, "Unable to get current location");
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            Log.w(TAG, "Location permission not granted");
        }
    }

    // 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationSettingsAndRequestLocation();
            } else {
                Log.w(TAG, "Location permission denied");
            }
        }
    }

    // 위치 서비스 활성화 요청 결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_SETTINGS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                requestLocationUpdates();
            } else {
                Log.w(TAG, "Location services not enabled.");
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}
