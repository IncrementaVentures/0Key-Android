package com.incrementaventures.okey.Models;

import android.text.TextUtils;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Master implements com.incrementaventures.okey.Models.ParseObject, Nameable {
    public static final String MASTER_CLASS_NAME = "Master";
    public static final String MASTER_NAME_CLASS_NAME = "MasterName";
    public static final String ID = "master_id";
    public static final String MASTER_ID = "master_id";
    public static final String NAME = "name";
    public static final String USER_ID = "user_id";
    private static final String CREATED_AT = "createdAt";
    private static final String UPDATED_AT = "upatedAt";
    private static final String DEFAULT_NAME = "Default name";

    private ParseObject mParseMaster;
    private ParseObject mMasterName;
    private List<Slave> mSlaves;

    public interface OnNetworkResponseListener {
        void onSlavesReceived(ArrayList<Slave> slaves);
        void onMastersReceived(ArrayList<Master> masters);
        void onMasterReceived(Master master);
    }

    private Master(String id, String name, String userId) {
        mParseMaster = ParseObject.create(MASTER_CLASS_NAME);
        mParseMaster.put(ID, id);
        mMasterName = ParseObject.create(MASTER_NAME_CLASS_NAME);
        mMasterName.put(NAME, name);
        mMasterName.put(USER_ID, userId);
        mMasterName.put(MASTER_ID, id);
    }

    private Master(ParseObject parseMaster) {
        mParseMaster = parseMaster;
        mMasterName = getParseMasterName(parseMaster, User.getLoggedUser().getId());
    }

    public static Master create(String id ,String name, String userUuid) {
        return new Master(id, name, userUuid);
    }

    public static Master create(ParseObject parseObject) {
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
        if (mMasterName == null) {
            mMasterName = getParseMasterName(mParseMaster, User.getLoggedUser().getId());
        }
        return mMasterName.getString(NAME);
    }

    public String getId() {
        return mParseMaster.getString(ID);
    }

    public void setName(String name) {
        mMasterName.put(NAME, name);
        mMasterName.put(MASTER_ID, getId());
        mMasterName.put(USER_ID, User.getLoggedUser().getId());
        mMasterName.pinInBackground();
        mMasterName.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    e.toString();
                }
            }
        });
    }

    public static ArrayList<Master> getMasters() {
        ParseQuery<ParseObject> query = new ParseQuery<>(Master.MASTER_CLASS_NAME);
        query.fromLocalDatastore();
        query.orderByDescending(CREATED_AT);
        ArrayList<Master> masters = new ArrayList<>();
        try {
            List<ParseObject> parseMasters  = query.find();
            for (ParseObject object : parseMasters) {
                if (object != null)
                    masters.add(Master.create(object));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return masters;
    }

    private static ParseObject getParseMasterName(ParseObject master, String userUuid) {
        ParseQuery<ParseObject> query = new ParseQuery<>(Master.MASTER_NAME_CLASS_NAME);
        query.whereEqualTo(MASTER_ID, master.get(ID));
        query.whereEqualTo(USER_ID, userUuid);
        query.fromLocalDatastore();
        ParseObject masterName;
        try {
            masterName = query.getFirst();
            if (masterName == null) {
                masterName = new ParseObject(MASTER_NAME_CLASS_NAME);
                masterName.put(USER_ID, userUuid);
                masterName.put(MASTER_ID, master.getString(ID));
                masterName.put(NAME, master.getString(ID));
            }
        } catch (ParseException e) {
            masterName = new ParseObject(MASTER_NAME_CLASS_NAME);
            masterName.put(USER_ID, userUuid);
            masterName.put(MASTER_ID, master.getString(ID));
            masterName.put(NAME, master.getString(ID));
        }
        return masterName;
    }

    /*
    public static void fetchMasters(final OnNetworkResponseListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ParseQuery<ParseObject> query = new ParseQuery<>(Master.MASTER_CLASS_NAME);
                query.orderByDescending(CREATED_AT);
                ArrayList<Master> masters = new ArrayList<>();
                try {
                    List<ParseObject> list  = query.find();
                    Master master;
                    for (ParseObject object : list){
                        master = getMaster(object.getString(OBJECT_ID));
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
    } */

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
    public void save() {
        saveLocal();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Master master = fetchMaster(getId());
                if (master == null) {
                    mParseMaster.saveEventually();
                }
                mMasterName.saveEventually();
            }
        }).start();
    }

    public void saveLocal() {
        mParseMaster.pinInBackground();
        if (mMasterName == null) {
            mMasterName = getParseMasterName(mParseMaster, User.getLoggedUser().getId());
        }
        mMasterName.pinInBackground();
    }

    public static void unpinAll() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ParseQuery<ParseObject> query = new ParseQuery<>(Master.MASTER_CLASS_NAME);
                query.fromLocalDatastore();
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> list, ParseException e) {
                        for (ParseObject o : list) {
                            o.unpinInBackground();
                        }
                    }
                });

                ParseQuery<ParseObject> query2 = new ParseQuery<>(Master.MASTER_NAME_CLASS_NAME);
                query2.fromLocalDatastore();
                query2.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> list, ParseException e) {
                        for (ParseObject o : list) {
                            o.unpinInBackground();
                        }
                    }
                });
            }
        }).start();
    }

    public static void deleteAllLocal() {
        ParseQuery<com.parse.ParseObject> query = ParseQuery.getQuery(MASTER_CLASS_NAME);
        query.fromLocalDatastore();
        try {
            List<com.parse.ParseObject> localMasters = query.find();
            for (com.parse.ParseObject localMaster : localMasters) {
                localMaster.unpinInBackground();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    public ArrayList<Permission> getAllPermissions() {
        if (mParseMaster == null) return null;
        ParseQuery query = new ParseQuery(Permission.PERMISSION_CLASS_NAME);
        query.whereEqualTo(Permission.MASTER_ID, getId());
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
        query.whereEqualTo(Permission.MASTER_ID, getId());
        query.whereEqualTo(Permission.USER_ID, user.getId());
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
        query.whereEqualTo(Slave.MASTER_ID, getId());
        query.orderByAscending(Slave.ID);
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

    public void fetchSlave(final int id, final OnNetworkResponseListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ParseQuery query = new ParseQuery(Slave.SLAVE_CLASS_NAME);
                query.whereEqualTo(Slave.ID, id);
                try {
                    ParseObject parseSlave = query.getFirst();
                    Slave slave = Slave.create(parseSlave);
                    // TODO: 16-12-2015 Create SlaveName
                    mSlaves.add(slave);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                listener.onSlavesReceived(new ArrayList<>(mSlaves));
            }
        }).start();
    }

    public void fetchSlaves() {
        ParseQuery query = new ParseQuery(Slave.SLAVE_CLASS_NAME);
        query.whereEqualTo(Slave.MASTER_ID, getId());
        query.orderByAscending(Slave.ID);
        mSlaves = new ArrayList<>();
        try {
            List<ParseObject> objects = query.find();
            Slave slave;
            for (ParseObject o : objects) {
                slave = Slave.getSlave(getId(), o.getInt(Slave.ID));
                if (slave == null) {
                    slave = Slave.create(o);
                    mSlaves.add(slave);
                }
                slave.save();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    
    @Override
    public boolean equals(Object o) {
        return ((Master)o).getId().equals(getId());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (getId() != null ? this.getId().hashCode() : 0);
        return hash;
    }

    public void addSlave(Slave slave){
        if (mSlaves == null) mSlaves = getSlaves();
        mSlaves.add(slave);
    }

    public static Master getMaster(String id, String userUuid) {
        if (TextUtils.isEmpty(id)) return null;
        ParseQuery<ParseObject> query = new ParseQuery<>(MASTER_CLASS_NAME);
        query.fromLocalDatastore();
        query.whereEqualTo(ID, id);
        try {
            ParseObject parseMaster = query.getFirst();
            return Master.create(parseMaster);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Master fetchMaster(final String id) {
        if (TextUtils.isEmpty(id)) return null;
        ParseQuery<ParseObject> query = new ParseQuery<>(MASTER_CLASS_NAME);
        query.whereEqualTo(ID, id);
        Master master = null;
        try {
            ParseObject parseMaster = query.getFirst();
            if (parseMaster != null) {
                master = Master.create(parseMaster);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return master;
    }

    private static ParseObject fetchParseMasterName(ParseObject master, String userUuid)
            throws ParseException {
        ParseQuery<ParseObject> query = new ParseQuery<>(Master.MASTER_NAME_CLASS_NAME);
        query.whereEqualTo(MASTER_ID, master.get(ID));
        query.whereEqualTo(USER_ID, userUuid);
        return query.getFirst();
    }
}
