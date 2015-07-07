package com.incrementaventures.okey.Bluetooth;


import android.bluetooth.BluetoothAdapter;

public class BluetoothClient {

    private BluetoothAdapter mBluetoothAdapter;

    public BluetoothClient(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Check if the device supports bluetooth
     * @return true if it's supported, false otherwise
     */
    public boolean isSupported(){
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            return false;
        }
        return true;
    }

    /**
     * Check if the device has the bluetooth enabled.
     * If it isn't enabled, you must start dialog to activate it.
     * @return true if its enabled, false otherwise
     */
    public boolean isEnabled(){
        if (!mBluetoothAdapter.isEnabled()){
            return false;
        }
        return true;
    }


}
