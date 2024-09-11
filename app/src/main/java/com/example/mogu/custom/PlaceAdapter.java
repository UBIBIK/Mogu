package com.example.mogu.custom;

import static com.example.mogu.custom.MyAdapter.DETAIL_SERVICE_URL;
import static com.example.mogu.custom.MyAdapter.MOBILE_APP;
import static com.example.mogu.custom.MyAdapter.MOBILE_OS;
import static com.example.mogu.custom.MyAdapter.OVERVIEWYN;
import static com.example.mogu.custom.MyAdapter.SERVICE_KEY;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
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
import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.ViewHolder> {

    private final ArrayList<TourApi> items;
    private String placeName;
    private final String day;

    // 장소 선택 리스너 인터페이스
    public interface OnPlaceSelectedListener {
        void onPlaceSelected(TourApi tourApi);
    }

    private OnPlaceSelectedListener onPlaceSelectedListener;

    public void setOnPlaceSelectedListener(OnPlaceSelectedListener listener) {
        this.onPlaceSelectedListener = listener;
    }

    public PlaceAdapter(ArrayList<TourApi> items, Context context, String day) {
        this.items = items;
        this.day = day;
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
        private final ImageView imgFirstImage;
        private final TextView txtTitle;
        private final TextView txtAddr1;
        private final TextView txtAddr2;

        public ViewHolder(View itemView) {
            super(itemView);
            imgFirstImage = itemView.findViewById(R.id.imgFirstImage);
            txtTitle = itemView.findViewById(R.id.tvTitle);
            txtAddr1 = itemView.findViewById(R.id.tvAddr1);
            txtAddr2 = itemView.findViewById(R.id.tvAddr2);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onPlaceSelectedListener != null) {
                    onPlaceSelectedListener.onPlaceSelected(items.get(position));
                }
            });

            imgFirstImage.setOnClickListener(v -> {
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

                placeName = item.getTitle();
                textPlaceName.setText(placeName);

                AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                        .setView(popupView)
                        .setPositiveButton("닫기", (dialog1, which) -> dialog1.dismiss())
                        .show();

                String contentId = item.getContentid();
                String requestUrl = DETAIL_SERVICE_URL +
                        "?serviceKey=" + SERVICE_KEY +
                        "&contentId=" + contentId +
                        "&MobileApp=" + MOBILE_APP +
                        "&MobileOS=" + MOBILE_OS +
                        "&overviewYN=" + OVERVIEWYN;

                fetchOverviewData(requestUrl, popupSummary);

                btnAddPlace.setOnClickListener(v1 -> {
                    try {
                        double latitude = item.getMapy();
                        double longitude = item.getMapx();

                        LatLng placeLatLng = new LatLng(latitude, longitude);

                        PlaceData placeData = new PlaceData();
                        placeData.addPlace(placeName, placeLatLng, "", ""); // 빈 노트를 기본값으로 설정

                        Log.d("PlaceAdapter", "날짜: " + day);
                        Log.d("PlaceAdapter", "장소 이름: " + placeName);
                        Log.d("PlaceAdapter", "위도: " + latitude + ", 경도: " + longitude);

                        if (onPlaceSelectedListener != null) {
                            onPlaceSelectedListener.onPlaceSelected(item);
                        }

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
            @SuppressLint("StaticFieldLeak")
            class FetchOverviewData extends AsyncTask<Void, Void, String> {
                private String page;

                @Override
                protected String doInBackground(Void... voids) {
                    try {
                        URL apiUrl = new URL(url);
                        BufferedReader bufReader = new BufferedReader(new InputStreamReader(apiUrl.openStream(), StandardCharsets.UTF_8));

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

                        overview = overview.replaceAll("<br\\s*/?>", "\n");

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
