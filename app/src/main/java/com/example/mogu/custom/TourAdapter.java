package com.example.mogu.custom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mogu.R;
import com.example.mogu.object.TourApi;

import java.util.ArrayList;

public class TourAdapter extends RecyclerView.Adapter<TourAdapter.ViewHolder> {
    private ArrayList<TourApi> items;

    public TourAdapter(ArrayList<TourApi> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public TourAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_tourapi, parent, false);
        return new TourAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TourAdapter.ViewHolder holder, int position) {
        TourApi item = items.get(position);
        holder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgFirstImage;
        private TextView txtTitle;
        private TextView txtAddr1;
        private TextView txtAddr2;

        public ViewHolder(View itemView) {
            super(itemView);
            imgFirstImage = itemView.findViewById(R.id.imgFirstImage);
            txtTitle = itemView.findViewById(R.id.tvTitle);
            txtAddr1 = itemView.findViewById(R.id.tvAddr1);
            txtAddr2 = itemView.findViewById(R.id.tvAddr2);
        }

        public void setItem(TourApi item) {
            Glide.with(imgFirstImage.getContext())
                    .load(item.getFirstimage())
                    .error(R.drawable.ic_launcher_foreground)
                    .into(imgFirstImage);
            txtTitle.setText(item.getTitle());
            txtAddr1.setText(item.getAddr1());
            txtAddr2.setText(item.getAddr2());
        }
    }
}
