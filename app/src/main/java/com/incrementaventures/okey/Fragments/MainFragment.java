package com.incrementaventures.okey.Fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.incrementaventures.okey.Models.Door;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {
    @Bind(R.id.button_open_door)
    Button openDoorButton;

    @Bind(R.id.button_close_door)
    Button closeDoorButton;

    User mCurrentUser;
    Door mTestDoor;


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
        testSetUp();

        return v;

    }


    private void authenticateUser(){
        mCurrentUser = User.getLoggedUser(getActivity());
    }

    private void setListeners(){

        openDoorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentUser.openDoor(mTestDoor);
            }
        });

        closeDoorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentUser.closeDoor(mTestDoor);
            }
        });
    }


    private void testSetUp(){
        mTestDoor = Door.create("Door", "Test door");
        Permission permission = Permission.create(mCurrentUser, mTestDoor, 0, "TEST");
        mCurrentUser.addPermission(permission);
    }


}
