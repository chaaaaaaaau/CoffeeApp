package com.example.chaaaaau.coffeeapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Alvin on 21/9/2018.
 */
public class BluetoothLeHelper implements Constants{

    private Activity mActivity;
    protected static ArrayList<Device> connectedDevice = null;
    protected BluetoothLeService mBluetoothLeService = null;
    protected BluetoothAdapter mBluetoothAdapter = null;
    private Handler mCustomHandler;

    private boolean initiativeDisconnect = false;
    protected static boolean mConnected = false;
    private Handler rssiHandler = null;
    private Runnable rssiRunnable = null;

    public BluetoothLeHelper(Activity activity){
        this.mActivity = activity;
    }
    public BluetoothLeHelper(Activity activity, Handler handler){

        this.mActivity = activity;
        this.mCustomHandler = handler;
    }

    protected void start(){
        connectedDevice = new ArrayList<>();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBluetoothAvailable();
    }

    private void stop(){
        if(mGattUpdateReceiver != null)
        if(mGattUpdateReceiver != null)
            mActivity.unregisterReceiver(mGattUpdateReceiver);
        //if(mServiceConnection != null)
        //    mActivity.unbindService(mServiceConnection);
        if(rssiHandler != null)
            rssiHandler.removeCallbacks(rssiRunnable);
        mBluetoothLeService = null;
    }

    private void checkBluetoothAvailable(){
        if (mBluetoothAdapter == null) {
            Toast.makeText(mActivity, "bluetooth not available", Toast.LENGTH_LONG).show();
            mActivity.finish();
        }
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!mActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(mActivity, "ble not supported", Toast.LENGTH_SHORT).show();
            mActivity.finish();
        }
    }

    protected void checkBluetoothEnable(){
        // Execute in onStart()
        // If BT is not on, request that it be enabled.
        if (mBluetoothAdapter!=null && !mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        if(!mConnected)
            updateState(STATE_NONE);
    }


    protected void startReceiverService(){
        // Execute in onResume()
        mActivity.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    protected void setupConnection(){
        Intent serverIntent = new Intent(mActivity, DeviceListActivity.class);
        mActivity.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                //System.out.println("[CONNECTED NOW !!!!!!!!!!!!!!!!!!!!!!!!!!!]");
                updateState(STATE_CONNECTED);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                //System.out.println("[DISCONNECTED NOW !!!!!!!!!!!!!!!!!!!!!!!!!!!]");
                mConnected = false;
                connectedDevice = null;
                if(initiativeDisconnect)
                    updateState(STATE_NONE);
                else
                    updateState(STATE_CONNECTION_LOST);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // All the supported services and characteristics ready for access.
                setServiceProperties();
                startRssiListener();
                updateState(STATE_DISCOVERED);
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String msg = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                updateStateWithBundle(MESSAGE_READ, Constants.BUNDLE_MSG_READ ,msg);
            }
        }
    };

    protected void connectDevice(Intent data) {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        int rssiValue = data.getExtras().getInt(DeviceListActivity.EXTRA_DEVICE_RSSI);
        // Get the BluetoothDevice object
        BluetoothDevice btDevice = mBluetoothAdapter.getRemoteDevice(address);
        connectedDevice.add(new Device(btDevice, rssiValue));
        // Attempt to connect to the device
        setupRemote();
    }

    protected void disconnectDevice() {
        initiativeDisconnect = true;
        connectedDevice = null;
        mBluetoothLeService.disconnect();
        updateState(STATE_NONE);
        this.stop();
    }

    ArrayList<Intent> gattServiceIntentList = new ArrayList<>();

    private void setupRemote(){
        // Initialize the BluetoothService to perform bluetooth connections
        int index = gattServiceIntentList.size();
        gattServiceIntentList.add(new Intent(mActivity, BluetoothLeService.class));
        //Intent gattServiceIntent = new Intent(mActivity, BluetoothLeService.class);
        mActivity.bindService(gattServiceIntentList.get(index),new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {

                String TAG = "[BluetoothHelper]";
                mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
                if (!mBluetoothLeService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    mActivity.finish();
                }
                mBluetoothLeService.setConnectionChangeHandler(mCustomHandler);
                //mBluetoothLeService.setBluetoothServerRespond((BluetoothServerRespond) mActivity);
                initiativeDisconnect = false;

                // Automatically connects to the device upon successful start-up initialization.
                Boolean result = mBluetoothLeService.connect(connectedDevice.get(connectedDevice.size()-1).getDeviceAddress()); // **********************************
                System.out.println("================================================================================================"+result);
                if(!result)
                    connectedDevice = null;
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mBluetoothLeService = null;
            }
        }, Context.BIND_AUTO_CREATE);
        System.out.println("=============================================================================================================" + String.valueOf(index));

    }

    // Code to manage Service lifecycle.
    /*
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {

            String TAG = "[BluetoothHelper]";
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                mActivity.finish();
            }
            mBluetoothLeService.setConnectionChangeHandler(mCustomHandler);
            //mBluetoothLeService.setBluetoothServerRespond((BluetoothServerRespond) mActivity);
            initiativeDisconnect = false;

            // Automatically connects to the device upon successful start-up initialization.
            Boolean result = mBluetoothLeService.connect(connectedDevice.get(connectedDevice.size()-1).getDeviceAddress()); // **********************************
            System.out.println("================================================================================================"+result);
            if(!result)
                connectedDevice = null;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    */
    private void setServiceProperties(){

        for (String address : mBluetoothLeService.mBluetoothDeviceAddress) {
            BluetoothGatt myGatt = mBluetoothLeService.getSupportGatt(address);
            BluetoothGattService Service = myGatt.getService(UUID.fromString(GattAttributes.ATTR_CUSTOM_SERVICE));
            BluetoothGattCharacteristic characteristic = Service.getCharacteristic(UUID.fromString(GattAttributes.ATTR_CUSTOM_CHARACTERISTIC));
            GattAttributes.mCustomCharacteristic = characteristic;

            final int charaProp = characteristic.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                mBluetoothLeService.readCharacteristic(characteristic, address);
            }
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                mBluetoothLeService.setCharacteristicNotification(characteristic, true, address);
            }
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                mBluetoothLeService.setCharacteristicNotification(characteristic, true, address);
            }
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    protected void sendMessage(String message, String address) {
        if(mConnected == true) {
            System.out.println("message==>"+message);
            GattAttributes.mCustomCharacteristic.setValue(message.getBytes());
            mBluetoothLeService.writeCharacteristic(GattAttributes.mCustomCharacteristic, address);
        }
    }

    private void startRssiListener(){
        rssiHandler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                rssiRunnable = new Runnable(){
                    public void run(){
                        if(mBluetoothLeService != null){
                            int last = mBluetoothLeService.mBluetoothDeviceAddress.size()-1;
                            String addr = mBluetoothLeService.mBluetoothDeviceAddress.get(last);
                            mBluetoothLeService.getSupportGatt(addr).readRemoteRssi();
                        }

                        if(mConnected == true)
                            rssiHandler.postDelayed(this, 1000);
                    }
                };
                rssiHandler.postDelayed(rssiRunnable, 1000);
            }
        }).start();
    }

    protected void updateState(int stateType){
        mCustomHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, stateType, -1).sendToTarget();
    }

    public void updateStateWithBundle(int stateType, String bundleKey, String bundleValue){
        Message msg = mCustomHandler.obtainMessage(stateType);
        if(bundleKey!=null && bundleValue != null) {
            Bundle bundle = new Bundle();
            bundle.putString(bundleKey, bundleValue);
            msg.setData(bundle);
        }
        mCustomHandler.sendMessage(msg);
    }
}