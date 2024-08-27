package com.example.mogu.screen;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mogu.R;
import com.example.mogu.object.TourApi;
import com.example.mogu.custom.MyAdapter;
import com.example.mogu.custom.TourAdapter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator3;

public class HomeFragment extends Fragment {

    // API 요청에 사용할 상수들
    private static final int NUM_OF_ROWS = 10; // 한 페이지당 항목 수
    private static final int PAGE_NO = 1; // 페이지 번호
    private static final String MOBILE_OS = "AND"; // 모바일 운영 체제
    private static final String MOBILE_APP = "AppTest"; // 모바일 애플리케이션 이름
    private static final String LIST_YN = "Y"; // 목록 여부
    private static final String ARRANGE = "A"; // 정렬 방식
    private static final int CONTENT_TYPE_ID = 15; // 첫 번째 콘텐츠 타입 ID (예: 축제공연행사)
    private static final int CONTENT_TYPE_ID2 = 12; // 두 번째 콘텐츠 타입 ID (예: 관광지)
    private static final int AREA_CODE = 38; // 지역 코드
    private static final int SIGUNGU_CODE = 8; // 시군구 코드
    private static final String OVERVIEWYN = "Y"; // 개요 여부
    private static final String SERVICE_URL = "http://apis.data.go.kr/B551011/KorService1/areaBasedList1"; // API 서비스 URL
    private static final String SERVICE_KEY = "iYq%2FBTYJSMKmITGfxxEBnluf6wJSfDjyGv8HUQJCYnqLkGKt%2BGTq4mNkwGDB5gEofiE34ur%2Fen1s7Nq1xWuLeg%3D%3D"; // API 서비스 키

    private ViewPager2 viewPager; // ViewPager2 객체 (슬라이딩 뷰)
    private MyAdapter myAdapter; // ViewPager2에 사용할 어댑터
    private TourAdapter tourAdapter; // RecyclerView에 사용할 어댑터
    private CircleIndicator3 indicator; // ViewPager2 페이지 인디케이터
    private RecyclerView rcTourList; // RecyclerView 객체 (리스트 뷰)

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 프래그먼트의 레이아웃을 인플레이트
        View view = inflater.inflate(R.layout.home, container, false);

        // ViewPager2, CircleIndicator3, RecyclerView 초기화
        viewPager = view.findViewById(R.id.viewpager);
        indicator = view.findViewById(R.id.indicator);
        rcTourList = view.findViewById(R.id.recyclerView);
        rcTourList.setLayoutManager(new LinearLayoutManager(getContext())); // RecyclerView에 LinearLayoutManager 설정

        // API 요청 URL을 생성
        String requestUrl1 = SERVICE_URL +
                "?serviceKey=" + SERVICE_KEY +
                "&pageNo=" + PAGE_NO +
                "&numOfRows=" + NUM_OF_ROWS +
                "&MobileApp=" + MOBILE_APP +
                "&MobileOS=" + MOBILE_OS +
                "&arrange=" + ARRANGE +
                "&contentTypeId=" + CONTENT_TYPE_ID +
                "&areaCode=" + AREA_CODE +
                "&sigunguCode=" + SIGUNGU_CODE +
                "&listYN=" + LIST_YN;

        String requestUrl2 = SERVICE_URL +
                "?serviceKey=" + SERVICE_KEY +
                "&pageNo=" + PAGE_NO +
                "&numOfRows=" + NUM_OF_ROWS +
                "&MobileApp=" + MOBILE_APP +
                "&MobileOS=" + MOBILE_OS +
                "&arrange=" + ARRANGE +
                "&contentTypeId=" + CONTENT_TYPE_ID2 +
                "&areaCode=" + AREA_CODE +
                "&sigunguCode=" + SIGUNGU_CODE +
                "&listYN=" + LIST_YN;

        // 두 개의 API 요청을 통해 데이터를 가져옴
        fetchXML(requestUrl1, CONTENT_TYPE_ID);
        fetchXML(requestUrl2, CONTENT_TYPE_ID2);

        return view;
    }

    // API로부터 XML 데이터를 가져오는 메서드
    private void fetchXML(String url, int contentTypeId) {
        class GetDangerGrade extends AsyncTask<Void, Void, Void> {
            private String page; // XML 데이터 문자열

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    // URL 객체를 생성하고 연결을 열어 XML 데이터 읽기
                    URL apiUrl = new URL(url);
                    BufferedReader bufReader = new BufferedReader(new InputStreamReader(apiUrl.openStream(), "UTF-8"));

                    page = "";
                    String line;
                    while ((line = bufReader.readLine()) != null) {
                        page += line;
                    }
                    bufReader.close();
                } catch (Exception e) {
                    e.printStackTrace(); // 예외 처리
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                ArrayList<TourApi> itemList = new ArrayList<>(); // TourApi 아이템을 담을 리스트

                // XML 태그 처리 플래그
                boolean tagImage = false;
                boolean tagTitle = false;
                boolean tagAddr1 = false;
                boolean tagAddr2 = false;
                boolean tagcontentid = false;

                // XML 데이터 저장 변수
                String firstimage = "";
                String title = "";
                String addr1 = "";
                String addr2 = "";
                String contentid = "";

                try {
                    // XML 파서를 초기화
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
                                // 첫 번째 이미지 URL이 포함된 경우에만 아이템 추가
                                if (firstimage.contains("http")) {
                                    TourApi item = new TourApi(firstimage, title, addr1, addr2, contentid,0,0);
                                    itemList.add(item);
                                }
                            }
                        }
                        eventType = xpp.next();
                    }
                } catch (Exception e) {
                    e.printStackTrace(); // 예외 처리
                }

                // 데이터에 따라 어댑터 설정
                if (contentTypeId == CONTENT_TYPE_ID) {
                    myAdapter = new MyAdapter(itemList);
                    viewPager.setAdapter(myAdapter); // ViewPager2에 어댑터 설정
                    indicator.setViewPager(viewPager); // CircleIndicator3에 ViewPager2 설정
                } else if (contentTypeId == CONTENT_TYPE_ID2) {
                    tourAdapter = new TourAdapter(itemList);
                    rcTourList.setAdapter(tourAdapter); // RecyclerView에 어댑터 설정
                }
            }
        }

        new GetDangerGrade().execute(); // AsyncTask 실행
    }
}
