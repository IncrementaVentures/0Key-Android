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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.incrementaventures.okey.Bluetooth.BluetoothClient;
import com.incrementaventures.okey.Bluetooth.BluetoothProtocol;
import com.incrementaventures.okey.Fragments.DoorFragment;
import com.incrementaventures.okey.Models.Master;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.Slave;
import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.R;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;

public class DoorActivity extends ActionBarActivity implements User.OnActionMasterResponse, User.OnPermissionsResponse, User.OnUserBluetoothToActivityResponse, DoorFragment.OnSlaveSelectedListener {

    public static final int NEW_PERMISSION_REQUEST = 40;
    public static final int EDIT_PERMISSION_REQUEST = 41;
    public static final String REQUEST_CODE = "request_code";
    private Master mMaster;
    private ProgressDialog mProgressDialog;
    private User mCurrentUser;
    private DoorFragment mDoorFragment;
    private boolean mScannedDoor;
    private boolean mConfiguring;
    private String mPermissionKey;

    private Bundle mPermissionData;


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
                openCreateEditPermissionActivity(NEW_PERMISSION_REQUEST);
            }
            else {
                Toast.makeText(this, R.string.you_are_not_admin, Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        else if (id == R.id.set_permission_key){
            showSetKeyDialog();
            return true;
        }

        else if( id == R.id.edit_permission){
            Intent intent = new Intent(DoorActivity.this, CreateEditPermissionActivity.class);
            Permission p = mMaster.getPermission();
            intent.putExtra(Permission.KEY, p.getKey());
            intent.putExtra(Permission.START_DATE,  p.getStartDate());
            intent.putExtra(Permission.END_DATE,  p.getEndDate());
            intent.putExtra(Permission.TYPE, BluetoothProtocol.getPermissionType(p.getType()));
            intent.putExtra(REQUEST_CODE, EDIT_PERMISSION_REQUEST);
            startActivityForResult(intent, EDIT_PERMISSION_REQUEST);
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

    private void openCreateEditPermissionActivity(int mode){
        Intent intent = new Intent(this, CreateEditPermissionActivity.class);
        intent.putExtra(REQUEST_CODE, mode);
        intent.putExtra(Permission.KEY, mMaster.getPermission().getKey());
        startActivityForResult(intent, mode);
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
                Permission p = null;
                if (mMaster != null) {
                    p = mMaster.getPermission();
                }
                if (p == null){
                    p = Permission.create(mCurrentUser, mMaster, Permission.UNKNOWN_PERMISSION,  mPermissionKey, Permission.UNKNOWN_DATE, Permission.UNKNOWN_DATE);
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
    public void slavesFound(ArrayList<HashMap<String,String>> slavesData) {
        if (mProgressDialog != null) mProgressDialog.dismiss();
        mDoorFragment.addSlave(slavesData);
    }

    @Override
    public void masterWithNoSlaves() {
        if (mProgressDialog != null) mProgressDialog.dismiss();
        Toast.makeText(this, R.string.no_permission, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void permissionCreated(final String key, final int type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) mProgressDialog.dismiss();
                if (mConfiguring){
                    mMaster = getDoor();
                    Permission p = Permission.create(mCurrentUser, mMaster, type, key, Permission.PERMANENT_DATE, Permission.PERMANENT_DATE);
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
    public void permissionEdited(String key, int type) {
        if (mProgressDialog != null) mProgressDialog.dismiss();
        String startHour = mPermissionData.getString(CreateEditPermissionActivity.PERMISSION_START_HOUR);
        String startDate = mPermissionData.getString(CreateEditPermissionActivity.PERMISSION_START_DATE);
        String endHour = mPermissionData.getString(CreateEditPermissionActivity.PERMISSION_END_HOUR);
        String endDate = mPermissionData.getString(CreateEditPermissionActivity.PERMISSION_END_DATE);
        Permission p = mMaster.getPermission();
        p.setStartDate(startDate + "T" + startHour);
        p.setEndDate(endDate + "T" + endHour);
        p.setType(type);
        p.save();
        Toast.makeText(this, R.string.permission_edited, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void permissionsReceived(final ArrayList<HashMap<String, String>> permissionsData) {
        if (mProgressDialog != null) mProgressDialog.dismiss();
        if (permissionsData.size() == 0){
            Toast.makeText(this, "No slaves configured yet.", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<String> array = new ArrayList<>();
        for (HashMap<String, String> permission : permissionsData){
            array.add(permission.get(Permission.KEY) +
                      permission.get(Slave.ID)+
                      permission.get(Permission.TYPE) +
                      permission.get(Permission.START_DATE) +
                      permission.get(Permission.END_DATE));
        }
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(DoorActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = inflater.inflate(R.layout.permissions_dialog, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle(R.string.permissions);
        ListView lv = (ListView) convertView.findViewById(R.id.list_permissions);
        ArrayAdapter<String> adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, array);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> p = permissionsData.get(position);
                Intent intent = new Intent(DoorActivity.this, CreateEditPermissionActivity.class);
                intent.putExtra(Permission.KEY, p.get(Permission.KEY));
                intent.putExtra(Permission.START_DATE, p.get(Permission.START_DATE));
                intent.putExtra(Permission.END_DATE, p.get(Permission.END_DATE));
                intent.putExtra(Permission.TYPE, p.get(Permission.TYPE));
                intent.putExtra(Slave.ID, p.get(Slave.ID));
                intent.putExtra(REQUEST_CODE, EDIT_PERMISSION_REQUEST);
                startActivityForResult(intent, EDIT_PERMISSION_REQUEST);
            }
        });
        lv.setAdapter(adapter);
        alertDialog.show();
    }

    @Override
    public void permissionReceived(int type, String key, String start, String end) {
        if (mProgressDialog != null) mProgressDialog.dismiss();
        Toast.makeText(this, "Permission received and saved", Toast.LENGTH_SHORT).show();
        Permission p = mMaster.getPermission();
        p.setType(type);
        p.setStartDate(start);
        p.setEndDate(end);
        p.save();
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
                    case BluetoothClient.PERMISSION_NOT_EDITED:
                        Toast.makeText(DoorActivity.this, R.string.permission_not_edited, Toast.LENGTH_SHORT).show();
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
                    case BluetoothClient.DOOR_NOT_CONFIGURED:
                        Toast.makeText(DoorActivity.this, R.string.door_not_configured, Toast.LENGTH_SHORT).show();
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
        if (mProgressDialog != null) mProgressDialog.dismiss();
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
                    mPermissionData = data.getExtras();
                    String type = mPermissionData.getString(CreateEditPermissionActivity.PERMISSION_TYPE);
                    String startHour = mPermissionData.getString(CreateEditPermissionActivity.PERMISSION_START_HOUR);
                    String startDate = mPermissionData.getString(CreateEditPermissionActivity.PERMISSION_START_DATE);
                    String endHour = mPermissionData.getString(CreateEditPermissionActivity.PERMISSION_END_HOUR);
                    String endDate = mPermissionData.getString(CreateEditPermissionActivity.PERMISSION_END_DATE);
                    String slave = mPermissionData.getString(CreateEditPermissionActivity.PERMISSION_SLAVE);
                    mCurrentUser.createNewPermission(type, slave, startDate, startHour, endDate, endHour, mMaster.getPermission().getKey(), mMaster.getName());
                }
                break;
            case DoorActivity.EDIT_PERMISSION_REQUEST:
                if (resultCode == RESULT_OK){
                    mProgressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.editing_permission));
                    Bundle extras = data.getExtras();
                    String type = extras.getString(CreateEditPermissionActivity.PERMISSION_TYPE);
                    String startHour = extras.getString(CreateEditPermissionActivity.PERMISSION_START_HOUR);
                    String startDate = extras.getString(CreateEditPermissionActivity.PERMISSION_START_DATE);
                    String endHour = extras.getString(CreateEditPermissionActivity.PERMISSION_END_HOUR);
                    String endDate = extras.getString(CreateEditPermissionActivity.PERMISSION_END_DATE);
                    String key = extras.getString(CreateEditPermissionActivity.PERMISSION_KEY);
                    mCurrentUser.editPermission(type, startDate, startHour, endDate, endHour, key, mMaster.getName());
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
        mProgressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.reading_permission));
        mCurrentUser.readMyPermission(master, slave, permissionKey);
    }

    @Override
    public void readAllPermissionsSelected(Master master, Slave slave, String permissionKey) {
        mProgressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.reading_permissions));
        mCurrentUser.readAllPermissions(master, slave, permissionKey);
    }

    @Override
    public void openWhenCloseSelected(Master master, Slave slave, String permissionKey) {
        mProgressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.searching_door));
        mCurrentUser.openWhenClose(master, slave, permissionKey);
    }
}
