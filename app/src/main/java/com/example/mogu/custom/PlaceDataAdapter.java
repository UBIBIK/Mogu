package com.example.mogu.custom;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mogu.R;
import com.example.mogu.object.LocationInfo;
import com.example.mogu.object.PlaceData;
import com.example.mogu.object.TripScheduleDetails;
import com.example.mogu.object.UserInfo;
import com.example.mogu.share.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

public class PlaceDataAdapter extends RecyclerView.Adapter<PlaceDataAdapter.ViewHolder> {

    // 리스너 인터페이스 정의
    public interface OnEditPlaceListener {
        void onEditPlace(int position, PlaceData placeData);
    }

    public interface OnPlaceDeletedListener {
        void onPlaceDeleted(int position, PlaceData updatedPlaceData);
    }

    // 리스너 멤버 변수
    private OnEditPlaceListener onEditPlaceListener;
    private OnPlaceDeletedListener onPlaceDeletedListener;

    // 데이터 관련 변수
    private PlaceData placeList;
    private List<String> placeNames;
    private List<String> notes;
    private List<String> imageList;  // 이미지 URL 리스트
    private List<Double> latitudeList;  // 위도 리스트
    private List<Double> longitudeList;  // 경도 리스트

    // 기타 변수
    private String day;
    private Context context;
    private UserInfo userInfo;
    private SharedPreferencesHelper sharedPreferencesHelper;

    // 생성자
    public PlaceDataAdapter(PlaceData placeList, List<String> placeNames, List<String> notes, List<String> imageList, List<Double> latitudeList, List<Double> longitudeList, Context context, String day, UserInfo userInfo) {
        this.placeList = placeList;
        this.placeNames = placeNames;
        this.notes = notes;
        this.imageList = imageList;
        this.latitudeList = latitudeList;
        this.longitudeList = longitudeList;
        this.context = context;
        this.day = day;
        this.userInfo = userInfo;
        this.sharedPreferencesHelper = new SharedPreferencesHelper(context);
    }

    // 리스너 설정 메서드
    public void setOnEditPlaceListener(OnEditPlaceListener listener) {
        this.onEditPlaceListener = listener;
    }

    public void setOnPlaceDeletedListener(OnPlaceDeletedListener listener) {
        this.onPlaceDeletedListener = listener;
    }

    // ViewHolder 생성
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_place_item, parent, false);
        return new ViewHolder(view);
    }

    // 데이터 바인딩
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.placeNameTextView.setText(placeNames.get(position));
        holder.noteTextView.setText(notes.get(position));
        holder.dayTextView.setText(day);
    }

    @Override
    public int getItemCount() {
        return placeNames.size();
    }

    // 데이터 업데이트 메서드
    public void updateData(PlaceData newData, List<String> newPlaceNames, List<String> newNotes) {
        this.placeList = newData;
        this.placeNames = newPlaceNames;
        this.notes = newNotes;
        notifyDataSetChanged();
    }

    // ViewHolder 정의
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView placeNameTextView;
        private TextView noteTextView;
        private TextView dayTextView;
        private Button deleteButton;
        private Button editPlaceButton;

        public ViewHolder(View itemView) {
            super(itemView);
            placeNameTextView = itemView.findViewById(R.id.placeNameTextView);
            noteTextView = itemView.findViewById(R.id.noteTextView);
            dayTextView = itemView.findViewById(R.id.dayTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editPlaceButton = itemView.findViewById(R.id.editPlaceButton);

            // 삭제 버튼 클릭 리스너
            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                deleteSpecificLocationInfo(position); // 모든 LocationInfo 삭제 후 UserInfo 갱신
            });

            // 편집 버튼 클릭 리스너
            editPlaceButton.setOnClickListener(v -> {
                if (onEditPlaceListener != null) {
                    int position = getAdapterPosition();
                    onEditPlaceListener.onEditPlace(position, placeList); // 장소 편집 후 UserInfo 갱신
                }
            });
        }

        public void deleteSpecificLocationInfo(int position) {
            Log.d("PlaceDataAdapter", "deleteSpecificLocationInfo called at position: " + position);

            if (placeNames != null && imageList != null && latitudeList != null && longitudeList != null) {
                // 삭제 전 리스트 상태 출력
                Log.d("PlaceDataAdapter", "Initial placeNames: " + placeNames.toString());
                Log.d("PlaceDataAdapter", "Initial imageList: " + imageList.toString());
                Log.d("PlaceDataAdapter", "Initial latitudeList: " + latitudeList.toString());
                Log.d("PlaceDataAdapter", "Initial longitudeList: " + longitudeList.toString());

                // 유효한 인덱스인지 확인
                if (position >= 0 && position < placeNames.size()) {
                    // 각 리스트에서 해당 인덱스의 항목 삭제
                    placeNames.remove(position);
                    imageList.remove(position);
                    latitudeList.remove(position);
                    longitudeList.remove(position);

                    Log.d("PlaceDataAdapter", "LocationInfo at position " + position + " has been deleted.");

                    // PlaceData 객체와 리스트 동기화
                    syncPlaceData();

                    // 삭제 후 리스트 상태 확인
                    Log.d("PlaceDataAdapter", "After removal - placeNames Size: " + placeNames.size());
                    Log.d("PlaceDataAdapter", "After removal - imageList Size: " + imageList.size());
                    Log.d("PlaceDataAdapter", "After removal - latitudeList Size: " + latitudeList.size());
                    Log.d("PlaceDataAdapter", "After removal - longitudeList Size: " + longitudeList.size());

                    // UserInfo 업데이트
                    updateUserInfo();

                    // UI 갱신
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, getItemCount());

                    // 리스트가 비었을 때 처리
                    if (placeNames.isEmpty()) {
                        handleEmptyDay();
                    }
                } else {
                    Log.e("PlaceDataAdapter", "Invalid position: " + position + " for placeNames size: " + placeNames.size());
                }
            } else {
                Log.e("PlaceDataAdapter", "List is null: placeNames or imageList or latitudeList or longitudeList is null");
            }
        }

        // PlaceData와 리스트 동기화 메서드
        private void syncPlaceData() {
            List<LocationInfo> updatedLocationInfoList = new ArrayList<>();
            for (int i = 0; i < placeNames.size(); i++) {
                updatedLocationInfoList.add(new LocationInfo(placeNames.get(i), null, latitudeList.get(i), longitudeList.get(i), notes.get(i), imageList.get(i)));
            }
            placeList.setLocationInfoList(updatedLocationInfoList);
        }

        // UserInfo 업데이트 메서드
        private void updateUserInfo() {
            if (userInfo != null) {
                Log.d("PlaceDataAdapter", "Updating UserInfo for day: " + day);

                // day에 해당하는 TripScheduleDetails를 찾아서 업데이트
                for (TripScheduleDetails details : userInfo.getGroupList().get(0).getTripScheduleList().get(0).getTripScheduleDetails()) {
                    if (details.getDay().equals(day)) {
                        details.setLocationInfo(new ArrayList<>(placeList.getLocationInfoList())); // PlaceData에서 동기화된 장소 정보 가져오기
                        Log.d("PlaceDataAdapter", "Updating LocationInfoList for day: " + day);
                        break;
                    }
                }

                sharedPreferencesHelper.saveUserInfo(userInfo); // UserInfo 저장
                Log.d("PlaceDataAdapter", "Updated UserInfo: " + userInfo);
            } else {
                Log.e("PlaceDataAdapter", "UserInfo is null, cannot update");
            }
        }


        // 리스트가 비었을 때 처리
        private void handleEmptyDay() {
            Log.d("PlaceDataAdapter", "No places left for this day.");
            // 필요한 경우 UI에서 빈 상태를 처리
        }
    }
}
