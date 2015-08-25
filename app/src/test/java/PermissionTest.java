import android.test.mock.MockApplication;
import android.test.mock.MockContext;

import com.incrementaventures.okey.Activities.DoorActivity;
import com.incrementaventures.okey.Activities.MainActivity;
import com.incrementaventures.okey.Models.Master;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.OkeyApplication;
import com.parse.Parse;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mock;

/**
 * Created by Andres on 25-08-2015.
 */
public class PermissionTest {

    @Test
    public void testIsValid(){

        MockContext mock = new MockContext();
        Parse.initialize(mock, "EAAYulhYX56NsqKAkxRGZjRz8DIDLndXENykO59I", "Mw37xHHNFlcwGsY1akSEbQRH7YZQGrL7xhA9jhwf");

        User u = new User("Test", "Test", "Test", "phone");
        Master m = Master.create("Name", "Description");
        Permission p1 = Permission.create(u, m, Permission.TEMPORAL_PERMISSION, "1234", "2015-08-24");
        Permission p2 = Permission.create(u, m, Permission.PERMANENT_PERMISSION, "1234", "2015-08-24");
        Permission p3 = Permission.create(u, m, Permission.ADMIN_PERMISSION, "1234", "2015-08-24");
        Assert.assertFalse(p1.isValid());
        Assert.assertTrue(p2.isValid());
        Assert.assertTrue(p3.isValid());
    }
}
