package com.incrementaventures.okey.Bluetooth;

import android.text.format.Time;

import com.incrementaventures.okey.Models.Master;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.Slave;
import com.incrementaventures.okey.Models.User;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

public class ProtocolTest {
    static final String MASTER_ID = "0000001";
    static final String MASTER_NAME = "Master";
    static final String SLAVE_NAME_ONE = "Slave1";
    static final String SLAVE_NAME_TWO = "Slave2";
    static final int SLAVE_ID_ONE = 1;
    static final int SLAVE_ID_TWO = 2;
    static final String USER_UUID = "lksd12-1231-9199-919191";
    static final String USER_EMAIL = "aamatte@uc.cl";
    static final String PERMISSION_KEY_TEMPORAL = "1111";
    static final String PERMISSION_KEY_ADMIN = "4321";
    static final String START_DATE = "2015-10-10T10:10";
    static final String END_DATE = "2015-10-10T18:00";
    Master mMaster;
    Permission mAdminPermission;
    Permission mToEditPermission;
    User mUser;
    Slave mSlave1;
    Slave mSlave2;

    @Before
    public void setUp() {
        mMaster = Master.create(MASTER_ID, MASTER_NAME, USER_UUID);
        mSlave1 = Slave.create(mMaster.getId(), SLAVE_NAME_ONE, 0, SLAVE_ID_TWO, USER_UUID);
        mSlave2 = Slave.create(mMaster.getId(), SLAVE_NAME_TWO, 0, SLAVE_ID_TWO, USER_UUID);
        mUser = User.getUser(USER_EMAIL);
        mAdminPermission = Permission.create(mUser, mMaster, Permission.ADMIN_PERMISSION, PERMISSION_KEY_ADMIN,
                START_DATE, END_DATE, mSlave1.getId());
        mToEditPermission = Permission.create(mUser, mMaster, Permission.TEMPORAL_PERMISSION, PERMISSION_KEY_TEMPORAL,
                START_DATE, END_DATE, mSlave2.getId());
    }

    @Test
    public void testFormatDate(){
        Time now = new Time();
        now.year = 2000;
        now.month = 0;
        now.monthDay = 1;
        now.hour = 0;
        now.minute = 0;

        String month = String.valueOf(now.month + 1);
        String day = String.valueOf(now.monthDay);
        String hour = String.valueOf(now.hour);
        String minute = String.valueOf(now.minute);
        String year = String.valueOf(now.year);

        if (month.length() == 1) month = "0" + month;
        if (day.length() == 1) day = "0" + day;
        if (hour.length() == 1) hour = "0" + hour;
        if (minute.length() == 1) minute = "0" + minute;

        String formattedDate = year + "-" + month + "-"
                + day + "T" + hour + ":" + minute;

        Assert.assertEquals("2000-01-01T00:00", formattedDate);
    }

    @Test
    public void testGetEmptySlaveList() {
        String response = "11;0;*";
        ArrayList<Slave> data = BluetoothProtocol.getSlavesList(response, USER_UUID);
        Assert.assertEquals(true, data.size() == 0);
    }

    @Test
    public void testGetSlaveListSizeOne() {
        //  11 ; id esclavo ; tipo esclavo ; & ; esclavo siguiente ; error ; *
        String response = "11;1;0;0;*";
        ArrayList<Slave> data = BluetoothProtocol.getSlavesList(response, USER_UUID);
        Assert.assertEquals(1, data.size());
    }

    @Test
    public void testGetPermissionsSizeOne(){
        String fullMessage = "05;1234;0;0;FECHAINICIO1;FECHATERMINO1;0;*";
        String errorCode = BluetoothProtocol.getErrorCode(fullMessage);
        ArrayList<HashMap<String, String>> permissions = new ArrayList<>();

        switch (errorCode){
            case BluetoothProtocol.OK_ERROR_CODE:
                String onlyPermissionData = fullMessage.substring(2, fullMessage.length() - 3);
                String[] permissionData = onlyPermissionData.split(BluetoothProtocol.ITEM_SEPARATOR);
                for (String p : permissionData){
                    permissions.add(BluetoothProtocol.getPermissionData(p));
                }
                break;
            default:
                break;
        }
        Assert.assertEquals("0", permissions.get(0).get(Permission.TYPE));
        Assert.assertEquals("1234", permissions.get(0).get(Permission.KEY));
        Assert.assertEquals("FECHAINICIO1", permissions.get(0).get(Permission.START_DATE));
        Assert.assertEquals("FECHATERMINO1", permissions.get(0).get(Permission.END_DATE));
    }

    @Test
    public void testGetPermissionsSizeTwo(){
        String fullMessage =
                "05;1234;0;0;FECHAINICIO1;FECHATERMINO1;&;1234;1;2;FECHAINICIO2;FECHATERMINO2;0;*";
        String errorCode = BluetoothProtocol.getErrorCode(fullMessage);
        ArrayList<HashMap<String, String>> permissions = new ArrayList<>();

        switch (errorCode){
            case BluetoothProtocol.OK_ERROR_CODE:
                String onlyPermissionData = fullMessage.substring(2, fullMessage.length() - 3);
                String[] permissionData = onlyPermissionData.split(BluetoothProtocol.ITEM_SEPARATOR);
                for (String p : permissionData){
                    permissions.add(BluetoothProtocol.getPermissionData(p));
                }
                break;
            default:
                break;
        }
        Assert.assertEquals("0", permissions.get(0).get(Permission.TYPE));
        Assert.assertEquals("2", permissions.get(1).get(Permission.TYPE));
        Assert.assertEquals("1234", permissions.get(0).get(Permission.KEY));
        Assert.assertEquals("1234", permissions.get(1).get(Permission.KEY));
        Assert.assertEquals("FECHAINICIO1", permissions.get(0).get(Permission.START_DATE));
        Assert.assertEquals("FECHAINICIO2", permissions.get(1).get(Permission.START_DATE));
        Assert.assertEquals("FECHATERMINO1", permissions.get(0).get(Permission.END_DATE));
        Assert.assertEquals("FECHATERMINO2", permissions.get(1).get(Permission.END_DATE));
    }

    @Test
    public void testIsLastMessagePart(){
        String lastPart = ";0;Esclavo3;0;*";
        String notLastPart = ";0;Esclavo3;0;";
        Assert.assertTrue(BluetoothProtocol.isLastMessagePart(lastPart));
        Assert.assertFalse(BluetoothProtocol.isLastMessagePart(notLastPart));
    }

    @Test
    public void testOpenDoorResponse(){
        String openResponse = "07;1;0;*";
        String notOpenResponse = "07;0;2;*";
        Assert.assertTrue(BluetoothProtocol.isDoorOpened(openResponse));
        Assert.assertFalse(BluetoothProtocol.isDoorOpened(notOpenResponse));
    }

    @Test
    public void testModifiedPermissionResponse(){
        String response = "06;1;1234;0;*";
        String error = "06;0;;1;*";
        Assert.assertEquals("1234", BluetoothProtocol.getNewPermissionKey(response));
        Assert.assertNull(BluetoothProtocol.getNewPermissionKey(error));
    }

    @Test
    public void testBuildGetSlavesMessage(){
        String message = BluetoothProtocol.buildGetSlavesMessage("1234");
        String[] parts = message.split(BluetoothProtocol.SEPARATOR);

        Assert.assertEquals("10", parts[0]);
        Assert.assertEquals("1234", parts[2]);
        Assert.assertEquals("*", parts[3]);
    }

    @Test
    public void testBuildNewAdminPermissionMessage(){
        /*
         02 ; 1 fecha mensaje ; 2 clave mensaje ; 3 clave permiso ; 4 id esclavo anterior ; 5 id esclavo nuevo
         ; 6 tipo modificación ; 7 tipo permiso ; 8 fecha inicio ; 9 fecha expiración ; *
         */
        String message = BluetoothProtocol.buildNewPermissionMessage(mAdminPermission, PERMISSION_KEY_ADMIN);
        String[] parts = message.split(BluetoothProtocol.SEPARATOR);

        Assert.assertEquals("02", parts[0]); // tipo mensaje
        Assert.assertEquals(PERMISSION_KEY_ADMIN, parts[2]); // clave mensaje admin
        Assert.assertEquals(BluetoothProtocol.EMPTY, parts[3]); // clave permiso editar
        Assert.assertEquals(BluetoothProtocol.EMPTY, parts[4]); // id esclavo anterior
        Assert.assertEquals(String.valueOf(mAdminPermission.getSlaveId()), parts[5]); // id esclavo nuevo
        Assert.assertEquals(String.valueOf(BluetoothProtocol.CREATE_NEW_PERMISSION_CODE),
                parts[6]); // tipo modificacion (crear)
        Assert.assertEquals(String.valueOf(Permission.ADMIN_PERMISSION), parts[7]); // tipo permiso
        Assert.assertEquals(mAdminPermission.getStartDate(), parts[8]); // fecha inicio
        Assert.assertEquals(BluetoothProtocol.EMPTY, parts[9]); // fecha termino
    }

    @Test
    public void testEditPermissionMessage(){
         /*
         02 ; 1 fecha mensaje ; 2 clave mensaje ; 3 clave permiso ; 4 id esclavo anterior ; 5 id esclavo nuevo
         ; 6 tipo modificación ; 7 tipo permiso ; 8 fecha inicio ; 9 fecha expiración ; *
         */
        String message = BluetoothProtocol.buildEditPermissionMessage(mToEditPermission, mSlave1.getId()
                , PERMISSION_KEY_ADMIN);

        String[] parts = message.split(BluetoothProtocol.SEPARATOR);

        Assert.assertEquals("02", parts[0]);
        Assert.assertEquals(PERMISSION_KEY_ADMIN, parts[2]); // admin message key
        Assert.assertEquals(mToEditPermission.getKey(), parts[3]); // edited permisison key
        Assert.assertEquals(String.valueOf(mSlave1.getId()), parts[4]); // old slave id
        Assert.assertEquals(String.valueOf(mToEditPermission.getSlaveId()), parts[5]); // new slave id
        Assert.assertEquals(String.valueOf(BluetoothProtocol.MODIFY_PERMISSION_CODE),
                parts[6]); // modification type
        Assert.assertEquals(String.valueOf(Permission.getType(mToEditPermission.getType())), parts[7]); // permission type
        Assert.assertEquals(mToEditPermission.getStartDate() , parts[8]); // start date
        Assert.assertEquals(mToEditPermission.getEndDate() , parts[9]); // end date
        Assert.assertEquals(BluetoothProtocol.MESSAGE_END , parts[10]); // end
    }

    @Test
    public void testBuildFirstConfigurationMessage() {

        String message = BluetoothProtocol.buildFirstConfigurationMessage("1234", "0000", "Puerta");
        String[] parts = message.split(BluetoothProtocol.SEPARATOR);

        Assert.assertEquals("03", parts[0]);
        Assert.assertEquals("1234", parts[2]);
        Assert.assertEquals("0000", parts[3]);
        Assert.assertEquals("Puerta", parts[4]);
        Assert.assertEquals("*", parts[5]);
    }

    @Test
    public void testBuildOpenMessage() {
        // 01 ; 1 fecha mensaje ; 2 clave mensaje ; 3 id esclavo clave ; 4 id esclavo ; 5 comando ; 6 *
        String message = BluetoothProtocol.buildOpenMessage(mToEditPermission.getKey(),
                mToEditPermission.getSlaveId(), SLAVE_ID_ONE);
        String[] parts = message.split(BluetoothProtocol.SEPARATOR);

        Assert.assertEquals("01", parts[0]);
        Assert.assertEquals(mToEditPermission.getKey(), parts[2]);
        Assert.assertEquals(String.valueOf(mToEditPermission.getSlaveId()), parts[3]);
        Assert.assertEquals(String.valueOf(SLAVE_ID_ONE), parts[4]);
        Assert.assertEquals("1", parts[5]);
        Assert.assertEquals(BluetoothProtocol.MESSAGE_END, parts[6]);
    }

    @Test
    public void testIsDoorOpened() {
        Assert.assertTrue(BluetoothProtocol.isDoorOpened("07;1;0;*"));
    }

    @Test
    public void testIsNotDoorOpened() {
        Assert.assertFalse(BluetoothProtocol.isDoorOpened("07;0;1;*"));
    }

    @Test
    public void testPairSlaveMessage() {
        // 12 ; 1 fecha mensaje ; 2 clave mensaje ; 3 id esclavo clave ; 4 id esclavo pareo ; *
        String message = BluetoothProtocol.buildPairSlavesMessage(mAdminPermission.getKey(),
                mAdminPermission.getSlaveId(), SLAVE_ID_TWO);
        String[] parts = message.split(BluetoothProtocol.SEPARATOR);

        Assert.assertEquals("12", parts[0]);
        Assert.assertEquals(mAdminPermission.getKey(), parts[2]);
        Assert.assertEquals(String.valueOf(mAdminPermission.getSlaveId()), parts[3]);
        Assert.assertEquals(String.valueOf(SLAVE_ID_TWO), parts[3]);
    }
}
