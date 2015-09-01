package com.incrementaventures.okey.Bluetooth;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseArray;

import com.incrementaventures.okey.Models.Master;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.Slave;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

public class BluetoothClient implements BluetoothAdapter.LeScanCallback {

    private final int NORMAL_SCAN_TIME = 4000;
    private final int LONG_SCAN_TIME = 120000;
    public static final int CLOSE_MODE = 0;

    public static final int OPEN_MODE = 1;
    public static final int SCAN_MODE = 2;
    public static final int FIRST_ADMIN_CONNECTION_MODE = 4;
    public static final int CREATE_NEW_PERMISSION_MODE = 7;
    public static final int GET_SLAVES_MODE = 8;
    public static final int READ_MY_PERMISSION_MODE = 9;
    public static final int READ_ALL_PERMISSIONS_MODE = 10;
    public static final int EDIT_PERMISSION_MODE = 11;

    public static final int DOOR_ALREADY_CLOSED = 2;
    public static final int DOOR_ALREADY_OPENED = 3;
    public static final int ADMIN_ALREADY_ASSIGNED = 5;
    public static final int ADMIN_ASSIGNED = 6;


    public static final int TIMEOUT = 101;
    public static final int RESPONSE_INCORRECT = 102;
    public static final int CANT_OPEN = 103;
    public static final int CANT_CONFIGURE = 104;
    public static final int PERMISSION_NOT_CREATED = 105;
    public static final int DONT_HAVE_PERMISSION = 106;
    public static final int BAD_INPUT = 107;
    public static final int DONT_HAVE_PERMISSION_THIS_HOUR = 108;
    public static final int PERMISSION_NOT_EDITED = 109;
    public static final int DOOR_NOT_CONFIGURED = 110;
    private boolean mScanning;
    private boolean mConnected;
    private boolean mSending;
    private boolean mWaitingResponse;

    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothGattCharacteristic mWriteCharacteristic;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private LinkedList<byte[]> mToSendMessageParts;

    private LinkedList<String> mReceivedMessageParts;

    /*
        Send callbacks to User about bluetooth stuff.
     */
    OnBluetoothToUserResponse mListener;
    /*
        Devices discovered in the scan.
     */
    private SparseArray<BluetoothDevice> mDevices;

    Context mContext;
    /*
        Used principally for stop scanning after NORMAL_SCAN_TIME miliseconds.
     */
    private Handler mHandler;
    /*
        The mode in which the class is permforming an action. For example, OPEN_MODE.
     */
    private int mMode;

    private String mPermissionKey;
    private String mFactoryKey;
    private String mMasterName;
    private int mSlaveId;


    private String mPermissionType;
    private String mEndDate;
    private String mEndHour;
    private String mStartDate;
    private String mStartHour;

    /**
     * Handles the connection with the BLE device
     * @param context context of the call
     * @param listener Usually a user, or any who implements OnBluetoothToUserResponse
     */
    public BluetoothClient(Context context,  OnBluetoothToUserResponse listener){
        mListener = listener;
        mContext = context;
        BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mHandler = new Handler();
        mDevices = new SparseArray<>();

    }


    public interface OnBluetoothToUserResponse{
        void deviceFound(BluetoothDevice device, int rssi, byte[] scanRecord);
        void deviceNotFound();
        void doorOpened(int state);
        void permissionCreated(String key, int type);
        void permissionEdited(String key, int type);
        void permissionReceived(int type, String key, String start, String end);
        void stopScanning();
        void slaveFound(String id, String type, String name);
        void doorClosed(int state);
        void error(int mode);
    }

    /**
     * Check if the device supports bluetooth
     * @return true if it's supported, false otherwise
     */
    public boolean isSupported(){
        if (mBluetoothAdapter == null) {
            // Device doesnt support Bluetooth
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

    public void scanDevices(){
        mMode = SCAN_MODE;
        startScan(LONG_SCAN_TIME);
    }


    public void executeOpenDoor(String key, String masterName, int slaveId){
        mPermissionKey = key;
        mSlaveId = slaveId;
        mMasterName = masterName;
        mMode = OPEN_MODE;
        startScan(NORMAL_SCAN_TIME);
    }

    public void executeOpenDoorWhenClose(String key, String masterName, int slaveId){
        mPermissionKey = key;
        mSlaveId = slaveId;
        mMasterName = masterName;
        mMode = OPEN_MODE;
        startScan(LONG_SCAN_TIME);
    }

    public void executeFirstConnectionConfiguration(String factoryKey, String permissionKey, String masterName){
        mPermissionKey = permissionKey;
        mFactoryKey = factoryKey;
        mMasterName = masterName;
        mMode = FIRST_ADMIN_CONNECTION_MODE;
        startScan(NORMAL_SCAN_TIME);
    }

    public void executeCreateNewPermission(String type, String startDate, String startHour, String endDate, String endHour, String permissionKey, String doorName){
        mMode = CREATE_NEW_PERMISSION_MODE;
        mMasterName = doorName;
        mPermissionType = type;
        mPermissionKey = permissionKey;
        mEndDate = endDate;
        mEndHour = endHour;
        mStartDate = startDate;
        mStartHour = startHour;
        startScan(NORMAL_SCAN_TIME);
    }

    public void executeEditPermission(String type, String startDate, String startHour, String endDate, String endHour, String permissionKey, String doorName){
        mMode = EDIT_PERMISSION_MODE;
        mMasterName = doorName;
        mPermissionType = type;
        mPermissionKey = permissionKey;
        mEndDate = endDate;
        mEndHour = endHour;
        mStartDate = startDate;
        mStartHour = startHour;
        startScan(NORMAL_SCAN_TIME);
    }

    public void executeReadUserPermission(String masterName, int slaveId, String permissionKey){
        mMasterName = masterName;
        mPermissionKey = permissionKey;
        mSlaveId = slaveId;
        mMode = READ_MY_PERMISSION_MODE;
        startScan(NORMAL_SCAN_TIME);
    }

    public void executeReadAllPermissions(String masterName, int slaveId, String permissionKey){
        mMasterName = masterName;
        mPermissionKey = permissionKey;
        mSlaveId = slaveId;
        mMode = READ_ALL_PERMISSIONS_MODE;
        startScan(NORMAL_SCAN_TIME);
    }

    public void executeGetSlaves(String masterName, String permissionKey){
        mMasterName = masterName;
        mPermissionKey = permissionKey;
        mMode = GET_SLAVES_MODE;
        startScan(NORMAL_SCAN_TIME);
    }


    private void startScan(int time){
        mScanning = true;
        if (mMode == SCAN_MODE){
            mBluetoothAdapter.startLeScan(this);

        } else{
            mBluetoothAdapter.startLeScan(new UUID[]{UUID.fromString(BluetoothProtocol.DOOR_SERVICE_UUID)}, this);
        }
        // Stop the scanning after NORMAL_SCAN_TIME miliseconds. Saves battery.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        }, time);
    }

    private void stopScan(){
        mScanning = false;
        mBluetoothAdapter.stopLeScan(this);
        mListener.stopScanning();
        if (mDevices.size() == 0 && mMode != SCAN_MODE){
            mListener.deviceNotFound();
        }
    }

    /*
        Called when a devices is found.
     */
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

        if ((mMode == OPEN_MODE && device.getName().equals(mMasterName))
                || (mMode == CREATE_NEW_PERMISSION_MODE && device.getName().equals(mMasterName))
                || (mMode == FIRST_ADMIN_CONNECTION_MODE && device.getName().equals(Master.FACTORY_NAME))
                || (mMode == READ_MY_PERMISSION_MODE && device.getName().equals(mMasterName))
                || (mMode == READ_ALL_PERMISSIONS_MODE && device.getName().equals(mMasterName))
                || (mMode == GET_SLAVES_MODE && device.getName().equals(mMasterName))
                || (mMode == EDIT_PERMISSION_MODE && device.getName().endsWith(mMasterName))){


            mDevices.put(device.hashCode(), device);
            device.connectGatt(mContext, true, mGattCallback);

        }
        else if (mMode == SCAN_MODE) {
            mListener.deviceFound(device, rssi, scanRecord);
        }
    }

    /*
        Handles the communication between the Peripheral device and the app.
     */
    BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            if (!mConnected && BluetoothProtocol.STATE_CONNECTED == newState){
                mConnected = true;
                gatt.discoverServices();
            }
            else if(BluetoothProtocol.STATE_DISCONNECTED == newState){
                mConnected = false;
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            gatt.setCharacteristicNotification(mNotifyCharacteristic, true);
            String message;
            switch (mMode){
                case OPEN_MODE:
                    // Separate the messsage in 20 bytes parts, then send each part
                    message = BluetoothProtocol.buildOpenMessage(mPermissionKey, mSlaveId);
                    break;
                case FIRST_ADMIN_CONNECTION_MODE:
                    message = BluetoothProtocol.buildFirstConfigurationMessage(mPermissionKey, mFactoryKey, mMasterName);
                    break;
                case CREATE_NEW_PERMISSION_MODE:
                    message = BluetoothProtocol.buildNewPermissionMessage(mPermissionType, mSlaveId, mStartDate, mStartHour, mEndDate, mEndHour, mPermissionKey);
                    break;
                case EDIT_PERMISSION_MODE:
                    message = BluetoothProtocol.buildEditPermissionMessage(mPermissionType, mSlaveId, mStartDate, mStartHour, mEndDate, mEndHour, mPermissionKey);
                    break;
                case GET_SLAVES_MODE:
                    message = BluetoothProtocol.buildGetSlavesMessage(mPermissionKey);
                    break;
                case READ_MY_PERMISSION_MODE:
                    message = BluetoothProtocol.buildGetUserPermissionMessage(mSlaveId, mPermissionKey);
                    break;
                case READ_ALL_PERMISSIONS_MODE:
                    message = BluetoothProtocol.buildGetAllPermissionsMessage(mSlaveId, mPermissionKey);
                    break;
                default:
                    return;
            }
            mToSendMessageParts = BluetoothProtocol.separateMessage(message);
            mSending = true;
            sendMessage(gatt, mWriteCharacteristic, mToSendMessageParts.poll());
        }


        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        /*
            Called when a notification is sended by the BLE server
         */
        @Override
        public void onCharacteristicChanged(final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            mWaitingResponse = false;

            String response = new String(characteristic.getValue());
            String responseCode = BluetoothProtocol.getResponseCode(response);

            if (mReceivedMessageParts == null) mReceivedMessageParts = new LinkedList<>();
            mReceivedMessageParts.offer(response);

            // if the response is the last part of the message, process the full message.
            if (!BluetoothProtocol.isLastMessagePart(response)){
                return;
            }

            String fullMessage = joinMessageParts(mReceivedMessageParts);

            switch (responseCode){
                case BluetoothProtocol.DOOR_OPENED_RESPONSE_CODE:
                    processOpenDoorResponse(fullMessage);
                    break;
                case BluetoothProtocol.PERMISSION_CREATED_OR_MODIFY_RESPONSE_CODE:
                    processModifyPermissionResponse(fullMessage);
                    break;
                case BluetoothProtocol.INDIVIDUAL_PERMISSION_RESPONSE_CODE:
                    processIndividualPermissionResponse(fullMessage);
                    break;
                case BluetoothProtocol.ALL_PERMISSIONS_RESPONSE_CODE:
                    break;
                case BluetoothProtocol.GET_SLAVES_RESPONSE_CODE:
                    processGetSlavesResponse(fullMessage);
                    break;


                default:
                    mListener.error(RESPONSE_INCORRECT);
                    break;
            }
        }

        /*
            Indicates the result of a write operation in a characteristic
         */
        @Override
        public void onCharacteristicWrite(final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                       /*
                Disconnect after 8 seconds
             */
            if (mToSendMessageParts.size() == 0){
                mWaitingResponse = true;
                mSending = false;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        gatt.disconnect();
                        gatt.close();
                        if (mWaitingResponse || mSending){
                            mListener.error(TIMEOUT);
                        }
                    }
                }, 8000);
                return;
            }


            // Sends the message until is finished
            sendMessage(gatt, characteristic, mToSendMessageParts.poll());
        }

        /*
            Indicates the result of a read operation in a characteristic
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        /*
            Called when the list of services offered by the peripheral are ready to be read
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BluetoothGattService service = gatt.getService(UUID.fromString(BluetoothProtocol.DOOR_SERVICE_UUID));

            // Get the read and notify characteristic
            mWriteCharacteristic = service.getCharacteristic(UUID.fromString(BluetoothProtocol.CHARACTERISTIC_WRITE_UUID));
            mNotifyCharacteristic = service.getCharacteristic(UUID.fromString(BluetoothProtocol.CHARACTERISTIC_NOTIFICATION_UUID));

            setNotificationsOn(gatt);
        }
    };


    /*
     * Turn on the notifications from the ble server
     */
    private void setNotificationsOn(BluetoothGatt gatt){

        // Write the configuration descriptor necessary to receive notifications
        UUID configNotifyUuid = UUID.fromString(BluetoothProtocol.CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
        BluetoothGattDescriptor descriptor = mNotifyCharacteristic.getDescriptor(configNotifyUuid);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
        
    }

    private void sendMessage(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] part){
        characteristic.setValue(part);
        gatt.writeCharacteristic(characteristic);
    }

    private String joinMessageParts(LinkedList<String> parts){
        StringBuilder builder = new StringBuilder();
        while (!parts.isEmpty()){
            builder.append(parts.poll());
        }
        return builder.toString();
    }

    private void processModifyPermissionResponse(String fullMessage){

        String errorCode = BluetoothProtocol.getErrorCode(fullMessage);

        // Returns the key created by the external device
        String key = BluetoothProtocol.getNewPermissionKey(fullMessage);
        if (!errorCode.equals(BluetoothProtocol.OK_ERROR_CODE)){
            int e = determineError(errorCode);
            mListener.error(e);
            return;
        }

        if (key != null){
            switch (mMode){
                case FIRST_ADMIN_CONNECTION_MODE:
                    mListener.permissionCreated(key, Permission.ADMIN_PERMISSION);
                    break;
                case CREATE_NEW_PERMISSION_MODE:
                    mListener.permissionCreated(key, BluetoothProtocol.getPermissionType(mPermissionType));
                    break;
                case EDIT_PERMISSION_MODE:
                    mListener.permissionEdited(key, BluetoothProtocol.getPermissionType(mPermissionType));
                    break;
                default:
                    mListener.error(RESPONSE_INCORRECT);
            }
        } else {
            switch (mMode){
                case FIRST_ADMIN_CONNECTION_MODE:
                    mListener.error(CANT_CONFIGURE);
                    break;
                case CREATE_NEW_PERMISSION_MODE:
                    mListener.error(PERMISSION_NOT_CREATED);
                    break;
                case EDIT_PERMISSION_MODE:
                    mListener.error(PERMISSION_NOT_EDITED);
                    break;
                default:
                    mListener.error(RESPONSE_INCORRECT);
            }
        }
    }

    private void processOpenDoorResponse(String fullMessage){
        if (BluetoothProtocol.isDoorOpened(fullMessage)) {
            mListener.doorOpened(OPEN_MODE);
        }
        else{
            String errorCode = BluetoothProtocol.getErrorCode(fullMessage);
            int e = determineError(errorCode);
            mListener.error(e);
        }
    }

    private void processIndividualPermissionResponse(String fullMessage){
        String errorCode = BluetoothProtocol.getErrorCode(fullMessage);
        switch (errorCode){
            case BluetoothProtocol.OK_ERROR_CODE:
                Bundle data = BluetoothProtocol.getPermissionData(fullMessage);
                int type = BluetoothProtocol.getPermissionType(data.getString(Permission.TYPE));
                mListener.permissionReceived(type,
                                             data.getString(Permission.KEY),
                                             data.getString(Permission.START_DATE),
                                             data.getString(Permission.END_DATE)) ;
                break;
            default:
                int e = determineError(errorCode);
                mListener.error(e);
                return;
        }
    }

    private void processAllPermissionsResponse(String fullMessage){
        // TODO: implement
        mListener.error(RESPONSE_INCORRECT);
    }

    private void processGetSlavesResponse(String fullMessage){
        String errorCode = BluetoothProtocol.getErrorCode(fullMessage);
        switch (errorCode){
            case BluetoothProtocol.OK_ERROR_CODE:
                ArrayList<HashMap<String,String>> slaves = BluetoothProtocol.getSlavesList(fullMessage);
                for (HashMap<String, String> values : slaves){
                    mListener.slaveFound(values.get(Slave.ID), values.get(Slave.TYPE), values.get(Slave.NAME));
                }
                break;
            default:
                int e = determineError(errorCode);
                mListener.error(e);
                return;
        }
    }

    private int determineError(String code){
        int e;
        switch (code){
            case BluetoothProtocol.NO_PERMISSION_ERROR_CODE:
                e = DONT_HAVE_PERMISSION;
                break;
            case BluetoothProtocol.MASTER_CANT_PROCESS_INPUT_ERROR_CODE:
                e = BAD_INPUT;
                break;
            case BluetoothProtocol.NO_ADMIN_PERMISSION_ERROR_CODE:
                e = DONT_HAVE_PERMISSION;
                break;
            case BluetoothProtocol.NO_PERMISSION_THIS_HOUR_ERROR_CODE:
                e = DONT_HAVE_PERMISSION_THIS_HOUR;
                break;
            case BluetoothProtocol.PERMISSION_EXPIRED_ERROR_CODE:
                e = DONT_HAVE_PERMISSION;
                break;
            case BluetoothProtocol.DOOR_NOT_CONFIGURED_ERROR_CODE:
                e = DOOR_NOT_CONFIGURED;
                break;
            default:
                e = RESPONSE_INCORRECT;
                break;
        }
        return e;
    }

}
