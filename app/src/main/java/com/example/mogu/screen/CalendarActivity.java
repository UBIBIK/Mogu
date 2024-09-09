package com.example.mogu.screen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mogu.R;
import com.example.mogu.object.GroupInfo;
import com.example.mogu.object.UserInfo;
import com.example.mogu.share.SharedPreferencesHelper;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView1, calendarView2;
    private Button apply_Btn;
    private TextView selectedDatesLabel, selectedDates, durationText;
    private Calendar firstSelectedDate = null;
    private Calendar secondSelectedDate = null;
    private String groupKey;  // groupKey를 저장할 변수
    private SharedPreferencesHelper sharedPreferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);

        // SharedPreferencesHelper 초기화
        sharedPreferencesHelper = new SharedPreferencesHelper(this);

        // Intent로부터 groupKey를 가져옴
        groupKey = getIntent().getStringExtra("group_key");

        calendarView1 = findViewById(R.id.calendarView1);
        calendarView2 = findViewById(R.id.calendarView2);
        selectedDatesLabel = findViewById(R.id.selectedDatesLabel);
        selectedDates = findViewById(R.id.selectedDates);
        durationText = findViewById(R.id.durationText);
        apply_Btn = findViewById(R.id.apply_Btn);

        // 오늘 날짜를 기본으로 설정
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        long todayMillis = today.getTimeInMillis();

        calendarView1.setMinDate(todayMillis); // 오늘 날짜 이후만 선택 가능
        calendarView2.setMinDate(todayMillis); // 오늘 날짜 이후만 선택 가능

        // 사용자 정보에서 일정 데이터를 가져와서 설정
        loadAndSetUserSchedule(groupKey, today);

        calendarView1.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, month, dayOfMonth);

                if (!selectedDate.before(today)) {
                    firstSelectedDate = selectedDate;
                    updateSelectedDatesDisplay();
                }
            }
        });

        calendarView2.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, month, dayOfMonth);

                if (!selectedDate.before(today)) {
                    secondSelectedDate = selectedDate;
                    updateSelectedDatesDisplay();
                }
            }
        });

        apply_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firstSelectedDate != null && secondSelectedDate != null) {
                    if (firstSelectedDate.after(secondSelectedDate)) {
                        Toast.makeText(CalendarActivity.this, "종료 날짜는 시작 날짜 이후여야 합니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        long startMillis = firstSelectedDate.getTimeInMillis();
                        long endMillis = secondSelectedDate.getTimeInMillis();

                        // 기간 계산 (두 날짜 모두 포함)
                        long duration = getDateDifference(firstSelectedDate, secondSelectedDate) + 1;

                        // 날짜 형식 지정
                        @SuppressLint("DefaultLocale") String formattedStartDate = String.format("%d년 %d월 %d일", firstSelectedDate.get(Calendar.YEAR),
                                firstSelectedDate.get(Calendar.MONTH) + 1, firstSelectedDate.get(Calendar.DAY_OF_MONTH));
                        @SuppressLint("DefaultLocale") String formattedEndDate = String.format("%d년 %d월 %d일", secondSelectedDate.get(Calendar.YEAR),
                                secondSelectedDate.get(Calendar.MONTH) + 1, secondSelectedDate.get(Calendar.DAY_OF_MONTH));

                        // 선택한 날짜와 기간을 Intent에 담아 MapActivity로 전달
                        Intent intent = new Intent(CalendarActivity.this, MapActivity.class);
                        intent.putExtra("startDate", startMillis);
                        intent.putExtra("endDate", endMillis);
                        intent.putExtra("duration", duration);
                        intent.putExtra("formattedStartDate", formattedStartDate);
                        intent.putExtra("formattedEndDate", formattedEndDate);
                        intent.putExtra("group_key", groupKey); // groupKey를 Intent에 추가
                        startActivity(intent);

                        // CalendarActivity 종료
                        finish();
                    }
                } else {
                    Toast.makeText(CalendarActivity.this, "날짜를 선택해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadAndSetUserSchedule(String groupKey, Calendar today) {
        // SharedPreferences에서 UserInfo 가져오기
        UserInfo userInfo = sharedPreferencesHelper.getUserInfo();
        if (userInfo != null) {
            // 해당 그룹의 일정 정보 찾기
            GroupInfo selectedGroup = null;
            for (GroupInfo group : userInfo.getGroupList()) {
                if (group.getGroupKey().equals(groupKey)) {
                    selectedGroup = group;
                    break;
                }
            }

            // selectedGroup과 그 안의 TripScheduleList가 null인지 체크
            if (selectedGroup != null && selectedGroup.getTripScheduleList() != null && !selectedGroup.getTripScheduleList().isEmpty()) {
                long startMillis = selectedGroup.getStartDateMillis();
                long endMillis = selectedGroup.getEndDateMillis();

                // 달력에 날짜 표시
                calendarView1.setDate(startMillis);
                calendarView2.setDate(endMillis);

                firstSelectedDate = Calendar.getInstance();
                firstSelectedDate.setTimeInMillis(startMillis);

                secondSelectedDate = Calendar.getInstance();
                secondSelectedDate.setTimeInMillis(endMillis);
            } else {
                // 일정이 없으면 오늘 날짜로 설정
                calendarView1.setDate(today.getTimeInMillis());
                calendarView2.setDate(today.getTimeInMillis());

                firstSelectedDate = (Calendar) today.clone();
                secondSelectedDate = (Calendar) today.clone();
            }
        } else {
            Toast.makeText(this, "사용자 정보 로드 실패", Toast.LENGTH_SHORT).show();
            // 일정이 없으면 오늘 날짜로 설정
            calendarView1.setDate(today.getTimeInMillis());
            calendarView2.setDate(today.getTimeInMillis());

            firstSelectedDate = (Calendar) today.clone();
            secondSelectedDate = (Calendar) today.clone();
        }

        updateSelectedDatesDisplay(); // 선택된 날짜를 갱신하여 화면에 표시
    }


    @SuppressLint("SetTextI18n")
    private void updateSelectedDatesDisplay() {
        if (firstSelectedDate != null && secondSelectedDate != null) {
            Calendar startDate = firstSelectedDate;
            Calendar endDate = secondSelectedDate;

            if (firstSelectedDate.after(secondSelectedDate)) {
                startDate = secondSelectedDate;
                endDate = firstSelectedDate;
            }

            @SuppressLint("DefaultLocale") String formattedStartDate = String.format("%d년 %d월 %d일", startDate.get(Calendar.YEAR),
                    startDate.get(Calendar.MONTH) + 1, startDate.get(Calendar.DAY_OF_MONTH));
            @SuppressLint("DefaultLocale") String formattedEndDate = String.format("%d년 %d월 %d일", endDate.get(Calendar.YEAR),
                    endDate.get(Calendar.MONTH) + 1, endDate.get(Calendar.DAY_OF_MONTH));

            selectedDates.setText(formattedStartDate + " ~ " + formattedEndDate);

            // 기간 계산 (두 날짜 모두 포함)
            long duration = getDateDifference(startDate, endDate) + 1;
            durationText.setText("기간: " + duration + "일");
        } else if (firstSelectedDate != null) {
            @SuppressLint("DefaultLocale") String formattedFirstDate = String.format("%d년 %d월 %d일", firstSelectedDate.get(Calendar.YEAR),
                    firstSelectedDate.get(Calendar.MONTH) + 1, firstSelectedDate.get(Calendar.DAY_OF_MONTH));
            selectedDates.setText(formattedFirstDate);
            durationText.setText("기간: 1일");
        }

        selectedDatesLabel.setVisibility(View.VISIBLE);
        selectedDates.setVisibility(View.VISIBLE);
        durationText.setVisibility(View.VISIBLE);
        apply_Btn.setVisibility(View.VISIBLE);
    }

    private long getDateDifference(Calendar startDate, Calendar endDate) {
        long diffInMillis = endDate.getTimeInMillis() - startDate.getTimeInMillis();
        return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
    }
}
