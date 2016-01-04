package com.incrementaventures.okey.Bluetooth;

import android.bluetooth.BluetoothDevice;

import com.incrementaventures.okey.Models.Master;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.Slave;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Andres on 04-01-2016.
 */
public class BluetoothClientTest {
    private BluetoothClient mBluetoothClient;
    private BluetoothClient.OnBluetoothToUserResponse mListener =
            new BluetoothClient.OnBluetoothToUserResponse() {
        @Override
        public void deviceFound(BluetoothDevice device, int rssi, byte[] scanRecord) {

        }

        @Override
        public void masterWithNoSlaves() {

        }

        @Override
        public void deviceNotFound() {

        }

        @Override
        public void doorOpened(int state) {
            Assert.assertTrue(false);
        }

        @Override
        public void permissionCreated(String key, Permission permission) {

        }

        @Override
        public void masterConfigured(Master master, Permission adminPermission) {

        }

        @Override
        public void permissionEdited(String key, Permission newPermission) {

        }

        @Override
        public void permissionDeleted(Permission permission) {

        }

        @Override
        public void permissionReceived(int type, String key, String start, String end) {

        }

        @Override
        public void permissionsReceived(ArrayList<HashMap<String, String>> permisionsData) {

        }

        @Override
        public void stopScanning() {

        }

        @Override
        public void slavesFound(Master master, ArrayList<Slave> slaves) {

        }

        @Override
        public void doorClosed(int state) {

        }

        @Override
        public void error(int mode) {

        }
    };

    @Before
    public void setUp() {
        mBluetoothClient = Mockito.mock(BluetoothClient.class);
        mBluetoothClient.mListener = mListener;
    }

    @Test
    public void testProcessOpenDoorResponse() {
        // 07 ; resultado ; error ; *
        mBluetoothClient.processOpenDoorResponse("07;1;0;*");
    }
}
