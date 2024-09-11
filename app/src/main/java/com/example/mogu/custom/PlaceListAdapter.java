package com.example.mogu.custom;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mogu.R;
import com.example.mogu.object.PlaceData;
import com.example.mogu.screen.FindDestination;
import com.example.mogu.share.LocationPreference;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class PlaceListAdapter extends RecyclerView.Adapter<PlaceListAdapter.ViewHolder> {

    // 장소 이름과 메모, Day 정보를 저장하는 리스트들
    private List<String> placeNames;  // 장소 이름 목록
    private List<String> notes;  // 메모 목록
    private String day;  // Day 정보
    private List<LatLng> locations;  // 장소 위치 목록
    private Context context; // Context를 저장

    // 생성자: 어댑터에 필요한 데이터들을 초기화
    public PlaceListAdapter(Context context,List<String> placeNames, List<String> notes, String day, List<LatLng> locations) {
        this.context = context;
        this.placeNames = placeNames;
        this.notes = notes;
        this.day = day;
        this.locations = locations;
    }

    @NonNull
    @Override
    // ViewHolder 생성: list_place_item_home 레이아웃을 기반으로 새로운 ViewHolder를 생성
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_place_item_home, parent, false);
        return new ViewHolder(view);
    }

    @Override
    // ViewHolder에 데이터를 바인딩: 해당 위치의 장소 이름, 메모, Day 정보를 설정하고 길찾기 버튼의 클릭 리스너를 설정
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.placeNameTextView.setText(placeNames.get(position));  // 장소 이름 설정
        holder.noteTextView.setText(notes.get(position));  // 메모 설정
        holder.dayTextView.setText(day);  // Day 정보 설정

        // 길찾기 버튼의 클릭 리스너 설정
        holder.routeButton.setOnClickListener(v ->handleRouteButtonClick(position));
    }

    private void handleRouteButtonClick(int position) {
        // 선택된 장소의 위치가 null이 아닌지 확인
        LatLng destinationLocation = locations.get(position);

        if (destinationLocation == null) {
            Log.e("PlaceListAdapter", "Destination location is missing for position: " + position);
            return;
        }

        // LocationPreference에서 현재 위치 가져오기
        LocationPreference locationPreference = new LocationPreference(context);
        double currentLat = locationPreference.getLatitude();
        double currentLng = locationPreference.getLongitude();

        if (currentLat == 0.0 || currentLng == 0.0) {
            Log.e("PlaceListAdapter", "Current location is missing.");
            return;
        }

        // 길찾기 화면으로 이동
        Intent intent = new Intent(context, FindDestination.class);
        intent.putExtra("currentLat", currentLat);
        intent.putExtra("currentLng", currentLng);
        intent.putExtra("destinationLat", destinationLocation.latitude);
        intent.putExtra("destinationLng", destinationLocation.longitude);
        context.startActivity(intent);
    }

    @Override
    // RecyclerView에 표시할 아이템의 수를 반환
    public int getItemCount() {
        return placeNames.size();  // 장소 이름 리스트의 크기를 반환
    }

    // ViewHolder 클래스: 각 리스트 아이템의 뷰를 관리
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView placeNameTextView;  // 장소 이름을 표시하는 TextView
        TextView noteTextView;  // 메모를 표시하는 TextView
        TextView dayTextView;  // Day 정보를 표시하는 TextView
        Button routeButton;  // 길찾기 버튼

        // ViewHolder 생성자: itemView에서 뷰들을 찾아 초기화
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            placeNameTextView = itemView.findViewById(R.id.placeNameTextView);
            noteTextView = itemView.findViewById(R.id.noteTextView);
            dayTextView = itemView.findViewById(R.id.dayTextView);
            routeButton = itemView.findViewById(R.id.routeButton);
        }
    }
}
