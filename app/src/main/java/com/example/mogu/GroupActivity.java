package com.example.mogu;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupActivity extends AppCompatActivity {

    private static final String TAG = "GroupActivity";
    private RecyclerView groupRecyclerView;
    private GroupAdapter groupAdapter;
    private ApiService apiService;
    private SharedPreferencesHelper sharedPreferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group);

        groupRecyclerView = findViewById(R.id.groupRecyclerView);
        groupRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupAdapter = new GroupAdapter();
        groupRecyclerView.setAdapter(groupAdapter);

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        sharedPreferencesHelper = new SharedPreferencesHelper(this);

        Button fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup();
            }
        });
    }

    private void showPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("그룹 관리")
                .setItems(new String[]{"그룹 생성", "그룹 참가"}, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                // 그룹 생성
                                createGroup();
                                break;
                            case 1:
                                // 그룹 참가
                                Log.d(TAG, "그룹 참가 선택");
                                break;
                        }
                    }
                });
        builder.create().show();
    }

    private void createGroup() {
        UserInfo userInfo = sharedPreferencesHelper.getUserInfo();
        if (userInfo.getUserEmail().isEmpty()) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.createGroup(userInfo).enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                if (response.isSuccessful()) {
                    UserInfo updatedUserInfo = response.body();
                    if (updatedUserInfo != null) {
                        sharedPreferencesHelper.saveUserInfo(updatedUserInfo);
                        Toast.makeText(GroupActivity.this, "그룹 생성 성공: " + updatedUserInfo.getUserName(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "그룹 생성 성공: " + updatedUserInfo);
                    }
                } else {
                    Toast.makeText(GroupActivity.this, "그룹 생성 실패: " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "그룹 생성 실패: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                Toast.makeText(GroupActivity.this, "그룹 생성 실패: 서버 오류", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "그룹 생성 실패", t);
            }
        });
    }
}
