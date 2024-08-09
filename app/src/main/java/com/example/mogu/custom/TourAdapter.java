package com.example.mogu.custom;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.text.Html;
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
import com.example.mogu.object.TourApi;
import com.example.mogu.screen.MapActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;

public class TourAdapter extends RecyclerView.Adapter<TourAdapter.ViewHolder> {
    private ArrayList<TourApi> items;
    private static final String DETAIL_SERVICE_URL = "http://apis.data.go.kr/B551011/KorService1/detailCommon1";
    private static final String SERVICE_KEY = "iYq%2FBTYJSMKmITGfxxEBnluf6wJSfDjyGv8HUQJCYnqLkGKt%2BGTq4mNkwGDB5gEofiE34ur%2Fen1s7Nq1xWuLeg%3D%3D";
    private static final String MOBILE_OS = "AND";
    private static final String MOBILE_APP = "AppTest";
    private static final String OVERVIEWYN = "Y";

    public TourAdapter(ArrayList<TourApi> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public TourAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_tourapi, parent, false);
        return new TourAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TourAdapter.ViewHolder holder, int position) {
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

        public ViewHolder(View itemView) {
            super(itemView);
            imgFirstImage = itemView.findViewById(R.id.imgFirstImage);
            txtTitle = itemView.findViewById(R.id.tvTitle);
            txtAddr1 = itemView.findViewById(R.id.tvAddr1);
            txtAddr2 = itemView.findViewById(R.id.tvAddr2);

            imgFirstImage.setOnClickListener(v -> {
                // 팝업창 띄우기
                View popupView = LayoutInflater.from(v.getContext()).inflate(R.layout.popup_image, null);
                ImageView popupImage = popupView.findViewById(R.id.popupImage);
                TextView popupSummary = popupView.findViewById(R.id.popupSummary);
                TextView textPlaceName = popupView.findViewById(R.id.textPlaceName);
                Button btnAddPlace = popupView.findViewById(R.id.btnAddPlace);

                TourApi item = items.get(getAdapterPosition());
                Glide.with(popupImage.getContext())
                        .load(item.getFirstimage())
                        .error(R.drawable.ic_launcher_foreground)
                        .into(popupImage);

                // 장소 이름 설정
                String placeName = item.getTitle();
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
                    Intent intent = new Intent(v.getContext(), MapActivity.class);
                    intent.putExtra("PLACE_NAME", placeName);
                    v.getContext().startActivity(intent);
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
                        // TextView에 텍스트 설정
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
