package com.example.mogu.screen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mogu.R;
import com.example.mogu.share.SharedPreferencesHelper;
import com.example.mogu.object.UserInfo;
import com.example.mogu.websocket.WebSocketService;
import com.example.mogu.retrofit.ApiService;
import com.example.mogu.retrofit.RetrofitClient;
import com.kakao.sdk.common.util.Utility;

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

        String keyHash = Utility.INSTANCE.getKeyHash(this);
        Log.d("KeyHash", keyHash);

        initializeViews();

        sharedPreferencesHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        UserInfo savedUser = sharedPreferencesHelper.getUserInfo();
        if (savedUser.getUserEmail() != null && !savedUser.getUserEmail().isEmpty()) {
            // 자동 로그인 또는 저장된 사용자 정보로 작업을 수행
            Toast.makeText(this, "자동 로그인: " + savedUser.getUserName(), Toast.LENGTH_SHORT).show();
            loginWithSavedUser(savedUser);
        }

        findViewById(R.id.loginButton).setOnClickListener(v -> loginUser(
                usernameEditText.getText().toString(),
                passwordEditText.getText().toString()
        ));

        findViewById(R.id.signupButton).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUp.class);
            startActivity(intent);
        });

        findViewById(R.id.findIdPwButton).setOnClickListener(v -> {
            // ID/PW 찾기 화면으로 이동
            // Intent intent = new Intent(MainActivity.this, FindIdPwActivity.class);
            // startActivity(intent);
        });
    }

    private void initializeViews() {
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
    }

    private void loginWithSavedUser(UserInfo userInfo) {
        apiService.login(userInfo).enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                if (response.isSuccessful()) {
                    UserInfo loggedInUser = response.body();
                    if (loggedInUser != null) {
                        Toast.makeText(MainActivity.this, "로그인 성공: " + loggedInUser.getUserName(), Toast.LENGTH_SHORT).show();
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

    private void loginUser(String email, String password) {
        UserInfo userInfo = new UserInfo(email, "", password, "");

        apiService.login(userInfo).enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                if (response.isSuccessful()) {
                    UserInfo loggedInUser = response.body();
                    if (loggedInUser != null) {
                        Toast.makeText(MainActivity.this, "로그인 성공: " + loggedInUser.getUserName(), Toast.LENGTH_SHORT).show();
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

    private void startWebSocketService() {
        Intent intent = new Intent(this, WebSocketService.class);
        startService(intent);
    }

    private void stopWebSocketService() {
        Intent intent = new Intent(this, WebSocketService.class);
        stopService(intent);
    }

    private void navigateToGroupActivity() {
        Intent intent = new Intent(MainActivity.this, GroupActivity.class);
        startActivity(intent);
        finish(); // 현재 액티비티를 종료하여 뒤로 가기 버튼으로 돌아오지 않게 합니다.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
