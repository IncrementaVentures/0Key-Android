package com.incrementaventures.okey.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.incrementaventures.okey.Bluetooth.BluetoothClient;
import com.incrementaventures.okey.Fragments.DoorFragment;
import com.incrementaventures.okey.Models.Master;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.Slave;
import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.R;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class DoorActivity extends ActionBarActivity implements User.OnActionMasterResponse, User.OnPermissionsResponse, User.OnUserBluetoothToActivityResponse, DoorFragment.OnSlaveSelectedListener {

    public static final int NEW_PERMISSION_REQUEST = 40;

    private Master mMaster;
    private ProgressDialog mProgressDialog;
    private User mCurrentUser;
    private DoorFragment mDoorFragment;
    private boolean mScannedDoor;
    private boolean mConfiguring;
    private String mPermissionKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_door);
        setTitle(getIntent().getExtras().getString(MainActivity.MASTER_NAME_EXTRA));
        mMaster = getDoor();
        mCurrentUser = User.getLoggedUser(this);
        mScannedDoor = getIntent().getExtras().getBoolean(MainActivity.SCANNED_DOOR_EXTRA);
        mDoorFragment = (DoorFragment) getSupportFragmentManager().findFragmentById(R.id.door_fragment);
    }


    private Master getDoor(){
        final String name = getIntent().getExtras().getString(MainActivity.MASTER_NAME_EXTRA);
        if (mScannedDoor){
            return Master.create(name, "");
        }

        ParseQuery query = new ParseQuery(Master.MASTER_CLASS_NAME);
        query.fromLocalDatastore();
        query.whereEqualTo(Master.UUID, getIntent().getExtras().getString(Master.UUID));

        try {
            ParseObject doorParse = query.getFirst();
            return Master.create(doorParse);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
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
            if (mMaster.getPermission().isAdmin()) {
                openNewPermissionActivity();
            } else {
                Toast.makeText(this, R.string.you_are_not_admin, Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        else if (id == R.id.set_permission_key){
            showSetKeyDialog();
            return true;
        }

        else if (id == R.id.first_config_action){
            if (mScannedDoor){
                openFirstConfigurationActivity();
            }
            else {
                Toast.makeText(this, R.string.door_already_configured, Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        else if (id == R.id.action_get_slaves){
            if (!mScannedDoor){
                getSlaves();
            } else{
                Toast.makeText(this, R.string.configure_or_set_key_first, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openNewPermissionActivity(){
        Intent intent = new Intent(this, NewPermissionActivity.class);
        startActivityForResult(intent, DoorActivity.NEW_PERMISSION_REQUEST);
    }

    private void openFirstConfigurationActivity(){
        Intent intent = new Intent(this, DoorConfigurationActivity.class);
        startActivityForResult(intent, MainActivity.FIRST_CONFIG);
        mConfiguring = true;
    }

    private void getSlaves(){
        mProgressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.getting_slaves));
        mCurrentUser.getSlaves(mMaster, mMaster.getPermission().getKey());
    }

    private void showSetKeyDialog(){
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(R.string.set_key_permission);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(4);
        input.setFilters(filterArray);

        b.setView(input);
        b.setPositiveButton(R.string.okey, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                mPermissionKey = input.getText().toString();
                mMaster = getDoor();
                Permission p = mMaster.getPermission();
                if (p == null){
                    p = Permission.create(mCurrentUser, mMaster, Permission.UNKNOWN_PERMISSION,  mPermissionKey, Permission.UNKNOWN_DATE);
                } else {
                    p.setKey(mPermissionKey);
                }
                p.save();
                mMaster.save();
                mScannedDoor = false;
                Toast.makeText(DoorActivity.this, R.string.permission_key_setted, Toast.LENGTH_SHORT).show();
            }
        });
        b.setNegativeButton(R.string.cancel, null);
        b.create().show();
    }

    public void doorOpened(final int state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) mProgressDialog.dismiss();
                if (state == BluetoothClient.OPEN_MODE) {
                    mMaster.save();
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
    public void slaveFound(String id, String type, String name) {
        if (mProgressDialog != null) mProgressDialog.dismiss();
        mDoorFragment.addSlave(id, type, name);
    }

    @Override
    public void permissionCreated(final String key, final int type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) mProgressDialog.dismiss();
                if (mConfiguring){
                    mMaster = getDoor();
                    Permission p = Permission.create(mCurrentUser, mMaster, type, key, Permission.PERMANENT_DATE);
                    mMaster.save();
                    p.save();
                    Toast.makeText(DoorActivity.this, "Success. Your private key is saved.", Toast.LENGTH_SHORT).show();
                    mConfiguring = false;
                    mScannedDoor = false;
                } else {
                    Toast.makeText(DoorActivity.this, "Permission added successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getBaseContext(), ShareKeyActivity.class);
                    intent.putExtra(Permission.KEY, key);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void permissionReceived(int type, String key, String start, String end) {
        if (mProgressDialog != null) mProgressDialog.dismiss();
        Toast.makeText(this, "Permission received", Toast.LENGTH_SHORT).show();
        // TODO: show in UI
    }

    @Override
    public void error(final int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) mProgressDialog.dismiss();
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
                    case BluetoothClient.PERMISSION_NOT_CREATED:
                        Toast.makeText(DoorActivity.this, R.string.permission_not_created, Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothClient.DONT_HAVE_PERMISSION:
                        Toast.makeText(DoorActivity.this, R.string.no_permission, Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothClient.DONT_HAVE_PERMISSION_THIS_HOUR:
                        Toast.makeText(DoorActivity.this, R.string.no_permission_this_hour, Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothClient.BAD_INPUT:
                        Toast.makeText(DoorActivity.this, R.string.bad_input_error, Toast.LENGTH_SHORT).show();
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
        if (mProgressDialog != null) mProgressDialog.dismiss();
        Toast.makeText(this, R.string.device_not_found, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void stopScanning() {

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
                                extras.getString( MainActivity.MASTER_NAME_EXTRA));
                    }
                    mConfiguring = false;
                }
                break;
            case MainActivity.FIRST_CONFIG:
                if (resultCode == RESULT_OK){
                    Bundle extras = data.getExtras();

                    mCurrentUser.makeFirstAdminConnection(extras.getString( MainActivity.DEFAULT_KEY_EXTRA),
                            extras.getString( MainActivity.NEW_KEY_EXTRA),
                            extras.getString( MainActivity.MASTER_NAME_EXTRA));
                    mProgressDialog = ProgressDialog.show(this, null,  getResources().getString(R.string.configuring_door_dialog));
                    mConfiguring = true;
                }
                break;
            case DoorActivity.NEW_PERMISSION_REQUEST:
                if (resultCode == RESULT_OK){
                    mProgressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.creating_permission));
                    Bundle extras = data.getExtras();
                    String type = extras.getString(NewPermissionActivity.NEW_PERMISSION_TYPE);
                    String startHour = extras.getString(NewPermissionActivity.NEW_PERMISSION_START_HOUR);
                    String startDate = extras.getString(NewPermissionActivity.NEW_PERMISSION_START_DATE);
                    String endHour = extras.getString(NewPermissionActivity.NEW_PERMISSION_END_HOUR);
                    String endDate = extras.getString(NewPermissionActivity.NEW_PERMISSION_END_DATE);
                    mCurrentUser.createNewPermission(type, startDate, startHour, endDate, endHour, mMaster.getPermission().getKey(), mMaster.getName());
                }
                break;
            default:
                Toast.makeText(this, R.string.not_implemented_code_on_result, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void openDoorSelected(Master master, Slave slave) {
        mCurrentUser.openDoor(master, slave);
    }

    @Override
    public void readMyPermissionSelected(Master master, Slave slave, String permissionKey) {
        mProgressDialog = ProgressDialog.show(this, null, "Reading permission");
        mCurrentUser.readMyPermission(master, slave, permissionKey);
    }

    @Override
    public void readAllPermissionsSelected(Master master, Slave slave, String permissionKey) {
        mProgressDialog = ProgressDialog.show(this, null, "Reading permissions");
        mCurrentUser.readAllPermissions(master, slave, permissionKey);

    }
}
