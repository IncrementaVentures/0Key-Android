package com.incrementaventures.okey.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.incrementaventures.okey.Models.Door;
import com.incrementaventures.okey.R;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class DoorFragment extends Fragment {

    private Door mDoor;

    public DoorFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_door, container, false);

        mDoor = getDoor();

        return v;
    }

    private Door getDoor(){
        ParseQuery query = new ParseQuery(Door.DOOR_CLASS_NAME);
        query.fromLocalDatastore();
        String name = getActivity().getIntent().getExtras().getString(Door.NAME);
        if (name.equals(Door.FACTORY_NAME)){
            return Door.create(name, "");
        }
        try {
            ParseObject doorParse = query.get(getActivity().getIntent().getExtras().getString(Door.ID));
            return Door.create(doorParse);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
