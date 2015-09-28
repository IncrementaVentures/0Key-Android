import com.incrementaventures.okey.Bluetooth.BluetoothProtocol;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.Slave;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

public class ProtocolTest {

    @Test
    public void testGetSlavesList(){
        String response = "11;1;0;Eslavo1;&;2;0;Esclavo2;&;3;0;Esclavo3;0;*";
        ArrayList<HashMap<String,String>> data = BluetoothProtocol.getSlavesList(response);
        HashMap<String,String> b = data.get(0);
        String id = b.get(Slave.ID);
        String type = b.get(Slave.TYPE);
        String name = b.get(Slave.NAME);
        Assert.assertEquals(id , "1");
        Assert.assertEquals(type, "0");
        Assert.assertNull(name);
    }

    @Test
    public void testGetEmptySlaveList(){
        String response = "11;0;*";
        ArrayList<HashMap<String,String>> data = BluetoothProtocol.getSlavesList(response);
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
        String fullMessage = "05;1234;0;0;FECHAINICIO1;FECHATERMINO1;&;1234;1;2;FECHAINICIO2;FECHATERMINO2;0;*";
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
    public void testBuildNewTemporalPermissionMessage(){
        String message = BluetoothProtocol.buildNewPermissionMessage("Temporal",
                1,
                "2015-12-18", "10:00",
                "2016-12-18", "09:00", "4444");
        String[] parts = message.split(BluetoothProtocol.SEPARATOR);

        Assert.assertEquals("02", parts[0]); // tipo mensaje
        Assert.assertEquals("4444", parts[2]); // clave permiso admin
        Assert.assertEquals("0", parts[3]); // clave permiso editar
        Assert.assertEquals("0", parts[4]); // id esclavo anterior
        Assert.assertEquals("1", parts[5]); // id esclavo a crear
        Assert.assertEquals("0", parts[6]); // tipo modificacion (crear)
        Assert.assertEquals("2", parts[7]); // tipo permiso
        Assert.assertEquals("2015-12-18T10:00", parts[8]); // fecha inicio
        Assert.assertEquals("2016-12-18T09:00", parts[9]); // fecha termino
    }

    @Test
    public void testBuildNewPermanentPermission(){
        String message = BluetoothProtocol.buildNewPermissionMessage("Permanent",
                1,
                "2015-12-18", "10:00",
                "2016-12-18", "09:00", "4444");
        String[] parts = message.split(BluetoothProtocol.SEPARATOR);

        Assert.assertEquals("02", parts[0]); // tipo mensaje
        Assert.assertEquals("4444", parts[2]); // clave permiso admin
        Assert.assertEquals("0", parts[3]); // clave permiso editar
        Assert.assertEquals("0", parts[4]); // id esclavo anterior
        Assert.assertEquals("1", parts[5]); // id esclavo a crear
        Assert.assertEquals("0", parts[6]); // tipo modificacion (crear)
        Assert.assertEquals("1", parts[7]); // tipo permiso
        Assert.assertEquals("2015-12-18T10:00", parts[8]); // fecha inicio
        Assert.assertEquals(BluetoothProtocol.EMPTY, parts[9]); // fecha termino
    }


    @Test
    public void testBuildNewAdminPermissionMessage(){
        String message = BluetoothProtocol.buildNewPermissionMessage("Administrator",
                1,
                "2015-12-18", "10:00",
                "2016-12-18", "09:00", "4444");
        String[] parts = message.split(BluetoothProtocol.SEPARATOR);

        Assert.assertEquals("02", parts[0]); // tipo mensaje
        Assert.assertEquals("4444", parts[2]); // clave permiso admin
        Assert.assertEquals("0", parts[3]); // clave permiso editar
        Assert.assertEquals("0", parts[4]); // id esclavo anterior
        Assert.assertEquals("1", parts[5]); // id esclavo a crear
        Assert.assertEquals("0", parts[6]); // tipo modificacion (crear)
        Assert.assertEquals("0", parts[7]); // tipo permiso
        Assert.assertEquals("2015-12-18T10:00", parts[8]); // fecha inicio
        Assert.assertEquals(BluetoothProtocol.EMPTY , parts[9]); // fecha termino
    }

    @Test
    public void testEditPermissionMessage(){
        String message = BluetoothProtocol.buildEditPermissionMessage("Temporal",
                1, 2,
                "2015-12-18", "10:00",
                "2016-12-18", "09:00", "4444", "5555");
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
