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

import java.util.UUID;

public class BluetoothClient implements BluetoothAdapter.LeScanCallback {

    private final int SCAN_TIME = 4000;
    public static final int CLOSE_MODE = 0;
    public static final int OPEN_MODE = 1;
    public static final int DOOR_ALREADY_CLOSED = 2;
    public static final int DOOR_ALREADY_OPENED = 3;
    public static final int ADMIN_ALREADY_ASSIGNED = 5;
    public static final int ADMIN_ASSIGNED = 6;
    public static final int FIRST_ADMIN_CONNECTION_MODE = 4;

    private boolean mScanning;
    private boolean mConnected;

    private BluetoothAdapter mBluetoothAdapter;
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

    private String mUserPhone;


    /**
     * Handles the connection with the BLE device
     * @param context context of the call
     * @param listener Usually a user, or any who implements OnBluetoothToUserResponse
     * @param mode 1 when is created to open a door, or 0 when is created to clse a door
     */
    public BluetoothClient(Context context,  OnBluetoothToUserResponse listener, int mode){
        mListener = listener;
        mContext = context;
        BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mHandler = new Handler();
        mDevices = new SparseArray<>();
        mMode = mode;

    }


    public interface OnBluetoothToUserResponse{
        void deviceFound(BluetoothDevice device, int rssi, byte[] scanRecord);
        void deviceNotFound();
        void doorOpened(int state);
        void doorClosed(int state);
        void adminAssigned(int state);
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


    public void startScan(){
        mScanning = true;
        mBluetoothAdapter.startLeScan(this);
        // Stop the scanning after SCAN_TIME miliseconds. Saves battery.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        }, SCAN_TIME);
    }

    public void stopScan(){
        mScanning = false;
        mBluetoothAdapter.stopLeScan(this);
        if (mDevices.size() == 0){
            mListener.deviceNotFound();
        }
    }

    /*
        Called when a devices is found.
     */
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

        mDevices.put(device.hashCode(), device);
        mListener.deviceFound(device, rssi, scanRecord);
        // TODO: filter devices.
        device.connectGatt(mContext, true, mGattCallback);

    }

    /*
        Handles the communication between the Peripheral device and the app.
     */
    BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (BluetoothProtocol.STATE_CONNECTED == newState){
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
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        /*
            Indicates the result of a write operation in a characteristic
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            byte[] bytes = characteristic.getValue();
            boolean mOpened = BluetoothProtocol.isDoorOpened(bytes);
            if (mMode == OPEN_MODE){
                if (mOpened){
                    mListener.doorOpened(OPEN_MODE);
                } else {
                    mListener.error(OPEN_MODE);
                }
            }
            else{
                if (!mOpened){
                    mListener.doorClosed(CLOSE_MODE);
                } else {
                    mListener.error(CLOSE_MODE);
                }

            }

            gatt.close();

        }

        /*
            Indicates the result of a read operation in a characteristic
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            byte[] bytes = characteristic.getValue();
            boolean mOpened;
            //Open
            switch (mMode){
                case OPEN_MODE:
                    mOpened = BluetoothProtocol.isDoorOpened(bytes);
                    if (!mOpened){
                        BluetoothProtocol.openDoor(gatt, characteristic);
                    }
                    else{
                        mListener.doorOpened(DOOR_ALREADY_OPENED);
                    }
                    break;

                case CLOSE_MODE:
                    mOpened = BluetoothProtocol.isDoorOpened(bytes);
                    if (mOpened){
                        BluetoothProtocol.closeDoor(gatt, characteristic);
                    }
                    else{
                        mListener.doorClosed(DOOR_ALREADY_CLOSED);
                    }
                    break;

                case FIRST_ADMIN_CONNECTION_MODE:
                    boolean mNeverConnected = BluetoothProtocol.hasNeverConnected(bytes);
                    if (mNeverConnected){
                        BluetoothProtocol.makeFirstAdminConnection(mUserPhone, gatt, characteristic);
                    } else{
                        mListener.adminAssigned(ADMIN_ALREADY_ASSIGNED);
                    }
            }
        }

        /*
            Called when the list of services offered by the peripheral are ready to be read
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BluetoothGattService service = gatt.getService(UUID.fromString(BluetoothProtocol.DOOR_SERVICE_UUID));
            BluetoothGattCharacteristic characteristic;

            if (mMode == FIRST_ADMIN_CONNECTION_MODE){
                characteristic = service.getCharacteristic(UUID.fromString(BluetoothProtocol.CONFIGURED_CHARACTERISTIC_UUID));
            } else{
                characteristic = service.getCharacteristic(UUID.fromString(BluetoothProtocol.STATE_CHARACTERISTIC_UUID));
            }

            // Check the state of the door
            gatt.readCharacteristic(characteristic);

        }
    };


    public void setUserPhone(String phone){
        mUserPhone = phone;
    }
}
