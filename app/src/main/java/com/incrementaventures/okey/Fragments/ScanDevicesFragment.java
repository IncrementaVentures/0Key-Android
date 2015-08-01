package com.incrementaventures.okey.Fragments;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.incrementaventures.okey.Activities.DoorActivity;
import com.incrementaventures.okey.Activities.MainActivity;
import com.incrementaventures.okey.Adapters.DoorsAdapter;
import com.incrementaventures.okey.Models.Door;
import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.R;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScanDevicesFragment extends Fragment {

    @Bind(R.id.scanned_devices_list)
    ListView mDevicesList;

    ArrayList<Door> mDoors;
    DoorsAdapter mAdapter;

    User mCurrentUser;

    public ScanDevicesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_scan_devices, container, false);
        ButterKnife.bind(this, v);

        authenticateUser();
        setUp();
        setListeners();


        mCurrentUser.scanDevices();
        return v;
    }

    private void setUp(){
        getActivity().setTitle(R.string.scan_devices);
        mDoors = new ArrayList<>();
        mAdapter = new DoorsAdapter(getActivity(), R.id.door_name_list_item, mDoors);
        mDevicesList.setAdapter(mAdapter);
    }

    private void authenticateUser(){
        mCurrentUser = User.getLoggedUser((MainActivity) getActivity());
    }

    private void setListeners(){
        mDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String doorId = mDoors.get(position).getId();
                Intent intent = new Intent (getActivity(), DoorActivity.class);
                intent.putExtra(Door.ID, doorId);
                intent.putExtra(Door.NAME, mDoors.get(position).getName());
                startActivity(intent);
            }
        });
    }

    public void addDevice(Door door){
        mDoors.add(door);
        ((BaseAdapter) mDevicesList.getAdapter()).notifyDataSetChanged();

    }



}
