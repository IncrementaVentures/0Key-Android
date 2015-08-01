package com.incrementaventures.okey.Models;

import com.parse.ParseException;
import com.parse.ParseObject;


public class Permission implements com.incrementaventures.okey.Models.ParseObject {

    public static final int ADMIN_PERMISSION = 0;
    public static final int PERMANENT_PERMISSION = 1;
    public static final int TEMPORAL_PERMISSION = 2;


    public static final String PERMISSION_CLASS_NAME = "Permission";
    public static final String USER = "user";
    public static final String DOOR = "door";
    public static final String TYPE = "type";
    public static final String KEY = "key";

    // TODO: CADA PERMISO TIENE UNA CLAVE, NO CADA USUARIO

    private ParseObject mParsePermission;

    private Permission(User user, Door door, int type, String key) {
        mParsePermission = ParseObject.create(PERMISSION_CLASS_NAME);
        mParsePermission.put(USER, user.getParseUser());
        mParsePermission.put(DOOR, door.getParseDoor());
        mParsePermission.put(TYPE, type);
        mParsePermission.put(KEY, key);
    }

    public static Permission create(User user, Door door, int type, String key){
        return new Permission(user, door, type, key);
    }

    @Override
    public String getId() {
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

    public String getKey(){
        return mParsePermission.getString(KEY);
    }

    protected static Permission getPermission(ParseObject parsePermission){
        User user = User.create(parsePermission.getParseUser(Permission.USER));
        Door door = Door.create(parsePermission.getParseObject(Permission.DOOR));
        int type = parsePermission.getInt(Permission.TYPE);
        String key = parsePermission.getString(Permission.KEY);
        return new Permission(user, door, type, key);
    }

}
