import com.incrementaventures.okey.Bluetooth.BluetoothProtocol;
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
        HashMap<String,String> b = data.get(1);
        String id = b.get(Slave.ID);
        String type = b.get(Slave.TYPE);
        String name = b.get(Slave.NAME);
        Assert.assertEquals("2", id);
        Assert.assertEquals("0", type);
        Assert.assertEquals("Esclavo2", name);
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
        String notOpenResponse = "07;01;1;*";
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
        String message = BluetoothProtocol.buildGetAllPermissions(1, "1234");
        String[] parts = message.split(BluetoothProtocol.SEPARATOR);

        Assert.assertEquals("09", parts[0]);
        Assert.assertEquals("1234", parts[2]);
        Assert.assertEquals("", parts[3]);
        Assert.assertEquals("*", parts[4]);
    }

    @Test
    public void testBuildGetUserPermissionMessage(){
        String message = BluetoothProtocol.buildGetUserPermissionMessage(1, "1234");
        String[] parts = message.split(BluetoothProtocol.SEPARATOR);

        Assert.assertEquals("08", parts[0]);
        Assert.assertEquals("1234", parts[2]);
        Assert.assertEquals("", parts[3]);
        Assert.assertEquals("*", parts[4]);
    }

    @Test
    public void testBuildNewTemporalPermissionMessage(){
        String message = BluetoothProtocol.buildNewPermissionMessage("Temporal",
                1,
                "2015-12-18", "10:00",
                "2016-12-18", "09:00", "4444");
        String[] parts = message.split(BluetoothProtocol.SEPARATOR);

        Assert.assertEquals("02", parts[0]);
        Assert.assertEquals("4444", parts[2]);
        Assert.assertEquals("", parts[3]);
        Assert.assertEquals("0", parts[4]); // tipo modificacion
        Assert.assertEquals("2", parts[5]); // tipo permiso
        Assert.assertEquals("2015-12-18T10:00", parts[6]); // fecha inicio
        Assert.assertEquals("2016-12-18T09:00", parts[7]); // fecha termino
        Assert.assertEquals("", parts[8]); // clave
    }

    @Test
    public void testBuildNewPermanentPermission(){
        String message = BluetoothProtocol.buildNewPermissionMessage("Permanent",
                1,
                "2015-12-18", "10:00",
                "2016-12-18", "09:00", "4444");
        String[] parts = message.split(BluetoothProtocol.SEPARATOR);

        Assert.assertEquals("02", parts[0]);
        Assert.assertEquals("4444", parts[2]);
        Assert.assertEquals("", parts[3]);
        Assert.assertEquals("0", parts[4]); // tipo modificacion
        Assert.assertEquals("1", parts[5]); // tipo permiso
        Assert.assertEquals("", parts[6]); // fecha inicio
        Assert.assertEquals("", parts[7]); // fecha termino
        Assert.assertEquals("", parts[8]); // clave
    }


    @Test
    public void testBuildNewAdminPermissionMessage(){
        String message = BluetoothProtocol.buildNewPermissionMessage("Administrator",
                1,
                "2015-12-18", "10:00",
                "2016-12-18", "09:00", "4444");
        String[] parts = message.split(BluetoothProtocol.SEPARATOR);

        Assert.assertEquals("02", parts[0]);
        Assert.assertEquals("4444", parts[2]);
        Assert.assertEquals("", parts[3]);
        Assert.assertEquals("0", parts[4]); // tipo modificacion
        Assert.assertEquals("0", parts[5]); // tipo permiso
        Assert.assertEquals("", parts[6]); // fecha inicio
        Assert.assertEquals("", parts[7]); // fecha termino
        Assert.assertEquals("", parts[8]); // clave
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
