package com.example.mogu.screen;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mogu.R;
import com.example.mogu.object.FindUserIdRequest;
import com.example.mogu.retrofit.ApiService;
import com.example.mogu.retrofit.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindIdActivity extends AppCompatActivity {

    private EditText inputName;
    private EditText inputPhoneNum;
    private Button buttonOk;
    private Button buttonCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_id);

        // 뷰 초기화
        inputName = findViewById(R.id.input_name);
        inputPhoneNum = findViewById(R.id.input_phonenum);
        buttonOk = findViewById(R.id.button_ok);
        buttonCancel = findViewById(R.id.button_cancel);

        // 확인 버튼 클릭 시
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 이름과 전화번호 가져오기
                String name = inputName.getText().toString().trim();
                String phoneNum = inputPhoneNum.getText().toString().trim();

                // 입력값 검증
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phoneNum)) {
                    Toast.makeText(FindIdActivity.this, "이름과 전화번호를 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 서버에 아이디 찾기 요청
                findUserId(name, phoneNum);
            }
        });

        // 취소 버튼 클릭 시 로그인 화면으로 이동
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FindIdActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // 현재 화면 종료
            }
        });
    }

    // 아이디 찾는 메서드 (서버 통신)
    private void findUserId(String name, String phoneNum) {
        FindUserIdRequest request = new FindUserIdRequest(name, phoneNum);

        // Retrofit API 호출
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        apiService.findUserId(request).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 서버에서 받은 응답 처리
                    String userId = response.body();
                    Toast.makeText(FindIdActivity.this, "찾은 ID: " + userId, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(FindIdActivity.this, "ID를 찾을 수 없습니다.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                // 서버 통신 실패 시 처리
                Toast.makeText(FindIdActivity.this, "서버와 통신에 실패했습니다.", Toast.LENGTH_LONG).show();
            }
        });
    }
}