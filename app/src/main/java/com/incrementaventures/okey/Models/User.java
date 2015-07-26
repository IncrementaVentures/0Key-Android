package com.incrementaventures.okey.Models;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.SparseArray;

import com.incrementaventures.okey.Activities.MainActivity;
import com.incrementaventures.okey.Bluetooth.BluetoothClient;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.ArrayList;

public class User implements BluetoothClient.OnBluetoothToUserResponse {

    private final String NAME = "name";
    private final String PHONE = "phone";

    private ParseUser mParseUser;
    private BluetoothClient mBluetoothClient;

    private OnUserBluetoothToActivityResponse mBluetoothListener;
    private OnUserActionsResponse mActionListener;
    private Context mContext;

    private SparseArray<Permission> mPermissions;


    public interface OnParseUserResponse{
        void userSignedUp();
        void userLoggedIn();
        void authError(ParseException e);
    }

    public interface OnUserBluetoothToActivityResponse {
        void enableBluetooth();
        void bluetoothNotSupported();
        void deviceFound(BluetoothDevice device, int rssi, byte[] scanRecord);
        void deviceNotFound();
    }

    public interface OnUserActionsResponse{
        void doorOpened(int state);
        void doorClosed(int state);
        void adminAssigned(int state);
        void error(int mode);
    }

    @Override
    public void deviceFound(BluetoothDevice device, int rssi, byte[] scanRecord) {
        mBluetoothListener.deviceFound(device, rssi, scanRecord);
    }

    @Override
    public void deviceNotFound() {
        mBluetoothListener.deviceNotFound();
    }

    @Override
    public void doorOpened(int state) {
        mActionListener.doorOpened(state);
    }

    @Override
    public void doorClosed(int state) {
        mActionListener.doorClosed(state);
    }

    @Override
    public void adminAssigned(int state) {
        mActionListener.adminAssigned(state);
    }

    @Override
    public void error(int mode) {
        mActionListener.error(mode);
    }


    private User(String name, String password, String email, String phone){

        mParseUser = new ParseUser();
        mParseUser.put(NAME, name);
        mParseUser.put(PHONE, phone);
        mParseUser.setUsername(email);
        mParseUser.setEmail(email);
        mParseUser.setPassword(password);
        mPermissions = new SparseArray<>();
    }

    private User(ParseUser parseUser){
        mParseUser = parseUser;
        mPermissions = new SparseArray<>();
    }

    /**
     * SignUp the user. OnParseUserResponse#userSignedUp(User) is called if it succeeds and
     * OnParseUserResponse#authError(ParseException) is called if there was an error.
     * @param listener Listener to alert the subscribed classes that the user is signed up
     * @param name User name
     * @param pass User password
     * @param email User email, the de username too
     * @param phone Device phone
     */
    public static void signUp(final OnParseUserResponse listener, String name, String pass, String email, String phone){

        final User user = new User(name, pass, email, phone);

        // Try yo save
        user.mParseUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {

                if (e == null) {
                    // Alert to the listener that user is signed up.
                    // IMPORTANT: use getLoggedUser() to obtain the user in the activity
                    listener.userSignedUp();

                } else {
                    listener.authError(e);
                }
            }
        });
    }

    /**
     * Log in the user. OnParseUserResponse#userLoggedIn(User) is called if it succeeds.
     * OnParseUserResponse#authError(ParseException) is called if it fails.
     * @param listener Listener to alert the subscribed classes that the user is logged in
     * @param email User email, the username too
     * @param pass User password
     */
    public static void logIn(final OnParseUserResponse listener, String email, String pass){

        ParseUser.logInInBackground(email, pass, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e == null) {
                    // IMPORTANT: use getLoggedUser() to obtain the user in the activity
                    listener.userLoggedIn();
                } else {
                    listener.authError(e);
                }
            }
        });
    }

    /**
     * @return the user logged in the device, or null if there is no user logged.
     */
    public static User getLoggedUser(Context context){
        ParseUser current = ParseUser.getCurrentUser();
        if (current == null){
            return null;
        }

        User user = new User(current);

        user.mBluetoothListener = (MainActivity) context;
        user.mActionListener = (MainActivity) context;
        user.mContext = context;
        return user;
    }

    protected static User create(ParseUser user){
        return new User(user);
    }

    protected ParseUser getParseUser(){
        return mParseUser;
    }


    public String getName(){
        return mParseUser.getString(NAME);
    }

    public String getPhone(){
        return mParseUser.getString(PHONE);
    }

    public String getEmail(){
        return mParseUser.getEmail();
    }

    public void addPermission(Permission permission){
        String doorName = permission.getDoor().getName();
        // TODO: mPermissions.put(door.MACADRESS, permission);
        mPermissions.put(doorName.hashCode(), permission);
    }

    /**
     * Opens a door via bluetooth.
     */
    public void openDoor(Door door){
        // TODO:
        // check if hasPermission.
        // if bluetooth is supported and enabled. If not enabled, send callback to activity to activate it.
        // get paired devices.
        // filter devices
        // and try to open.

        if (!hasPermission(door)){
            return;
        }

        mBluetoothClient = new BluetoothClient(mContext, this, BluetoothClient.OPEN_MODE);

        if (!mBluetoothClient.isSupported()){
            mBluetoothListener.bluetoothNotSupported();
            return;
        }
        else if (!mBluetoothClient.isEnabled()){
            mBluetoothListener.enableBluetooth();
            return;
        }

        mBluetoothClient.startScan();
    }

    public void closeDoor(Door door){
        // TODO:
        // check if hasPermission.
        // if bluetooth is supported and enabled. If not enabled, send callback to activity to activate it.
        // get paired devices.
        // filter devices
        // and try to close.

        if (!hasPermission(door)){
            return;
        }

        mBluetoothClient = new BluetoothClient(mContext, this, BluetoothClient.CLOSE_MODE);

        if (!mBluetoothClient.isSupported()){
            mBluetoothListener.bluetoothNotSupported();
            return;
        }
        else if (!mBluetoothClient.isEnabled()){
            mBluetoothListener.enableBluetooth();
            return;
        }

        mBluetoothClient.startScan();
    }

    public void makeFirstAdminConnection(String defaultKey){
        mBluetoothClient = new BluetoothClient(mContext, this, BluetoothClient.FIRST_ADMIN_CONNECTION_MODE);


        if (!mBluetoothClient.isSupported()){
            mBluetoothListener.bluetoothNotSupported();
            return;
        }
        else if (!mBluetoothClient.isEnabled()){
            mBluetoothListener.enableBluetooth();
            return;
        }
        mBluetoothClient.setUserPhone(getPhone());
        mBluetoothClient.startScan();
    }


    public boolean hasPermission(Door door){
        //TODO: check if there is a permission with the same objectId as the door
        Permission p = mPermissions.get(door.getName().hashCode());
        return true;
    }

    public ArrayList<Permission> getPermissions(){
        //TODO: get from mParseUser and transform each to the model
        return null;
    }

    public ArrayList<Permission> getPermissions(Door door){
        //TODO: get from mParseUser and transform each to the model. Filter by door.
        return null;
    }




}
