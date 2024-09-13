package com.example.mogu.screen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mogu.R;
import com.example.mogu.object.GroupInfo;
import com.example.mogu.share.SharedPreferencesHelper;
import com.example.mogu.object.UserInfo;
import com.example.mogu.websocket.WebSocketService;
import com.example.mogu.retrofit.ApiService;
import com.example.mogu.retrofit.RetrofitClient;
import com.kakao.sdk.common.util.Utility;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ApiService apiService;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private SharedPreferencesHelper sharedPreferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 카카오 SDK를 이용해 키 해시를 로그에 출력
        String keyHash = Utility.INSTANCE.getKeyHash(this);
        Log.d("KeyHash", keyHash);

        // 뷰 초기화
        initializeViews();

        // SharedPreferencesHelper 및 ApiService 초기화
        sharedPreferencesHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        sharedPreferencesHelper.clearUserInfo();
        // 저장된 사용자 정보가 있을 경우 자동 로그인 시도
        /*UserInfo savedUser = sharedPreferencesHelper.getUserInfo();
        if (savedUser.getUserEmail() != null && !savedUser.getUserEmail().isEmpty()) {
            // 자동 로그인 또는 저장된 사용자 정보로 작업을 수행
            Toast.makeText(this, "자동 로그인: " + savedUser.getUserName(), Toast.LENGTH_SHORT).show();
            //TODO:자동로그인 해제 해둔거 나중에 주석 풀기 loginWithSavedUser(savedUser);
        }*/

        // 로그인 버튼 클릭 리스너 설정
        findViewById(R.id.loginButton).setOnClickListener(v -> loginUser(
                usernameEditText.getText().toString(),
                passwordEditText.getText().toString()
        ));

        // 회원가입 버튼 클릭 리스너 설정
        findViewById(R.id.signupButton).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUp.class);
            startActivity(intent);
        });

        // ID 찾기 버튼 클릭 리스너 설정
        findViewById(R.id.findIdPwButton).setOnClickListener(v -> {
            // ID 찾기 화면으로 이동
            Intent intent = new Intent(MainActivity.this, FindIdActivity.class);
            startActivity(intent);
        });

        // 테스트버튼
        findViewById(R.id.testbutton).setOnClickListener(v -> {
            navigateToGroupActivity();
        });
    }

    // 뷰 초기화 메서드
    private void initializeViews() {
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
    }

    // 저장된 사용자 정보로 로그인 시도
    private void loginWithSavedUser(UserInfo userInfo) {
        apiService.login(userInfo).enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                if (response.isSuccessful()) {
                    UserInfo loggedInUser = response.body();
                    if (loggedInUser != null) {
                        Toast.makeText(MainActivity.this, "로그인 성공: " + loggedInUser.getUserName(), Toast.LENGTH_SHORT).show();
                        if (loggedInUser != null) {
                            ArrayList<GroupInfo> groupList = loggedInUser.getGroupList();
                            if (groupList != null && !groupList.isEmpty()) {
                                Log.d("UserInfo", "Group List size: " + groupList.size());
                                for (GroupInfo group : groupList) {
                                    Log.d("UserInfo", "Group Name: " + group.getGroupName());
                                    Log.d("UserInfo", "Group Key: " + group.getGroupKey());
                                }
                            }
                            else {
                                Log.d("UserInfo", "Group List is null");
                            }
                        }
                        sharedPreferencesHelper.saveUserInfo(loggedInUser);
                        startWebSocketService();
                        navigateToGroupActivity();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "로그인 실패: 잘못된 사용자 정보입니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                Log.e(TAG, "로그인 실패", t);
                Toast.makeText(MainActivity.this, "로그인 실패: 서버 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 사용자 입력 정보로 로그인 시도
    private void loginUser(String email, String password) {
        UserInfo userInfo = new UserInfo(email, "", password, "");

        apiService.login(userInfo).enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                if (response.isSuccessful()) {
                    UserInfo loggedInUser = response.body();
                    if (loggedInUser != null) {
                        Toast.makeText(MainActivity.this, "로그인 성공: " + loggedInUser.getUserName(), Toast.LENGTH_SHORT).show();
                        ArrayList<GroupInfo> groupList = loggedInUser.getGroupList();
                        if (groupList != null && !groupList.isEmpty()) {
                            Log.d("UserInfo", "Group List size: " + groupList.size());
                            for (GroupInfo group : groupList) {
                                Log.d("UserInfo", "Group Name: " + group.getGroupName());
                                Log.d("UserInfo", "Group Key: " + group.getGroupKey());
                            }
                        } else {
                            Log.d("UserInfo", "Group List is null");
                        }
                        sharedPreferencesHelper.saveUserInfo(loggedInUser);
                        startWebSocketService();
                        navigateToGroupActivity();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "로그인 실패: 잘못된 사용자 정보입니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                Log.e(TAG, "로그인 실패", t);
                Toast.makeText(MainActivity.this, "로그인 실패: 서버 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // WebSocketService 시작
    private void startWebSocketService() {
        Intent intent = new Intent(this, WebSocketService.class);
        startService(intent);
    }

    // WebSocketService 종료
    private void stopWebSocketService() {
        Intent intent = new Intent(this, WebSocketService.class);
        stopService(intent);
    }

    // Home 이동
    private void navigateToGroupActivity() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish(); // 현재 액티비티를 종료하여 뒤로 가기 버튼으로 돌아오지 않게 합니다.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
