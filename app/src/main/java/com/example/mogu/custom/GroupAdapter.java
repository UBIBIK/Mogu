package com.example.mogu.custom;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mogu.R;
import com.example.mogu.object.GroupInfo;
import com.example.mogu.object.GroupMember;
import com.example.mogu.screen.CalendarActivity;
import com.example.mogu.screen.GroupFragment;
import com.example.mogu.object.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    private List<GroupInfo> groupList = new ArrayList<>();
    private GroupFragment fragment; // GroupFragment 참조 추가

    public GroupAdapter(GroupFragment fragment) {
        this.fragment = fragment;
    }

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
        holder.bind(group);
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView groupName;
        LinearLayout expandedView;
        LinearLayout groupMembersList;
        Button btnCopyInviteCode;
        Button btnDeleteGroup;
        Button btnClose;
        Button btnManageSchedule;

        ViewHolder(View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.group_name);
            expandedView = itemView.findViewById(R.id.expanded_view);
            groupMembersList = itemView.findViewById(R.id.group_members_list);
            btnCopyInviteCode = itemView.findViewById(R.id.btn_copy_invite_code);
            btnDeleteGroup = itemView.findViewById(R.id.btn_delete_group);
            btnClose = itemView.findViewById(R.id.btn_close);
            btnManageSchedule = itemView.findViewById(R.id.btn_manage_schedule);
        }

        void bind(GroupInfo group) {
            boolean isGroupExpanded = group.isExpanded();
            expandedView.setVisibility(isGroupExpanded ? View.VISIBLE : View.GONE);

            groupName.setOnClickListener(v -> {
                group.setExpanded(!group.isExpanded());
                notifyItemChanged(getAdapterPosition());
            });

            groupMembersList.removeAllViews();
            boolean isLeader = isGroupLeader(group);
            UserInfo userInfo = fragment.getSharedPreferencesHelper().getUserInfo();
            for (GroupMember member : group.getGroupMember()) {
                View memberView = LayoutInflater.from(itemView.getContext()).inflate(R.layout.group_member_item, groupMembersList, false);
                TextView memberNameView = memberView.findViewById(R.id.member_name);
                Button btnDeleteMember = memberView.findViewById(R.id.btn_delete_member);

                memberNameView.setText(member.getMemberName());

                if (isLeader && !member.getMemberEmail().equals(userInfo.getUserEmail())) {
                    btnDeleteMember.setVisibility(View.VISIBLE);
                    btnDeleteMember.setOnClickListener(v -> showDeleteMemberConfirmationDialog(group, member));
                } else {
                    btnDeleteMember.setVisibility(View.GONE);
                }

                groupMembersList.addView(memberView);
            }

            btnCopyInviteCode.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("초대코드 복사", group.getGroupKey());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(itemView.getContext(), "초대코드가 복사되었습니다.", Toast.LENGTH_SHORT).show();
            });

            btnClose.setOnClickListener(v -> {
                group.setExpanded(false);
                notifyItemChanged(getAdapterPosition());
            });

            // 그룹 삭제 버튼을 그룹장에게만 표시
            if (isLeader) {
                btnDeleteGroup.setVisibility(View.VISIBLE);
                btnDeleteGroup.setOnClickListener(v -> showDeleteGroupConfirmationDialog(group));
            } else {
                btnDeleteGroup.setVisibility(View.GONE);
            }

            // 그룹이 확장되었을 때 일정관리 버튼을 표시
            if (isGroupExpanded) {
                btnManageSchedule.setVisibility(View.VISIBLE);
                btnManageSchedule.setOnClickListener(v -> {
                    // 일정관리 화면으로 이동
                    Intent intent = new Intent(fragment.getContext(), CalendarActivity.class);
                    intent.putExtra("group_key", group.getGroupKey());
                    fragment.startActivity(intent);
                });
            } else {
                btnManageSchedule.setVisibility(View.GONE);
            }
        }

        private void showDeleteMemberConfirmationDialog(GroupInfo group, GroupMember member) {
            new AlertDialog.Builder(itemView.getContext())
                    .setTitle("멤버 삭제")
                    .setMessage("멤버를 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> fragment.deleteGroupMember(group, member))
                    .setNegativeButton("취소", null)
                    .show();
        }

        private void showDeleteGroupConfirmationDialog(GroupInfo group) {
            new AlertDialog.Builder(itemView.getContext())
                    .setTitle("그룹 삭제")
                    .setMessage("그룹을 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> fragment.deleteGroup(group))
                    .setNegativeButton("취소", null)
                    .show();
        }

        private boolean isGroupLeader(GroupInfo group) {
            UserInfo userInfo = fragment.getSharedPreferencesHelper().getUserInfo();
            return userInfo.getUserEmail().equals(group.getGmEmail());
        }
    }
}
