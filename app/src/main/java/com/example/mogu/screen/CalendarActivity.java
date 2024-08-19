package com.example.mogu.screen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import com.example.mogu.R;
import com.example.mogu.share.SharedPreferencesHelper;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class CalendarActivity extends AppCompatActivity {

    public CalendarView calendarView1, calendarView2;
    public Button apply_Btn;
    public TextView selectedDatesLabel, selectedDates, durationText;
    private Calendar firstSelectedDate = null;
    private Calendar secondSelectedDate = null;
    private SharedPreferencesHelper sharedPreferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);

        // SharedPreferencesHelper 초기화
        sharedPreferencesHelper = new SharedPreferencesHelper(this);

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

        // 이전에 저장된 날짜가 있는지 확인하고 불러오기
        SharedPreferencesHelper.DatePeriodData datePeriodData = sharedPreferencesHelper.getDates();
        if (datePeriodData.getStartDateMillis() != -1 && datePeriodData.getEndDateMillis() != -1) {
            firstSelectedDate = Calendar.getInstance();
            firstSelectedDate.setTimeInMillis(datePeriodData.getStartDateMillis());
            calendarView1.setDate(datePeriodData.getStartDateMillis());

            secondSelectedDate = Calendar.getInstance();
            secondSelectedDate.setTimeInMillis(datePeriodData.getEndDateMillis());
            calendarView2.setDate(datePeriodData.getEndDateMillis());

            updateSelectedDatesDisplay();
        } else {
            // 기본적으로 오늘 날짜를 선택하도록 설정
            calendarView1.setDate(todayMillis);
            calendarView2.setDate(todayMillis);

            firstSelectedDate = (Calendar) today.clone();
            secondSelectedDate = (Calendar) today.clone();
        }

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
                    long startMillis = firstSelectedDate.getTimeInMillis();
                    long endMillis = secondSelectedDate.getTimeInMillis();

                    // 선택한 날짜를 SharedPreferences에 저장
                    sharedPreferencesHelper.saveDates(startMillis, endMillis);

                    Intent intent = new Intent(CalendarActivity.this, MapActivity.class);
                    intent.putExtra("startDate", startMillis);
                    intent.putExtra("endDate", endMillis);
                    startActivity(intent);
                }
            }
        });
    }

    private void updateSelectedDatesDisplay() {
        if (firstSelectedDate != null && secondSelectedDate != null) {
            Calendar startDate = firstSelectedDate;
            Calendar endDate = secondSelectedDate;

            if (firstSelectedDate.after(secondSelectedDate)) {
                startDate = secondSelectedDate;
                endDate = firstSelectedDate;
            }

            String formattedStartDate = String.format("%d년 %d월 %d일", startDate.get(Calendar.YEAR),
                    startDate.get(Calendar.MONTH) + 1, startDate.get(Calendar.DAY_OF_MONTH));
            String formattedEndDate = String.format("%d년 %d월 %d일", endDate.get(Calendar.YEAR),
                    endDate.get(Calendar.MONTH) + 1, endDate.get(Calendar.DAY_OF_MONTH));

            selectedDates.setText(formattedStartDate + " ~ " + formattedEndDate);

            long duration = getDateDifference(startDate, endDate);
            durationText.setText("기간: " + duration + "일");
        } else if (firstSelectedDate != null) {
            String formattedFirstDate = String.format("%d년 %d월 %d일", firstSelectedDate.get(Calendar.YEAR),
                    firstSelectedDate.get(Calendar.MONTH) + 1, firstSelectedDate.get(Calendar.DAY_OF_MONTH));
            selectedDates.setText(formattedFirstDate);
            durationText.setText("");
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
