package com.incrementaventures.okey.Activities;

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.incrementaventures.okey.Bluetooth.BluetoothClient;
import com.incrementaventures.okey.Fragments.InsertPinFragment;
import com.incrementaventures.okey.Fragments.MainFragment;
import com.incrementaventures.okey.Fragments.ScanDevicesFragment;
import com.incrementaventures.okey.Models.Master;
import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.R;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MainActivity extends ActionBarActivity implements InsertPinFragment.PinDialogListener, User.OnUserBluetoothToActivityResponse, User.OnActionMasterResponse, User.OnPermissionsResponse {
    public static final int REQUEST_ENABLE_BT = 1;
    public static final int FIRST_CONFIG = 2;
    public static final String DEFAULT_KEY_EXTRA = "defaultkey";
    public static final String NEW_KEY_EXTRA = "newkey";
    public static final String MASTER_NAME_EXTRA = "doorname";
    public static final String SCANNED_DOOR_EXTRA = "scanneddoor";

    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @Bind(R.id.left_drawer)
    ListView mDrawerList;

    private String[] mDrawerItems;
    private ActionBarDrawerToggle mDrawerToggle;
    private ProgressDialog mProgressDialog;
    private ScanDevicesFragment mScanDevicesFragment;
    private User mCurrentUser;
    private boolean mScanning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        authenticateUser();
        drawerSetup();
        checkPreferences();
        checkBluetoothLeSupport();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void checkPreferences(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref.getBoolean("protect_with_pin", false)){
            InsertPinFragment dialog = new InsertPinFragment();
            dialog.show(getFragmentManager(), "dialog_pin");
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);

        return super.onPrepareOptionsMenu(menu);
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

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, PreferencesActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.scan_devices_action){
            mScanDevicesFragment = new ScanDevicesFragment();
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, mScanDevicesFragment)
                    .addToBackStack(null)
                    .commit();
            mScanning = true;
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


    /*
        Configure the side drawer navigation menu
     */
    public void drawerSetup(){
        mDrawerItems = new String[] {"Main page", "Doors", "Permissions", "Log"};
        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, mDrawerItems));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected = mDrawerItems[position];
                switch (selected){
                    case "Doors":
                        /*DoorsFragment doorsFragment = new DoorsFragment();
                        getFragmentManager().beginTransaction()
                                .replace(R.id.content_frame, doorsFragment)
                                .addToBackStack(null)
                                .commit();*/
                        break;
                    case "Main page":
                        MainFragment mainFragment = new MainFragment();
                        getFragmentManager().beginTransaction()
                            .replace(R.id.content_frame, mainFragment)
                            .addToBackStack(null)
                            .commit();
                        break;
                    case "Permissions":
                        break;
                    case "Log":
                        break;
                    default:
                        break;
                }
                mDrawerLayout.closeDrawers();
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_name, R.string.app_name){
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(R.string.masters);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(R.string.app_name);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);



        MainFragment newFragment = new MainFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.content_frame, newFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    private void checkBluetoothLeSupport(){
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    /*
        OnBluetoothUserResponse callbacks
     */
    @Override
    public void enableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void bluetoothNotSupported() {
        Toast.makeText(this, "Sorry, your device doesn't support bluetooth", Toast.LENGTH_LONG).show();
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
    public void slavesFound(ArrayList<HashMap<String, String>> slavesData) {
    }

    @Override
    public void masterWithNoSlaves() {
    }


    @Override
    public void permissionCreated(String key, int type) {

    }

    @Override
    public void permissionEdited(String key, int type) {

    }

    @Override
    public void permissionsReceived(ArrayList<HashMap<String, String>> permissionsData) {

    }

    @Override
    public void permissionReceived(int type, String key, String start, String end) {

    }

    @Override
    public void error(final int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) mProgressDialog.dismiss();
                switch (code) {
                    case BluetoothClient.TIMEOUT:
                        Toast.makeText(MainActivity.this, R.string.door_cant_open_timeout, Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothClient.RESPONSE_INCORRECT:
                        Toast.makeText(MainActivity.this, R.string.door_cant_open_bad_code, Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothClient.CANT_OPEN:
                        Toast.makeText(MainActivity.this, R.string.door_cant_open, Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothClient.CANT_CONFIGURE:
                        Toast.makeText(MainActivity.this, R.string.door_cant_configure, Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothClient.PERMISSION_NOT_CREATED:
                        Toast.makeText(MainActivity.this, R.string.permission_not_created, Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothClient.DONT_HAVE_PERMISSION:
                        Toast.makeText(MainActivity.this, R.string.no_permission, Toast.LENGTH_SHORT).show();
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
                    if(resultCode == RESULT_OK){
                        mScanDevicesFragment = new ScanDevicesFragment();
                        getFragmentManager().beginTransaction()
                                .replace(R.id.content_frame, mScanDevicesFragment)
                                .addToBackStack(null)
                                .commit();
                        mScanning = false;
                    } else {
                        mScanDevicesFragment.stopScanning();
                    }
                }

                break;
            case FIRST_CONFIG:
                if (resultCode == RESULT_OK){
                    Bundle extras = data.getExtras();

                    mCurrentUser.makeFirstAdminConnection(extras.getString(DEFAULT_KEY_EXTRA),
                                                          extras.getString(NEW_KEY_EXTRA),
                                                          extras.getString(MASTER_NAME_EXTRA));
                    mProgressDialog = ProgressDialog.show(this, null,  getResources().getString(R.string.configuring_door_dialog));
                }
                break;
            default:
                Toast.makeText(this, R.string.not_implemented_code_on_result, Toast.LENGTH_SHORT).show();
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

}
