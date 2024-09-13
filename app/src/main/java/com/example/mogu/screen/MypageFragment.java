package com.example.mogu.screen;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mogu.R;
import com.example.mogu.object.UserInfo;
import com.example.mogu.retrofit.ApiService;
import com.example.mogu.retrofit.RetrofitClient;
import com.example.mogu.share.SharedPreferencesHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MypageFragment extends Fragment {

    private static final String TAG = "MypageFragment";

    private Button logoutButton;
    private Button withdrawButton; // 회원탈퇴 버튼
    private SharedPreferencesHelper sharedPreferencesHelper;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 프래그먼트의 레이아웃을 인플레이트하여 뷰 생성
        View view = inflater.inflate(R.layout.my_information, container, false);

        // SharedPreferencesHelper 초기화
        sharedPreferencesHelper = new SharedPreferencesHelper(requireContext());

        // Retrofit ApiService 초기화
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // 로그아웃 버튼 찾기
        logoutButton = view.findViewById(R.id.button_logout);

        // 회원탈퇴 버튼 찾기
        withdrawButton = view.findViewById(R.id.button_withdraw);

        // 로그아웃 버튼 클릭 이벤트 처리
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 저장된 사용자 정보 삭제
                sharedPreferencesHelper.clearUserInfo();

                // 로그아웃 후 로그인 화면으로 이동
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish(); // 현재 액티비티 종료

                // 로그아웃 메시지 표시
                Toast.makeText(getActivity(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // 회원탈퇴 버튼 클릭 이벤트 처리
        withdrawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SharedPreferences에서 사용자 정보 가져오기
                UserInfo userInfo = sharedPreferencesHelper.getUserInfo();

                // 서버에 회원탈퇴 요청
                apiService.signout(userInfo).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                            // 회원탈퇴 성공 시 처리
                            sharedPreferencesHelper.clearUserInfo();

                            // 로그인 화면으로 이동
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            getActivity().finish(); // 현재 액티비티 종료

                            // 회원탈퇴 성공 메시지 표시
                            Toast.makeText(getActivity(), "회원탈퇴 되었습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            // 실패 시 처리
                            Toast.makeText(getActivity(), "회원탈퇴 실패: 서버 오류", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        // 네트워크 오류 등 처리
                        Toast.makeText(getActivity(), "회원탈퇴 실패: 네트워크 오류", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return view;
    }
}