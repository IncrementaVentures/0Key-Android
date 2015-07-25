package com.incrementaventures.okey.Models;

import com.parse.ParseObject;

public class Door {
    private final String DOOR_CLASS_NAME = "Door";
    private final String NAME = "name";
    private final String DESCRIPTION = "description";

    private ParseObject mParseDoor;

    private Door(String name, String description){
        mParseDoor = ParseObject.create(DOOR_CLASS_NAME);
        mParseDoor.put(NAME, name);
        mParseDoor.put(DESCRIPTION, description);
    }

    private Door(ParseObject parseObject){
        mParseDoor = parseObject;
    }

    public static Door create(String name ,String description){
        return new Door(name, description);
    }

    protected static Door create(ParseObject parseObject){
        return new Door(parseObject);
    }

    protected ParseObject getParseDoor(){
        return mParseDoor;
    }

    public String getName(){
        return mParseDoor.getString(NAME);
    }

    public String getDescription(){
        return mParseDoor.getString(DESCRIPTION);
    }



}
