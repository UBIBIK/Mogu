package com.example.mogu.custom;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mogu.R;
import com.example.mogu.object.PlaceData;

import java.util.List;

public class PlaceDataAdapter extends RecyclerView.Adapter<PlaceDataAdapter.ViewHolder> {

    // 인터페이스 정의
    public interface OnEditPlaceListener {
        void onEditPlace(int position, PlaceData placeData);
    }

    private OnEditPlaceListener onEditPlaceListener;

    public void setOnEditPlaceListener(OnEditPlaceListener listener) {
        this.onEditPlaceListener = listener;
    }

    private PlaceData placeList;
    private List<String> placeNames;  // 장소 이름 목록
    private List<String> notes;  // 메모 목록
    private String day;
    private Context context;

    public PlaceDataAdapter(PlaceData placeList, List<String> placeNames, List<String> notes, Context context, String day) {
        this.placeList = placeList;
        this.context = context;
        this.day = day;
        this.placeNames = placeNames;
        this.notes = notes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_place_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.placeNameTextView.setText(placeNames.get(position));  // 장소 이름 설정
        holder.noteTextView.setText(notes.get(position));  // 메모 설정
        holder.dayTextView.setText(day);  // Day 정보 설정
    }

    @Override
    public int getItemCount() {
        return placeNames.size();
    }

    public void updateData(PlaceData newData, List<String> newPlaceNames, List<String> newNotes) {
        this.placeList = newData;
        this.placeNames = newPlaceNames;
        this.notes = newNotes;
        notifyDataSetChanged();  // 데이터 변경 시 어댑터 갱신
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView placeNameTextView;
        private TextView noteTextView;
        private TextView dayTextView;
        private Button addNoteButton;
        private Button deleteButton;
        private Button editPlaceButton;

        public ViewHolder(View itemView) {
            super(itemView);
            placeNameTextView = itemView.findViewById(R.id.placeNameTextView);
            noteTextView = itemView.findViewById(R.id.noteTextView);
            dayTextView = itemView.findViewById(R.id.dayTextView);
            addNoteButton = itemView.findViewById(R.id.addNoteButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editPlaceButton = itemView.findViewById(R.id.editPlaceButton);

            addNoteButton.setOnClickListener(v -> showAddOrEditNoteDialog(getAdapterPosition()));

            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                removePlace(position);
            });

            editPlaceButton.setOnClickListener(v -> {
                if (onEditPlaceListener != null) {
                    int position = getAdapterPosition();  // 현재 아이템의 위치를 가져옴
                    PlaceData placeData = placeList;
                    onEditPlaceListener.onEditPlace(position, placeData);  // 위치와 데이터를 전달
                }
            });
        }

        private void showAddOrEditNoteDialog(int position) {
            // 메모를 추가하거나 편집할 수 있는 다이얼로그를 표시
            View dialogView = LayoutInflater.from(context).inflate(R.layout.popup_add_memo, null);
            EditText noteEditText = dialogView.findViewById(R.id.noteEditText);
            Button saveNoteButton = dialogView.findViewById(R.id.saveNoteButton);

            // 현재 메모를 에디트텍스트에 표시
            noteEditText.setText(notes.get(position));

            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setView(dialogView)
                    .create();

            saveNoteButton.setOnClickListener(v -> {
                String newNote = noteEditText.getText().toString().trim();
                if (!newNote.isEmpty()) {
                    // 새 메모를 리스트에 저장
                    notes.set(position, newNote);
                    notifyItemChanged(position);

                    // PlaceData 객체를 업데이트
                    placeList.setNotes(notes);
                    placeList.setPlaceName(placeNames); // 수정된 장소명도 함께 저장

                    dialog.dismiss();
                } else {
                    noteEditText.setError("Note cannot be empty");
                }
            });

            dialog.show();
        }

        private void removePlace(int position) {
            // 선택한 장소 삭제
            placeNames.remove(position);
            notes.remove(position);
            placeList.setPlaceName(placeNames);
            placeList.setNotes(notes);

            notifyItemRemoved(position);
            notifyItemRangeChanged(position, placeNames.size());
        }
    }
}
