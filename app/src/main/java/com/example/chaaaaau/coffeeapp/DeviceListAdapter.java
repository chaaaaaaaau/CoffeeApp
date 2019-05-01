package com.example.chaaaaau.coffeeapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.LinkedHashMap;

/**
 * Created by Alvin on 15/9/2018.
 */

public class DeviceListAdapter extends BaseAdapter {
    private Context context;
    private LinkedHashMap<String, Device> deviceList;

    //public constructor
    public DeviceListAdapter(@NonNull Context context, LinkedHashMap<String, Device> list) {
        this.context = context;
        this.deviceList = list;
    }

    @Override
    public int getCount() {
        return deviceList.size(); //returns total of items in the list
    }

    @Override
    public Object getItem(int position) {
        return deviceList.values().toArray()[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.row_device_list, parent, false);

        Device currentItem = (Device) getItem(position);

        TextView TV_deviceName = (TextView) convertView.findViewById(R.id.TV_deviceName);
        TextView TV_deviceMac = (TextView) convertView.findViewById(R.id.TV_deviceMac);
        LinearLayout LL_status = (LinearLayout) convertView.findViewById(R.id.LL_status);


        TV_deviceName.setText(currentItem.getDeviceName());
        TV_deviceMac.setText(currentItem.getDeviceAddress());

        if(currentItem.isOnline())
            LL_status.setVisibility(View.VISIBLE);

        return convertView;
    }
}