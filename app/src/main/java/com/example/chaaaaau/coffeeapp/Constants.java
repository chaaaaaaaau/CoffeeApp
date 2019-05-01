package com.example.chaaaaau.coffeeapp;

/**
 * Created by Alvin on 14/9/2018.
 */
public interface Constants {

    // Intent request codes
    public static final int REQUEST_CONNECT_DEVICE = 1;
    public static final int REQUEST_ENABLE_BT = 3;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;


    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static final String BUNDLE_MSG_READ = "bundle_msg_read";
    public static final String BUNDLE_MSG_WRITE = "bundle_msg_write";

    /////////////////////////////////////////////////////////////
    public static final int STATE_CONNECTION_LOST = 600;

    // Remote status
    public static final int REMOTE_NORMAL = 11;
    public static final int REMOTE_LOADING= 22;
    public static final int REMOTE_WARNING= 33;

    // ERROR
    public static final int ERROR_BT_NOT_ENABLE = 900;
    public static final int ERROR_BT_SIGNAL_WEAK = 902;


    // Constants that indicate the current connection state
    public static final int STATE_NONE = 50;       // we're doing nothing
    public static final int STATE_LISTEN = 51;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 52; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 53;  // now connected to a remote device
    public static final int STATE_DISCOVERED = 54;
    public static final int STATE_DISCONNECTED = 55;



}