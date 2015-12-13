package com.incrementaventures.okey.Activities;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.incrementaventures.okey.Bluetooth.BluetoothClient;
import com.incrementaventures.okey.Fragments.DoorsFragment;
import com.incrementaventures.okey.Fragments.MasterFragment;
import com.incrementaventures.okey.Fragments.InsertPinFragment;
import com.incrementaventures.okey.Fragments.MenuFragment;
import com.incrementaventures.okey.Fragments.ModifyPermissionFragment;
import com.incrementaventures.okey.Fragments.ScanDevicesFragment;
import com.incrementaventures.okey.Models.Master;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.Slave;
import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements InsertPinFragment.PinDialogListener,
        User.OnUserBluetoothToActivityResponse,
        User.OnActionMasterResponse,
        User.OnPermissionsResponse,
        Permission.OnNetworkResponseListener,
        MasterFragment.OnSlaveSelectedListener {

    public static final int REQUEST_ENABLE_BT = 1;
    public static final int FIRST_CONFIG = 2;
    public static final String DEFAULT_KEY_EXTRA = "defaultkey";
    public static final String NEW_KEY_EXTRA = "newkey";
    public static final String MASTER_NAME_EXTRA = "doorname";
    public static final String SCANNED_DOOR_EXTRA = "scanneddoor";

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    private ProgressDialog mProgressDialog;
    private ScanDevicesFragment mScanDevicesFragment;
    private MasterFragment mMasterFragment;
    private User mCurrentUser;
    private boolean mScanning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setUpToolbar();
        authenticateUser();
        if (mCurrentUser == null) return;
        checkNewPermissions();
        checkPreferences();
        checkBluetoothLeSupport();
        mMasterFragment = new MasterFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, mMasterFragment, MasterFragment.TAG)
                .commit();
        setUpToolbar();
    }

    private void setUpToolbar() {
        mToolbar.setNavigationIcon(R.drawable.ic_action_menu);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void showToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }
    }

    private void checkNewPermissions() {
        Permission.getNewPermissions(this, mCurrentUser);
    }

    private void checkPreferences(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref.getBoolean("protect_with_pin", false)){
            InsertPinFragment dialog = new InsertPinFragment();
            dialog.show(getFragmentManager(), "dialog_pin");
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            finish();
        } else {
            super.onBackPressed();
            getSupportFragmentManager().popBackStack();
            MasterFragment masterFragment = (MasterFragment)
                    getSupportFragmentManager().findFragmentByTag(MasterFragment.TAG);
            if (masterFragment != null && masterFragment.isVisible()) {
                showToolbar();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        if (id == android.R.id.home) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace( R.id.container, new MenuFragment())
                    .addToBackStack(MenuFragment.TAG).commit();
            return true;
        }
        /*if (id == R.id.action_settings) {
            Intent intent = new Intent(this, PreferencesActivity.class);
            startActivity(intent);
            return true;
        }*/
        else if (id == R.id.scan_devices_action){
            // TODO: 10-12-2015 Make new activity for result for scanning. Put scanFragment inside.
            /*mScanDevicesFragment = new ScanDevicesFragment();
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, mScanDevicesFragment)
                    .addToBackStack(null)
                    .commit();
            mScanning = true;*/
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Gets the current user, and if it's null, starts AuthActivity and finishes this activity
     */
    private void authenticateUser(){
        mCurrentUser = User.getLoggedUser(this);
        if (mCurrentUser == null){
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void checkBluetoothLeSupport(){
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void enableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void bluetoothNotSupported() {
        Toast.makeText(this, R.string.device_doesnt_support_bluetooth, Toast.LENGTH_LONG).show();
    }

    @Override
    public void deviceFound(BluetoothDevice device, int rssi, byte[] scanRecord) {
        String deviceName = device.getName();
        if (deviceName == null){
            deviceName = "No name";
        }
        if (mScanDevicesFragment != null)
            mScanDevicesFragment.addDevice(Master.create(deviceName, ""));
    }

    @Override
    public void deviceNotFound() {
        if (mProgressDialog != null) mProgressDialog.dismiss();
        Toast.makeText(this, R.string.device_not_found, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void stopScanning() {
        if (mScanDevicesFragment != null) mScanDevicesFragment.stopScanning();
    }


    /*
        OnUserActionResponse callbacks
     */
    @Override
    public void doorOpened(final int state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) mProgressDialog.dismiss();
                if (state == BluetoothClient.OPEN_MODE) {
                    Toast.makeText(MainActivity.this, R.string.door_opened, Toast.LENGTH_SHORT).show();
                } else if (state == BluetoothClient.DOOR_ALREADY_OPENED) {
                    Toast.makeText(MainActivity.this, R.string.door_already_opened, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void doorOpening() {
        mProgressDialog = ProgressDialog.show(this, null,
              getResources().getString(R.string.opening_door), true);
    }

    @Override
    public void noPermission() {
        Toast.makeText(this, R.string.no_permission, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void slavesFound(ArrayList<HashMap<String, String>> slavesData) { }

    @Override
    public void masterWithNoSlaves() {  }

    @Override
    public void permissionCreated(String key, int type) { }

    @Override
    public void permissionEdited(String key, int type) { }

    @Override
    public void permissionDeleted(String key) { }

    @Override
    public void permissionsReceived(ArrayList<HashMap<String, String>> permissionsData) { }

    @Override
    public void permissionReceived(int type, String key, String start, String end) { }

    @Override
    public void error(final int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) mProgressDialog.dismiss();
                switch (code) {
                    case BluetoothClient.TIMEOUT:
                        Toast.makeText(MainActivity.this,
                                R.string.door_cant_open_timeout, Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothClient.RESPONSE_INCORRECT:
                        Toast.makeText(MainActivity.this,
                                R.string.door_cant_open_bad_code, Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothClient.CANT_OPEN:
                        Toast.makeText(MainActivity.this,
                                R.string.door_cant_open, Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothClient.CANT_CONFIGURE:
                        Toast.makeText(MainActivity.this,
                                R.string.door_cant_configure, Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothClient.PERMISSION_NOT_CREATED:
                        Toast.makeText(MainActivity.this,
                                R.string.permission_not_created, Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothClient.DONT_HAVE_PERMISSION:
                        Toast.makeText(MainActivity.this,
                                R.string.no_permission, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if (mScanning){
                    /* if(resultCode == RESULT_OK){
                        mScanDevicesFragment = new ScanDevicesFragment();
                        getFragmentManager().beginTransaction()
                                .replace(R.id.content_frame, mScanDevicesFragment)
                                .addToBackStack(null)
                                .commit();
                        mScanning = false;
                    } else {
                        mScanDevicesFragment.stopScanning();
                    }*/
                }
                break;
            case FIRST_CONFIG:
                if (resultCode == RESULT_OK){
                    Bundle extras = data.getExtras();
                    mCurrentUser.makeFirstAdminConnection(extras.getString(DEFAULT_KEY_EXTRA),
                                                          extras.getString(NEW_KEY_EXTRA),
                                                          extras.getString(MASTER_NAME_EXTRA));
                    mProgressDialog = ProgressDialog.show(this, null,
                            getResources().getString(R.string.configuring_door_dialog));
                }
                break;
            default:
                Toast.makeText(this, R.string.not_implemented_code_on_result,
                        Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPinDialogPositiveClick(String pin) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPref.getString("protect_pin", "").equals(pin)){
            Toast.makeText(this, R.string.pin_incorrect, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onPinDialogNegativeClick() {
        finish();
    }

    @Override
    public void onNewPermissions(HashMap<Master, Permission> permissions) {
        for (Map.Entry<Master, Permission> pair : permissions.entrySet()) {
            Permission permission = pair.getValue();
            Master master = pair.getKey();
            if (permission == null || master == null) {
                continue;
            }
            permission.save();
            master.save();
            MasterFragment masterFragment = (MasterFragment)
                    getSupportFragmentManager().findFragmentById(R.id.container);
            masterFragment.masterNetworkFound(master);
        }
    }

    @Override
    public void openDoorSelected(Master master, Slave slave) {

    }

    @Override
    public void readMyPermissionSelected(Master master, Slave slave, String permissionKey) {

    }

    @Override
    public void readAllPermissionsSelected(Master master, Slave slave, String permissionKey) {

    }

    @Override
    public void openWhenCloseSelected(Master master, Slave slave, String permissionKey) {

    }


    public void onGoHomeClicked(View view) {
        getSupportFragmentManager().popBackStack();
        MasterFragment masterFragment = (MasterFragment)
                getSupportFragmentManager().findFragmentByTag(MasterFragment.TAG);
        if (masterFragment == null || !masterFragment.isVisible()) {
            getSupportFragmentManager().popBackStack(MasterFragment.TAG,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        showToolbar();
    }

    public void onAddNew0keyClicked(View view) {
        getSupportFragmentManager().popBackStack();

    }

    public void onAddNewDoorClicked(View view) {
        getSupportFragmentManager().popBackStack();

    }

    public void onAddNewPermissionClicked(View view) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack();
        Bundle args = new Bundle();
        args.putString(Master.UUID, mMasterFragment.getSelectedMaster().getUUID());
        Slave slave = mMasterFragment.getSelectedSlave();
        if (slave != null) {
            args.putString(Slave.UUID, slave.getUUID());
            args.putString(Permission.KEY, slave.getPermission(User.getLoggedUser()).getKey());
            args.putString(Permission.SLAVE_ID, slave.getPermission(User.getLoggedUser())
                    .getSlaveId());
            args.putString(Permission.NAME, "Permission name");
        }
        ModifyPermissionFragment fragment = new ModifyPermissionFragment();
        fragment.setArguments(args);
        fragmentManager.beginTransaction()
                .replace( R.id.container, fragment)
                .addToBackStack(MenuFragment.TAG)
                .commit();
    }

    public void onSettingsClicked(View view) {
        getSupportFragmentManager().popBackStack();

    }
}
