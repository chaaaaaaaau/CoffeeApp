package com.example.chaaaaau.coffeeapp;

import android.content.Context;
import android.media.Rating;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

public class CoffeeDataListAdapter extends BaseAdapter {
    ArrayList<CoffeeRecordData> recordData = new ArrayList<>();
    private Context context;
    public CoffeeDataListAdapter(Context context, ArrayList<CoffeeRecordData> data) {
        super();
        this.context = context;
        this.recordData = data;
    }

    @Override
    public int getCount() {
        return recordData.size();
    }

    @Override
    public Object getItem(int position) {
        return recordData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_layout, parent, false);
        }
        TextView view = convertView.findViewById(R.id.dbCoffeeRecord);
        RatingBar ratingBar = convertView.findViewById(R.id.dbRatingBarOverall);

        view.setText(recordData.get(position).coffeeName);
        ratingBar.setRating(recordData.get(position).ratingValue);

        return convertView;
    }
}
