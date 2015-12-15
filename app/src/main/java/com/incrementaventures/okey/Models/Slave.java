package com.incrementaventures.okey.Models;

import android.text.TextUtils;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.List;

public class Slave implements ParseObject, Nameable {
    public static final String SLAVE_CLASS_NAME = "Slave";
    public static final String UUID = "uuid";
    public static final String ID = "slave_id";
    public static final String MASTER_UUID = "master_uuid";
    public static final String MASTER_ID = "master_id";
    public static final String NAME = "name";
    public static final String TYPE = "type";

    private com.parse.ParseObject mParseSlave;

    private Slave(String uuidMaster, String masterId, String name, int type, int id) {
        mParseSlave = com.parse.ParseObject.create(SLAVE_CLASS_NAME);
        mParseSlave.put(UUID, java.util.UUID.randomUUID().toString());
        mParseSlave.put(MASTER_UUID, uuidMaster);
        mParseSlave.put(MASTER_ID, masterId);
        mParseSlave.put(NAME, name);
        mParseSlave.put(TYPE, type);
        mParseSlave.put(ID, id);
    }

    private Slave(com.parse.ParseObject parseSlave){
        mParseSlave = parseSlave;
    }

    public static Slave create( String uuidMaster, String masterId, String name, int type, int id){
        return new Slave(uuidMaster, masterId, name, type, id);
    }

    public static Slave create(com.parse.ParseObject parseSlave){
        return new Slave(parseSlave);
    }

    @Override
    public String getObjectId() {
        return mParseSlave.getObjectId();
    }

    @Override
    public void deleteFromLocal() {
        mParseSlave.unpinInBackground();
    }

    @Override
    public void save() {
        mParseSlave.pinInBackground();
        mParseSlave.saveEventually();
    }

    @Override
    public String getUUID() {
        return mParseSlave.getString(UUID);
    }

    public int getId(){
        return mParseSlave.getInt(ID);
    }

    @Override
    public String getName(){
        return mParseSlave.getString(NAME);
    }

    public int getType(){
        return mParseSlave.getInt(TYPE);
    }

    public String getUuidMaster(){
        return mParseSlave.getString(MASTER_UUID);
    }

    public Permission getPermission(User user) {
        ParseQuery query = ParseQuery.getQuery(Permission.PERMISSION_CLASS_NAME);
        query.fromLocalDatastore();
        query.whereEqualTo(Permission.MASTER_UUID, getUuidMaster());
        query.whereEqualTo(Permission.USER_UUID, user.getUUID());
        query.whereEqualTo(Permission.SLAVE_ID, getId());
        try {
            return Permission.create(query.getFirst());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Slave getSlave(String uuid) {
        if (TextUtils.isEmpty(uuid)) return null;
        ParseQuery<com.parse.ParseObject> query = new ParseQuery<>(SLAVE_CLASS_NAME);
        query.fromLocalDatastore();
        query.whereEqualTo(UUID, uuid);
        try {
            com.parse.ParseObject parseObject = query.getFirst();
            return Slave.create(parseObject);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void unpinAll() {
        ParseQuery<com.parse.ParseObject> query = new ParseQuery<>(Slave.SLAVE_CLASS_NAME);
        query.fromLocalDatastore();

        query.findInBackground(new FindCallback<com.parse.ParseObject>() {
            @Override
            public void done(List<com.parse.ParseObject> list, ParseException e) {
                for (com.parse.ParseObject o : list){
                    try {
                        o.unpin();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    public void setMasterUuid(String masterUuid) {
        mParseSlave.put(MASTER_UUID, masterUuid);
    }

    public void setMasterId(String masterId) {
        mParseSlave.put(MASTER_ID, masterId);
    }

    @Override
    public boolean equals(Object o) {
        return ((Slave)o).getId() == this.getId();
    }

    @Override
    public String toString() {
        return getName();
    }
}
