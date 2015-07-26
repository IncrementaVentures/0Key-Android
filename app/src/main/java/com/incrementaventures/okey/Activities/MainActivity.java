package com.incrementaventures.okey.Activities;

import android.app.FragmentTransaction;
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
import com.incrementaventures.okey.Models.Door;
import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.R;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MainActivity extends ActionBarActivity implements InsertPinFragment.PinDialogListener, User.OnUserBluetoothToActivityResponse, User.OnUserActionsResponse {
    public static final int REQUEST_ENABLE_BT = 1;


    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @Bind(R.id.left_drawer)
    ListView mDrawerList;

    private User mCurrentUser;

    private String[] mDrawerItems;
    private ActionBarDrawerToggle mDrawerToggle;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        drawerSetup();

        checkPreferences();
        authenticateUser();

        checkBluetoothLeSupport();
    }


    public void drawerSetup(){
        mDrawerItems = new String[] {"Main page", "Doors", "Permissions", "Log"};
        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, mDrawerItems));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_name, R.string.app_name){
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(R.string.app_name);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, PreferencesActivity.class);
            startActivity(intent);
            return true;
        }

        else if (id == R.id.open_door_action) {
            mCurrentUser.openDoor(Door.create("Test", "TEST DOOR"));
            return true;
        }

        else if (id == R.id.close_door_action) {
            mCurrentUser.closeDoor(Door.create("Test", "TEST DOOR"));
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
        Toast.makeText(this, R.string.device_found, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void deviceNotFound() {
        Toast.makeText(this, R.string.device_not_found, Toast.LENGTH_SHORT).show();
    }


    /*
        OnUserActionResponse callbacks
     */
    @Override
    public void doorOpened(final int state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (state == BluetoothClient.OPEN_MODE) {
                    Toast.makeText(MainActivity.this, R.string.door_opened, Toast.LENGTH_SHORT).show();
                } else if (state == BluetoothClient.DOOR_ALREADY_OPENED) {
                    Toast.makeText(MainActivity.this, R.string.door_already_opened, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void doorClosed(final int state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (state == BluetoothClient.CLOSE_MODE) {
                    Toast.makeText(MainActivity.this, R.string.door_closed, Toast.LENGTH_SHORT).show();
                } else if (state == BluetoothClient.DOOR_ALREADY_CLOSED) {
                    Toast.makeText(MainActivity.this, R.string.door_already_closed, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void adminAssigned(final int state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (state == BluetoothClient.ADMIN_ALREADY_ASSIGNED){
                    Toast.makeText(MainActivity.this , R.string.admin_already_assigned, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void error(final int mode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mode == BluetoothClient.OPEN_MODE){
                    Toast.makeText(MainActivity.this , R.string.door_cant_open, Toast.LENGTH_SHORT).show();
                } else{
                    Toast.makeText(MainActivity.this , R.string.door_cant_close, Toast.LENGTH_SHORT).show();

                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if(resultCode == RESULT_OK){
                    mCurrentUser.openDoor(Door.create("Test", "TEST"));
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
