package com.incrementaventures.okey.Bluetooth;


import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.text.format.Time;

import java.util.LinkedList;

public class BluetoothProtocol {
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    public static final String SEPARATOR = ";";
    public static final String MESSAGE_END = "*";

    public static final String OPEN_CLOSE_MESSAGE_CODE = "01";
    public static final String MODIFY_PERMISSIONS_MESSAGE_CODE = "02";
    public static final String FIRST_CONFIGURATION_MESSAGE_CODE = "03";
    public static final String INDIVIDUAL_PERMISSION_RESPONSE_CODE = "04";
    public static final String ALL_PERMISSIONS_RESPONSE_CODE = "05";
    public static final String PERMISSION_CREATED_OR_MODIFY_RESPONSE_CODE = "06";
    public static final String DOOR_OPENED_RESPONSE_CODE = "07";

    public static final int ADMIN_PERMISSION = 0;
    public static final int PERMANENT_PERMISSION = 1;
    public static final int TEMPORAL_PERMISSION = 2;

    public static final int SUCCESS = 1;
    public static final int ERROR = 0;

    public static final int CREATE_NEW_PERMISSION_CODE = 0;
    public static final int MODIFY_PERMISSION_CODE = 1;
    public static final int DELETE_PERMISSION_CODE = 2;



    public static final String DOOR_SERVICE_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String CHARACTERISTIC_WRITE_UUID = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String CHARACTERISTIC_NOTIFICATION_UUID="6e400003-b5a3-f393-e0a9-e50e24dcca9e";

    public static final String CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID ="00002902-0000-1000-8000-00805f9b34fb";


    protected static void sendMessage(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] part){
        characteristic.setValue(part);
        gatt.writeCharacteristic(characteristic);
    }


    public static String buildOpenMessage(String permissionKey){

        StringBuilder builder = new StringBuilder();

        builder.append(OPEN_CLOSE_MESSAGE_CODE);
        builder.append(SEPARATOR);

        Time now = new Time();
        now.setToNow();

        builder.append(formatDate(now));
        builder.append(SEPARATOR);

        builder.append(permissionKey);
        builder.append(SEPARATOR);

        builder.append(MESSAGE_END);

        return builder.toString();
    }

    public static String buildFirstConfigurationMessage(String permissionKey, String factoryKey, String doorName){
        StringBuilder builder = new StringBuilder();

        builder.append(FIRST_CONFIGURATION_MESSAGE_CODE);
        builder.append(SEPARATOR);

        Time now = new Time();
        now.setToNow();

        builder.append(formatDate(now));
        builder.append(SEPARATOR);

        builder.append(permissionKey);
        builder.append(SEPARATOR);

        builder.append(factoryKey);
        builder.append(SEPARATOR);

        builder.append(doorName);
        builder.append(SEPARATOR);

        builder.append(MESSAGE_END);

        return builder.toString();
    }

    public static String buildNewPermissionMessage(String permissionType, String endDate, String endHour, String adminKey) {
        StringBuilder builder = new StringBuilder();

        builder.append(MODIFY_PERMISSIONS_MESSAGE_CODE);
        builder.append(SEPARATOR);

        Time time = new Time();
        time.setToNow();
        builder.append(formatDate(time));
        builder.append(SEPARATOR);

        builder.append(adminKey);
        builder.append(SEPARATOR);

        builder.append(getPermissionType(permissionType));
        builder.append(SEPARATOR);

        builder.append(CREATE_NEW_PERMISSION_CODE);
        builder.append(SEPARATOR);

        builder.append(endDate + "T" + endHour);
        builder.append(SEPARATOR);

        builder.append(MESSAGE_END);

        return builder.toString();
    }

    public static int getPermissionType(String p){
        switch (p){
            case "Temporal":
                return 2;
            case "Permanent":
                return 1;
            case "Administrator":
                return 0;
            default:
                return 3;
        }
    }


    private static String formatDate(Time now){
        String month = String.valueOf(now.month);
        String day = String.valueOf(now.month);
        String hour = String.valueOf(now.month);
        String minute = String.valueOf(now.month);

        if (month.length() == 1) month = "0" + month;
        if (day.length() == 1) day = "0" + day;
        if (hour.length() == 1) hour = "0" + hour;
        if (minute.length() == 1) minute = "0" + minute;

        return now.year + "-" + month + "-" + day + "T" + hour + ":" + minute;

    }

    public static LinkedList<byte[]> separateMessage(String message){
        byte[] fullMessage = message.getBytes();
        LinkedList<byte[]> parts = new LinkedList<>();
        int numberParts = fullMessage.length / 20 + 1;
        for (int i = 0; i < numberParts; i++){
            if (i == numberParts - 1){
                byte[] part = new byte[fullMessage.length - 20*i];
                System.arraycopy(fullMessage, i*20, part, 0, fullMessage.length - 20*i);
                parts.offer(part);
            }
            else{
                byte[] part = new byte[20];
                System.arraycopy(fullMessage, i*20, part, 0, 20);
                parts.offer(part);
            }
        }
        return parts;
    }

    /*
        Process the incomming permission created response. Returns the key if succeeded and null otherwise.
     */
    public static String processPermissionCreationResponse(String response){
        String[] parts = response.split(SEPARATOR);

        String resultCode = parts[1];
        if (resultCode.equals(String.valueOf(ERROR))){
            return null;
        }
        else if (resultCode.equals(String.valueOf(SUCCESS))){
            return parts[2];
        }
        else{
            return null;
        }
    }

    public static boolean processOpenDoorResponse(String response){
        String[] parts = response.split(SEPARATOR);

        String resultCode = parts[1];
        if (resultCode.equals(String.valueOf(ERROR))){
            return false;
        }
        else if (resultCode.equals(String.valueOf(SUCCESS))){
            return true;
        }
        else{
            return false;
        }
    }

    public static String getResponseCode(String response){
        return response.substring(0, 2);
    }


}
