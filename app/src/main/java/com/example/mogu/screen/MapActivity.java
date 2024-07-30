package com.example.mogu.screen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.example.mogu.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.MapView;
import com.kakao.sdk.common.KakaoSdk;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MapActivity extends AppCompatActivity {

    private MapView mapView;
    private KakaoMap kakaoMap;
    private BottomSheetBehavior<FrameLayout> bottomSheetBehavior;
    private LinearLayout dateInfoLayout;
    private Button addPlaceButton;
    private Button addNoteButton;
    private Button selectedDayButton = null;
    private TextView noteTextView;
    private LinearLayout notePopupLayout;
    private EditText popupNoteEditText;
    private Button popupSaveButton;
    private Button toggleBottomSheetButton;

    private Map<String, String> dayNotesMap = new HashMap<>();
    private long startMillis;
    private long endMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        mapView = findViewById(R.id.map_view);
        mapView.start(new MapLifeCycleCallback() {
            @Override
            public void onMapDestroy() {
                // 지도 API가 정상적으로 종료될 때 호출
                Log.d("KakaoMap", "onMapDestroy: ");
            }

            @Override
            public void onMapError(Exception error) {
                // 인증 실패 및 지도 사용 중 에러가 발생할 때 호출
                Log.e("KakaoMap", "onMapError: ", error);
            }
        }, new KakaoMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull KakaoMap map) {
                // 정상적으로 인증이 완료되었을 때 호출
                // KakaoMap 객체를 얻어 옵니다.
                kakaoMap = map;
            }
        });


        CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinatorLayout);
        FrameLayout bottomSheet = findViewById(R.id.bottomSheetContainer);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED); // 기본 상태 설정

        dateInfoLayout = findViewById(R.id.dateInfoLayout);
        addPlaceButton = findViewById(R.id.addPlaceButton);
        addNoteButton = findViewById(R.id.addNoteButton);
        noteTextView = findViewById(R.id.noteTextView);
        notePopupLayout = findViewById(R.id.notePopupLayout);
        popupNoteEditText = findViewById(R.id.popupNoteEditText);
        popupSaveButton = findViewById(R.id.popupSaveButton);
        toggleBottomSheetButton = findViewById(R.id.toggleBottomSheetButton);

        // 날짜 버튼들 생성
        createDayButtons();

        // 장소 추가 버튼 클릭 리스너
        addPlaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedDayButton != null) {
                    Toast.makeText(MapActivity.this, "장소 추가 버튼 클릭됨", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 메모 추가 버튼 클릭 리스너
        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedDayButton != null) {
                    showNotePopup();
                }
            }
        });

        // 메모 팝업 창의 저장 버튼 클릭 리스너
        popupSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNote();
            }
        });

        // 하단 시트 열기/닫기 버튼 클릭 리스너
        toggleBottomSheetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    toggleBottomSheetButton.setText("하단 시트 닫기");
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    toggleBottomSheetButton.setText("하단 시트 열기");
                }
            }
        });
    }

    private void createDayButtons() {
        Calendar startDate = Calendar.getInstance();
        startDate.setTimeInMillis(startMillis);

        Calendar endDate = Calendar.getInstance();
        endDate.setTimeInMillis(endMillis);

        long numberOfDays = getDateDifference(startDate, endDate) + 1; // +1 추가

        dateInfoLayout.removeAllViews(); // 기존 버튼 삭제

        for (int i = 0; i < numberOfDays; i++) {
            final Button dayButton = new Button(this);
            dayButton.setText("DAY" + (i + 1));
            dayButton.setBackgroundResource(R.drawable.rounded_button_selected);
            dayButton.setTextColor(getResources().getColorStateList(R.drawable.button_text_selected));
            dayButton.setTextSize(12);
            dayButton.setAllCaps(true);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    160,
                    100
            );
            params.setMargins(24, 0, 24, 0);
            dayButton.setLayoutParams(params);

            dayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectDayButton(dayButton);
                }
            });

            dateInfoLayout.addView(dayButton);
        }

        // DAY1 버튼을 기본적으로 클릭된 상태로 설정
        if (dateInfoLayout.getChildCount() > 0) {
            Button firstDayButton = (Button) dateInfoLayout.getChildAt(0);
            selectDayButton(firstDayButton);
        }
    }

    private void selectDayButton(Button button) {
        // 기존 선택된 버튼의 상태를 복원
        if (selectedDayButton != null) {
            selectedDayButton.setSelected(false);
            selectedDayButton.setBackgroundResource(R.drawable.rounded_button_selected);
            selectedDayButton.setTextColor(getResources().getColorStateList(R.drawable.button_text_selected));
        }

        // 현재 버튼을 선택 상태로 설정
        button.setSelected(true);
        button.setBackgroundResource(R.drawable.rounded_button_selected);
        button.setTextColor(getResources().getColorStateList(R.drawable.button_text_selected));
        selectedDayButton = button;

        // 선택된 DAY 버튼에 해당하는 메모를 TextView에 표시
        String day = selectedDayButton.getText().toString();
        String note = dayNotesMap.get(day);
        noteTextView.setText(note != null ? note : "");

        // 하단 시트가 닫히지 않도록 설정
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }



    private void showNotePopup() {
        String day = selectedDayButton.getText().toString();
        String note = dayNotesMap.get(day);
        popupNoteEditText.setText(note != null ? note : "");
        notePopupLayout.setVisibility(View.VISIBLE);
    }

    private void saveNote() {
        String day = selectedDayButton.getText().toString();
        String note = popupNoteEditText.getText().toString();
        dayNotesMap.put(day, note);
        noteTextView.setText(note);
        notePopupLayout.setVisibility(View.GONE);
        // 하단 시트 상태 유지
        bottomSheetBehavior.setState(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED
                ? BottomSheetBehavior.STATE_EXPANDED : BottomSheetBehavior.STATE_COLLAPSED);
    }

    private long getDateDifference(Calendar startDate, Calendar endDate) {
        long diffInMillis = endDate.getTimeInMillis() - startDate.getTimeInMillis();
        return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
    }
}
