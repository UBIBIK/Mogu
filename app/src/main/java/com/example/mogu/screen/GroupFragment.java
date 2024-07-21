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
import com.example.mogu.object.JoinGroupRequest;
import com.example.mogu.object.UserInfo;
import com.example.mogu.retrofit.ApiService;
import com.example.mogu.retrofit.RetrofitClient;
import com.example.mogu.share.SharedPreferencesHelper;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupFragment extends Fragment {

    private static final String TAG = "GroupFragment"; // 로그 태그
    private GroupAdapter groupAdapter; // 그룹 리스트를 위한 어댑터
    private ApiService apiService; // API 호출을 위한 서비스
    private SharedPreferencesHelper sharedPreferencesHelper; // 사용자 정보 저장을 위한 헬퍼 클래스

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 프래그먼트의 레이아웃을 인플레이트하여 뷰 생성
        View view = inflater.inflate(R.layout.group, container, false);

        // RecyclerView 초기화
        RecyclerView groupRecyclerView = view.findViewById(R.id.groupRecyclerView);
        groupRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        groupAdapter = new GroupAdapter();
        groupRecyclerView.setAdapter(groupAdapter);

        // ApiService 및 SharedPreferencesHelper 초기화
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        sharedPreferencesHelper = new SharedPreferencesHelper(getContext());

        // FloatingActionButton 클릭 리스너 설정
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
        // 사용자 정보 로드
        loadSavedUserInfo();
    }

    // 저장된 사용자 정보를 로드하는 메서드
    private void loadSavedUserInfo() {
        UserInfo savedUserInfo = sharedPreferencesHelper.getUserInfo();
        if (savedUserInfo != null) {
            // 그룹 리스트 가져오기
            ArrayList<GroupInfo> groupList = savedUserInfo.getGroupList();
            if (groupList != null && !groupList.isEmpty()) {
                Log.d(TAG, "Group List size: " + groupList.size());
                for (GroupInfo group : groupList) {
                    Log.d(TAG, "Group Name: " + group.getGroupName());
                    Log.d(TAG, "Group Key: " + group.getGroupKey());
                }
                // 그룹 리스트 업데이트
                updateGroupList(savedUserInfo);
            } else {
                Log.d(TAG, "Group List is null or empty");
            }
        } else {
            Log.d(TAG, "No saved user info found");
        }
    }

    // 팝업을 표시하는 메서드
    private void showPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("그룹 관리")
                .setItems(new String[]{"그룹 생성", "그룹 참가"}, (dialog, which) -> {
                    if (which == 0) {
                        // 그룹 생성 다이얼로그 표시
                        showGroupNameInputDialog();
                    } else {
                        // 그룹 참가 다이얼로그 표시
                        showGroupKeyInputDialog();
                    }
                });
        builder.create().show();
    }

    // 그룹 이름 입력 다이얼로그를 표시하는 메서드
    private void showGroupNameInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("그룹 이름 입력");

        final EditText input = new EditText(getContext());
        builder.setView(input);

        builder.setPositiveButton("생성", (dialog, which) -> {
            String groupName = input.getText().toString().trim();
            if (!groupName.isEmpty()) {
                // 그룹 생성 요청
                createGroup(groupName);
            } else {
                Toast.makeText(getContext(), "그룹 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // 그룹 키 입력 다이얼로그를 표시하는 메서드
    private void showGroupKeyInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("그룹 키 입력");

        final EditText input = new EditText(getContext());
        builder.setView(input);

        builder.setPositiveButton("참가", (dialog, which) -> {
            String groupKey = input.getText().toString().trim();
            if (!groupKey.isEmpty()) {
                // 그룹 참가 요청
                joinGroup(groupKey);
            } else {
                Toast.makeText(getContext(), "그룹 키를 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // 그룹을 생성하는 메서드
    private void createGroup(String groupName) {
        UserInfo userInfo = sharedPreferencesHelper.getUserInfo();
        if (userInfo.getUserEmail().isEmpty()) {
            Toast.makeText(getContext(), "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        CreateGroupRequest request = new CreateGroupRequest(userInfo, groupName);

        apiService.createGroup(request).enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserInfo updatedUserInfo = response.body();
                    sharedPreferencesHelper.saveUserInfo(updatedUserInfo);

                    Log.d(TAG, "Email: " + updatedUserInfo.getUserEmail());
                    Log.d(TAG, "Name: " + updatedUserInfo.getUserName());
                    Log.d(TAG, "Phone Number: " + updatedUserInfo.getPhoneNumber());

                    ArrayList<GroupInfo> groupList = updatedUserInfo.getGroupList();
                    if (groupList != null && !groupList.isEmpty()) {
                        Log.d(TAG, "Group List size: " + groupList.size());
                        for (GroupInfo group : groupList) {
                            Log.d(TAG, "Group Name: " + group.getGroupName());
                            Log.d(TAG, "Group Key: " + group.getGroupKey());
                        }
                    } else {
                        Log.d(TAG, "Group List is null or empty");
                    }

                    // 그룹 리스트 업데이트
                    updateGroupList(updatedUserInfo);
                    Toast.makeText(getContext(), "그룹 생성 성공", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "그룹 생성 실패", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Response unsuccessful or body is null");
                }
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                Toast.makeText(getContext(), "그룹 생성 실패: 서버 오류", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "그룹 생성 실패", t);
            }
        });
    }

    // 그룹에 참가하는 메서드
    private void joinGroup(String groupKey) {
        UserInfo userInfo = sharedPreferencesHelper.getUserInfo();
        if (userInfo.getUserEmail().isEmpty()) {
            Toast.makeText(getContext(), "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        JoinGroupRequest request = new JoinGroupRequest(userInfo, groupKey);

        apiService.joinGroup(request).enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserInfo updatedUserInfo = response.body();
                    sharedPreferencesHelper.saveUserInfo(updatedUserInfo);

                    Log.d(TAG, "Email: " + updatedUserInfo.getUserEmail());
                    Log.d(TAG, "Name: " + updatedUserInfo.getUserName());
                    Log.d(TAG, "Phone Number: " + updatedUserInfo.getPhoneNumber());

                    ArrayList<GroupInfo> groupList = updatedUserInfo.getGroupList();
                    if (groupList != null && !groupList.isEmpty()) {
                        Log.d(TAG, "Group List size: " + groupList.size());
                        for (GroupInfo group : groupList) {
                            Log.d(TAG, "Group Name: " + group.getGroupName());
                            Log.d(TAG, "Group Key: " + group.getGroupKey());
                        }
                    } else {
                        Log.d(TAG, "Group List is null or empty");
                    }

                    // 그룹 리스트 업데이트
                    updateGroupList(updatedUserInfo);
                    Toast.makeText(getContext(), "그룹 참가 성공", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "그룹 참가 실패", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Response unsuccessful or body is null");
                }
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                Toast.makeText(getContext(), "그룹 참가 실패: 서버 오류", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "그룹 참가 실패", t);
            }
        });
    }

    // 그룹 리스트를 업데이트하는 메서드
    private void updateGroupList(UserInfo updatedUserInfo) {
        groupAdapter.updateGroupList(updatedUserInfo.getGroupList());
    }
}
