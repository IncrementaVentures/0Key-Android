package com.incrementaventures.okey.Models;

import android.text.TextUtils;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

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
    public static final String DEFAULT_NAME = "Slave";

    private com.parse.ParseObject mParseSlave;
    private com.parse.ParseObject mParseSlaveName;
    private String mName;

    public interface OnSlavesNetworkResponse {
        void onSlaveReceived(Slave slave);
    }

    private Slave(String masterId, String name, int type, int id, String userId) {
        mParseSlave = com.parse.ParseObject.create(SLAVE_CLASS_NAME);
        mParseSlave.put(MASTER_ID, masterId);
        mParseSlave.put(ID, id);
        mParseSlave.put(TYPE, type);
        mParseSlaveName = com.parse.ParseObject.create(SLAVE_NAME_CLASS_NAME);
        mParseSlaveName.put(NAME, name);
        mParseSlaveName.put(MASTER_ID, masterId);
        mParseSlaveName.put(SLAVE_ID, id);
        mParseSlaveName.put(USER_ID, userId);
    }

    public Slave(com.parse.ParseObject parseSlave) {
        mParseSlave = parseSlave;
        mParseSlaveName = getParseSlaveName(User.getLoggedUser().getId(), parseSlave.getString(MASTER_ID),
                parseSlave.getInt(SLAVE_ID));
    }

    public static Slave create(String masterId, String name, int type, int id, String userUuid) {
        return new Slave(masterId, name, type, id, userUuid);
    }

    public static Slave create(com.parse.ParseObject parseSlave) {
        return new Slave(parseSlave);
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
                mParseSlave.saveEventually();
                mParseSlaveName.saveEventually();
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
        query.whereEqualTo(Permission.MASTER_ID, Master.getMaster(getMasterId(), user.getId()).getId());
        query.whereEqualTo(Permission.USER_ID, user.getId());
        query.whereEqualTo(Permission.SLAVE_ID, getId());
        try {
            return Permission.create(query.getFirst());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        return ((Slave)o).getId() == getId();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + getId();
        return hash;
    }

    public void setName(String name) {
        mParseSlaveName.put(NAME, name);
        mParseSlaveName.put(SLAVE_ID, getId());
        mParseSlaveName.put(MASTER_ID, getMasterId());
        mParseSlaveName.put(USER_ID, User.getLoggedUser().getId());
        mParseSlaveName.pinInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    e.toString();
                }
            }
        });
        mParseSlaveName.saveEventually();
    }

    public static Slave fetchSlave(final String masterId, final int id) {
        if (TextUtils.isEmpty(masterId)) return null;
        ParseQuery<com.parse.ParseObject> query = ParseQuery.getQuery(SLAVE_CLASS_NAME);
        query.whereEqualTo(ID, id);
        query.whereEqualTo(MASTER_ID, masterId);
        try {
            com.parse.ParseObject parseSlave = query.getFirst();
            if (parseSlave != null) {
                Slave slave = new Slave(parseSlave);
                return slave;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
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
        query.whereEqualTo(SLAVE_ID, slaveId);
        query.whereEqualTo(USER_ID, userUuid);
        com.parse.ParseObject parseObject = null;
        try {
            parseObject = query.getFirst();
            if (parseObject == null) {
                parseObject = com.parse.ParseObject.create(SLAVE_NAME_CLASS_NAME);
                parseObject.put(MASTER_ID, masterId);
                parseObject.put(SLAVE_ID, slaveId);
                parseObject.put(USER_ID, userUuid);
                parseObject.put(NAME, DEFAULT_NAME + " " + String.valueOf(slaveId));
            }
        } catch (ParseException e) {
            parseObject = com.parse.ParseObject.create(SLAVE_NAME_CLASS_NAME);
            parseObject.put(MASTER_ID, masterId);
            parseObject.put(SLAVE_ID, slaveId);
            parseObject.put(USER_ID, userUuid);
            parseObject.put(NAME, DEFAULT_NAME + " " + String.valueOf(slaveId));
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
    public String toString() {
        return mParseSlaveName.getString(NAME);
    }
}
