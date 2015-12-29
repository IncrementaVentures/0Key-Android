import android.text.format.Time;

import com.incrementaventures.okey.Bluetooth.BluetoothProtocol;
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
    static final String PERMISSION_KEY = "4321";
    static final String START_DATE = "2015-10-10T10:10";
    static final String END_DATE = "2015-10-10T18:00";
    Master mMaster;
    Permission mAdminPermission;
    User mUser;
    Slave mSlave1;
    Slave mSlave2;

    @Before
    public void setUp() {
        mMaster = mMaster.create(MASTER_ID, MASTER_NAME, USER_UUID);
        mSlave1 = Slave.create(mMaster.getUUID(), mMaster.getId(), SLAVE_NAME_ONE, 0, SLAVE_ID_ONE);
        mSlave1 = Slave.create(mMaster.getUUID(), mMaster.getId(), SLAVE_NAME_TWO, 0, SLAVE_ID_TWO);
        mUser = User.getUser(USER_EMAIL);
        mAdminPermission = Permission.create(mUser, mMaster, Permission.ADMIN_PERMISSION, PERMISSION_KEY,
                START_DATE, END_DATE, mSlave1.getId());
    }

    @Test
    public void testFormatDate(){
        Time time = new Time();
        time.set(0, 0, 0, 0, 0, 2015);

        String month = String.valueOf(time.month + 1);
        String day = String.valueOf(time.monthDay + 1);
        String hour = String.valueOf(time.hour);
        String minute = String.valueOf(time.minute);
        String year = String.valueOf(time.year);

        if (month.length() == 1) month = "0" + month;
        if (day.length() == 1) day = "0" + day;
        if (hour.length() == 1) hour = "0" + hour;
        if (minute.length() == 1) minute = "0" + minute;

        String formattedDate =  year + "-" + month + "-"
                + day + "T" + hour + ":" + minute;
        Assert.assertEquals("2000-01-01T00:00", formattedDate);
    }

    @Test
    public void testGetSlavesList() {
        String response = "11;1;0;&;2;0;&;3;0;0;*";
        ArrayList<Slave> data = BluetoothProtocol.getSlavesList(response);
        Slave b = data.get(0);
        int id = b.getId();
        int type = b.getType();
        String name = b.getName();
        Assert.assertEquals(id , 1);
        Assert.assertEquals(type, 0);
        Assert.assertNull(name);
    }

    @Test
    public void testGetEmptySlaveList(){
        String response = "11;0;*";
        ArrayList<Slave> data = BluetoothProtocol.getSlavesList(response);
        Assert.assertEquals(true, data.size() == 0);
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
    public void testBuildGetAllPermissionsMessage(){
        String message = BluetoothProtocol.buildGetAllPermissionsMessage(1, "1234");
        String[] parts = message.split(BluetoothProtocol.SEPARATOR);

        Assert.assertEquals("09", parts[0]);
        Assert.assertEquals("1234", parts[2]);
        Assert.assertEquals(BluetoothProtocol.EMPTY , parts[3]);
        Assert.assertEquals("*", parts[4]);
    }

    @Test
    public void testBuildGetUserPermissionMessage(){
        String message = BluetoothProtocol.buildGetUserPermissionMessage(1, "1234");
        String[] parts = message.split(BluetoothProtocol.SEPARATOR);

        Assert.assertEquals("08", parts[0]);
        Assert.assertEquals("1234", parts[2]);
        Assert.assertEquals(BluetoothProtocol.EMPTY , parts[3]);
        Assert.assertEquals("*", parts[4]);
    }

    @Test
    public void testBuildNewAdminPermissionMessage(){
        String message = BluetoothProtocol.buildNewPermissionMessage(mAdminPermission, PERMISSION_KEY);
        String[] parts = message.split(BluetoothProtocol.SEPARATOR);

        Assert.assertEquals("02", parts[0]); // tipo mensaje
        Assert.assertEquals(mAdminPermission.getKey(), parts[2]); // clave permiso admin
        Assert.assertEquals("0", parts[3]); // clave permiso editar
        Assert.assertEquals("0", parts[4]); // id esclavo anterior
        Assert.assertEquals(mAdminPermission.getKey(), parts[5]); // id esclavo a crear
        Assert.assertEquals("0", parts[6]); // tipo modificacion (crear)
        Assert.assertEquals(mAdminPermission.getKey(), parts[7]); // tipo permiso
        Assert.assertEquals(START_DATE, parts[8]); // fecha inicio
        Assert.assertEquals(BluetoothProtocol.EMPTY, parts[9]); // fecha termino
    }

    @Test
    public void testEditPermissionMessage(){
        Assert.fail();
        String message = BluetoothProtocol.buildEditPermissionMessage(null, null, PERMISSION_KEY);

        String[] parts = message.split(BluetoothProtocol.SEPARATOR);

        Assert.assertEquals("02", parts[0]);
        Assert.assertEquals("4444", parts[2]); // admin key
        Assert.assertEquals("5555", parts[3]); // edited permisison key
        Assert.assertEquals("1", parts[4]); // old slave id
        Assert.assertEquals("2", parts[5]); // new slave id
        Assert.assertEquals("1", parts[6]); // modification type
        Assert.assertEquals("2", parts[7]); // permission type
        Assert.assertEquals("2015-12-18T10:00" , parts[8]); // start date
        Assert.assertEquals("2016-12-18T09:00" , parts[9]); // end date
        Assert.assertEquals(BluetoothProtocol.MESSAGE_END , parts[10]); // end
    }

    @Test
    public void testBuildFirstConfigurationMessage(){
        String message = BluetoothProtocol.buildFirstConfigurationMessage("1234", "0000", "Puerta");
        String[] parts = message.split(BluetoothProtocol.SEPARATOR);

        Assert.assertEquals("03", parts[0]);
        Assert.assertEquals("1234", parts[2]);
        Assert.assertEquals("0000", parts[3]);
        Assert.assertEquals("Puerta", parts[4]);
        Assert.assertEquals("*", parts[5]);
    }

    @Test
    public void testBuildOpenMessage(){
        String message = BluetoothProtocol.buildOpenMessage("1234", 1);
        String[] parts = message.split(BluetoothProtocol.SEPARATOR);

        Assert.assertEquals("01", parts[0]);
        Assert.assertEquals("1234", parts[2]);
        Assert.assertEquals("1", parts[3]);
        Assert.assertEquals("1", parts[4]);
        Assert.assertEquals("*", parts[5]);
    }
}
