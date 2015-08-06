package com.incrementaventures.okey.Fragments;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.incrementaventures.okey.Activities.DoorActivity;
import com.incrementaventures.okey.Activities.MainActivity;
import com.incrementaventures.okey.Adapters.DoorsAdapter;
import com.incrementaventures.okey.Models.Door;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.R;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MainFragment extends Fragment implements Door.OnDoorDataListener{

    @Bind(R.id.door_list_main)
    ListView mDoorList;

    ArrayList<Door> mDoors;
    DoorsAdapter mAdapter;

    User mCurrentUser;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, v);

        authenticateUser();
        setListeners();
        setUp();
        setDoorList();


        return v;

    }


    private void authenticateUser(){
        mCurrentUser = User.getLoggedUser((MainActivity) getActivity());
    }

    private void setListeners(){
        mDoorList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String doorUuid = mDoors.get(position).getUUID();
                Intent intent = new Intent (getActivity(), DoorActivity.class);
                intent.putExtra(Door.UUID, doorUuid);
                intent.putExtra(MainActivity.DOOR_NAME_EXTRA, mDoors.get(position).getName());
                intent.putExtra(MainActivity.SCANNED_DOOR_EXTRA, false);
                startActivity(intent);
            }
        });
    }


    private void setUp(){
        mDoors = new ArrayList<>();
        if (mCurrentUser != null){
            //Door.deleteAll();
            Door.getDoors(this);
        }

    }

    private void setDoorList(){
        mAdapter = new DoorsAdapter(getActivity(), R.layout.door_list_item, mDoors);
        mDoorList.setAdapter(mAdapter);
    }


    @Override
    public void doorFinded(Door door) {
        if (door == null ){
            createFakeData();
        } else{
            mDoors.add(door);
            mAdapter = new DoorsAdapter(getActivity(), R.layout.door_list_item, mDoors);
            mDoorList.setAdapter(mAdapter);
        }
    }

    private void createFakeData(){
        if (mDoors.size() == 0){
            Door d1 = Door.create("Fake door 1", "This is a fake door.");
            Door d2 = Door.create("Fake door 2", "This is a fake door too.");
            d1.save();
            d2.save();
            mDoors.add(d1);
            mDoors.add(d2);
            Permission p1 = Permission.create(mCurrentUser, d1, 0, "1234", Permission.PERMANENT_DATE);
            Permission p2 = Permission.create(mCurrentUser, d2, 0, "1234", Permission.PERMANENT_DATE);
            p1.save();
            p2.save();

            mAdapter = new DoorsAdapter(getActivity(), R.layout.door_list_item, mDoors);
            mDoorList.setAdapter(mAdapter);
        }
    }
}
