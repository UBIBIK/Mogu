package com.example.mogu.screen;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mogu.R;
import com.example.mogu.object.UserInfo;
import com.example.mogu.retrofit.ApiService;
import com.example.mogu.retrofit.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUp extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    private EditText emailEditText;
    private EditText nameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private EditText phoneEditText;
    private Button signupButton;
    private CheckBox agreeCheckBox;
    private Button showTermsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        // 뷰 초기화
        emailEditText = findViewById(R.id.emailEditText);
        nameEditText = findViewById(R.id.nameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        signupButton = findViewById(R.id.signupButton);
        agreeCheckBox = findViewById(R.id.agreeCheckBox);
        showTermsButton = findViewById(R.id.showTermsButton);

        // 약관 보기 버튼 클릭 리스너 설정
        showTermsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTermsPopup();
            }
        });

        // 버튼 클릭 리스너 설정
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSignup();
            }
        });
    }

    private void showTermsPopup() {
        // 팝업창에 표시할 메시지
        String termsMessage = "모구모구는 목포와 서비스 회원가입을 위해 아래와 같이 개인정보를 수집, 이용합니다.\n\n"
                + "수집 목적: 회원 식별 및 회원제 서비스 제공\n"
                + "수집 항목: 아이디, 비밀번호, 전화번호, 이메일\n"
                + "수집 근거: 개인정보 보호법 제 15조 제1항\n\n"
                + "* 귀하는 목포와의 서비스 이용에 필요한 최소한의 개인정보 수집, 이용에 동의하지 않을 수 있으나, "
                + "동의를 거부할 경우 서비스 이용이 불가합니다.";

        // AlertDialog 빌더로 팝업창 생성
        new AlertDialog.Builder(SignUp.this)
                .setTitle("개인정보 수집 및 이용 동의")
                .setMessage(termsMessage)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 확인 버튼 클릭 시 체크박스를 체크 상태로 변경
                        agreeCheckBox.setChecked(true);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 닫기 버튼 클릭 시 아무것도 하지 않고 팝업 닫기
                        dialog.dismiss();
                    }
                })
                .show();
    }

    // 회원가입 처리 메서드
    private void handleSignup() {
        String email = emailEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        // 모든 필드를 입력했는지 확인
        if (email.isEmpty() || name.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phone.isEmpty()) {
            Toast.makeText(SignUp.this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 개인정보 수집 및 이용 동의 확인
        if (!agreeCheckBox.isChecked()) {
            Toast.makeText(SignUp.this, "개인정보 수집 및 이용에 동의해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 이메일 형식이 올바른지 확인
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(SignUp.this, "유효한 이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 비밀번호와 비밀번호 확인이 일치하는지 확인
        if (!password.equals(confirmPassword)) {
            Toast.makeText(SignUp.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 회원가입 처리 로직 (서버로 데이터 전송)
        UserInfo user = new UserInfo(email, name, password, phone);
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<String> call = apiService.signup(user);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "회원가입 성공: " + response.body());
                    Toast.makeText(SignUp.this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
                    finish(); // 회원가입 성공 후 현재 액티비티 종료
                } else {
                    Log.e(TAG, "회원가입 실패: " + response.message());
                    Toast.makeText(SignUp.this, "회원가입 실패: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "회원가입 오류: ", t);
                Toast.makeText(SignUp.this, "회원가입 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
