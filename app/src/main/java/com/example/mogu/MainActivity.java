package com.example.mogu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.kakao.sdk.common.util.Utility;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private WebSocketHelper webSocketHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String keyHash = Utility.INSTANCE.getKeyHash(this);
        Log.d("KeyHash", keyHash);

        webSocketHelper = WebSocketHelper.getInstance();
        webSocketHelper.connect("ws://your-server-url/ws");

        // Initialize views
        EditText usernameEditText = findViewById(R.id.usernameEditText);
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        Button loginButton = findViewById(R.id.loginButton);
        Button signupButton = findViewById(R.id.signupButton);
        Button findIdPwButton = findViewById(R.id.findIdPwButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 서버로 메시지 전송 (예: 로그인 정보)
                String message = "로그인 시도: " + usernameEditText.getText().toString() + ", " + passwordEditText.getText().toString();
                webSocketHelper.sendMessage(message);
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 회원가입 화면으로 이동
                Intent intent = new Intent(MainActivity.this, SignUp.class);
                startActivity(intent);
            }
        });

        findIdPwButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ID/PW 찾기 화면으로 이동
                // Intent intent = new Intent(MainActivity.this, FindIdPwActivity.class);
                // startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webSocketHelper.disconnect();
    }
}
