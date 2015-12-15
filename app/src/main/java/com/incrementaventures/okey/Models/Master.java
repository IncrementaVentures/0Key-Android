package com.incrementaventures.okey.Models;

import android.text.TextUtils;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Master implements com.incrementaventures.okey.Models.ParseObject, Nameable {
    public static final String MASTER_CLASS_NAME = "Master";
    public static final String ID = "master_id";
    public static final String UUID = "uuid";
    public static final String USER_UUID = "user_uuid";
    public static final String NAME = "name";
    private static final String CREATED_AT = "createdAt";
    private static final String UPDATED_AT = "upatedAt";

    private ParseObject mParseMaster;
    private List<Slave> mSlaves;

    public interface OnNetworkResponseListener {
        void onSlavesReceived(ArrayList<Slave> slaves);
        void onMastersReceived(ArrayList<Master> masters);
        void onMasterReceived(Master master);
    }

    private Master(String id, String name, String userUuid) {
        mParseMaster = ParseObject.create(MASTER_CLASS_NAME);
        mParseMaster.put(UUID, java.util.UUID.randomUUID().toString());
        mParseMaster.put(NAME, name);
        mParseMaster.put(USER_UUID, userUuid);
        mParseMaster.put(ID, id);
    }

    private Master(ParseObject parseObject){
        mParseMaster = parseObject;
    }

    public static Master create(String id ,String name, String userUuid){
        return new Master(id, name, userUuid);
    }

    public static Master create(ParseObject parseObject){
        if (parseObject == null) {
            return null;
        } else {
            return new Master(parseObject);
        }
    }

    public ParseObject getParseMaster(){
        return mParseMaster;
    }

    @Override
    public String getName(){
        return mParseMaster.getString(NAME);
    }

    public String getId() {
        return mParseMaster.getString(ID);
    }

    public static ArrayList<Master> getMasters() {
        ParseQuery<ParseObject> query = new ParseQuery<>(Master.MASTER_CLASS_NAME);
        query.fromLocalDatastore();
        query.orderByDescending(CREATED_AT);
        query.whereEqualTo(USER_UUID, User.getLoggedUser().getUUID());
        ArrayList<Master> masters = new ArrayList<>();
        try {
            List<ParseObject> list  = query.find();
            for (ParseObject object : list){
                masters.add(Master.create(object));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return masters;
    }

    public static void fetchMasters(final OnNetworkResponseListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ParseQuery<ParseObject> query = new ParseQuery<>(Master.MASTER_CLASS_NAME);
                query.orderByDescending(CREATED_AT);
                query.whereEqualTo(USER_UUID, User.getLoggedUser().getUUID());
                ArrayList<Master> masters = new ArrayList<>();
                try {
                    List<ParseObject> list  = query.find();
                    Master master;
                    for (ParseObject object : list){
                        master = getMaster(object.getString(UUID));
                        if (master == null) {
                            master = Master.create(object);
                            masters.add(master);
                        }
                        master.save();
                        master.fetchSlaves(listener);
                        master.fetchPermissions(User.getLoggedUser());
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                listener.onMastersReceived(masters);
            }
        }).start();
    }

    public String getObjectId(){
        return mParseMaster.getObjectId();
    }

    @Override
    public void deleteFromLocal(){
        try {
            mParseMaster.unpin();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void save(){
        mParseMaster.pinInBackground();
        mParseMaster.saveEventually();
    }

    @Override
    public String getUUID() {
        if (mParseMaster == null) return null;
        return mParseMaster.getString(UUID);
    }

    public static void unpinAll(){
        ParseQuery<ParseObject> query = new ParseQuery<>(Master.MASTER_CLASS_NAME);
        query.fromLocalDatastore();

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                    for (ParseObject o : list){
                        try {
                            o.unpin();
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                    }
            }
        });
    }

    public ArrayList<Permission> getAllPermissions() {
        if (mParseMaster == null) return null;
        ParseQuery query = new ParseQuery(Permission.PERMISSION_CLASS_NAME);
        query.whereEqualTo(Permission.MASTER_UUID, getUUID());
        query.fromLocalDatastore();
        ArrayList<Permission> permissions = new ArrayList<>();
        try {
            List<ParseObject> list = query.find();
            if (list != null) {
                for (ParseObject parsePermission : list) {
                    permissions.add(Permission.create(parsePermission));
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return permissions;
    }

    public HashMap<Integer, Permission> getPermissions(User user){
        if (mParseMaster == null) return null;
        ParseQuery query = new ParseQuery(Permission.PERMISSION_CLASS_NAME);
        query.whereEqualTo(Permission.MASTER_UUID, getUUID());
        query.whereEqualTo(Permission.USER_UUID, user.getUUID());
        query.fromLocalDatastore();
        HashMap<Integer, Permission>  permissions = new HashMap<>();
        try {
            List<ParseObject> list = query.find();
            if (list != null) {
                for (ParseObject parsePermission : list) {
                    permissions.put(parsePermission.getInt(Permission.SLAVE_ID),
                            Permission.create(parsePermission));
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return permissions;
    }

    public void fetchPermissions(User user) {
        ParseQuery query = new ParseQuery(Permission.PERMISSION_CLASS_NAME);
        query.whereEqualTo(Permission.MASTER_UUID, getUUID());
        query.whereEqualTo(Permission.USER_UUID, user.getUUID());
        HashMap<Integer, Permission>  permissions = new HashMap<>();
        try {
            List<ParseObject> list = query.find();
            if (list != null) {
                for (ParseObject parsePermission : list) {
                    parsePermission.pinInBackground();
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public List<Slave> getSlaves() {
        if (mSlaves != null) return mSlaves;
        ParseQuery query = new ParseQuery(Slave.SLAVE_CLASS_NAME);
        query.fromLocalDatastore();
        query.whereEqualTo(Slave.MASTER_UUID, getUUID());
        query.orderByAscending(NAME);
        mSlaves = new ArrayList<>();
        try {
            List<ParseObject> objects = query.find();
            for (ParseObject o : objects){
                mSlaves.add(Slave.create(o));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return mSlaves;
    }

    public void fetchSlaves(final OnNetworkResponseListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ParseQuery query = new ParseQuery(Slave.SLAVE_CLASS_NAME);
                query.whereEqualTo(Slave.MASTER_UUID, getUUID());
                query.orderByAscending(Slave.ID);
                mSlaves = new ArrayList<>();
                try {
                    List<ParseObject> objects = query.find();
                    Slave slave;
                    for (ParseObject o : objects) {
                        slave = Slave.getSlave(o.getString(Slave.UUID));
                        if (slave == null) {
                            slave = Slave.create(o);
                            mSlaves.add(slave);
                        }
                        slave.save();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                listener.onSlavesReceived(new ArrayList<>(mSlaves));
            }
        }).start();
    }

    public void addSlave(Slave slave){
        if (mSlaves == null) mSlaves = getSlaves();
        mSlaves.add(slave);
    }

    public static Master getMaster(String uuid) {
        if (TextUtils.isEmpty(uuid)) return null;
        ParseQuery<ParseObject> query = new ParseQuery<>(MASTER_CLASS_NAME);
        query.fromLocalDatastore();
        query.whereEqualTo(UUID, uuid);
        try {
            ParseObject parseObject = query.getFirst();
            return Master.create(parseObject);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void fetchMaster(final String uuid, final OnNetworkResponseListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(uuid)) return;
                ParseQuery<ParseObject> query = new ParseQuery<>(MASTER_CLASS_NAME);
                query.whereEqualTo(UUID, uuid);
                try {
                    ParseObject parseObject = query.getFirst();
                    listener.onMasterReceived(Master.create(parseObject));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
