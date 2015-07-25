package com.incrementaventures.okey.Activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.incrementaventures.okey.Bluetooth.BluetoothClient;
import com.incrementaventures.okey.Fragments.InsertPinFragment;
import com.incrementaventures.okey.Models.Door;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.R;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MainActivity extends ActionBarActivity implements InsertPinFragment.PinDialogListener, User.OnUserBluetoothToActivityResponse, User.OnUserActionsResponse {
    public static final int REQUEST_ENABLE_BT = 1;

    @Bind(R.id.button_open_door)
    Button openDoorButton;

    @Bind(R.id.button_close_door)
    Button closeDoorButton;


    User mCurrentUser;
    Door mTestDoor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        checkPreferences();
        authenticateUser();
        setListeners();
        checkBluetoothLeSupport();

        testSetUp();

    }

    private void checkPreferences(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref.getBoolean("protect_with_pin", false)){
            InsertPinFragment dialog = new InsertPinFragment();
            dialog.show(getFragmentManager(), "dialog_pin");
        }
    }

    private void testSetUp(){
        mTestDoor = Door.create("Door", "Test door");
        Permission permission = Permission.create(mCurrentUser, mTestDoor, 0, "TEST");
        mCurrentUser.addPermission(permission);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent intent = new Intent(this, PreferencesActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void setListeners(){

        openDoorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentUser.openDoor(mTestDoor);
            }
        });

        closeDoorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentUser.closeDoor(mTestDoor);
            }
        });
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
        // TODO: implement onResult and ask to open or close the door again.
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


    /*
        OnUserActionResponse callbacks
     */
    @Override
    public void doorOpened(final int state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (state == BluetoothClient.OPEN_MODE){
                    Toast.makeText(MainActivity.this , R.string.door_opened, Toast.LENGTH_SHORT).show();
                }
                else if (state == BluetoothClient.DOOR_ALREADY_OPENED){
                    Toast.makeText(MainActivity.this , R.string.door_already_opened, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void doorClosed(final int state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (state == BluetoothClient.CLOSE_MODE){
                    Toast.makeText(MainActivity.this , R.string.door_closed, Toast.LENGTH_SHORT).show();
                }
                else if (state == BluetoothClient.DOOR_ALREADY_CLOSED){
                    Toast.makeText(MainActivity.this , R.string.door_already_closed, Toast.LENGTH_SHORT).show();
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
                    mCurrentUser.openDoor(mTestDoor);
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
