package com.example.mogu.custom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;  // Import SwitchCompat from AndroidX
import androidx.recyclerview.widget.RecyclerView;

import com.example.mogu.R;

import java.util.List;

public class SafeAdapter extends RecyclerView.Adapter<SafeAdapter.ViewHolder> {

    private final List<SafeItem> safeItems;
    private final OnSwitchCheckedChangeListener listener;

    public SafeAdapter(List<SafeItem> safeItems, OnSwitchCheckedChangeListener listener) {
        this.safeItems = safeItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_switch, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SafeItem item = safeItems.get(position);
        holder.switchTitle.setText(item.getTitle());
        holder.switchCompat.setChecked(item.isChecked());  // Set the checked state
        holder.switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setChecked(isChecked);  // Update the checked state in the item
            if (listener != null) {
                listener.onSwitchCheckedChanged(position, isChecked);  // Notify listener
            }
        });
    }

    @Override
    public int getItemCount() {
        return safeItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView switchTitle;
        public SwitchCompat switchCompat;

        public ViewHolder(View itemView) {
            super(itemView);
            switchTitle = itemView.findViewById(R.id.switchTitle);
            switchCompat = itemView.findViewById(R.id.switchCompat);  // Find SwitchCompat by ID
        }
    }

    public interface OnSwitchCheckedChangeListener {
        void onSwitchCheckedChanged(int position, boolean isChecked);  // Listener for switch changes
    }
}
