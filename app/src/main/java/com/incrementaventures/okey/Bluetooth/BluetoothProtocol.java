package com.incrementaventures.okey.Bluetooth;


import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import java.nio.ByteBuffer;

public class BluetoothProtocol {
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;



    public static final String DEVICE_NAME = "Okey";
    public static final String DOOR_SERVICE_UUID = "00001111-0000-1000-8000-00805f9b34fb";
    public static final String STATE_CHARACTERISTIC_UUID = "00002222-00000-1000-8000-00805f9b34fb";
    public static final String CONFIGURED_CHARACTERISTIC_UUID = "00003333-00000-1000-8000-00805f9b34fb";

    protected static boolean isDoorOpened(byte[] bytes){
        if (bytes.length == 1){
            int value = bytes[0];
            if (value == 1){
                return true;
            }
            return false;
        }
        int value = java.nio.ByteBuffer.wrap(bytes).getInt();
        // If door is opened
        if (value == 1){
            return true;
        }
        return false;
    }

    protected static void openDoor(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
        int openedValue = 1;
        byte[] newBytes = ByteBuffer.allocate(4).putInt(openedValue).array();
        characteristic.setValue(newBytes);
        gatt.writeCharacteristic(characteristic);
    }

    protected static void closeDoor(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
        int closedValue = 0;
        byte[] newBytes = ByteBuffer.allocate(4).putInt(closedValue).array();
        characteristic.setValue(newBytes);
        gatt.writeCharacteristic(characteristic);
    }

    protected static boolean wasNeverConnected(byte[] bytes){
        if (bytes.length == 1){
            int value = bytes[0];
            if (value == 1){
                return false;
            }
            return true;
        }
        int value = java.nio.ByteBuffer.wrap(bytes).getInt();
        // If door is opened
        if (value == 1){
            return false;
        }
        return true;
    }

    protected static void makeFirstAdminConnection(String phone, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){

    }
}
