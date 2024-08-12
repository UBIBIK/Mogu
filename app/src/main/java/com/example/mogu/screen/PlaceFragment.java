package com.example.mogu.screen;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import java.net.URLEncoder;
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

    private static final String TAG = "PlaceFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place_search, container, false);

        rcPlaceList = view.findViewById(R.id.recyclerViewPlaces);
        rcPlaceList.setLayoutManager(new LinearLayoutManager(getContext()));

        etSearch = view.findViewById(R.id.etSearch);
        spCategory = view.findViewById(R.id.spinnerCategory);

        // 스피너 어댑터 설정
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.category_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);

        // 기본 카테고리 (전체) 설정
        contentTypeId = 0; // 전체 카테고리의 ID 설정
        spCategory.setSelection(0); // '전체'를 기본 선택 항목으로 설정

        // 카테고리 변경 시
        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                contentTypeId = getContentTypeId(position);
                fetchData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // 기본 카테고리 유지
            }
        });

        // 검색 버튼 클릭 시
        Button btnSearch = view.findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v -> fetchData());

        return view;
    }

    private String buildRequestUrl(int contentTypeId) {
        StringBuilder urlBuilder = new StringBuilder(SERVICE_URL)
                .append("?serviceKey=").append(SERVICE_KEY)
                .append("&pageNo=").append(PAGE_NO)
                .append("&numOfRows=").append(NUM_OF_ROWS)
                .append("&MobileApp=").append(MOBILE_APP)
                .append("&MobileOS=").append(MOBILE_OS)
                .append("&arrange=").append(ARRANGE)
                .append("&areaCode=").append(AREA_CODE)
                .append("&sigunguCode=").append(SIGUNGU_CODE)
                .append("&listYN=").append(LIST_YN);

        if (contentTypeId != 0) {
            urlBuilder.append("&contentTypeId=").append(contentTypeId);
        }

        return urlBuilder.toString();
    }

    private int getContentTypeId(int position) {
        switch (position) {
            case 0: return 0; // 전체
            case 1: return 12; // 관광지
            case 2: return 14; // 문화시설
            case 3: return 15; // 축제공연행사
            case 4: return 25; // 여행코스
            case 5: return 28; // 레포츠
            case 6: return 32; // 숙박
            case 7: return 38; // 쇼핑
            case 8: return 39; // 음식점
            default: return 0; // 기본값 전체
        }
    }

    private void fetchData() {
        String query = etSearch.getText().toString().trim();
        final String requestUrl = buildRequestUrl(contentTypeId);
        Log.d(TAG, "Request URL: " + requestUrl);
        Log.d(TAG, "Search Query: " + query);
        new FetchPlaceDataTask(requestUrl, query).execute();
    }

    private class FetchPlaceDataTask extends AsyncTask<Void, Void, ArrayList<TourApi>> {
        private String url;
        private String searchQuery;

        public FetchPlaceDataTask(String url, String searchQuery) {
            this.url = url;
            this.searchQuery = searchQuery.trim(); // 공백 제거
        }

        @Override
        protected ArrayList<TourApi> doInBackground(Void... voids) {
            ArrayList<TourApi> itemList = new ArrayList<>();
            try {
                URL apiUrl = new URL(url);
                BufferedReader bufReader = new BufferedReader(new InputStreamReader(apiUrl.openStream(), "UTF-8"));

                StringBuilder page = new StringBuilder();
                String line;
                while ((line = bufReader.readLine()) != null) {
                    page.append(line);
                }
                bufReader.close();

                Log.d(TAG, "API Response: " + page.toString());

                boolean tagImage = false;
                boolean tagTitle = false;
                boolean tagAddr1 = false;
                boolean tagAddr2 = false;
                boolean tagContentid = false;

                String firstimage = "";
                String title = "";
                String addr1 = "";
                String addr2 = "";
                String contentid = "";

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new StringReader(page.toString()));

                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        String tagName = xpp.getName();
                        if (tagName.equals("firstimage")) tagImage = true;
                        if (tagName.equals("title")) tagTitle = true;
                        if (tagName.equals("addr1")) tagAddr1 = true;
                        if (tagName.equals("addr2")) tagAddr2 = true;
                        if (tagName.equals("contentid")) tagContentid = true;
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
                        if (tagContentid) {
                            contentid = xpp.getText();
                            tagContentid = false;
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        if (xpp.getName().equals("item")) {
                            Log.d(TAG, "Item Title: " + title);
                            if (firstimage.contains("http")) {
                                boolean isMatch = searchQuery.isEmpty() || title.toLowerCase().contains(searchQuery.toLowerCase());
                                if (isMatch) {
                                    TourApi item = new TourApi(firstimage, title, addr1, addr2, contentid);
                                    itemList.add(item);
                                    Log.d(TAG, "Included Item: " + title);
                                } else {
                                    Log.d(TAG, "Excluded Item: " + title);
                                }
                                Log.d(TAG, "Search Query: " + searchQuery);
                            }
                        }
                    }
                    eventType = xpp.next();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error: ", e);
            }
            return itemList;
        }

        @Override
        protected void onPostExecute(ArrayList<TourApi> result) {
            super.onPostExecute(result);
            tourAdapter = new TourAdapter(result);
            rcPlaceList.setAdapter(tourAdapter);
            Log.d(TAG, "Result List Size: " + result.size());
        }
    }
}
