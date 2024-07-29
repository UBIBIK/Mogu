package com.example.mogu.custom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import com.example.mogu.R;
import java.util.ArrayList;

public class DaysPagerAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<String> daysList;

    public DaysPagerAdapter(Context context, ArrayList<String> daysList) {
        this.context = context;
        this.daysList = daysList;
    }

    @Override
    public int getCount() {
        return daysList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.day_item, container, false);
        TextView dayTextView = view.findViewById(R.id.dayTextView);
        dayTextView.setText(daysList.get(position));
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
