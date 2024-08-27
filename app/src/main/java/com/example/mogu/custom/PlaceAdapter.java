package com.example.mogu.custom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mogu.R;
import com.example.mogu.object.PlaceData;
import com.example.mogu.object.TourApi;
import com.example.mogu.screen.MapActivity;
import com.example.mogu.share.SharedPreferencesHelper;
import com.kakao.vectormap.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.ViewHolder> {
    private ArrayList<TourApi> items;
    private SharedPreferencesHelper sharedPreferencesHelper;
    private static final String DETAIL_SERVICE_URL = "http://apis.data.go.kr/B551011/KorService1/detailCommon1";
    private static final String SERVICE_KEY = "iYq%2FBTYJSMKmITGfxxEBnluf6wJSfDjyGv8HUQJCYnqLkGKt%2BGTq4mNkwGDB5gEofiE34ur%2Fen1s7Nq1xWuLeg%3D%3D";
    private static final String MOBILE_OS = "AND";
    private static final String MOBILE_APP = "AppTest";
    private static final String OVERVIEWYN = "Y";
    private PlaceData placeData;
    private String placeName;
    private String day;
    private PlaceData editPlaceData;  // 수정 중인 장소 데이터를 저장
    private int editPosition;  // 수정 중인 위치를 저장

    public PlaceAdapter(ArrayList<TourApi> items, Context context, String day, PlaceData editPlaceData, int editPosition) {
        this.items = items;
        this.sharedPreferencesHelper = new SharedPreferencesHelper(context);
        this.day = day;
        this.editPlaceData = editPlaceData;
        this.editPosition = editPosition;
    }

    @NonNull
    @Override
    public PlaceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_tourapi, parent, false);
        return new PlaceAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceAdapter.ViewHolder holder, int position) {
        TourApi item = items.get(position);
        holder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgFirstImage;
        private TextView txtTitle;
        private TextView txtAddr1;
        private TextView txtAddr2;
        private Map<String, PlaceData> placesMap;

        public ViewHolder(View itemView) {
            super(itemView);
            imgFirstImage = itemView.findViewById(R.id.imgFirstImage);
            txtTitle = itemView.findViewById(R.id.tvTitle);
            txtAddr1 = itemView.findViewById(R.id.tvAddr1);
            txtAddr2 = itemView.findViewById(R.id.tvAddr2);

            // placesMap 초기화
            placesMap = sharedPreferencesHelper.getAllPlaces();
            if (placesMap == null) {
                placesMap = new HashMap<>(); // null이면 새로 초기화
            }

            imgFirstImage.setOnClickListener(v -> {
                // 팝업창 띄우기
                View popupView = LayoutInflater.from(v.getContext()).inflate(R.layout.popup_image_place, null);
                ImageView popupImage = popupView.findViewById(R.id.popupImagePlace);
                TextView popupSummary = popupView.findViewById(R.id.popupSummaryPlace);
                TextView textPlaceName = popupView.findViewById(R.id.textPlaceName_search);
                Button btnAddPlace = popupView.findViewById(R.id.btnAddPlace);

                TourApi item = items.get(getAdapterPosition());
                Glide.with(popupImage.getContext())
                        .load(item.getFirstimage())
                        .error(R.drawable.ic_launcher_foreground)
                        .into(popupImage);

                // 장소 이름 설정
                placeName = item.getTitle();
                textPlaceName.setText(placeName);

                AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                        .setView(popupView)
                        .setPositiveButton("닫기", (dialog1, which) -> dialog1.dismiss())
                        .show();

                // API 호출하여 개요 데이터 가져오기
                String contentId = item.getContentid();
                String requestUrl = DETAIL_SERVICE_URL +
                        "?serviceKey=" + SERVICE_KEY +
                        "&contentId=" + contentId +
                        "&MobileApp=" + MOBILE_APP +
                        "&MobileOS=" + MOBILE_OS +
                        "&overviewYN=" + OVERVIEWYN;

                fetchOverviewData(requestUrl, popupSummary);

                // 장소 추가 버튼 클릭 리스너
                btnAddPlace.setOnClickListener(v1 -> {
                    // 위도와 경도 설정
                    try {
                        double latitude = item.getMapx();
                        double longitude = item.getMapy();

                        LatLng kakaoLatLng = LatLng.from(latitude, longitude);

                        // 장소 편집인지 여부 확인
                        if (editPlaceData != null && editPosition != -1) {
                            // 기존 데이터를 수정
                            editPlaceData.setPlaceNameAt(editPosition, placeName);
                            editPlaceData.setLocationAt(editPosition, kakaoLatLng);

                            // 장소 맵에 수정된 데이터 저장
                            placesMap.put(day, editPlaceData);
                        } else {
                            // 새로 추가하는 경우
                            PlaceData placeData = placesMap.get(day);
                            if (placeData == null) {
                                placeData = new PlaceData();
                            }
                            placeData.addPlace(placeName, kakaoLatLng, ""); // 빈 노트를 기본값으로 설정

                            // 장소 맵에 추가
                            placesMap.put(day, placeData);
                        }

                        // 장소 저장
                        sharedPreferencesHelper.savePlaces(placesMap);

                        // 로그 출력 - 저장된 PlaceData 확인
                        Log.d("PlaceAdapter", "날짜: " + day);
                        Log.d("PlaceAdapter", "장소 이름: " + placeName);
                        Log.d("PlaceAdapter", "위도: " + latitude + ", 경도: " + longitude);
                        Log.d("PlaceAdapter", "전체 장소 데이터: " + placesMap.toString());

                        // 장소 추가 후 맵 화면으로 이동
                        Intent intent = new Intent(v.getContext(), MapActivity.class);
                        v.getContext().startActivity(intent);

                        dialog.dismiss();
                    } catch (Exception e) {
                        Log.e("PlaceAdapter", "Error adding place", e);
                    }
                });


            });
        }

        public void setItem(TourApi item) {
            Glide.with(imgFirstImage.getContext())
                    .load(item.getFirstimage())
                    .error(R.drawable.ic_launcher_foreground)
                    .into(imgFirstImage);
            txtTitle.setText(item.getTitle());
            txtAddr1.setText(item.getAddr1());
            txtAddr2.setText(item.getAddr2());
        }

        private void fetchOverviewData(String url, TextView popupSummary) {
            class FetchOverviewData extends AsyncTask<Void, Void, String> {
                private String page;

                @Override
                protected String doInBackground(Void... voids) {
                    try {
                        URL apiUrl = new URL(url);
                        BufferedReader bufReader = new BufferedReader(new InputStreamReader(apiUrl.openStream(), "UTF-8"));

                        page = "";
                        String line;
                        while ((line = bufReader.readLine()) != null) {
                            page += line;
                        }
                        bufReader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return page;
                }

                @Override
                protected void onPostExecute(String result) {
                    super.onPostExecute(result);
                    try {
                        // XML 파싱
                        String overview = "";
                        boolean tagOverview = false;

                        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                        factory.setNamespaceAware(true);
                        XmlPullParser xpp = factory.newPullParser();
                        xpp.setInput(new StringReader(result));

                        int eventType = xpp.getEventType();
                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            if (eventType == XmlPullParser.START_TAG) {
                                String tagName = xpp.getName();
                                if (tagName.equals("overview")) tagOverview = true;
                            } else if (eventType == XmlPullParser.TEXT) {
                                if (tagOverview) {
                                    overview = xpp.getText();
                                    tagOverview = false;
                                }
                            }
                            eventType = xpp.next();
                        }

                        // HTML 태그 제거
                        overview = overview.replaceAll("<br\\s*/?>", "\n");

                        // 팝업창에 개요 데이터 설정
                        popupSummary.setText(Html.fromHtml(overview, Html.FROM_HTML_MODE_LEGACY));

                    } catch (Exception e) {
                        e.printStackTrace();
                        popupSummary.setText("개요를 불러오는 데 실패했습니다.");
                    }
                }
            }

            new FetchOverviewData().execute();
        }
    }
}
