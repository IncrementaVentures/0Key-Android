package com.incrementaventures.okey.Bluetooth;

import android.text.format.Time;

import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.Slave;
import com.incrementaventures.okey.Models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;


public class BluetoothProtocol {
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    public static final String SEPARATOR = ";";
    public static final String ITEM_SEPARATOR = "&";
    public static final String MESSAGE_END = "*";
    public static final String EMPTY = "0";

    public static final String OPEN_CLOSE_MESSAGE_CODE = "01";
    public static final String MODIFY_PERMISSIONS_MESSAGE_CODE = "02";
    public static final String FIRST_CONFIGURATION_MESSAGE_CODE = "03";
    public static final String INDIVIDUAL_PERMISSION_RESPONSE_CODE = "04";
    public static final String ALL_PERMISSIONS_RESPONSE_CODE = "05";
    public static final String PERMISSION_CREATED_OR_MODIFY_RESPONSE_CODE = "06";
    public static final String DOOR_OPENED_RESPONSE_CODE = "07";
    public static final String GET_USER_PERMISSION_MESSAGE_CODE = "08";
    public static final String GET_ALL_PERMISSIONS_MESSAGE_CODE = "09";
    public static final String GET_SLAVES_MESSAGE_CODE = "10";
    public static final String GET_SLAVES_RESPONSE_CODE = "11";
    public static final String PAIR_SLAVES_MESSAGE_CODE = "12";

    public static final String OK_ERROR_CODE = "0";
    public static final String NO_PERMISSION_ERROR_CODE = "1";
    public static final String PERMISSION_EXPIRED_ERROR_CODE = "2";
    public static final String NO_PERMISSION_THIS_HOUR_ERROR_CODE = "3";
    public static final String MASTER_CANT_PROCESS_INPUT_ERROR_CODE = "4";
    public static final String NO_ADMIN_PERMISSION_ERROR_CODE = "5";
    public static final String DOOR_NOT_CONFIGURED_ERROR_CODE = "6";

    public static final int ADMIN_PERMISSION = 0;
    public static final int PERMANENT_PERMISSION = 1;
    public static final int TEMPORAL_PERMISSION = 2;

    public static final int SUCCESS = 1;
    public static final int ERROR = 0;

    public static final int CREATE_NEW_PERMISSION_CODE = 0;
    public static final int MODIFY_PERMISSION_CODE = 1;
    public static final int DELETE_PERMISSION_CODE = 2;

    public static final String
            DOOR_SERVICE_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String
            CHARACTERISTIC_WRITE_UUID = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String
            CHARACTERISTIC_NOTIFICATION_UUID="6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String
            CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID ="00002902-0000-1000-8000-00805f9b34fb";

    public static String buildOpenMessage(String permissionKey, int permissionSlaveId, int slaveId){

        StringBuilder builder = new StringBuilder();
        Time now = new Time();
        now.setToNow();

        // "01;"
        builder.append(OPEN_CLOSE_MESSAGE_CODE);
        builder.append(SEPARATOR);

        // "01;date;"
        builder.append(formatDate(now));
        builder.append(SEPARATOR);

        // "01;date;key;"
        builder.append(permissionKey);
        builder.append(SEPARATOR);

        // "01;date;key;adminSlaveId;"
        builder.append(permissionSlaveId);
        builder.append(SEPARATOR);

        // "01;date;key;adminSlaveId;slaveId;
        builder.append(slaveId);
        builder.append(SEPARATOR);

        // "01;date;key;adminSlaveId;slaveId;1;
        builder.append(1);
        builder.append(SEPARATOR);

        // "01;date;key;adminSlaveId;slaveId;1;*
        builder.append(MESSAGE_END);

        return builder.toString();
    }

    public static String buildFirstConfigurationMessage(String permissionKey, String factoryKey,
                                                        String doorName){
        StringBuilder builder = new StringBuilder();
        Time now = new Time();
        now.setToNow();

        // "03;"
        builder.append(FIRST_CONFIGURATION_MESSAGE_CODE);
        builder.append(SEPARATOR);

        // "03;date;"
        builder.append(formatDate(now));
        builder.append(SEPARATOR);

        // "03;date;key;"
        builder.append(permissionKey);
        builder.append(SEPARATOR);

        // "03;date;key;factoryKey;"
        builder.append(factoryKey);
        builder.append(SEPARATOR);

        // "03;date;key;factoryKey;doorName;"
        builder.append(doorName);
        builder.append(SEPARATOR);

        // "03;date;key;factoryKey;doorName;*"
        builder.append(MESSAGE_END);

        return builder.toString();
    }

    public static String buildNewPermissionMessage(Permission permission, String adminKey) {
        StringBuilder builder = new StringBuilder();
        Time time = new Time();
        time.setToNow();

        // "02;"
        builder.append(MODIFY_PERMISSIONS_MESSAGE_CODE);
        builder.append(SEPARATOR);

        // "02;date;"
        builder.append(formatDate(time));
        builder.append(SEPARATOR);

        // "02;date;key;"
        builder.append(adminKey);
        builder.append(SEPARATOR);

        // "02;date;key;0;"
        builder.append(EMPTY);
        builder.append(SEPARATOR);

        // "02;date;key;0;"
        builder.append(EMPTY);
        builder.append(SEPARATOR);

        // "02;date;key;0;0;"
        builder.append(permission.getSlaveId());
        builder.append(SEPARATOR);

        // "02;date;key;0;0;slaveid;0;"
        builder.append(CREATE_NEW_PERMISSION_CODE);
        builder.append(SEPARATOR);

        int type = Permission.getType(permission.getType());
        // "02;date;key;0;0;slaveid;0;permissionType;"
        builder.append(type);
        builder.append(SEPARATOR);

        if (type == TEMPORAL_PERMISSION) {
            // "02;date;key;0;0;slaveid;0;permissionType;startDateTstartHour;"
            builder.append(permission.getStartDate());
            builder.append(SEPARATOR);
            // "02;date;key;0;0;slaveid;0;permissionType;startDateTstartHour;endDateTendHour;"
            builder.append(permission.getEndDate());
            builder.append(SEPARATOR);
        } else {
            // "02;date;key;0;0;slaveid;0;permissionType;startDateTstartHour;0;"
            builder.append(permission.getStartDate());
            builder.append(SEPARATOR);
            builder.append(EMPTY);
            builder.append(SEPARATOR);
        }
        // "02;date;key;0;0;slaveid;0;permissionType;startDateTstartHour;endDateTendHour;*"
        builder.append(MESSAGE_END);
        return builder.toString();
    }

    public static String buildEditPermissionMessage(Permission toEditPermission, int oldSlaveId,
                                                    String adminKey) {
        StringBuilder builder = new StringBuilder();
        Time time = new Time();
        time.setToNow();

        // "02;"
        builder.append(MODIFY_PERMISSIONS_MESSAGE_CODE).append(SEPARATOR);
        // "02;date;"
        builder.append(formatDate(time)).append(SEPARATOR);
        // "02;date;key;"
        builder.append(adminKey).append(SEPARATOR);
        // "02;date;key;slaveId;"
        builder.append(toEditPermission.getKey()).append(SEPARATOR);
        // "02;date;key;slaveId;permissionKey;"
        builder.append(oldSlaveId).append(SEPARATOR);
        // "02;date;key;slaveId;permissionKey;oldSlaveId;"
        builder.append(toEditPermission.getSlaveId()).append(SEPARATOR);
        // "02;date;key;slaveId;permissionKey;oldSlaveId;newSlaveId;1;"
        builder.append(MODIFY_PERMISSION_CODE).append(SEPARATOR);

        int type = Permission.getType(toEditPermission.getType());
        // "02;date;key;slaveId;permissionKey;oldSlaveId;newSlaveId;1;permissionType;"
        builder.append(type).append(SEPARATOR);

        if (type == TEMPORAL_PERMISSION){
            // "02;date;key;slaveId;permissionKey;oldSlaveId;newSlaveId;1;permissionType;start;"
            builder.append(toEditPermission.getStartDate()).append(SEPARATOR);
            // "02;date;key;slaveId;permissionKey;oldSlaveId;newSlaveId;1;permissionType;start;end;"
            builder.append(toEditPermission.getEndDate()).append(SEPARATOR);
        } else {
            // "02;date;key;slaveId;permissionKey;oldSlaveId;newSlaveId;1;permissionType;start;"
            builder.append(toEditPermission.getStartDate()).append(SEPARATOR);
            // "02;date;key;slaveId;permissionKey;oldSlaveId;newSlaveId;1;permissionType;start;0;"
            builder.append(EMPTY).append(SEPARATOR);
        }
        // "02;date;key;slaveId;permissionKey;oldSlaveId;newSlaveId;1;permissionType;start;end;*"
        builder.append(MESSAGE_END);
        return builder.toString();
    }

    public static String buildDeletePermissionMessage(String adminKey, Permission permission) {
        StringBuilder builder = new StringBuilder();
        Time time = new Time();
        time.setToNow();

        // "02;"
        builder.append(MODIFY_PERMISSIONS_MESSAGE_CODE).append(SEPARATOR);
        // "02;date;"
        builder.append(formatDate(time)).append(SEPARATOR);
        // "02;date;adminKey;"
        builder.append(adminKey).append(SEPARATOR);
        // "02;date;adminKey;permissionKey;"
        builder.append(permission.getKey()).append(SEPARATOR);
        // "02;date;adminKey;permissionKey;"
        builder.append(permission.getSlaveId()).append(SEPARATOR);
        // "02;date;adminKey;permissionKey;0"
        builder.append(EMPTY).append(SEPARATOR);
        // "02;date;adminKey;permissionKey;0;2"
        builder.append(DELETE_PERMISSION_CODE).append(SEPARATOR);
        // "02;date;adminKey;permissionKey;0;2;0;"
        builder.append(EMPTY).append(SEPARATOR);
        // "02;date;adminKey;permissionKey;0;2;0;0;"
        builder.append(EMPTY).append(SEPARATOR);
        // "02;date;adminKey;permissionKey;0;2;0;0;0;"
        builder.append(EMPTY).append(SEPARATOR);
        // "02;date;adminKey;permissionKey;0;2;0;0;0;*"
        builder.append(MESSAGE_END).append(SEPARATOR);

        return builder.toString();
    }

    public static String buildGetUserPermissionMessage(int slaveId, String permissionKey) {
        StringBuilder builder = new StringBuilder();
        Time time = new Time();
        time.setToNow();

        // "08;"
        builder.append(GET_USER_PERMISSION_MESSAGE_CODE);
        builder.append(SEPARATOR);

        // "08;date;"
        builder.append(formatDate(time));
        builder.append(SEPARATOR);

        // "08;date;key;"
        builder.append(permissionKey);
        builder.append(SEPARATOR);

        builder.append(slaveId);
        builder.append(SEPARATOR);

        // "08;date;key;slaveId;*"
        builder.append(MESSAGE_END);

        return builder.toString();
    }

    public static String buildGetAllPermissionsMessage(int slaveId, String permissionKey) {
        StringBuilder builder = new StringBuilder();
        Time time = new Time();
        time.setToNow();

        // "09;"
        builder.append(GET_ALL_PERMISSIONS_MESSAGE_CODE);
        builder.append(SEPARATOR);

        // "09;date;"
        builder.append(formatDate(time));
        builder.append(SEPARATOR);

        // "09;date;key;"
        builder.append(permissionKey);
        builder.append(SEPARATOR);

        // "09;date;key;slaveId;"
        builder.append(slaveId);
        builder.append(SEPARATOR);

        // "09;date;key;slaveId;*"
        builder.append(MESSAGE_END);

        return builder.toString();
    }

    public static String buildGetSlavesMessage(String permissionKey) {
        StringBuilder builder = new StringBuilder();
        Time time = new Time();
        time.setToNow();

        // "10;"
        builder.append(GET_SLAVES_MESSAGE_CODE);
        builder.append(SEPARATOR);

        // "10;date;"
        builder.append(formatDate(time));
        builder.append(SEPARATOR);

        // "10;date;key;"
        builder.append(permissionKey);
        builder.append(SEPARATOR);

        // "10;date;key;*"
        builder.append(MESSAGE_END);

        return builder.toString();
    }

    public static String buildPairSlavesMessage(String permissionKey, int idKeySlave,
                                                int idPairSlave) {
        StringBuilder builder = new StringBuilder();
        Time time = new Time();
        time.setToNow();

        // "10;"
        builder.append(PAIR_SLAVES_MESSAGE_CODE);
        builder.append(SEPARATOR);

        // "10;date;"
        builder.append(formatDate(time));
        builder.append(SEPARATOR);

        // "10;date;key;"
        builder.append(permissionKey);
        builder.append(SEPARATOR);

        // "10;date;key;idKeySlave;"
        builder.append(idKeySlave);
        builder.append(SEPARATOR);

        // "10;date;key;idKeySlave;idPairSlave;"
        builder.append(idPairSlave);
        builder.append(SEPARATOR);

        // "10;date;key;*"
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

    public static String formatDate(Time now){
        String month = String.valueOf(now.month + 1);
        String day = String.valueOf(now.monthDay);
        String hour = String.valueOf(now.hour);
        String minute = String.valueOf(now.minute);
        String year = String.valueOf(now.year);

        if (month.length() == 1) month = "0" + month;
        if (day.length() == 1) day = "0" + day;
        if (hour.length() == 1) hour = "0" + hour;
        if (minute.length() == 1) minute = "0" + minute;

        return year + "-" + month + "-"
                + day + "T" + hour + ":" + minute;
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
            else {
                byte[] part = new byte[20];
                System.arraycopy(fullMessage, i*20, part, 0, 20);
                parts.offer(part);
            }
        }
        return parts;
    }

    /**
     *  Process the incoming permission created response.
     *  Returns the key if succeeded and null otherwise.
     */
    public static String getNewPermissionKey(String response) {
        String[] parts = response.split(SEPARATOR);
        String resultCode = parts[1];
        if (resultCode.equals(String.valueOf(ERROR))) {
            return null;
        }
        else if (resultCode.equals(String.valueOf(SUCCESS))) {
            return parts[2];
        }
        else {
            return null;
        }
    }

    public static HashMap<String,String> getPermissionData(String permissionPart) {
        String[] parts = permissionPart.split(SEPARATOR);
        HashMap<String,String> data = new HashMap<>();
        data.put(Permission.KEY, parts[1]);
        data.put(Slave.ID, parts[2]);
        data.put(Permission.TYPE, parts[3]);
        data.put(Permission.START_DATE, parts[4]);
        data.put(Permission.END_DATE, parts[5]);
        return data;
    }

    public static String getErrorCode(String response) {
        String[] messageParts = response.split(SEPARATOR);
        // the error code is always the part previous to the MESSAGE_END part
        return messageParts[messageParts.length-2];
    }

    public static boolean isDoorOpened(String response) {
        String[] parts = response.split(SEPARATOR);
        String resultCode = parts[1];
        String errorCode = parts[2];
        if (resultCode.equals(String.valueOf(ERROR)) || !errorCode.equals(EMPTY)) {
            return false;
        }
        else if (resultCode.equals(String.valueOf(SUCCESS))) {
            return true;
        }
        else {
            return false;
        }
    }

    public static String getResponseCode(String response){
        return response.substring(0, 2);
    }

    public static boolean isLastMessagePart(String message){
        if (message.lastIndexOf(MESSAGE_END) >= 0){
            return true;
        }
        return false;
    }

    public static ArrayList<Slave> getSlavesList(String response, String userId){
        response = response.substring(2, response.length()-3);
        String[] parts = response.split(ITEM_SEPARATOR);
        ArrayList<Slave> slaves = new ArrayList<>();
        if (response.equals(SEPARATOR)) return slaves;
        for (String part : parts){
            part = part.substring(1, part.length()-1);
            String id = part.split(SEPARATOR)[0];
            String type = part.split(SEPARATOR)[1];
            slaves.add(Slave.create("", Slave.DEFAULT_NAME + " " + id, Integer.valueOf(type),
                    Integer.valueOf(id), userId));
        }
        return slaves;
    }
}
