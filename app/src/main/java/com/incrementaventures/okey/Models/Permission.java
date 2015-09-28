package com.incrementaventures.okey.Models;

import android.text.format.Time;

import com.incrementaventures.okey.Bluetooth.BluetoothProtocol;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;


public class Permission implements com.incrementaventures.okey.Models.ParseObject {

    public static final int ADMIN_PERMISSION = 0;
    public static final int PERMANENT_PERMISSION = 1;
    public static final int TEMPORAL_PERMISSION = 2;
    public static final int UNKNOWN_PERMISSION = 3;

    public static final String PERMANENT_DATE = "Permanent";
    public static final String UNKNOWN_DATE = "Unknown";

    public static final String PERMISSION_CLASS_NAME = "Permission";
    public static final String USER_UUID = "user_uuid";
    public static final String MASTER_UUID = "master_uuid";
    public static final String SLAVE_UUID = "master_uuid";
    public static final String UUID = "uuid";
    public static final String TYPE = "type";
    public static final String KEY = "key";
    public static final String START_DATE = "start_date";
    public static final String END_DATE = "end_date";

    private ParseObject mParsePermission;

    private Permission(ParseObject parsePermission){
        mParsePermission = parsePermission;
    }

    private Permission(User user, Master master, int type, String key, String startDate, String endDate) {
        mParsePermission = ParseObject.create(PERMISSION_CLASS_NAME);
        mParsePermission.put(USER_UUID, user.getUUID());
        mParsePermission.put(MASTER_UUID, master.getUUID());
        mParsePermission.put(TYPE, type);
        mParsePermission.put(KEY, key);
        mParsePermission.put(START_DATE, startDate);
        mParsePermission.put(END_DATE, endDate);
        mParsePermission.put(UUID, java.util.UUID.randomUUID().toString());
    }

    public static Permission create(User user, Master master, int type, String key, String startDate, String endDate){
        return new Permission(user, master, type, key, startDate, endDate);
    }

    public static Permission create(ParseObject parsePermission){
        return new Permission(parsePermission);
    }

    @Override
    public String getObjectId() {
        return mParsePermission.getObjectId();
    }

    @Override
    public void deleteFromLocal() {
        try {
            mParsePermission.unpin();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(){
        mParsePermission.pinInBackground();
        mParsePermission.saveEventually();
    }

    @Override
    public String getUUID() {
        return mParsePermission.getString(UUID);
    }

    public Master getMaster(){
        ParseQuery query = new ParseQuery(Master.MASTER_CLASS_NAME);
        query.whereEqualTo(Master.UUID, mParsePermission.getString(MASTER_UUID));
        try {
            ParseObject o = query.getFirst();
            return Master.create(o);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUser(){
        ParseQuery<ParseUser> query = new ParseQuery<>(User.USER_CLASS_NAME);
        query.whereEqualTo(User.UUID, mParsePermission.getString(USER_UUID));
        try {
            ParseUser o = query.getFirst();
            return User.create(o);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getType(){
        switch (mParsePermission.getInt(TYPE)){
            case TEMPORAL_PERMISSION:
                return "Temporal";
            case PERMANENT_PERMISSION:
                return "Permanent";
            case ADMIN_PERMISSION:
                return  "Administrator";
            default:
                return "Unknown";
        }
    }

    public void setType(int type){
        mParsePermission.put(TYPE, type);
    }

    public String getKey(){
        return mParsePermission.getString(KEY);
    }

    private boolean started(Time time){
        if (mParsePermission.getString(START_DATE) == null) return true;
        return mParsePermission.getString(START_DATE).compareTo(BluetoothProtocol.formatDate(time))> 0;
    }

    private boolean finished(Time time){
        if (mParsePermission.getString(END_DATE) == null) return true;
        return mParsePermission.getString(END_DATE).compareTo(BluetoothProtocol.formatDate(time)) > 0;
    }

    public boolean isValid(){
        Time time = new Time();
        if (mParsePermission.getInt(TYPE) == ADMIN_PERMISSION) return true;
        else if (mParsePermission.getInt(TYPE) == PERMANENT_PERMISSION && started(time)){
            return true;
        }
        else if (mParsePermission.getInt(TYPE) == TEMPORAL_PERMISSION && started(time) && !finished(time)){
            return true;
        }
        return false;
    }

    public String getStartDate(){
        return mParsePermission.getString(START_DATE);
    }

    public void setStartDate(String startDate){
        mParsePermission.put(START_DATE, startDate);
    }

    public String getEndDate(){
        return mParsePermission.getString(END_DATE);
    }

    public void setEndDate(String endDate){
        mParsePermission.put(START_DATE, endDate);
    }

    public void setKey(String key){
        mParsePermission.put(KEY, key);
    }

    public static void deleteAll(){
        ParseQuery<ParseObject> query = new ParseQuery<>(Permission.PERMISSION_CLASS_NAME);
        query.fromLocalDatastore();

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                for (ParseObject o : list){
                    try {
                        o.deleteEventually();
                        o.unpin();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    public boolean isAdmin(){
        if (mParsePermission.getInt(TYPE) == ADMIN_PERMISSION)
            return true;
        return false;
    }

}
