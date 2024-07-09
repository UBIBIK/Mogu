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


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private ArrayList<TourApi> items;

    public MyAdapter(ArrayList<TourApi> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.image, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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

        public ViewHolder(View itemView) {
            super(itemView);
            imgFirstImage = itemView.findViewById(R.id.imageView);
            txtTitle = itemView.findViewById(R.id.textView);
        }

        public void setItem(TourApi item) {
            Glide.with(imgFirstImage.getContext())
                    .load(item.getFirstimage())
                    .error(R.drawable.ic_launcher_foreground)
                    .into(imgFirstImage);
            txtTitle.setText(item.getTitle());
        }
    }
}
