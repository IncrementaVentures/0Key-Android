package com.incrementaventures.okey.Models;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

public class Door implements com.incrementaventures.okey.Models.ParseObject{
    public static final String DOOR_CLASS_NAME = "Door";
    public static final String ID = "id";
    public static final String UUID = "uuid";
    public static final String NAME = "name";
    private final String DESCRIPTION = "description";

    public static final String FACTORY_NAME = "PSN";

    private ParseObject mParseDoor;

    public interface OnDoorDataListener{
        void doorFinded(Door door);
    }


    private Door(String name, String description){
        mParseDoor = ParseObject.create(DOOR_CLASS_NAME);
        mParseDoor.put(UUID, java.util.UUID.randomUUID().toString());
        mParseDoor.put(NAME, name);
        mParseDoor.put(DESCRIPTION, description);
    }

    private Door(ParseObject parseObject){
        mParseDoor = parseObject;
    }

    public static Door create(String name ,String description){
        return new Door(name, description);
    }

    public static Door create(ParseObject parseObject){
        return new Door(parseObject);
    }

    public ParseObject getParseDoor(){
        return mParseDoor;
    }

    public String getName(){
        return mParseDoor.getString(NAME);
    }

    public String getDescription(){
        return mParseDoor.getString(DESCRIPTION);
    }



    public static void getDoors(final OnDoorDataListener listener){
        ParseQuery<ParseObject> query = new ParseQuery<>(Door.DOOR_CLASS_NAME);
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (list == null || list.size() == 0) {
                    listener.doorFinded(null);
                    return;
                }
                for (ParseObject object : list){
                    listener.doorFinded(Door.create(object));
                }
            }
        });

    }

    @Override
    public String getId(){
        return mParseDoor.getObjectId();
    }

    @Override
    public void deleteFromLocal(){
        try {
            mParseDoor.unpin();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void save(){
        mParseDoor.pinInBackground();
        mParseDoor.saveEventually();
    }

    @Override
    public String getUUID() {
        if (mParseDoor == null) return null;
        return mParseDoor.getString(UUID);
    }

    public static void deleteAll(){
        ParseQuery<ParseObject> query = new ParseQuery<>(Door.DOOR_CLASS_NAME);
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

    public Permission getPermission(){
        if (mParseDoor == null) return null;
        ParseQuery query = new ParseQuery(Permission.PERMISSION_CLASS_NAME);
        query.whereEqualTo(Permission.DOOR_UUID, getUUID());
        query.fromLocalDatastore();
        try {
            ParseObject o = query.getFirst();
            if (o != null){
                return Permission.create(o);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
