package com.example.chaaaaau.coffeeapp;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.HashMap;

/**
 * Created by Alvin on 21/9/2018.
 */
public class GattAttributes {

    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public static String ATTR_CUSTOM_SERVICE = "0000FFE0-0000-1000-8000-00805F9B34FB";
    public static String ATTR_CUSTOM_CHARACTERISTIC = "0000FFE1-0000-1000-8000-00805F9B34FB";

    public static BluetoothGattCharacteristic mCustomCharacteristic = null;

    static {
        /*
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        */

        //SERVICE 1
        attributes.put("00001800-0000-1000-8000-00805F9B34FB", "GENERIC ACCESS");
        //SERVICE 1 Characteristics.
        attributes.put("00002A00-0000-1000-8000-00805F9B34FB", "DEVICE NAME");
        attributes.put("00002A01-0000-1000-8000-00805F9B34FB", "APPEARANCE");
        attributes.put("00002A02-0000-1000-8000-00805F9B34FB", "PERIPHERAL PRIVACY FLAG");
        attributes.put("00002A03-0000-1000-8000-00805F9B34FB", "RECONNECTION ADDRESS");
        attributes.put("00002A04-0000-1000-8000-00805F9B34FB", "PERIPHERAL PREFERRED CONNECTION PARAMETERS");

        //SERVICE 2
        attributes.put("00001801-0000-1000-8000-00805F9B34FB", "GENERIC ATTRIBUTE");
        //SERVICE 2 Characteristics.
        attributes.put("00002A05-0000-1000-8000-00805F9B34FB", "SERVICE CHANGED");

        //SERVICE 3
        attributes.put("0000FFE0-0000-1000-8000-00805F9B34FB", "CUSTOM SERVICE");
        //SERVICE 3 Characteristics.
        attributes.put("0000FFE1-0000-1000-8000-00805F9B34FB", "CUSTOM CHARACTERISTIC");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
