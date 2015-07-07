package com.incrementaventures.okey.Models;

import com.parse.ParseObject;


public class Permission {

    private final String PERMISSION_CLASS_NAME = "Permission";
    private final String USER = "user";
    private final String DOOR = "door";
    private final String TYPE = "type";
    private final String TAG = "tag";

    private ParseObject mParsePermission;

    private Permission(User user, Door door, int type, String tag){
        mParsePermission = ParseObject.create(PERMISSION_CLASS_NAME);
        mParsePermission.put(USER, user.getParseUser());
        mParsePermission.put(DOOR, door.getParseDoor());
        mParsePermission.put(TYPE, type);
        mParsePermission.put(TAG, tag);
    }

    public static Permission create(User user, Door door, int type, String tag){
        return new Permission(user, door, type, tag);
    }

    public void save(){
        mParsePermission.pinInBackground();
        mParsePermission.saveEventually();
    }

    public Door getDoor(){
        return Door.create(mParsePermission.getParseObject(DOOR));
    }

    public User getUser(){
        return User.create(mParsePermission.getParseUser(USER));
    }

    public int getType(){
        return mParsePermission.getInt(TYPE);
    }

    public String getTag(){
        return mParsePermission.getString(TAG);
    }

}
