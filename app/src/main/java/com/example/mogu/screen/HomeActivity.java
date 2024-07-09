package com.example.mogu.screen;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.mogu.R;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        findViewById(R.id.HomeButton).setOnClickListener(v -> replaceFragment(new HomeFragment()));
        findViewById(R.id.GroupButton).setOnClickListener(v -> replaceFragment(new GroupFragment()));

        // 초기 화면 설정
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
