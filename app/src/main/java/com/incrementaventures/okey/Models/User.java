package com.incrementaventures.okey.Models;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.incrementaventures.okey.Activities.MainActivity;
import com.incrementaventures.okey.Bluetooth.BluetoothClient;
import com.incrementaventures.okey.Networking.NetworkingUtils;
import com.incrementaventures.okey.Networking.ParseErrorHandler;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.HashMap;

public class User implements BluetoothClient.OnBluetoothToUserResponse,
        com.incrementaventures.okey.Models.ParseObject {

    public static final String USER_CLASS_NAME = "User";
    private static final String NAME = "name";
    private static final String EMAIL = "email";
    private static final String BIRTHDAY = "birthday";
    private static final String SEX = "sex";
    private static final String PHONE = "phone";
    public static final String OBJECT_ID = "objectId";
    public static final String MALE = "m";
    public static final String FEMALE = "f";
    public static final String EMAIL_VERIFIED = "emailVerified";
    public static final String BACKUP_EMAIL = "backupEmail";

    private ParseUser mParseUser;
    private BluetoothClient mBluetoothClient;

    private OnUserBluetoothToActivityResponse mBluetoothListener;
    private OnActionMasterResponse mMasterListener;
    private OnPermissionsResponse mPermissionsListener;

    private Context mContext;

    private static User sLoggedUser;

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

    public void saveLocal() {
        mParseUser.pinInBackground();
    }

    public String getId() {
        return getObjectId();
    }

    public interface OnParseUserGetted {
        void onUserGetted(User user);
    }

    public interface OnParseUserLoginResponse {
        void userSignedUp();
        void userLoggedIn(ParseUser parseUser);
        void authError(ParseException e);
    }

    public interface OnParseUserLogoutListener {
        void userLoggedOut();
    }

    public interface OnUserBluetoothToActivityResponse {
        void enableBluetooth();
        void bluetoothNotSupported();
        void deviceFound(BluetoothDevice device, int rssi, byte[] scanRecord);
        void deviceNotFound();
        void stopScanning();
    }

    public interface OnActionMasterResponse {
        void doorOpened(int state);
        void doorOpening();
        void noPermission();
        void slavesFound(Master master, ArrayList<Slave> slaves);
        void masterWithNoSlaves();
        void error(int code);
    }

    public interface OnPermissionsResponse{
        void permissionCreated(String key, Permission permission);
        void masterConfigured(Master master, Permission adminPermission);
        void permissionEdited(String key, Permission newPermission);
        void permissionDeleted(Permission permission);
        void permissionsReceived(ArrayList<HashMap<String, String>> permissionsData);
        void permissionReceived(int type, String key, String start, String end);
        void error(int code);
    }

    @Override
    public void deviceFound(BluetoothDevice device, int rssi, byte[] scanRecord) {
        mBluetoothListener.deviceFound(device, rssi, scanRecord);
    }

    @Override
    public void masterWithNoSlaves() {
        mMasterListener.masterWithNoSlaves();
    }

    @Override
    public void deviceNotFound() {
        mBluetoothListener.deviceNotFound();
    }

    @Override
    public void doorOpened(int state) {
        mMasterListener.doorOpened(state);
    }

    @Override
    public void permissionCreated(String key, Permission permission) {
        mPermissionsListener.permissionCreated(key, permission);
    }

    @Override
    public void masterConfigured(Master master, Permission adminPermission) {
        mPermissionsListener.masterConfigured(master, adminPermission);
    }

    @Override
    public void permissionEdited(String key, Permission newPermission) {
        mPermissionsListener.permissionEdited(key, newPermission);
    }

    @Override
    public void permissionDeleted(Permission permission) {
        mPermissionsListener.permissionDeleted(permission);
    }

    @Override
    public void permissionReceived(int type, String key, String start, String end) {
        mPermissionsListener.permissionReceived(type, key, start, end);
    }

    @Override
    public void permissionsReceived(ArrayList<HashMap<String, String>> permissionsData) {
        mPermissionsListener.permissionsReceived(permissionsData);
    }

    @Override
    public void stopScanning() {
        mBluetoothListener.stopScanning();
    }

    @Override
    public void slavesFound(Master master, ArrayList<Slave> slaves) {
        mMasterListener.slavesFound(master, slaves);
    }

    @Override
    public void doorClosed(int state) { }

    @Override
    public void error(int code) {
        mMasterListener.error(code);
    }


    public User(String name, String password, String email, String phone, String sex,
                String birthday) {
        mParseUser = new ParseUser();
        mParseUser.put(NAME, name);
        mParseUser.put(PHONE, phone);
        mParseUser.setUsername(email);
        mParseUser.put(SEX, sex);
        mParseUser.put(BIRTHDAY, birthday);
        mParseUser.setEmail(email);
        mParseUser.setPassword(password);
        mParseUser.put(BACKUP_EMAIL, email);
    }

    private User(ParseUser parseUser){
        mParseUser = parseUser;
    }

    /**
     * SignUp the user. OnParseUserLoginResponse#userSignedUp(User) is called if it succeeds and
     * OnParseUserLoginResponse#authError(ParseException) is called if there was an error.
     * @param listener Listener to alert the subscribed classes that the user is signed up
     * @param name User name
     * @param pass User password
     * @param email User email, the de username too
     * @param phone Device phone
     */
    public static void signUp(final OnParseUserLoginResponse listener, String name, String pass,
                              String email, String phone, String sex, String birthday) {
        final User user = new User(name, pass, email, phone, sex, birthday);

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
     * Log in the user. OnParseUserLoginResponse#userLoggedIn(User) is called if it succeeds.
     * OnParseUserLoginResponse#authError(ParseException) is called if it fails.
     * @param listener Listener to alert the subscribed classes that the user is logged in
     * @param email User email, the username too
     * @param pass User password
     */
    public static void logIn(final OnParseUserLoginResponse listener, String email, String pass){

        ParseUser.logInInBackground(email, pass, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e == null && parseUser != null) {
                    // IMPORTANT: use getLoggedUser() to obtain the user in the activity
                    listener.userLoggedIn(parseUser);
                } else {
                    listener.authError(e);
                }
            }
        });
    }

    /**
     * @return the user logged in the device, or null if there is no user logged.
     */
    public static User getLoggedUser(Context context) {
        ParseUser current = ParseUser.getCurrentUser();
        if (current == null){
            return null;
        }

        User user = new User(current);
        user.mBluetoothListener =  (OnUserBluetoothToActivityResponse) context;
        user.mMasterListener = (OnActionMasterResponse) context;
        user.mPermissionsListener = (OnPermissionsResponse) context;
        user.mContext = context;
        sLoggedUser = user;
        return user;
    }

    public static User getLoggedUser(){
        return sLoggedUser;
    }


    protected static User create(ParseUser user){
        if (user == null) {
            return null;
        } else {
            return new User(user);
        }
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

    public String getBirthday() {
        return mParseUser.getString(BIRTHDAY);
    }

    public boolean isMale() {
        if (mParseUser.getString(SEX).equals(MALE)) {
            return true;
        }
        return false;
    }

    public String getEmail(){
        return mParseUser.getEmail();
    }

    private Permission getValidPermission(Master master, Slave slave) {
        HashMap<Integer, Permission> permissions = master.getPermissions(this);
        if (permissions == null || permissions.size() == 0) {
            return null;
        }
        Permission permission = permissions.get(0);
        if (permission == null ||
                Permission.getType(permission.getType()) != Permission.ADMIN_PERMISSION) {
            permission = null;
            Object[] permissionsArray  = permissions.values().toArray();
            for (int i = 0; i < permissionsArray.length; i++) {
                Permission p = (Permission) permissionsArray[i];
                // If it is valid and (the permission is associated with the same slave id OR it is
                // for all slaves)
                if (p.isValid() && (p.getSlaveId() == slave.getId() || p.getSlaveId() == Slave.ALL_SLAVES)) {
                    permission = (Permission) permissionsArray[i];
                    break;
                }
            }
        }
        return permission;
    }

    /**
     * Opens a door if the user has a valid permission.
     */
    public void openDoor(Master master, Slave slave) {

        Permission permission = getValidPermission(master, slave);
        if (permission == null) {
            mPermissionsListener.error(BluetoothClient.DONT_HAVE_PERMISSION);
            return;
        }

        mBluetoothClient = BluetoothClient.getInstance(mContext, this);

        if (!mBluetoothClient.isSupported()){
            mBluetoothListener.bluetoothNotSupported();
            return;
        }
        else if (!mBluetoothClient.isEnabled()) {
            mBluetoothListener.enableBluetooth();
            return;
        }
        mMasterListener.doorOpening();
        mBluetoothClient.executeOpenDoor(permission, master.getId(), slave.getId());
    }

    public void makeFirstAdminConnection(String permissionKey, String defaultKey, Master master) {
        mBluetoothClient = BluetoothClient.getInstance(mContext, this);
        if (!mBluetoothClient.isSupported()){
            mBluetoothListener.bluetoothNotSupported();
            return;
        }
        else if (!mBluetoothClient.isEnabled()){
            mBluetoothListener.enableBluetooth();
            return;
        }
        mBluetoothClient.executeFirstConnectionConfiguration(defaultKey, permissionKey, master);
    }

    public void scanDevices(){
        mBluetoothClient = BluetoothClient.getInstance(mContext, this);

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

    public void createNewPermission(Permission permission, String permissionKey, String masterId) {
        mBluetoothClient = BluetoothClient.getInstance(mContext, this);

        if (!mBluetoothClient.isSupported()){
            mBluetoothListener.bluetoothNotSupported();
            return;
        }
        else if (!mBluetoothClient.isEnabled()){
            mBluetoothListener.enableBluetooth();
            return;
        }

        mBluetoothClient.executeCreateNewPermission(permission, permissionKey, masterId);
    }

    public void openWhenClose(Master master, Slave slave, String key) {
        Permission p = slave.getPermission(this);
        if (p == null || !p.isValid()){
            mPermissionsListener.error(BluetoothClient.DONT_HAVE_PERMISSION);
            return;
        }
        mBluetoothClient = BluetoothClient.getInstance(mContext, this);
        if (!mBluetoothClient.isSupported()){
            mBluetoothListener.bluetoothNotSupported();
            return;
        }
        else if (!mBluetoothClient.isEnabled()){
            mBluetoothListener.enableBluetooth();
            return;
        }
        mBluetoothClient.executeOpenDoorWhenClose(p.getKey(), master.getName(), slave.getId());
    }

    public void editPermission(Permission toEditPermission, int oldSlaveId, String adminKey,
                               String doorId) {
        mBluetoothClient = BluetoothClient.getInstance(mContext, this);
        if (!mBluetoothClient.isSupported()){
            mBluetoothListener.bluetoothNotSupported();
            return;
        }else if (!mBluetoothClient.isEnabled()){
            mBluetoothListener.enableBluetooth();
            return;
        }
        mBluetoothClient.executeEditPermission(toEditPermission, oldSlaveId, adminKey, doorId);
    }

    public void deletePermission(String masterId, String adminKey, Permission permission){
        mBluetoothClient = BluetoothClient.getInstance(mContext, this);
        if (!mBluetoothClient.isSupported()){
            mBluetoothListener.bluetoothNotSupported();
            return;
        }else if (!mBluetoothClient.isEnabled()){
            mBluetoothListener.enableBluetooth();
            return;
        }
        mBluetoothClient.executeDeletePermission(masterId, adminKey, permission);
    }

    public void readMyPermission(Master master, Slave slave, String permissionKey){
        mBluetoothClient = BluetoothClient.getInstance(mContext, this);
        if (!mBluetoothClient.isSupported()){
            mBluetoothListener.bluetoothNotSupported();
            return;
        } else if (!mBluetoothClient.isEnabled()){
            mBluetoothListener.enableBluetooth();
            return;
        }
        mBluetoothClient.executeReadUserPermission(master.getName(), slave.getId(), permissionKey);
    }

    public void readAllPermissions(Master master, Slave slave, String permissionKey){
        mBluetoothClient = BluetoothClient.getInstance(mContext, this);
        if (!mBluetoothClient.isSupported()){
            mBluetoothListener.bluetoothNotSupported();
            return;
        }else if (!mBluetoothClient.isEnabled()){
            mBluetoothListener.enableBluetooth();
            return;
        }
        mBluetoothClient.executeReadAllPermissions(master.getName(), slave.getId(), permissionKey);
    }

    public void getSlaves(Master master, String permissionKey){
        mBluetoothClient = BluetoothClient.getInstance(mContext, this);
        if (!mBluetoothClient.isSupported()){
            mBluetoothListener.bluetoothNotSupported();
            return;
        } else if (!mBluetoothClient.isEnabled()){
            mBluetoothListener.enableBluetooth();
            return;
        }
        mBluetoothClient.executeGetSlaves(master, permissionKey);
    }

    public void pairSlaves(String masterName, String adminKey, int keySlaveId, int pairSlaveId) {
        mBluetoothClient = BluetoothClient.getInstance(mContext, this);
        if (!mBluetoothClient.isSupported()){
            mBluetoothListener.bluetoothNotSupported();
            return;
        } else if (!mBluetoothClient.isEnabled()){
            mBluetoothListener.enableBluetooth();
            return;
        }
        mBluetoothClient.executePairSlaves(masterName, adminKey, keySlaveId, pairSlaveId);
    }

    public Permission getAdminPermission(Master master) {
        if (master == null) return null;
        ParseQuery<com.parse.ParseObject> query = ParseQuery.getQuery(Permission.PERMISSION_CLASS_NAME);
        query.fromLocalDatastore();
        query.whereEqualTo(Permission.USER_ID, getId());
        query.whereEqualTo(Permission.TYPE, Permission.ADMIN_PERMISSION);
        query.whereEqualTo(Permission.MASTER_ID, master.getId());
        try {
            com.parse.ParseObject parsePermission = query.getFirst();
            return Permission.create(parsePermission);
        } catch (ParseException e) {
            return null;
        }
    }

    public Permission getPermission(Master master, int id) {
        return master.getPermissions(this).get(id);
    }

    public static User getUser(String email) {
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.whereEqualTo(EMAIL, email);
        try {
            ParseUser parseUser = query.getFirst();
            return User.create(parseUser);
        } catch (ParseException e) {
            return null;
        }
    }

    public static void fetchUser(String id, final OnParseUserGetted listener) {
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.whereEqualTo(User.OBJECT_ID, id);
        query.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e == null) {
                    listener.onUserGetted(User.create(parseUser));
                } else {
                    ParseErrorHandler.handleError(e);
                }
            }
        });
    }

    public static User getUserLocal(String email) {
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.fromLocalDatastore();
        query.whereEqualTo(EMAIL, email);
        try {
            ParseUser parseUser = query.getFirst();
            return User.create(parseUser);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<Permission> getInterestedPermissions(Master master) {
        ArrayList<Permission> permissions = new ArrayList<>();
        HashMap<Integer, Permission> userPermissions =
                master.getPermissions(User.getLoggedUser());
        if (userPermissions == null)
            return permissions;
        if (userPermissions.containsKey(0)) {
            permissions.addAll(master.getAllPermissions());
        } else {
            permissions.addAll(userPermissions.values());
        }
        return permissions;
    }

    public void logout(final OnParseUserLogoutListener listener) {
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                Master.unpinAll();
                Permission.unpinAll();
                Slave.unpinAll();
                listener.userLoggedOut();
            }
        });
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
