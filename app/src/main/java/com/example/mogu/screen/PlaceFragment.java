package com.example.mogu.screen;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mogu.R;
import com.example.mogu.object.TourApi;
import com.example.mogu.custom.TourAdapter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;

public class PlaceFragment extends Fragment {

    private static final int NUM_OF_ROWS = 10;
    private static final int PAGE_NO = 1;
    private static final String MOBILE_OS = "AND";
    private static final String MOBILE_APP = "AppTest";
    private static final String LIST_YN = "Y";
    private static final String ARRANGE = "A";
    private static final int AREA_CODE = 38;
    private static final int SIGUNGU_CODE = 8;
    private static final String SERVICE_URL = "http://apis.data.go.kr/B551011/KorService1/areaBasedList1";
    private static final String SERVICE_KEY = "iYq%2FBTYJSMKmITGfxxEBnluf6wJSfDjyGv8HUQJCYnqLkGKt%2BGTq4mNkwGDB5gEofiE34ur%2Fen1s7Nq1xWuLeg%3D%3D";

    private RecyclerView rcPlaceList;
    private TourAdapter tourAdapter;
    private EditText etSearch;
    private Spinner spCategory;
    private int contentTypeId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place_search, container, false);

        rcPlaceList = view.findViewById(R.id.recyclerViewPlaces);
        rcPlaceList.setLayoutManager(new LinearLayoutManager(getContext()));

        etSearch = view.findViewById(R.id.etSearch);
        spCategory = view.findViewById(R.id.spinnerCategory);

        // 기본 카테고리 (관광지) 설정
        contentTypeId = 12;
        final String initialRequestUrl = buildRequestUrl(contentTypeId);
        fetchXML(initialRequestUrl, contentTypeId);

        // 카테고리 변경 시
        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                contentTypeId = getContentTypeId(position);
                final String requestUrl = buildRequestUrl(contentTypeId);
                fetchXML(requestUrl, contentTypeId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // 기본 카테고리 유지
            }
        });

        // 검색 버튼 클릭 시
        Button btnSearch = view.findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                final String requestUrl = buildRequestUrlWithQuery(query, contentTypeId);
                fetchXML(requestUrl, contentTypeId);
            }
        });

        return view;
    }

    private String buildRequestUrl(int contentTypeId) {
        return SERVICE_URL +
                "?serviceKey=" + SERVICE_KEY +
                "&pageNo=" + PAGE_NO +
                "&numOfRows=" + NUM_OF_ROWS +
                "&MobileApp=" + MOBILE_APP +
                "&MobileOS=" + MOBILE_OS +
                "&arrange=" + ARRANGE +
                "&areaCode=" + AREA_CODE +
                "&sigunguCode=" + SIGUNGU_CODE+
                "&contentTypeId=" + contentTypeId +
                "&listYN=" + LIST_YN;
    }

    private String buildRequestUrlWithQuery(String query, int contentTypeId) {
        return SERVICE_URL +
                "?serviceKey=" + SERVICE_KEY +
                "&pageNo=" + PAGE_NO +
                "&numOfRows=" + NUM_OF_ROWS +
                "&MobileApp=" + MOBILE_APP +
                "&MobileOS=" + MOBILE_OS +
                "&arrange=" + ARRANGE +
                "&areaCode=" + AREA_CODE +
                "&sigunguCode=" + SIGUNGU_CODE+
                "&contentTypeId=" + contentTypeId +
                "&listYN=" + LIST_YN +
                "&keyword=" + query;
    }

    private int getContentTypeId(int position) {
        switch (position) {
            case 0: return 12; // 관광지
            case 1: return 14; // 문화시설
            case 2: return 15; // 축제공연행사
            case 3: return 25; // 여행코스
            case 4: return 28; // 레포츠
            case 5: return 32; // 숙박
            case 6: return 38; // 쇼핑
            case 7: return 39; // 음식점
            default: return 12; // 기본값 관광지
        }
    }

    private void fetchXML(String url, int contentTypeId) {
        class GetPlaceData extends AsyncTask<Void, Void, Void> {
            private String page;

            @Override
            protected Void doInBackground(Void... voids) {
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
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                ArrayList<TourApi> itemList = new ArrayList<>();

                boolean tagImage = false;
                boolean tagTitle = false;
                boolean tagAddr1 = false;
                boolean tagAddr2 = false;
                boolean tagcontentid = false;

                String firstimage = "";
                String title = "";
                String addr1 = "";
                String addr2 = "";
                String contentid = "";

                try {
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    XmlPullParser xpp = factory.newPullParser();
                    xpp.setInput(new StringReader(page));

                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG) {
                            String tagName = xpp.getName();
                            if (tagName.equals("firstimage")) tagImage = true;
                            if (tagName.equals("title")) tagTitle = true;
                            if (tagName.equals("addr1")) tagAddr1 = true;
                            if (tagName.equals("addr2")) tagAddr2 = true;
                            if (tagName.equals("contentid")) tagcontentid = true;
                        } else if (eventType == XmlPullParser.TEXT) {
                            if (tagImage) {
                                firstimage = xpp.getText();
                                tagImage = false;
                            }
                            if (tagTitle) {
                                title = xpp.getText();
                                tagTitle = false;
                            }
                            if (tagAddr1) {
                                addr1 = xpp.getText();
                                tagAddr1 = false;
                            }
                            if (tagAddr2) {
                                addr2 = xpp.getText();
                                tagAddr2 = false;
                            }
                            if (tagcontentid){
                                contentid = xpp.getText();
                                tagcontentid = false;
                            }
                        } else if (eventType == XmlPullParser.END_TAG) {
                            if (xpp.getName().equals("item")) {
                                if (firstimage.contains("http")) {
                                    TourApi item = new TourApi(firstimage, title, addr1, addr2, contentid);
                                    itemList.add(item);
                                }
                            }
                        }
                        eventType = xpp.next();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                tourAdapter = new TourAdapter(itemList);
                rcPlaceList.setAdapter(tourAdapter);
            }
        }

        new GetPlaceData().execute();
    }
}
