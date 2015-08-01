package com.incrementaventures.okey.Activities;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.incrementaventures.okey.Bluetooth.BluetoothClient;
import com.incrementaventures.okey.Models.Door;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.R;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class DoorActivity extends ActionBarActivity implements User.OnOpenDoorActionsResponse, User.OnPermissionsResponse, User.OnUserBluetoothToActivityResponse {

    private Door mDoor;
    private ProgressDialog mProgressDialog;
    private User mCurrentUser;

    private boolean mConfiguring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_door);
        setTitle(getIntent().getExtras().getString(Door.NAME));
        setDoor();
        mCurrentUser = User.getLoggedUser(this);
    }


    private void setDoor(){

        final String id = getIntent().getExtras().getString(Door.ID);
        final String name = getIntent().getExtras().getString(Door.NAME);
        final ParseQuery query = new ParseQuery<>(Door.DOOR_CLASS_NAME);
        query.fromLocalDatastore();
        query.getInBackground(id, new GetCallback() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (name.equals(Door.FACTORY_NAME)){
                    mDoor = Door.create(name, "");
                } else{
                    mDoor = Door.create(parseObject);
                }
            }

            @Override
            public void done(Object o, Throwable throwable) {
                if (name.equals(Door.FACTORY_NAME)){
                    mDoor = Door.create(name, "");
                } else {
                    mDoor = Door.create((ParseObject) o);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_door, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.action_new_permission){
            return true;
        }

        else if (id == R.id.open_door_action) {
            mCurrentUser.openDoor(mDoor);
            return true;
        }

        else if (id == R.id.first_config_action){
            Intent intent = new Intent(this, DoorConfigurationActivity.class);
            startActivityForResult(intent, MainActivity.FIRST_CONFIG);
            mConfiguring = true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void doorOpened(final int state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.dismiss();

                if (state == BluetoothClient.OPEN_MODE) {
                    Toast.makeText(DoorActivity.this, R.string.door_opened, Toast.LENGTH_SHORT).show();
                } else if (state == BluetoothClient.DOOR_ALREADY_OPENED) {
                    Toast.makeText(DoorActivity.this, R.string.door_already_opened, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void doorOpening() {
        mProgressDialog = ProgressDialog.show(this, null ,getResources().getString(R.string.opening_door));
    }

    @Override
    public void noPermission() {
        Toast.makeText(this, R.string.no_permission, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void permissionCreated(final String key, final int type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.dismiss();
                Permission p = Permission.create(mCurrentUser, mDoor, type, key);
                p.save();
                Toast.makeText(DoorActivity.this, "Success, the key is:" + key, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void error(final int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.dismiss();
                switch (code) {
                    case BluetoothClient.TIMEOUT:
                        Toast.makeText(DoorActivity.this, R.string.door_cant_open_timeout, Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothClient.RESPONSE_INCORRECT:
                        Toast.makeText(DoorActivity.this, R.string.door_cant_open_bad_code, Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothClient.CANT_OPEN:
                        Toast.makeText(DoorActivity.this, R.string.door_cant_open, Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothClient.CANT_CONFIGURE:
                        Toast.makeText(DoorActivity.this, R.string.door_cant_configure, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /*
        OnBluetoothUserResponse callbacks
     */
    @Override
    public void enableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, MainActivity.REQUEST_ENABLE_BT);
    }

    @Override
    public void bluetoothNotSupported() {
        Toast.makeText(this, "Sorry, your device doesn't support bluetooth", Toast.LENGTH_LONG).show();
    }

    @Override
    public void deviceFound(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Toast.makeText(this, R.string.device_found, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void deviceNotFound() {
        mProgressDialog.dismiss();
        Toast.makeText(this, R.string.device_not_found, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case MainActivity.REQUEST_ENABLE_BT:
                if(resultCode == RESULT_OK){
                    if (mConfiguring){
                        Bundle extras = data.getExtras();

                        mCurrentUser.makeFirstAdminConnection(extras.getString( MainActivity.DEFAULT_KEY_EXTRA),
                                extras.getString( MainActivity.NEW_KEY_EXTRA),
                                extras.getString( MainActivity.DOOR_NAME_EXTRA));
                    } else{
                        mCurrentUser.openDoor(mDoor);
                    }
                    mConfiguring = false;
                }
                break;
            case MainActivity.FIRST_CONFIG:
                if (resultCode == RESULT_OK){
                    Bundle extras = data.getExtras();

                    mCurrentUser.makeFirstAdminConnection(extras.getString( MainActivity.DEFAULT_KEY_EXTRA),
                            extras.getString( MainActivity.NEW_KEY_EXTRA),
                            extras.getString( MainActivity.DOOR_NAME_EXTRA));
                    mProgressDialog = ProgressDialog.show(this, null,  getResources().getString(R.string.configuring_door_dialog));
                }
                break;
            default:
                Toast.makeText(this, R.string.not_implemented_code_on_result, Toast.LENGTH_SHORT).show();
        }
    }
}
