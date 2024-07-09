package com.example.mogu.custom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mogu.R;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    // 그룹 이름들을 저장하는 리스트
    private List<String> groupList = new ArrayList<>();

    // ViewHolder를 생성하는 메서드
    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 아이템 뷰를 레이아웃 인플레이터를 사용하여 생성
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(itemView);
    }

    // ViewHolder와 데이터를 바인딩하는 메서드
    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        // 현재 위치의 그룹 이름을 가져옴
        String groupName = groupList.get(position);
        // 그룹 이름을 TextView에 설정
        holder.groupNameTextView.setText(groupName);
    }

    // 아이템의 총 개수를 반환하는 메서드
    @Override
    public int getItemCount() {
        return groupList.size();
    }

    // 그룹 리스트를 설정하고 데이터 변경을 알리는 메서드
    public void setGroupList(List<String> groupList) {
        this.groupList = groupList;
        notifyDataSetChanged();
    }

    // ViewHolder 클래스 정의
    static class GroupViewHolder extends RecyclerView.ViewHolder {

        // 그룹 이름을 표시하는 TextView
        TextView groupNameTextView;

        // ViewHolder 생성자
        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            // TextView 초기화
            groupNameTextView = itemView.findViewById(R.id.groupNameTextView);
        }
    }
}
