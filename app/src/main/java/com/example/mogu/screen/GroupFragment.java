package com.example.mogu.screen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mogu.R;
import com.example.mogu.custom.GroupAdapter;
import com.example.mogu.object.UserInfo;
import com.example.mogu.retrofit.ApiService;
import com.example.mogu.retrofit.RetrofitClient;
import com.example.mogu.share.SharedPreferencesHelper;

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

    private void showPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("그룹 관리")
                .setItems(new String[]{"그룹 생성", "그룹 참가"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            createGroup();
                            break;
                        case 1:
                            Toast.makeText(getContext(), "그룹 참가 선택", Toast.LENGTH_SHORT).show();
                            break;
                    }
                });
        builder.create().show();
    }

    private void createGroup() {
        UserInfo userInfo = sharedPreferencesHelper.getUserInfo();
        if (userInfo.getUserEmail().isEmpty()) {
            Toast.makeText(getContext(), "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.createGroup(userInfo).enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                if (response.isSuccessful()) {
                    UserInfo updatedUserInfo = response.body();
                    sharedPreferencesHelper.saveUserInfo(updatedUserInfo);
                    Toast.makeText(getContext(), "그룹 생성 성공", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "그룹 생성 실패: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                Toast.makeText(getContext(), "그룹 생성 실패: 서버 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
