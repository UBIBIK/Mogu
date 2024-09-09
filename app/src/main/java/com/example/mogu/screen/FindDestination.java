package com.example.mogu.screen;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mogu.R;
import com.example.mogu.custom.SafeAdapter;
import com.example.mogu.custom.SafeItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FindDestination extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener, SafeAdapter.OnSwitchCheckedChangeListener {

    private static final String TAG = "NextActivity";
    private static final int ZOOM_THRESHOLD = 15; // 줌 레벨 임계값
    private static final long SAFETY_DISPLAY_DELAY = 500; // 0.5초 딜레이
    private GoogleMap mMap;
    private LatLng markerLatLng;
    private LatLng mokpoUnivLatLng;
    private Polyline currentPolyline;
    private JSONArray safetyData;
    private double averageSafetyIndex = 0.0;  // 평균 안전지수
    private final Map<String, Polyline> displayedPolylines = new HashMap<>(); // 화면에 표시된 폴리라인들
    private final Handler safetyDisplayHandler = new Handler();
    private Runnable safetyDisplayRunnable;

    // 경로 데이터를 캐싱하기 위한 맵
    private final Map<String, JSONArray> routeCache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_destination);

        double currentLat = getIntent().getDoubleExtra("currentLat", 0.0);
        double currentLng = getIntent().getDoubleExtra("currentLng", 0.0);
        double destinationLat = getIntent().getDoubleExtra("destinationLat", 0.0);
        double destinationLng = getIntent().getDoubleExtra("destinationLng", 0.0);

        // Null이나 기본값일 경우 로그로 확인
        if (currentLat == 0.0 || currentLng == 0.0 || destinationLat == 0.0 || destinationLng == 0.0) {
            Log.e("FindDestination", "Invalid location data received.");
        }

        markerLatLng = new LatLng(currentLat, currentLng);
        mokpoUnivLatLng = new LatLng(destinationLat, destinationLng);

        loadSafetyData();  // 안전지수 데이터 로드 및 평균 계산

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "onCreate: 지도 프래그먼트가 null입니다.");
        }

        Button btnShortest = findViewById(R.id.btn_shortest);
        Button btnSafest = findViewById(R.id.btn_safest);

        btnShortest.setOnClickListener(v -> {
            clearCurrentPolyline();
            loadRoute("shortest_path");
        });

        btnSafest.setOnClickListener(v -> {
            clearCurrentPolyline();
            loadRoute("safety_path");
        });

        // Switch RecyclerView 설정
        RecyclerView switchRecyclerView = findViewById(R.id.switchRecyclerView);
        switchRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        List<SafeItem> safeItems = getSwitchItems();
        SafeAdapter safeAdapter = new SafeAdapter(safeItems, this);
        switchRecyclerView.setAdapter(safeAdapter);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.addMarker(new MarkerOptions().position(markerLatLng).title("도착지"));
        mMap.addMarker(new MarkerOptions().position(mokpoUnivLatLng).title("목포해양대학교"));

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(markerLatLng)
                .include(mokpoUnivLatLng)
                .build();

        // 두 마커가 모두 보이도록 카메라를 이동하고 줌 레벨을 조정합니다.
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100)); // 패딩 값으로 여유를 줌

        mMap.setOnCameraIdleListener(this); // 카메라 이동이 멈췄을 때 리스너 설정

        loadRoute("shortest_path");
    }

    private List<SafeItem> getSwitchItems() {
        List<SafeItem> safeItems = new ArrayList<>();
        safeItems.add(new SafeItem("소방서", false));
        safeItems.add(new SafeItem("CCTV", false));
        safeItems.add(new SafeItem("비상벨", false));
        safeItems.add(new SafeItem("스쿨존", false));
        safeItems.add(new SafeItem("편의점", false));
        safeItems.add(new SafeItem("범죄자 거주지", false));
        safeItems.add(new SafeItem("사고다발지역", false));
        return safeItems;
    }

    private void loadRoute(String routeType) {
        if (routeCache.containsKey(routeType)) {
            // 캐시된 경로 데이터를 사용
            try {
                drawPolyline(Objects.requireNonNull(routeCache.get(routeType)));
                if (mMap.getCameraPosition().zoom >= ZOOM_THRESHOLD) {
                    scheduleSafetyDisplay();
                }
            } catch (JSONException e) {
                Log.e(TAG, "loadRoute: 캐시된 경로 데이터 처리 중 오류 발생", e);
            }
        } else {
            // 서버에서 경로 데이터를 가져옴
            new FetchRouteTask().execute(routeType);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class FetchRouteTask extends AsyncTask<String, Void, JSONArray> {

        private String routeType;

        @Override
        protected JSONArray doInBackground(String... params) {
            routeType = params[0];
            try {
                //String urlString = "http://10.0.2.2:5000/calculate_route";
                String urlString = "http://34.64.214.135:5000/calculate_route";
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");

                JSONObject jsonParam = new JSONObject();
                JSONObject start = new JSONObject();
                start.put("lat", mokpoUnivLatLng.latitude);
                start.put("lon", mokpoUnivLatLng.longitude);
                JSONObject end = new JSONObject();
                end.put("lat", markerLatLng.latitude);
                end.put("lon", markerLatLng.longitude);

                jsonParam.put("start", start);
                jsonParam.put("end", end);

                conn.setDoOutput(true);
                conn.getOutputStream().write(jsonParam.toString().getBytes(StandardCharsets.UTF_8));

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder content = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                in.close();
                conn.disconnect();

                JSONObject jsonResponse = new JSONObject(content.toString());
                JSONArray route = jsonResponse.getJSONArray(routeType);

                // 캐시에 저장
                routeCache.put(routeType, route);

                return route;

            } catch (Exception e) {
                Log.e(TAG, "FetchRouteTask: 경로 요청 중 예외 발생", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONArray path) {
            if (path != null) {
                try {
                    if (mMap.getCameraPosition().zoom >= ZOOM_THRESHOLD) {
                        scheduleSafetyDisplay();
                    }
                    drawPolyline(path);
                } catch (JSONException e) {
                    Log.e(TAG, "FetchRouteTask: 폴리라인 그리기 중 JSONException 발생", e);
                }
            }
        }
    }

    private void loadSafetyData() {
        safetyData = new JSONArray();
        double totalSafetyIndex = 0;
        int safetyCount = 0;

        try {
            InputStream inputStream = getResources().openRawResource(R.raw.safety_mokpo);
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer, 0, buffer.length)) != -1) {
                builder.append(buffer, 0, read);
            }

            String jsonString = builder.toString();
            JSONObject geoJsonObject = new JSONObject(jsonString);
            safetyData = geoJsonObject.getJSONArray("features");

            for (int i = 0; i < safetyData.length(); i++) {
                JSONObject feature = safetyData.getJSONObject(i);
                JSONObject properties = feature.getJSONObject("properties");
                totalSafetyIndex += properties.getDouble("safety_index");
                safetyCount++;
            }

            if (safetyCount > 0) {
                averageSafetyIndex = totalSafetyIndex / safetyCount;
            }

        } catch (Exception e) {
            Log.e(TAG, "loadSafetyData: 안전 정보 로드 중 예외 발생", e);
        }
    }

    private void drawPolyline(JSONArray path) throws JSONException {
        PolylineOptions routePolylineOptions = new PolylineOptions();

        for (int i = 0; i < path.length(); i++) {
            JSONObject segment = path.getJSONObject(i);
            JSONArray start = segment.getJSONArray("start");
            JSONArray end = segment.getJSONArray("end");

            LatLng startLatLng = new LatLng(start.getDouble(1), start.getDouble(0));
            LatLng endLatLng = new LatLng(end.getDouble(1), end.getDouble(0));

            routePolylineOptions.add(startLatLng, endLatLng).color(Color.BLUE).width(10).zIndex(1.0f); // 경로 폴리라인의 zIndex를 1로 설정
        }

        currentPolyline = mMap.addPolyline(routePolylineOptions);
    }

    private void drawSafetyInfoWithinBounds(JSONArray features, LatLngBounds bounds) throws JSONException {
        if (features == null) return;

        // 현재 화면에 보이는 폴리라인만 유지하고 나머지는 제거
        List<String> currentKeys = new ArrayList<>();
        for (int i = 0; i < features.length(); i++) {
            JSONObject feature = features.getJSONObject(i);
            JSONObject geometry = feature.getJSONObject("geometry");
            String type = geometry.getString("type");

            if (type.equals("LineString") || type.equals("MultiLineString")) {
                JSONArray coordinatesArray = type.equals("LineString")
                        ? geometry.getJSONArray("coordinates")
                        : geometry.getJSONArray("coordinates").getJSONArray(0);

                boolean isInBounds = false;
                PolylineOptions polylineOptions = new PolylineOptions();
                StringBuilder keyBuilder = new StringBuilder();

                for (int j = 0; j < coordinatesArray.length(); j++) {
                    JSONArray latLng = coordinatesArray.getJSONArray(j);
                    double lng = latLng.getDouble(0);
                    double lat = latLng.getDouble(1);
                    LatLng point = new LatLng(lat, lng);
                    polylineOptions.add(point);
                    keyBuilder.append(lat).append(",").append(lng).append(";");
                    if (bounds.contains(point)) {
                        isInBounds = true;
                    }
                }

                String polylineKey = keyBuilder.toString();
                currentKeys.add(polylineKey);

                if (isInBounds && !displayedPolylines.containsKey(polylineKey)) {
                    int safetyIndex = feature.getJSONObject("properties").getInt("safety_index");
                    int color = getColorForSafetyScore(safetyIndex);
                    polylineOptions.color(color).width(10).zIndex(0.5f); // 안전 폴리라인의 zIndex를 0.5로 설정
                    Polyline polyline = mMap.addPolyline(polylineOptions);
                    displayedPolylines.put(polylineKey, polyline);
                }
            }
        }

        // 화면에 보이지 않는 폴리라인 제거
        displayedPolylines.keySet().retainAll(currentKeys);
    }

    private void scheduleSafetyDisplay() {
        // 이전에 예약된 작업이 있다면 취소
        if (safetyDisplayRunnable != null) {
            safetyDisplayHandler.removeCallbacks(safetyDisplayRunnable);
        }

        safetyDisplayRunnable = () -> {
            LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
            try {
                drawSafetyInfoWithinBounds(safetyData, bounds);
            } catch (JSONException e) {
                Log.e(TAG, "scheduleSafetyDisplay: 안전 정보 표시 중 JSONException 발생", e);
            }
        };

        // 딜레이 후에 실행
        safetyDisplayHandler.postDelayed(safetyDisplayRunnable, SAFETY_DISPLAY_DELAY);
    }

    private int getColorForSafetyScore(int safetyScore) {
        // 평균값을 기준으로 노란색, 그 이후로 초록색으로 변환
        float hue;
        if (safetyScore < averageSafetyIndex) {
            hue = 60f * (safetyScore / (float) averageSafetyIndex);  // 빨강(0)에서 노랑(60)으로 변환
        } else {
            hue = 60f + 60f * ((safetyScore - (float) averageSafetyIndex) / (100f - (float) averageSafetyIndex));  // 노랑(60)에서 초록(120)으로 변환
        }
        return Color.HSVToColor(new float[]{hue, 1f, 1f});
    }

    @Override
    public void onCameraIdle() {
        if (mMap.getCameraPosition().zoom >= ZOOM_THRESHOLD) {
            scheduleSafetyDisplay();
        } else {
            // 줌 아웃 시 모든 안전 폴리라인을 제거
            for (Polyline polyline : displayedPolylines.values()) {
                polyline.remove();
            }
            displayedPolylines.clear();
        }
    }

    private void clearCurrentPolyline() {
        if (currentPolyline != null) {
            currentPolyline.remove();
        }
    }

    private JSONArray readGeoJsonFromFile(int rawResourceId) {
        InputStream inputStream = getResources().openRawResource(rawResourceId);
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[1024];
        int read;
        try {
            while ((read = reader.read(buffer, 0, buffer.length)) != -1) {
                builder.append(buffer, 0, read);
            }
            String jsonString = builder.toString();
            JSONObject geoJsonObject = new JSONObject(jsonString);
            return geoJsonObject.getJSONArray("features"); // GeoJSON에서 'features' 배열을 추출
        } catch (Exception e) {
            Log.e(TAG, "readGeoJsonFromFile: 파일 읽기 오류", e);
        }
        return null;
    }

    private List<Marker> addMarkersFromGeoJson(GoogleMap map, JSONArray geoJsonFeatures, int position) {
        List<Marker> markers = new ArrayList<>();

        try {
            for (int i = 0; i < geoJsonFeatures.length(); i++) {
                JSONObject feature = geoJsonFeatures.getJSONObject(i);
                JSONObject geometry = feature.getJSONObject("geometry");

                if (geometry.getString("type").equals("Point")) {
                    JSONArray coordinates = geometry.getJSONArray("coordinates");
                    double lon = coordinates.getDouble(0);
                    double lat = coordinates.getDouble(1);

                    LatLng positionLatLng = new LatLng(lat, lon);
                    MarkerOptions markerOptions = new MarkerOptions().position(positionLatLng);

                    // 소방서 마커 커스텀 (position == 0 일 때)
                    if (position == 0) { // 소방서 항목일 때
                        Bitmap smallMarker = BitmapFactory.decodeResource(getResources(), R.drawable.fire_fighting); // 원하는 크기로 조절
                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker)); // 커스텀 마커 설정
                    }

                    // 마커 추가
                    Marker marker = map.addMarker(markerOptions);
                    markers.add(marker);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "addMarkersFromGeoJson: 마커 추가 중 오류", e);
        }

        return markers;
    }

    // 마커를 저장할 리스트
    private Map<Integer, List<Marker>> displayedMarkers = new HashMap<>();

    @Override
    public void onSwitchCheckedChanged(int position, boolean isChecked) {
        int resourceId = 0;
        List<Marker> markers;

        switch (position) {
            case 0: // 소방서
                resourceId = R.raw.fire_fighting;
                break;
            case 1: // CCTV
                resourceId = R.raw.cctv;
                break;
            case 2: // 비상벨
                resourceId = R.raw.emergency_bell;
                break;
            case 3: // 스쿨존
                resourceId = R.raw.school_zone;
                break;
            case 4: // 편의점
                resourceId = R.raw.convenience;
                break;
            case 5: // 범죄자 거주지
                resourceId = R.raw.crime;
                break;
            case 6: // 사고다발지역
                resourceId = R.raw.accidents;
                break;
        }

        if (isChecked) {
            // GeoJSON 파일을 읽어와서 마커 표시
            JSONArray geoJsonFeatures = readGeoJsonFromFile(resourceId);
            markers = addMarkersFromGeoJson(mMap, geoJsonFeatures, position); // position을 전달
            displayedMarkers.put(position, markers);
        } else {
            // 해당 위치의 마커를 제거
            markers = displayedMarkers.get(position);
            if (markers != null) {
                for (Marker marker : markers) {
                    marker.remove();
                }
                displayedMarkers.remove(position);
            }
        }
    }
}
