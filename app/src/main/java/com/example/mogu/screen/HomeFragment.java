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

    private static final int NUM_OF_ROWS = 10;
    private static final int PAGE_NO = 1;
    private static final String MOBILE_OS = "AND";
    private static final String MOBILE_APP = "AppTest";
    private static final String LIST_YN = "Y";
    private static final String ARRANGE = "A";
    private static final int CONTENT_TYPE_ID = 15;
    private static final int CONTENT_TYPE_ID2 = 12;
    private static final int AREA_CODE = 38;
    private static final int SIGUNGU_CODE = 8;
    private static final String SERVICE_URL = "http://api.visitkorea.or.kr/openapi/service/rest/KorService/areaBasedList";
    private static final String SERVICE_KEY = "iYq%2FBTYJSMKmITGfxxEBnluf6wJSfDjyGv8HUQJCYnqLkGKt%2BGTq4mNkwGDB5gEofiE34ur%2Fen1s7Nq1xWuLeg%3D%3D";

    private ViewPager2 viewPager;
    private MyAdapter myAdapter;
    private TourAdapter tourAdapter;
    private CircleIndicator3 indicator;
    private RecyclerView rcTourList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home, container, false);

        viewPager = view.findViewById(R.id.viewpager);
        indicator = view.findViewById(R.id.indicator);
        rcTourList = view.findViewById(R.id.recyclerView);
        rcTourList.setLayoutManager(new LinearLayoutManager(getContext()));

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

        fetchXML(requestUrl1, CONTENT_TYPE_ID);
        fetchXML(requestUrl2, CONTENT_TYPE_ID2);

        return view;
    }

    private void fetchXML(String url, int contentTypeId) {
        class GetDangerGrade extends AsyncTask<Void, Void, Void> {
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
                            if (tagAddr1) {
                                addr1 = xpp.getText();
                                tagAddr1 = false;
                            }
                            if (tagAddr2) {
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

                if (contentTypeId == CONTENT_TYPE_ID) {
                    myAdapter = new MyAdapter(itemList);
                    viewPager.setAdapter(myAdapter);
                    indicator.setViewPager(viewPager);
                } else if (contentTypeId == CONTENT_TYPE_ID2) {
                    tourAdapter = new TourAdapter(itemList);
                    rcTourList.setAdapter(tourAdapter);
                }
            }
        }

        new GetDangerGrade().execute();
    }
}
