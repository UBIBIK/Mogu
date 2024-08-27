package com.example.mogu.custom;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mogu.R;
import com.example.mogu.object.PlaceData;

import java.util.List;

public class PlaceListAdapter extends RecyclerView.Adapter<PlaceListAdapter.ViewHolder> {

    // 장소 이름과 메모, Day 정보를 저장하는 리스트들
    private List<String> placeNames;  // 장소 이름 목록
    private List<String> notes;  // 메모 목록
    private String day;  // Day 정보

    // 생성자: 어댑터에 필요한 데이터들을 초기화
    public PlaceListAdapter(List<String> placeNames, List<String> notes, String day) {
        this.placeNames = placeNames;
        this.notes = notes;
        this.day = day;
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

        holder.routeButton.setOnClickListener(v -> {
            // TODO: 길찾기 버튼

        });
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
