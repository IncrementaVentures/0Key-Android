package com.incrementaventures.okey.Bluetooth;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Time;
import android.util.Log;
import android.util.SparseArray;

import com.incrementaventures.okey.Models.Master;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.Slave;
import com.incrementaventures.okey.Models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

public class BluetoothClient implements BluetoothAdapter.LeScanCallback {

    private final int NORMAL_SCAN_TIME = 5000;
    private final int LONG_SCAN_TIME = 120000;
    private final int MEDIUM_SCAN_TIME = 12000;

    private final int MAX_RETRIES = 5;
    private final int RETRY_INTERVAL_MS = 1000;

    public static final int CLOSE_MODE = 0;
    public static final int OPEN_MODE = 1;
    public static final int SCAN_MODE = 2;
    public static final int FIRST_ADMIN_CONNECTION_MODE = 4;
    public static final int CREATE_NEW_PERMISSION_MODE = 7;
    public static final int GET_SLAVES_MODE = 8;
    public static final int READ_MY_PERMISSION_MODE = 9;
    public static final int READ_ALL_PERMISSIONS_MODE = 10;
    public static final int EDIT_PERMISSION_MODE = 11;
    public static final int DELETE_PERMISSION_MODE = 12;
    public static final int PAIR_SLAVES_MODE = 13;

    /**
     * Error messages.
     */
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
    public static final int STILL_SCANNING = 111;

    private boolean mScanning;
    private boolean mTryingToDiscoverServices;
    private boolean mTryingToConnect;
    private boolean mConnected;
    private boolean mSending;
    private boolean mWaitingResponse;
    private boolean mConnectionFinished;
    private int mConnectionsCount;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;

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
    private String mMasterId;
    private int mSlaveId;
    private int mNewSlaveId;
    private Permission mPermission;
    private Master mMaster;

    private static BluetoothClient sInstance;


    private int mRetryCount;

    /**
     * @param context context of the call
     * @param listener Usually a user, or any who implements OnBluetoothToUserResponse
     */
    private BluetoothClient(Context context, OnBluetoothToUserResponse listener) {
        mListener = listener;
        mContext = context;
        mBluetoothManager = (BluetoothManager) mContext.getApplicationContext()
                .getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mHandler = new Handler();
        mDevices = new SparseArray<>();
        mConnectionFinished = true;
    }

    public static BluetoothClient getInstance(Context context, OnBluetoothToUserResponse listener) {
        if (sInstance == null) {
            sInstance = new BluetoothClient(context, listener);
        }
        sInstance.mListener = listener;
        sInstance.mContext = context;
        sInstance.mBluetoothManager = (BluetoothManager) context.getApplicationContext()
                .getSystemService(Context.BLUETOOTH_SERVICE);
        sInstance.mBluetoothAdapter = sInstance.mBluetoothManager.getAdapter();
        return sInstance;
    }

    public interface OnBluetoothToUserResponse{
        void deviceFound(BluetoothDevice device, int rssi, byte[] scanRecord);
        void masterWithNoSlaves();
        void deviceNotFound();
        void doorOpened(int state);
        void permissionCreated(String key, Permission permission);
        void masterConfigured(Master master, Permission adminPermission);
        void permissionEdited(String key, Permission newPermission);
        void permissionDeleted(Permission permission);
        void permissionReceived(int type, String key, String start, String end);
        void permissionsReceived(ArrayList<HashMap<String, String>> permisionsData);
        void stopScanning();
        void slavesFound(Master master, ArrayList<Slave> slaves);
        void doorClosed(int state);
        void error(int mode);
    }

    /**
     * Check if the device supports bluetooth
     * @return true if it's supported, false otherwise
     */
    public boolean isSupported(){
        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
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
        if (!mBluetoothAdapter.isEnabled()) {
            return false;
        }
        return true;
    }

    public void scanDevices(){
        mMode = SCAN_MODE;
        startScan(NORMAL_SCAN_TIME);
    }

    public void executeOpenDoor(Permission adminPermission, String masterName, int slaveId) {
        mPermission = adminPermission;
        mPermissionKey = adminPermission.getKey();
        mSlaveId = slaveId;
        mMasterId = masterName;
        mMode = OPEN_MODE;
        startScan(NORMAL_SCAN_TIME);
    }

    public void executeOpenDoorWhenClose(String key, String masterName, int slaveId){
        mPermissionKey = key;
        mSlaveId = slaveId;
        mMasterId = masterName;
        mMode = OPEN_MODE;
        startScan(LONG_SCAN_TIME);
    }

    public void executeFirstConnectionConfiguration(String factoryKey, String permissionKey,
                                                    Master master){
        mPermissionKey = permissionKey;
        mFactoryKey = factoryKey;
        mMasterId = master.getId();
        mMaster = master;
        mMode = FIRST_ADMIN_CONNECTION_MODE;
        startScan(NORMAL_SCAN_TIME);
    }

    public void executeCreateNewPermission(Permission permission, String permissionKey,
                                           String doorId) {
        mMode = CREATE_NEW_PERMISSION_MODE;
        mPermission = permission;
        mMasterId = doorId;
        mSlaveId = permission.getSlaveId();
        mPermissionKey = permissionKey;
        startScan(NORMAL_SCAN_TIME);
    }

    public void executeEditPermission(Permission toEditPermission, int oldSlaveId, String adminKey,
                                      String doorId) {
        mMode = EDIT_PERMISSION_MODE;
        mSlaveId = oldSlaveId;
        mPermission = toEditPermission;
        mMasterId = doorId;
        mPermissionKey = adminKey;
        startScan(NORMAL_SCAN_TIME);
    }

    public void executeDeletePermission(String masterId, String adminKey, Permission permission) {
        mMasterId = masterId;
        mPermission = permission;
        mMode = DELETE_PERMISSION_MODE;
        mPermissionKey = adminKey;
        startScan(NORMAL_SCAN_TIME);
    }

    public void executeReadUserPermission(String masterName, int slaveId, String permissionKey){
        mMasterId = masterName;
        mPermissionKey = permissionKey;
        mSlaveId = slaveId;
        mMode = READ_MY_PERMISSION_MODE;
        startScan(NORMAL_SCAN_TIME);
    }

    public void executeReadAllPermissions(String masterName, int slaveId, String permissionKey){
        mMasterId = masterName;
        mPermissionKey = permissionKey;
        mSlaveId = slaveId;
        mMode = READ_ALL_PERMISSIONS_MODE;
        startScan(NORMAL_SCAN_TIME);
    }

    public void executeGetSlaves(Master master, String permissionKey){
        mMasterId = master.getId();
        mMaster = master;
        mPermissionKey = permissionKey;
        mMode = GET_SLAVES_MODE;
        startScan(NORMAL_SCAN_TIME);
    }

    public void executePairSlaves(String masterId, String adminKey, int keySlaveId, int pairSlaveId) {
        mMaster = Master.getMaster(masterId, User.getLoggedUser().getId());
        mPermissionKey = adminKey;
        mSlaveId = keySlaveId;
        mNewSlaveId = pairSlaveId;
        mMasterId = masterId;
        mMode = PAIR_SLAVES_MODE;
        startScan(MEDIUM_SCAN_TIME);
    }

    private void startScan(int time){
        Log.d("BLUETOOTH CONNECTION", "---------------------");
        Log.d("BLUETOOTH CONNECTION", "Starting scanning");
        if (!mBluetoothAdapter.startLeScan(this)) {
            Log.d("BLUETOOTH CONNECTION", "Cant start Scan");
            mListener.error(STILL_SCANNING);
            stopScan();
        } else {
            mConnectionsCount++;
            mScanning = true;
            mDevices = new SparseArray<>();
            mConnectionFinished = false;
            final int previousCount = mConnectionsCount;
            // Stop the scanning after time miliseconds.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d("BLUETOOTH CONNECTION", "After 'time' ms, trying to stop scan");
                    if (previousCount == mConnectionsCount) {
                        stopScan();
                    }
                }
            }, time);
        }
    }

    private void stopScan() {
        if (mScanning) {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(this);
            Log.d("BLUETOOTH CONNECTION", "Scan stopped");
            mListener.stopScanning();
            if (mDevices.size() == 0 && mMode != SCAN_MODE && !isWorking()) {
                mListener.deviceNotFound();
            }
        }
    }

    public boolean isWorking() {
        return mTryingToConnect || mTryingToDiscoverServices || mConnected || mWaitingResponse || mSending;
    }

    private boolean isRetryLimitReached() {
        return mRetryCount > MAX_RETRIES;
    }

    private void retryIfNecessary(final BluetoothDevice device, final BluetoothGatt gatt) {
        if (isRetryLimitReached()) {
            Log.d("BLUETOOTH CONNECTION", "Try count limit reached");
            finishConnection(gatt);
            mRetryCount = 0;
            mListener.error(TIMEOUT);
            return;
        }
        mRetryCount++;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("BLUETOOTH CONNECTION", "Check if is frozen.");
                if (isWorking()) {
                    Log.d("BLUETOOTH CONNECTION", "Frozen, create new connection.");
                    BluetoothGatt gatt = device.connectGatt(mContext, false, mGattCallback);
                    retryIfNecessary(device, gatt);
                }
            }
        }, RETRY_INTERVAL_MS);
    }

    private void finishConnection(BluetoothGatt gatt) {
        if (gatt != null) {
            int connectionState =
                    mBluetoothManager.getConnectionState(gatt.getDevice(), BluetoothGatt.GATT);
            if (connectionState == BluetoothGatt.STATE_CONNECTED
                    || connectionState == BluetoothGatt.STATE_CONNECTING) {
                Log.d("BLUETOOTH CONNECTION", "Disconnecting gatt.");
                gatt.disconnect();
            }
        }
        mRetryCount = 0;
        mConnected = false;
        mTryingToConnect = false;
        mTryingToDiscoverServices = false;
        mWaitingResponse = false;
        mSending = false;
        mConnectionFinished = true;
        stopScan();
    }

    /*
        Called when a devices is found.
     */
    @Override
    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
        String deviceName = device.getName();
        if (deviceName == null) return;
        Log.d("BLUETOOTH CONNECTION", "Device found: " + device.getName());
        if ((mMode == OPEN_MODE && deviceName.equals(mMasterId))
                || (mMode == CREATE_NEW_PERMISSION_MODE && deviceName.equals(mMasterId))
                || (mMode == FIRST_ADMIN_CONNECTION_MODE && deviceName.equals(mMasterId))
                || (mMode == READ_MY_PERMISSION_MODE && deviceName.equals(mMasterId))
                || (mMode == READ_ALL_PERMISSIONS_MODE && deviceName.equals(mMasterId))
                || (mMode == GET_SLAVES_MODE && deviceName.equals(mMasterId))
                || (mMode == DELETE_PERMISSION_MODE && deviceName.equals(mMasterId))
                || (mMode == EDIT_PERMISSION_MODE && deviceName.endsWith(mMasterId))
                || (mMode == PAIR_SLAVES_MODE && deviceName.equals(mMasterId))) {
            mDevices.put(device.hashCode(), device);
            stopScan();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Log.d("BLUETOOTH CONNECTION", "Executing first device.connectGatt()");
                    BluetoothGatt gatt = device.connectGatt(mContext, false, mGattCallback);
                    retryIfNecessary(device, gatt);
                    mTryingToConnect = true;
                }
            });
        } else if (mMode == SCAN_MODE) {
            mListener.deviceFound(device, rssi, scanRecord);
        }
    }

    /*
        Handles the communication between the Peripheral device and the app.
     */
    BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            Log.d("BLUETOOTH CONNECTION", "On connection state changed. Device: "+ gatt.getDevice().getAddress());
            if (!mConnected && BluetoothGatt.STATE_CONNECTED == newState) {
                Log.d("BLUETOOTH CONNECTION", "Connected");
                mTryingToConnect = false;
                mTryingToDiscoverServices = true;
                mConnected = true;
                gatt.discoverServices();
            }
            else if(BluetoothGatt.STATE_DISCONNECTED == newState) {
                Log.d("BLUETOOTH CONNECTION", "Disconnected and closing gatt.");
                mConnected = false;
                gatt.close();
                if (!mConnectionFinished && mRetryCount == 0) {
                    finishConnection(gatt);
                }
            } else if (BluetoothGatt.STATE_CONNECTING == newState) {
                Log.d("BLUETOOTH CONNECTION", "Connecting.");
            } else if (BluetoothGatt.STATE_DISCONNECTING == newState) {
                Log.d("BLUETOOTH CONNECTION", "Disconnecting.");
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
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                      int status) {
            gatt.setCharacteristicNotification(mNotifyCharacteristic, true);
            String message;
            switch (mMode) {
                case OPEN_MODE:
                    // Separate the messsage in 20 bytes parts, then send each part
                    message = BluetoothProtocol.buildOpenMessage(mPermissionKey, mPermission.getSlaveId(), mSlaveId);
                    break;
                case FIRST_ADMIN_CONNECTION_MODE:
                    message = BluetoothProtocol.buildFirstConfigurationMessage(mPermissionKey,
                            mFactoryKey, mMasterId);
                    break;
                case CREATE_NEW_PERMISSION_MODE:
                    message = BluetoothProtocol.buildNewPermissionMessage(mPermission,
                            mPermissionKey);
                    break;
                case EDIT_PERMISSION_MODE:
                    message = BluetoothProtocol.buildEditPermissionMessage(mPermission, mSlaveId, mPermissionKey);
                    break;
                case DELETE_PERMISSION_MODE:
                    message = BluetoothProtocol.buildDeletePermissionMessage(mPermissionKey, mPermission);
                    break;
                case GET_SLAVES_MODE:
                    message = BluetoothProtocol.buildGetSlavesMessage(mPermissionKey);
                    break;
                case READ_MY_PERMISSION_MODE:
                    message = BluetoothProtocol.
                            buildGetUserPermissionMessage(mSlaveId, mPermissionKey);
                    break;
                case READ_ALL_PERMISSIONS_MODE:
                    message = BluetoothProtocol.
                            buildGetAllPermissionsMessage(mSlaveId, mPermissionKey);
                    break;
                case PAIR_SLAVES_MODE:
                    message = BluetoothProtocol.buildPairSlavesMessage(mPermissionKey, mSlaveId, mNewSlaveId);
                    break;
                default:
                    return;
            }
            mToSendMessageParts = BluetoothProtocol.separateMessage(message);
            mSending = true;
            sendMessage(gatt, mWriteCharacteristic, mToSendMessageParts.poll());
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                     int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        /*
            Called when a notification is sended by the BLE server
         */
        @Override
        public void onCharacteristicChanged(final BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            mWaitingResponse = false;
            String response = new String(characteristic.getValue());
            Log.d("BLUETOOTH CONNECTION", "Message received: " + response);
            if (mReceivedMessageParts == null) mReceivedMessageParts = new LinkedList<>();
            mReceivedMessageParts.offer(response);

            // if the response is the last part of the message, process the full message.
            if (!BluetoothProtocol.isLastMessagePart(response)){
                return;
            }
            finishConnection(gatt);

            String fullMessage = joinMessageParts(mReceivedMessageParts);
            String responseCode = BluetoothProtocol.getResponseCode(fullMessage);

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
                    processAllPermissionsResponse(fullMessage);
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
        public void onCharacteristicWrite(final BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {

            if (mToSendMessageParts.size() == 0){
                mWaitingResponse = true;
                mSending = false;
                return;
            }
            // Sends the message until is finished
            sendMessage(gatt, characteristic, mToSendMessageParts.poll());
        }

        /*
            Indicates the result of a read operation in a characteristic
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        /*
            Called when the list of services offered by the peripheral are ready to be read
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d("BLUETOOTH CONNECTION", "Services discovered");
            mTryingToDiscoverServices = false;
            BluetoothGattService service = gatt.getService(UUID.fromString(
                    BluetoothProtocol.DOOR_SERVICE_UUID));

            // Get the read and notify characteristic
            mWriteCharacteristic = service.getCharacteristic(UUID.fromString(
                    BluetoothProtocol.CHARACTERISTIC_WRITE_UUID));
            mNotifyCharacteristic = service.getCharacteristic(UUID.fromString(
                    BluetoothProtocol.CHARACTERISTIC_NOTIFICATION_UUID));

            setNotificationsOn(gatt);
        }
    };


    /*
     * Turn on the notifications from the ble server
     */
    private void setNotificationsOn(BluetoothGatt gatt){

        // Write the configuration descriptor necessary to receive notifications
        UUID configNotifyUuid = UUID.fromString(
                BluetoothProtocol.CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
        BluetoothGattDescriptor descriptor = mNotifyCharacteristic.getDescriptor(configNotifyUuid);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);

    }

    private void sendMessage(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                             byte[] part){
        Log.d("BLUETOOTH CONNECTION", "Sending message: " + new String(part));
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

        if (key != null) {
            switch (mMode) {
                case FIRST_ADMIN_CONNECTION_MODE:
                    mMaster.save();
                    Time now = new Time();
                    now.setToNow();
                    mPermission = Permission.create(User.getLoggedUser(),
                            mMaster,
                            Permission.ADMIN_PERMISSION,
                            key,
                            BluetoothProtocol.formatDate(now),
                            Permission.PERMANENT_DATE,
                            0);
                    mListener.masterConfigured(mMaster, mPermission);
                    break;
                case CREATE_NEW_PERMISSION_MODE:
                    mListener.permissionCreated(key, mPermission);
                    break;
                case EDIT_PERMISSION_MODE:
                    mPermission.setSlaveId(mPermission.getSlaveId());
                    mPermission.setStartDate(mPermission.getStartDate());
                    mPermission.setEndDate(mPermission.getEndDate());
                    mPermission.setType(Permission.getType(mPermission.getType()));
                    mListener.permissionEdited(key, mPermission);
                    break;
                case DELETE_PERMISSION_MODE:
                    mListener.permissionDeleted(mPermission);
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

    protected void processOpenDoorResponse(String fullMessage) {
        if (BluetoothProtocol.isDoorOpened(fullMessage)) {
            mListener.doorOpened(OPEN_MODE);
        }
        else {
            String errorCode = BluetoothProtocol.getErrorCode(fullMessage);
            int e = determineError(errorCode);
            mListener.error(e);
        }
    }

    private void processIndividualPermissionResponse(String fullMessage){
        String errorCode = BluetoothProtocol.getErrorCode(fullMessage);
        switch (errorCode){
            case BluetoothProtocol.OK_ERROR_CODE:
                HashMap<String, String> data = BluetoothProtocol.getPermissionData(fullMessage);
                int type = Integer.valueOf(data.get(Permission.TYPE));
                mListener.permissionReceived(type,
                        data.get(Permission.KEY),
                        data.get(Permission.START_DATE),
                        data.get(Permission.END_DATE)) ;
                break;
            default:
                int e = determineError(errorCode);
                mListener.error(e);
                return;
        }
    }

    private void processAllPermissionsResponse(String fullMessage){
        String errorCode = BluetoothProtocol.getErrorCode(fullMessage);
        switch (errorCode){
            case BluetoothProtocol.OK_ERROR_CODE:
                String onlyPermissionData = fullMessage.substring(2, fullMessage.length()-3);
                String[] permissionData = onlyPermissionData.split(BluetoothProtocol.ITEM_SEPARATOR);
                ArrayList<HashMap<String, String>> permissions = new ArrayList<>();
                if (onlyPermissionData.equals(";")){
                    mListener.permissionsReceived(permissions);
                    return;
                }
                for (String p : permissionData){
                    permissions.add(BluetoothProtocol.getPermissionData(p));
                }

                mListener.permissionsReceived(permissions) ;
                break;
            default:
                int e = determineError(errorCode);
                mListener.error(e);
                return;
        }
    }

    private void processGetSlavesResponse(String fullMessage){
        String errorCode = BluetoothProtocol.getErrorCode(fullMessage);
        switch (errorCode){
            case BluetoothProtocol.OK_ERROR_CODE:
                ArrayList<Slave> slaves = BluetoothProtocol.getSlavesList(fullMessage,
                        User.getLoggedUser().getId());
                if (slaves.size() == 0) mListener.masterWithNoSlaves();
                mListener.slavesFound(mMaster, slaves);
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
