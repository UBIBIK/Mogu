package com.example.mogu.screen;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mogu.R;
import com.example.mogu.custom.GroupAdapter;
import com.example.mogu.object.CreateGroupRequest;
import com.example.mogu.object.GroupInfo;
import com.example.mogu.object.UserInfo;
import com.example.mogu.retrofit.ApiService;
import com.example.mogu.retrofit.RetrofitClient;
import com.example.mogu.share.SharedPreferencesHelper;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupFragment extends Fragment {

    private static final String TAG = "GroupFragment";
    private RecyclerView groupRecyclerView;
    private GroupAdapter groupAdapter;
    private ApiService apiService;
    private SharedPreferencesHelper sharedPreferencesHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.group, container, false);

        groupRecyclerView = view.findViewById(R.id.groupRecyclerView);
        groupRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        groupAdapter = new GroupAdapter();
        groupRecyclerView.setAdapter(groupAdapter);

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        sharedPreferencesHelper = new SharedPreferencesHelper(getContext());

        Button fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadSavedUserInfo();
    }

    private void loadSavedUserInfo() {
        UserInfo savedUserInfo = sharedPreferencesHelper.getUserInfo();
        if (savedUserInfo != null) {
            ArrayList<GroupInfo> groupList = savedUserInfo.getGroupList();
            if (groupList != null && !groupList.isEmpty()) {

                Log.d("UserInfo", "Group List size: " + groupList.size());
                for (GroupInfo group : groupList) {
                    Log.d("UserInfo", "Group Name: " + group.getGroupName());
                    Log.d("UserInfo", "Group Key: " + group.getGroupKey());
                }

                updateGroupList(savedUserInfo);
            }

            else {
                Log.d("UserInfo", "Group List is null");
            }
        }
    }

    private void showPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("그룹 관리")
                .setItems(new String[]{"그룹 생성", "그룹 참가"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showGroupNameInputDialog();
                            break;
                        case 1:
                            Toast.makeText(getContext(), "그룹 참가 선택", Toast.LENGTH_SHORT).show();
                            break;
                    }
                });
        builder.create().show();
    }

    private void showGroupNameInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("그룹 이름 입력");

        // 그룹 이름 입력 필드 추가
        final EditText input = new EditText(getContext());
        builder.setView(input);

        builder.setPositiveButton("생성", (dialog, which) -> {
            String groupName = input.getText().toString();
            if (!groupName.isEmpty()) {
                createGroup(groupName);
            } else {
                Toast.makeText(getContext(), "그룹 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void createGroup(String groupName) {
        UserInfo userInfo = sharedPreferencesHelper.getUserInfo();
        if (userInfo.getUserEmail().isEmpty()) {
            Toast.makeText(getContext(), "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // UserInfo에 그룹 이름을 추가하는 새로운 클래스 작성 (CreateGroupRequest)
        CreateGroupRequest request = new CreateGroupRequest(userInfo, groupName);

        apiService.createGroup(request).enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                if (response.isSuccessful()) {
                    UserInfo updatedUserInfo = response.body();
                    sharedPreferencesHelper.saveUserInfo(updatedUserInfo);

                    // 서버에서 받아온 UserInfo 객체의 정보를 로그로 출력
                    Log.d("UserInfo", "Email: " + updatedUserInfo.getUserEmail());
                    Log.d("UserInfo", "Name: " + updatedUserInfo.getUserName());
                    Log.d("UserInfo", "Phone Number: " + updatedUserInfo.getPhoneNumber());

                    ArrayList<GroupInfo> groupList = updatedUserInfo.getGroupList();
                    if (groupList != null) {
                        Log.d("UserInfo", "Group List size: " + groupList.size());
                        for (GroupInfo group : groupList) {
                            Log.d("UserInfo", "Group Name: " + group.getGroupName());
                            Log.d("UserInfo", "Group Key: " + group.getGroupKey());
                        }
                    } else {
                        Log.d("UserInfo", "Group List is null");
                    }

                    // 그룹 리스트 업데이트
                    updateGroupList(updatedUserInfo);

                    Toast.makeText(getContext(), "그룹 생성 성공", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "그룹 생성 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                Toast.makeText(getContext(), "그룹 생성 실패: 서버 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateGroupList(UserInfo updatedUserInfo) {
        // 새로운 그룹 리스트를 GroupAdapter에 전달하여 업데이트
        groupAdapter.updateGroupList(updatedUserInfo.getGroupList());
    }
}
