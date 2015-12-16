package com.incrementaventures.okey.Models;

import android.text.TextUtils;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.List;

public class Slave implements ParseObject, Nameable {
    public static final String SLAVE_CLASS_NAME = "Slave";
    public static final String SLAVE_NAME_CLASS_NAME = "SlaveName";
    public static final String ID = "slave_id";
    public static final String SLAVE_ID = "slave_id";
    public static final String MASTER_ID = "master_id";
    public static final String USER_ID = "user_id";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String DEFAULT_NAME = "Default name";

    private com.parse.ParseObject mParseSlave;
    private com.parse.ParseObject mParseSlaveName;

    public interface OnSlavesNetworkResponse {
        void onSlaveReceived(Slave slave);
    }

    private Slave(String masterId, String name, int type, int id, String userUuid) {
        mParseSlave = com.parse.ParseObject.create(SLAVE_CLASS_NAME);
        mParseSlave.put(MASTER_ID, masterId);
        mParseSlave.put(ID, id);
        mParseSlave.put(TYPE, type);
        mParseSlaveName = com.parse.ParseObject.create(SLAVE_NAME_CLASS_NAME);
        mParseSlaveName.put(NAME, name);
        mParseSlaveName.put(MASTER_ID, masterId);
        mParseSlaveName.put(SLAVE_ID, id);
        mParseSlaveName.put(USER_ID, userUuid);
    }

    private Slave(com.parse.ParseObject parseSlave, com.parse.ParseObject parseSlaveName){
        mParseSlave = parseSlave;
        mParseSlaveName = parseSlaveName;
    }

    public static Slave create(String masterId, String name, int type, int id, String userUuid) {
        return new Slave(masterId, name, type, id, userUuid);
    }

    public static Slave create(com.parse.ParseObject parseSlave) {
        return new Slave(parseSlave, getParseSlaveName(User.getLoggedUser().getId(),
                parseSlave.getString(MASTER_ID), parseSlave.getInt(ID)));
    }

    @Override
    public String getObjectId() {
        return mParseSlave.getObjectId();
    }

    @Override
    public void deleteFromLocal() {
        mParseSlave.unpinInBackground();
        mParseSlaveName.unpinInBackground();
    }

    @Override
    public void save() {
        saveLocal();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Slave slave = fetchSlave(mParseSlave.getString(MASTER_ID), getId());
                if (slave != null) {
                    mParseSlave.saveEventually();
                    mParseSlaveName.saveEventually();
                }
            }
        }).start();
    }

    public void saveLocal() {
        mParseSlave.pinInBackground();
        if (mParseSlaveName == null) {
            mParseSlaveName = getParseSlaveName(User.getLoggedUser().getId(), getMasterId(), getId());
        }
        mParseSlaveName.pinInBackground();
    }

    public int getId(){
        return mParseSlave.getInt(ID);
    }

    public String getMasterId() {
        return mParseSlave.getString(MASTER_ID);
    }

    @Override
    public String getName(){
        if (mParseSlaveName == null) {
            mParseSlaveName = getParseSlaveName(User.getLoggedUser().getId(), getMasterId(), getId());
        }
        return mParseSlaveName.getString(NAME);
    }

    public int getType(){
        return mParseSlave.getInt(TYPE);
    }

    public Permission getPermission(User user) {
        ParseQuery query = ParseQuery.getQuery(Permission.PERMISSION_CLASS_NAME);
        query.fromLocalDatastore();
        query.whereEqualTo(Permission.MASTER_ID, Master.getMaster(getMasterId(), user.getId()));
        query.whereEqualTo(Permission.USER_ID, user.getId());
        query.whereEqualTo(Permission.SLAVE_ID, getId());
        try {
            return Permission.create(query.getFirst());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Slave fetchSlave(final String masterId, final int id) {
        if (TextUtils.isEmpty(masterId)) return null;
        ParseQuery<com.parse.ParseObject> query = ParseQuery.getQuery(SLAVE_CLASS_NAME);
        query.whereEqualTo(ID, id);
        query.whereEqualTo(MASTER_ID, masterId);
        Slave slave = null;
        try {
            com.parse.ParseObject parseSlave = query.getFirst();
            if (parseSlave != null) {
                slave = Slave.create(parseSlave);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return slave;
    }

    public static void deleteAllLocal() {
        ParseQuery<com.parse.ParseObject> query = ParseQuery.getQuery(SLAVE_CLASS_NAME);
        query.fromLocalDatastore();
        try {
            List<com.parse.ParseObject> localSlaves = query.find();
            for (com.parse.ParseObject localSlave : localSlaves) {
                localSlave.unpin();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static Slave getSlave(String masterId, int slaveId) {
        ParseQuery<com.parse.ParseObject> query = new ParseQuery<>(SLAVE_CLASS_NAME);
        query.fromLocalDatastore();
        query.whereEqualTo(MASTER_ID, masterId);
        query.whereEqualTo(ID, slaveId);
        try {
            com.parse.ParseObject parseSlave = query.getFirst();
            return Slave.create(parseSlave);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static com.parse.ParseObject getParseSlaveName(String userUuid, String masterId,
                                                           int slaveId) {
        ParseQuery<com.parse.ParseObject> query = ParseQuery.getQuery(SLAVE_NAME_CLASS_NAME);
        query.fromLocalDatastore();
        query.whereEqualTo(MASTER_ID, masterId);
        query.whereEqualTo(ID, slaveId);
        query.whereEqualTo(USER_ID, userUuid);
        com.parse.ParseObject parseObject = null;
        try {
            parseObject = query.getFirst();
            if (parseObject == null) {
                parseObject = com.parse.ParseObject.create(SLAVE_NAME_CLASS_NAME);
                parseObject.put(MASTER_ID, masterId);
                parseObject.put(ID, slaveId);
                parseObject.put(USER_ID, userUuid);
                parseObject.put(NAME, slaveId);
            }
        } catch (ParseException e) {
            parseObject = com.parse.ParseObject.create(SLAVE_NAME_CLASS_NAME);
            parseObject.put(MASTER_ID, masterId);
            parseObject.put(ID, slaveId);
            parseObject.put(USER_ID, userUuid);
            parseObject.put(NAME, slaveId);
        }
        return parseObject;
    }

    public static void unpinAll() {
        ParseQuery<com.parse.ParseObject> query = new ParseQuery<>(Slave.SLAVE_CLASS_NAME);
        query.fromLocalDatastore();

        query.findInBackground(new FindCallback<com.parse.ParseObject>() {
            @Override
            public void done(List<com.parse.ParseObject> list, ParseException e) {
                for (com.parse.ParseObject o : list) {
                    try {
                        o.unpin();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
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
