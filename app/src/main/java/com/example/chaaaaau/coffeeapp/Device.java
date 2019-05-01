package com.example.chaaaaau.coffeeapp;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Alvin on 15/9/2018.
 */
public class Device {

    private BluetoothDevice mDevice;
    private boolean isOnline = false;
    private int deviceRSSI = 0;

    public Device(BluetoothDevice device){
        this.mDevice = device;
    }

    public Device(BluetoothDevice device, int rssi){
        this.mDevice = device;
        this.deviceRSSI = rssi;
        this.isOnline = true;
    }

    public String getDeviceName() {
        if(mDevice.getName() == null)
            return mDevice.getAddress();
        else
            return this.mDevice.getName();
    }

    public String getDeviceAddress() {
        return this.mDevice.getAddress();
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public int getDeviceRSSI() {
        return deviceRSSI;
    }

    public void setDeviceRSSI(int deviceRSSI) {
        this.deviceRSSI = deviceRSSI;
    }

    public BluetoothDevice getDevice(){
        return this.mDevice;
    }

}
