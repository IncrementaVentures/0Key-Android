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
import android.os.Handler;
import android.util.SparseArray;

import com.incrementaventures.okey.Models.Door;
import com.incrementaventures.okey.Models.Permission;

import java.util.LinkedList;
import java.util.UUID;

public class BluetoothClient implements BluetoothAdapter.LeScanCallback {

    private final int SCAN_TIME = 4000;
    public static final int CLOSE_MODE = 0;
    public static final int OPEN_MODE = 1;
    public static final int SCAN_MODE = 2;
    public static final int DOOR_ALREADY_CLOSED = 2;
    public static final int DOOR_ALREADY_OPENED = 3;
    public static final int ADMIN_ALREADY_ASSIGNED = 5;
    public static final int ADMIN_ASSIGNED = 6;
    public static final int FIRST_ADMIN_CONNECTION_MODE = 4;

    public static final int TIMEOUT = 101;
    public static final int RESPONSE_INCORRECT = 102;
    public static final int CANT_OPEN = 103;
    public static final int CANT_CONFIGURE = 104;

    private boolean mScanning;
    private boolean mConnected;
    private boolean mSending;
    private boolean mWaitingResponse;

    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothGattCharacteristic mWriteCharacteristic;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private LinkedList<byte[]> mMessageParts;
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
        Used principally for stop scanning after SCAN_TIME miliseconds.
     */
    private Handler mHandler;
    /*
        Open or close door mode
     */
    private int mMode;

    private String mPermissionKey;
    private String mFactoryKey;
    private String mDoorName;



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
        startScan();
    }


    public void executeOpenDoor(String key, String doorName){
        mPermissionKey = key;
        mDoorName = doorName;
        mMode = OPEN_MODE;
        startScan();
    }

    public void executeFirstConnectionConfiguration(String factoryKey, String permissionKey, String doorName){
        mPermissionKey = permissionKey;
        mFactoryKey = factoryKey;
        mDoorName = doorName;
        mMode = FIRST_ADMIN_CONNECTION_MODE;
        startScan();
    }


    private void startScan(){
        mScanning = true;
        if (mMode == SCAN_MODE){
            mBluetoothAdapter.startLeScan(this);

        } else{
            mBluetoothAdapter.startLeScan(new UUID[]{UUID.fromString(BluetoothProtocol.DOOR_SERVICE_UUID)}, this);
        }
        // Stop the scanning after SCAN_TIME miliseconds. Saves battery.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        }, SCAN_TIME);
    }

    private void stopScan(){
        mScanning = false;
        mBluetoothAdapter.stopLeScan(this);
        if (mDevices.size() == 0 && mMode != SCAN_MODE){
            mListener.deviceNotFound();
        }
    }

    /*
        Called when a devices is found.
     */
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        //TODO: filter by the real door name
        if ((mMode == OPEN_MODE && device.getName().equals(mDoorName))
                || (mMode == FIRST_ADMIN_CONNECTION_MODE && device.getName().equals(Door.FACTORY_NAME))){
            mDevices.put(device.hashCode(), device);
            device.connectGatt(mContext, true, mGattCallback);
        }
        else if (mMode == SCAN_MODE){
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

            if (mMode == OPEN_MODE){
                // Separate the messsage in 20 bytes parts, then send each part
                String openMessage = BluetoothProtocol.buildOpenMessage(mPermissionKey);
                mMessageParts = BluetoothProtocol.separateMessage(openMessage);
                mSending = true;
                BluetoothProtocol.sendMessage(gatt, mWriteCharacteristic, mMessageParts.poll());
            }
            else if (mMode == FIRST_ADMIN_CONNECTION_MODE){
                String configurationMessage = BluetoothProtocol.buildFirstConfigurationMessage(mPermissionKey, mFactoryKey, mDoorName);
                mMessageParts = BluetoothProtocol.separateMessage(configurationMessage);
                mSending = true;
                BluetoothProtocol.sendMessage(gatt, mWriteCharacteristic, mMessageParts.poll());
            }
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

            switch (responseCode){
                case BluetoothProtocol.DOOR_OPENED_RESPONSE_CODE:
                    // If opened
                    if (BluetoothProtocol.processOpenDoorResponse(response)) {
                        mListener.doorOpened(OPEN_MODE);
                    }
                    else{
                        mListener.error(CANT_OPEN);
                    }
                    break;
                case BluetoothProtocol.PERMISSION_CREATED_OR_MODIFY_RESPONSE_CODE:
                    // Returns the key created by the external device
                    String key = BluetoothProtocol.processPermissionCreationResponse(response);

                    if (key != null){
                        if (mMode == FIRST_ADMIN_CONNECTION_MODE){
                            mListener.permissionCreated(key, Permission.ADMIN_PERMISSION);
                        }
                    } else {
                        mListener.error(CANT_CONFIGURE);
                    }
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
            if (mMessageParts.size() == 0){
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
            BluetoothProtocol.sendMessage(gatt, characteristic, mMessageParts.poll());
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


}
