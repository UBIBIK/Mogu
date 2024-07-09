package com.example.mogu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private List<String> groupList = new ArrayList<>();

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        String groupName = groupList.get(position);
        holder.groupNameTextView.setText(groupName);
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public void setGroupList(List<String> groupList) {
        this.groupList = groupList;
        notifyDataSetChanged();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {

        TextView groupNameTextView;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupNameTextView = itemView.findViewById(R.id.groupNameTextView);
        }
    }
}
