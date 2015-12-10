package com.incrementaventures.okey.Models;

import com.parse.ParseException;
import com.parse.ParseQuery;

public class Slave implements ParseObject{
    public static final String SLAVE_CLASS_NAME = "Slave";
    public static final String UUID = "uuid";
    public static final String ID = "id";
    public static final String MASTER_UUID = "master_uuid";
    public static final String NAME = "name";
    public static final String TYPE = "type";

    private com.parse.ParseObject mParseSlave;


    private Slave(String uuidMaster, String name, int type, int id){
        mParseSlave = com.parse.ParseObject.create(SLAVE_CLASS_NAME);
        mParseSlave.put(UUID, java.util.UUID.randomUUID().toString());
        mParseSlave.put(MASTER_UUID, uuidMaster);
        mParseSlave.put(NAME, name);
        mParseSlave.put(TYPE, type);
        mParseSlave.put(ID, id);
    }

    private Slave(com.parse.ParseObject parseSlave){
        mParseSlave = parseSlave;
    }

    public static Slave create( String uuidMaster, String name, int type, int id){
        return new Slave(uuidMaster, name, type, id);
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
        ParseQuery query =
                ParseQuery.getQuery(Permission.PERMISSION_CLASS_NAME);
        query.whereEqualTo(Permission.MASTER_UUID, getUuidMaster());
        query.whereEqualTo(Permission.USER_UUID, user.getUUID());
        try {
            return Permission.create(query.getFirst());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        return ((Slave)o).getId() == this.getId();
    }
}
