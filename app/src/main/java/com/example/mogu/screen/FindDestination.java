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

    private static final String TAG = "FindDestination";
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
    private Marker lastSelectedMarker = null;

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

        mMap.addMarker(new MarkerOptions().position(markerLatLng).title("출발지"));
        mMap.addMarker(new MarkerOptions().position(mokpoUnivLatLng).title("도착지"));

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(markerLatLng)
                .include(mokpoUnivLatLng)
                .build();

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

        // 관광지 마커 추가
        JSONArray tourFeatures = readGeoJsonFromFile(R.raw.tour);
        addTourMarkersFromGeoJson(mMap, tourFeatures);

        mMap.setOnCameraIdleListener(this);

        // 마커 클릭 리스너 설정
        mMap.setOnMarkerClickListener(marker -> {
            String placeName = marker.getTitle();
            String imageUrl = (String) marker.getTag();  // 마커에 이미지 URL을 태그로 저장

            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                // 이전에 선택된 마커가 있으면 원래의 상태로 복원
                if (lastSelectedMarker != null && lastSelectedMarker != marker) {
                    resetMarkerToDefault(lastSelectedMarker);
                }
                // 선택된 마커에 이미지 설정
                new LoadImageTask(mMap, marker).execute(imageUrl);
                lastSelectedMarker = marker;
            }

            return false; // 기본 동작을 수행하도록 false 반환
        });

        // 지도를 클릭하면 선택된 마커를 복원
        mMap.setOnMapClickListener(latLng -> {
            if (lastSelectedMarker != null) {
                resetMarkerToDefault(lastSelectedMarker);
                lastSelectedMarker = null;
            }
        });

        loadRoute("shortest_path");
    }

    // 두 좌표 사이의 거리를 미터 단위로 계산
    private double calculateDistance(LatLng latLng1, LatLng latLng2) {
        double earthRadius = 6371000; // 미터 단위
        double dLat = Math.toRadians(latLng2.latitude - latLng1.latitude);
        double dLng = Math.toRadians(latLng2.longitude - latLng1.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(latLng1.latitude)) * Math.cos(Math.toRadians(latLng2.latitude)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    private void addTourMarkersFromGeoJson(GoogleMap map, JSONArray geoJsonFeatures) {
        try {
            for (int i = 0; i < geoJsonFeatures.length(); i++) {
                JSONObject feature = geoJsonFeatures.getJSONObject(i);
                JSONObject geometry = feature.getJSONObject("geometry");
                JSONObject properties = feature.getJSONObject("properties");

                if (geometry.getString("type").equals("Point")) {
                    JSONArray coordinates = geometry.getJSONArray("coordinates");
                    double lon = coordinates.getDouble(0);
                    double lat = coordinates.getDouble(1);

                    String placeName = properties.getString("PlaceName");
                    String imageUrl = properties.getString("Image");

                    LatLng positionLatLng = new LatLng(lat, lon);

                    // 도착지와의 거리가 10m 이하이면 관광지 마커를 추가하지 않음
                    if (calculateDistance(positionLatLng, mokpoUnivLatLng) <= 10) {
                        Log.i(TAG, "Tour marker '" + placeName + "' is too close to the destination. Skipping.");
                        continue; // 이 관광지 마커는 추가하지 않고 넘어감
                    }

                    // 마커 추가
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(positionLatLng)
                            .title(placeName)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)); // 기본 마커 색상 설정

                    Marker marker = map.addMarker(markerOptions);

                    if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                        marker.setTag(imageUrl); // 이미지 URL을 태그로 저장
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "addTourMarkersFromGeoJson: 관광지 마커 추가 중 오류", e);
        }
    }

    // 마커 클릭 시 이미지를 다운로드하여 표시
    @SuppressLint("StaticFieldLeak")
    private static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final GoogleMap map;
        private final Marker marker;

        public LoadImageTask(GoogleMap map, Marker marker) {
            this.map = map;
            this.marker = marker;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            try {
                InputStream in = new URL(url).openStream();
                Bitmap image = BitmapFactory.decodeStream(in);
                return Bitmap.createScaledBitmap(image, 300, 300, false);
            } catch (Exception e) {
                Log.e(TAG, "LoadImageTask: 이미지 로드 중 오류 발생", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                // 이미지를 마커로 변경
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
            }
        }
    }

    // 마커를 기본 마커로 되돌림
    private void resetMarkerToDefault(Marker marker) {
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
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
                start.put("lat", markerLatLng.latitude);
                start.put("lon", markerLatLng.longitude);
                JSONObject end = new JSONObject();
                end.put("lat", mokpoUnivLatLng.latitude);
                end.put("lon", mokpoUnivLatLng.longitude);

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

            Log.i(TAG, "Safety data loaded successfully");

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

        List<String> currentKeys = new ArrayList<>();
        for (int i = 0; i < features.length(); i++) {
            JSONObject feature = features.getJSONObject(i);
            JSONObject geometry = feature.getJSONObject("geometry");
            String type = geometry.getString("type");

            Log.i(TAG, "Processing feature: " + i + " of type: " + type);

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
                    Log.i(TAG, "Drawing polyline for safety index: " + safetyIndex);
                    int color = getColorForSafetyScore(safetyIndex);
                    polylineOptions.color(color).width(10).zIndex(0.5f);
                    Polyline polyline = mMap.addPolyline(polylineOptions);
                    displayedPolylines.put(polylineKey, polyline);
                } else {
                    Log.i(TAG, "Feature not in bounds or already displayed.");
                }
            }
        }

        // 화면에 보이지 않는 폴리라인 제거
        displayedPolylines.keySet().retainAll(currentKeys);
    }



    private void scheduleSafetyDisplay() {
        if (safetyDisplayRunnable != null) {
            safetyDisplayHandler.removeCallbacks(safetyDisplayRunnable);
        }

        safetyDisplayRunnable = () -> {
            LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
            Log.i(TAG, "Map bounds: " + bounds.toString());
            try {
                drawSafetyInfoWithinBounds(safetyData, bounds);
            } catch (JSONException e) {
                Log.e(TAG, "scheduleSafetyDisplay: 안전 정보 표시 중 JSONException 발생", e);
            }
        };

        Log.i(TAG, "Scheduling safety display with delay.");
        safetyDisplayHandler.postDelayed(safetyDisplayRunnable, SAFETY_DISPLAY_DELAY);
    }

    private int getColorForSafetyScore(int safetyScore) {
        float hue;
        if (safetyScore < averageSafetyIndex) {
            hue = 60f * (safetyScore / (float) averageSafetyIndex);
        } else {
            hue = 60f + 60f * ((safetyScore - (float) averageSafetyIndex) / (100f - (float) averageSafetyIndex));
        }
        Log.i(TAG, "Safety score: " + safetyScore + " -> Color hue: " + hue);
        return Color.HSVToColor(new float[]{hue, 1f, 1f});
    }

    @Override
    public void onCameraIdle() {
        float zoom = mMap.getCameraPosition().zoom;
        Log.i(TAG, "Current zoom level: " + zoom);
        if (zoom >= ZOOM_THRESHOLD) {
            Log.i(TAG, "Zoom level is sufficient, scheduling safety display.");
            scheduleSafetyDisplay();
        } else {
            Log.i(TAG, "Zoom level is too low, clearing polylines.");
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
            return geoJsonObject.getJSONArray("features");
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

                    // 각 마커 아이콘의 이미지를 64x64로 축소
                    Bitmap smallMarker = null;
                    switch (position) {
                        case 0: // 소방서
                            smallMarker = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.fire_fighting), 100, 100, false);
                            break;
                        case 1: // CCTV
                            smallMarker = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.cctv), 100, 100, false);
                            break;
                        case 2: // 비상벨
                            smallMarker = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bell), 100, 100, false);
                            break;
                        case 3: // 스쿨존
                            smallMarker = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.school_zone), 100, 100, false);
                            break;
                        case 4: // 편의점
                            smallMarker = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.convenience), 100, 100, false);
                            break;
                        case 5: // 범죄자 거주지
                            smallMarker = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.crime), 200, 200, false);
                            break;
                        case 6: // 사고다발지역
                            smallMarker = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.accidents), 150, 150, false);
                            break;
                    }

                    // 마커에 아이콘 설정
                    if (smallMarker != null) {
                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
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