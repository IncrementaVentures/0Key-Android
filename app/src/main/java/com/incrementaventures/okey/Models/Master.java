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
    public static final String ID = "id";
    public static final String UUID = "uuid";
    public static final String NAME = "name";
    private static final String CREATED_AT = "createdAt";
    private static final String UPDATED_AT = "upatedAt";

    private ParseObject mParseMaster;
    private List<Slave> mSlaves;


    public interface OnNetworkResponseListener {
        void onSlavesReceived(ArrayList<Slave> slaves);
        void onMastersReceivd(ArrayList<Master> masters);
        void onMasterReceivd(Master master);
    }

    private Master(String id, String name) {
        mParseMaster = ParseObject.create(MASTER_CLASS_NAME);
        mParseMaster.put(UUID, java.util.UUID.randomUUID().toString());
        mParseMaster.put(NAME, name);
        mParseMaster.put(ID, id);
    }

    private Master(ParseObject parseObject){
        mParseMaster = parseObject;
    }

    public static Master create(String name ,String description){
        return new Master(name, description);
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

    public static void fetchMasters(OnNetworkResponseListener listener) {
        ParseQuery<ParseObject> query = new ParseQuery<>(Master.MASTER_CLASS_NAME);
        query.fromLocalDatastore();
        query.orderByDescending(CREATED_AT);
        ArrayList<Master> masters = new ArrayList<>();
        try {
            List<ParseObject> list  = query.find();
            for (ParseObject object : list){
                masters.add(Master.create(object));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        listener.onMastersReceivd(masters);
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

    public static void deleteAll(){
        ParseQuery<ParseObject> query = new ParseQuery<>(Master.MASTER_CLASS_NAME);
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

    public HashMap<Integer, Permission> getPermissions(){
        if (mParseMaster == null) return null;
        ParseQuery query = new ParseQuery(Permission.PERMISSION_CLASS_NAME);
        query.whereEqualTo(Permission.MASTER_UUID, getUUID());
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

    public void fetchSlaves(OnNetworkResponseListener listener) {
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
        listener.onSlavesReceived(new ArrayList<>(mSlaves));
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

    public static void fetchMaster(String uuid, OnNetworkResponseListener listener) {
        if (TextUtils.isEmpty(uuid)) return;
        ParseQuery<ParseObject> query = new ParseQuery<>(MASTER_CLASS_NAME);
        query.whereEqualTo(UUID, uuid);
        try {
            ParseObject parseObject = query.getFirst();
            listener.onMasterReceivd(Master.create(parseObject));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
