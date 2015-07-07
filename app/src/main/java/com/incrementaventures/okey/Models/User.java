package com.incrementaventures.okey.Models;


import com.incrementaventures.okey.Bluetooth.BluetoothClient;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;

public class User {

    private final String NAME = "name";
    private final String PHONE = "phone";

    private ParseUser mParseUser;
    private BluetoothClient mBluetoothClient;

    private OnBluetoothUserResponse mBluetoothListener;

    public interface OnParseUserResponse{
        void userSignedUp();
        void userLoggedIn();
        void authError(ParseException e);
    }

    public interface OnBluetoothUserResponse{
        void enableBluetooth();
        void bluetoothNotSupported();
    }

    private User(String name, String password, String email, String phone){

        mParseUser = new ParseUser();
        mParseUser.put(NAME, name);
        mParseUser.put(PHONE, phone);
        mParseUser.setUsername(email);
        mParseUser.setEmail(email);
        mParseUser.setPassword(password);

    }

    private User(ParseUser parseUser){
        mParseUser = parseUser;
    }

    /**
     * SignUp the user. OnParseUserResponse#userSignedUp(User) is called if it succeeds and
     * OnParseUserResponse#authError(ParseEcepction) is called if there was an error.
     * @param listener Listener to alert the subscribed classes that the user is signed up
     * @param name User name
     * @param pass User password
     * @param email User email, the de username too
     * @param phone Device phone
     */
    public static void signUp(final OnParseUserResponse listener, String name, String pass, String email, String phone){

        final User user = new User(name, pass, email, phone);

        // Try yo save
        user.mParseUser.saveInBackground(new SaveCallback() {
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
    };

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
                    User user = new User(parseUser);

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
    public static User getLoggedUser(OnBluetoothUserResponse listener){
        User user = new User(ParseUser.getCurrentUser());
        user.mBluetoothListener = listener;
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

    public void addPermission(){
        // TODO: create permission, and then add it to the user
    }

    /**
     * Opens a door via bluetooth.
     */
    public void openDoor(Door door){
        // TODO:
        // check if hasPermission.
        // if bluetooth is supported and enabled. If not enabled, send callback to activity to activate it.
        // get paired devices.
        // check if there is one starting with 'Okey'.
        // and try to open.

        if (!hasPermission(door)){
            return;
        }

        if (mBluetoothClient == null) mBluetoothClient = new BluetoothClient();

        if (!mBluetoothClient.isSupported()){
            mBluetoothListener.bluetoothNotSupported();
            return;
        }
        else if (!mBluetoothClient.isEnabled()){
            mBluetoothListener.enableBluetooth();
            return;
        }


    }

    public boolean hasPermission(Door door){
        //TODO: check if there is a permission with the same objectId as the door
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
