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

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView1, calendarView2;
    private Button apply_Btn;
    private TextView selectedDatesLabel, selectedDates, durationText;
    private Calendar firstSelectedDate = null;
    private Calendar secondSelectedDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);

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

        // 기본적으로 오늘 날짜를 선택하도록 설정
        calendarView1.setDate(todayMillis);
        calendarView2.setDate(todayMillis);

        firstSelectedDate = (Calendar) today.clone();
        secondSelectedDate = (Calendar) today.clone();

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

                        // 로그 출력
                        Log.d("CalendarActivity", "선택한 여행 기간: " + formattedStartDate + " ~ " + formattedEndDate + ", 총 " + duration + "일");

                        // 선택한 날짜와 기간을 Intent에 담아 MapActivity로 전달
                        Intent intent = new Intent(CalendarActivity.this, MapActivity.class);
                        intent.putExtra("startDate", startMillis);
                        intent.putExtra("endDate", endMillis);
                        intent.putExtra("duration", duration);
                        intent.putExtra("formattedStartDate", formattedStartDate);
                        intent.putExtra("formattedEndDate", formattedEndDate);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(CalendarActivity.this, "날짜를 선택해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
