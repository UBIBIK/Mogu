package com.example.mogu.custom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mogu.R;
import com.example.mogu.object.GroupInfo;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    private List<GroupInfo> groupList = new ArrayList<>();

    public void updateGroupList(List<GroupInfo> newGroupList) {
        groupList.clear();
        groupList.addAll(newGroupList);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        GroupInfo group = groupList.get(position);
        holder.groupName.setText(group.getGroupName());
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView groupName;

        ViewHolder(View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.group_name);
        }
    }
}
