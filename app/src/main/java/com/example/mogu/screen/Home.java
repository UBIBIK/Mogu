package com.example.mogu.screen;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
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

public class Home extends AppCompatActivity {

    // Api 요소 설정
    private static final int NUM_OF_ROWS = 10;
    private static final int PAGE_NO = 1;
    private static final String MOBILE_OS = "AND";
    private static final String MOBILE_APP = "AppTest";
    private static final String LIST_YN = "Y";
    private static final String ARRANGE = "A";
    private static final int CONTENT_TYPE_ID = 15;
    private static final int CONTENT_TYPE_ID2 = 12;
    private static final int AREA_CODE = 38;
    private static final int SIGUNGU_CODE = 8 ;
    private static final String SERVICE_URL = "http://api.visitkorea.or.kr/openapi/service/rest/KorService/areaBasedList";
    private static final String SERVICE_KEY = "iYq%2FBTYJSMKmITGfxxEBnluf6wJSfDjyGv8HUQJCYnqLkGKt%2BGTq4mNkwGDB5gEofiE34ur%2Fen1s7Nq1xWuLeg%3D%3D";
    private ViewPager2 viewPager;
    private MyAdapter myadapter;
    private TourAdapter tourAdapter;
    private CircleIndicator3 indicator;
    private static final String TAG = "Home";
    private RecyclerView rcTourList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        viewPager = findViewById(R.id.viewpager); // 배너용 viewPager
        indicator = findViewById(R.id.indicator);

        rcTourList = findViewById(R.id.recyclerView);
        rcTourList.setLayoutManager(new LinearLayoutManager(this));


        String[] contentTypes = {String.valueOf(CONTENT_TYPE_ID), String.valueOf(CONTENT_TYPE_ID2)};
        // Api키 요청 항목
        String requestUrl = SERVICE_URL +
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


        Button boardButton = findViewById(R.id.BoardButton);
        Button groupButton = findViewById(R.id.GroupButton);
        Button homeButton = findViewById(R.id.HomeButton);
        Button mapButton = findViewById(R.id.MapButton);
        Button mypageButton = findViewById(R.id.MypageButton);

        boardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Home.class);
                startActivity(intent);

            }
        });


        groupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Home.class);
                startActivity(intent);
            }
        });


        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Home.class);
                startActivity(intent);
            }
        });


        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Home.class);
                startActivity(intent);
            }
        });


        mypageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Home.class);
                startActivity(intent);
            }
        });


        fetchXML(requestUrl,CONTENT_TYPE_ID);
        fetchXML(requestUrl2,CONTENT_TYPE_ID2);

    }



    // xml 파싱
    private void fetchXML(String url, int num) {
        class GetDangerGrade extends AsyncTask<Void, Void, Void> {
            private String page;

            // xml 데이터 가져와서 파싱
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


            // 읽은 데이터 파싱해서 출력
            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                ArrayList<TourApi> itemList = new ArrayList<>();

                boolean tagImage = false;
                boolean tagTitle = false;
                boolean tagAddr1 = false;
                boolean tagAddr2 = false;

                String firstimage = "";
                String title = "";
                String addr1 = "";
                String addr2 = "";

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
                        } else if (eventType == XmlPullParser.TEXT) {
                            if (tagImage) {
                                firstimage = xpp.getText();
                                tagImage = false;
                            }
                            if (tagTitle) {
                                title = xpp.getText();
                                tagTitle = false;
                            }
                            if (tagAddr1){
                                addr1 = xpp.getText();
                                tagAddr1 = false;
                            }
                            if (tagAddr2){
                                addr2 = xpp.getText();
                                tagAddr2 = false;
                            }

                        } else if (eventType == XmlPullParser.END_TAG) {
                            if (xpp.getName().equals("item")) {
                                if (firstimage.contains("http")) {
                                    TourApi item = new TourApi(firstimage, title, addr1, addr2);
                                    itemList.add(item);
                                }
                            }
                        }
                        eventType = xpp.next();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }



                if(num == 15){
                    myadapter = new MyAdapter(itemList);
                    viewPager.setAdapter(myadapter);
                    indicator.setViewPager(viewPager);
                }
                else if(num == 12) {
                    tourAdapter = new TourAdapter(itemList);
                    rcTourList.setAdapter(tourAdapter);
                }
            }
        }

        new GetDangerGrade().execute();
    }
}
