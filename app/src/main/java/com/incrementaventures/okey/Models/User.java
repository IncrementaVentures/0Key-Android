package com.incrementaventures.okey.Models;


import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.incrementaventures.okey.Activities.DoorActivity;
import com.incrementaventures.okey.Activities.MainActivity;
import com.incrementaventures.okey.Bluetooth.BluetoothClient;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.ArrayList;

public class User implements BluetoothClient.OnBluetoothToUserResponse, com.incrementaventures.okey.Models.ParseObject {

    public static final String USER_CLASS_NAME = "User";
    private final String NAME = "name";
    private final String PHONE = "phone";
    public static final String UUID = "uuid";

    private ParseUser mParseUser;
    private BluetoothClient mBluetoothClient;

    private OnUserBluetoothToActivityResponse mBluetoothListener;
    private OnOpenDoorActionsResponse mOpenListener;
    private OnPermissionsResponse mPermissionsListener;

    private Context mContext;

    @Override
    public String getObjectId() {
        return mParseUser.getObjectId();
    }

    @Override
    public void deleteFromLocal() {
        try {
            mParseUser.unpin();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save() {
        mParseUser.pinInBackground();
        mParseUser.saveEventually();
    }

    @Override
    public String getUUID() {
        return mParseUser.getString(UUID);
    }


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
        void stopScanning();
    }

    public interface OnOpenDoorActionsResponse{
        void doorOpened(int state);
        void doorOpening();
        void noPermission();
        void error(int code);
    }

    public interface OnPermissionsResponse{
        void permissionCreated(String key, int type);
        void error(int code);
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
        mOpenListener.doorOpened(state);
    }

    @Override
    public void permissionCreated(String key, int type) {
        mPermissionsListener.permissionCreated(key, type);
    }

    @Override
    public void stopScanning() {
        mBluetoothListener.stopScanning();
    }

    @Override
    public void doorClosed(int state) { }

    @Override
    public void error(int mode) {
        mOpenListener.error(mode);
    }


    private User(String name, String password, String email, String phone){
        mParseUser = new ParseUser();
        mParseUser.put(NAME, name);
        mParseUser.put(PHONE, phone);
        mParseUser.setUsername(email);
        mParseUser.setEmail(email);
        mParseUser.setPassword(password);
        mParseUser.put(UUID, java.util.UUID.randomUUID().toString());
    }

    private User(ParseUser parseUser){
        mParseUser = parseUser;
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
    public static User getLoggedUser(MainActivity activity){
        ParseUser current = ParseUser.getCurrentUser();
        if (current == null){
            return null;
        }

        User user = new User(current);
        user.mBluetoothListener =  activity;
        user.mOpenListener = activity;
        user.mPermissionsListener = activity;
        user.mContext = activity;
        return user;
    }

    public static User getLoggedUser(DoorActivity activity){
        ParseUser current = ParseUser.getCurrentUser();
        if (current == null){
            return null;
        }

        User user = new User(current);
        user.mBluetoothListener =  activity;
        user.mOpenListener = activity;
        user.mPermissionsListener = activity;
        user.mContext = activity;
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

    /**
     * Opens a door via bluetooth.
     */
    public void openDoor(Master master, Slave slave){
        // TODO:
        // check if hasPermission.
        // if bluetooth is supported and enabled. If not enabled, send callback to activity to activate it.
        // get paired devices.
        // filter devices
        // and try to open.

        Permission p = master.getPermission();

        if (p == null){
            mPermissionsListener.error(BluetoothClient.DONT_HAVE_PERMISSION);
            return;
        }


        mBluetoothClient = new BluetoothClient(mContext, this);

        if (!mBluetoothClient.isSupported()){
            mBluetoothListener.bluetoothNotSupported();
            return;
        }
        else if (!mBluetoothClient.isEnabled()){
            mBluetoothListener.enableBluetooth();
            return;
        }

        mOpenListener.doorOpening();

        mBluetoothClient.executeOpenDoor(p.getKey(), master.getName(), slave.getId());

    }



    public void makeFirstAdminConnection(String permissionKey, String defaultKey, String doorName){
        mBluetoothClient = new BluetoothClient(mContext, this);

        if (!mBluetoothClient.isSupported()){
            mBluetoothListener.bluetoothNotSupported();
            return;
        }
        else if (!mBluetoothClient.isEnabled()){
            mBluetoothListener.enableBluetooth();
            return;
        }

        mBluetoothClient.executeFirstConnectionConfiguration(permissionKey, defaultKey, doorName);
    }

    public void scanDevices(){
        mBluetoothClient = new BluetoothClient(mContext, this);

        if (!mBluetoothClient.isSupported()){
            mBluetoothListener.bluetoothNotSupported();
            return;
        }
        else if (!mBluetoothClient.isEnabled()){
            mBluetoothListener.enableBluetooth();
            return;
        }

        mBluetoothClient.scanDevices();
    }

    public void createNewPermission(String type, String date, String hour, String permissionKey, String doorName){
        mBluetoothClient = new BluetoothClient(mContext, this);

        if (!mBluetoothClient.isSupported()){
            mBluetoothListener.bluetoothNotSupported();
            return;
        }
        else if (!mBluetoothClient.isEnabled()){
            mBluetoothListener.enableBluetooth();
            return;
        }

        mBluetoothClient.executeCreateNewPermission(type, date, hour, permissionKey, doorName);

    }


    public boolean hasPermission(Master master){
        //TODO: check if there is a permission with the same objectId as the door
        return true;
    }

    public ArrayList<Permission> getPermissions(){
        //TODO: get from mParseUser and transform each to the model
        return null;
    }

    public ArrayList<Permission> getPermissions(Master master){
        //TODO: get from mParseUser and transform each to the model. Filter by door.
        return null;
    }




}
