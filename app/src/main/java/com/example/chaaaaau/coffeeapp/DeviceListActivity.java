package com.example.chaaaaau.coffeeapp;

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.LinkedHashMap;

/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
public class DeviceListActivity extends Activity {

    private static final String TAG = "DeviceListActivity";

    public static String EXTRA_DEVICE_NAME = "device_name";
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static String EXTRA_DEVICE_RSSI = "device_RSSI";
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mScannerHandler = null;

    private DeviceListAdapter mDevicesArrayAdapter;
    private LinkedHashMap<String, Device> mDevicesArray = new LinkedHashMap<>();

    private TextView TV_title_pairedDevices, TV_title_newDevices, Btn_scan;
    private TextView TV_none_device1, TV_none_device2;
    private ImageView Btn_close;
    private ListView LV_Devices;
    private ProgressBar PB_scanning;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        //TV_title_pairedDevices = (TextView) findViewById(R.id.TV_title_pairedDevices);
        //TV_title_newDevices = (TextView) findViewById(R.id.TV_title_newDevices);
        LV_Devices = (ListView) findViewById(R.id.LV_newDevices);
        Btn_scan = (TextView) findViewById(R.id.Btn_scan);
        PB_scanning = (ProgressBar) findViewById(R.id.PB_scanning);
        Btn_close = (ImageView) findViewById(R.id.Btn_close);
        //TV_none_device1 = (TextView) findViewById(R.id.TV_none_device1);
        TV_none_device2 = (TextView) findViewById(R.id.TV_none_device2);

        // Set result CANCELED in case the user backs out
        //setResult(Activity.RESULT_CANCELED);

        // Initialize the button to perform device discovery
        Btn_scan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScanning();
            }
        });
        Btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Initialize array adapters. One for newly discovered devices
        mDevicesArrayAdapter = new DeviceListAdapter(this, mDevicesArray);

        // Find and set up the ListView for newly discovered devices
        LV_Devices.setAdapter(mDevicesArrayAdapter);
        LV_Devices.setOnItemClickListener(mDeviceClickListener);

        // Get the local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mScannerHandler = new Handler();
    }

    private void enableLocationService() {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }else{
            if( !areLocationServicesEnabled(getApplicationContext()) ) {
                new AlertDialog.Builder(DeviceListActivity.this)
                        .setTitle("Location request")
                        .setMessage("Android 6.0+ required to enabled location service to find the near by BLE devices.")
                        .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(gpsOptionsIntent);
                            }
                        })
                        .show();
            }
        }
    }
    public boolean areLocationServicesEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                AlertDialog d1 = new AlertDialog.Builder(DeviceListActivity.this)
                        .setMessage("You must enable location service")
                        .show();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                d1.dismiss();
                //enableLocationService();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        //enableLocationService();
        startScanning();
    }

    private void startScanning(){
        LV_Devices.setVisibility(View.VISIBLE);
        TV_none_device2.setVisibility(View.GONE);
        mDevicesArray.clear();
        mDevicesArrayAdapter.notifyDataSetInvalidated();

        scanLeDevice(true);
        Btn_scan.setVisibility(View.GONE);
        PB_scanning.setVisibility(View.VISIBLE);
    }


    private void stopScanning(){
        mScanning = false;
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mDevicesArray.size() == 0){
                    LV_Devices.setVisibility(View.GONE);
                    TV_none_device2.setVisibility(View.VISIBLE);
                }
                Btn_scan.setVisibility(View.VISIBLE);
                PB_scanning.setVisibility(View.GONE);
            }
        });
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mDevicesArray.clear();
            // Stops scanning after a pre-defined scan period.
            mScannerHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScanning();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            stopScanning();
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!mDevicesArray.containsKey(device.getAddress())) {
                        mDevicesArray.put(device.getAddress(), new Device(device, rssi));
                        mDevicesArrayAdapter.notifyDataSetChanged();
                    }else{
                        mDevicesArray.get(device.getAddress()).setDeviceRSSI(rssi);
                    }
                }
            });
        }
    };


    /**
     * The on-click listener for all devices in the ListViews
     */
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String address = ((TextView) view.findViewById(R.id.TV_deviceMac)).getText().toString();
            final BluetoothDevice device = mDevicesArray.get(address).getDevice();
            if (device == null) return;

            System.out.println("-----------------------> connect to -->" + device.getAddress());
            final Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_RSSI, mDevicesArray.get(address).getDeviceRSSI());
            intent.putExtra(EXTRA_DEVICE_ADDRESS, device.getAddress());
            if (mScanning) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                mScanning = false;
            }
            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mDevicesArray.clear();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
        System.out.println("-----------[DeviceListActivity]------onDestroy()---------------------");
    }

}