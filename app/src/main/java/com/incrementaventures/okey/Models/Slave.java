package com.incrementaventures.okey.Models;

public class Slave implements ParseObject{
    public static final String SLAVE_CLASS_NAME = "Slave";
    public static final String UUID = "uuid";
    public static final String ID = "id";
    public static final String UUID_MASTER = "uuid_master";
    public static final String NAME = "name";
    public static final String TYPE = "type";

    private com.parse.ParseObject mParseSlave;


    private Slave(String uuidMaster, String name, int type){
        mParseSlave = com.parse.ParseObject.create(SLAVE_CLASS_NAME);
        mParseSlave.put(UUID, java.util.UUID.randomUUID().toString());
        mParseSlave.put(UUID_MASTER, uuidMaster);
        mParseSlave.put(NAME, name);
        mParseSlave.put(TYPE, type);
    }

    private Slave(com.parse.ParseObject parseSlave){
        mParseSlave = parseSlave;
    }

    public static Slave create( String uuidMaster, String name, int type){
        return new Slave(uuidMaster, name, type);
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
        return mParseSlave.getString(UUID_MASTER);
    }

}
