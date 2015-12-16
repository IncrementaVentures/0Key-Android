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
import android.widget.TextView;

import com.incrementaventures.okey.Activities.DoorActivity;
import com.incrementaventures.okey.Activities.MainActivity;
import com.incrementaventures.okey.Views.Adapters.MasterAdapter;
import com.incrementaventures.okey.Models.Master;
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
    @Bind(R.id.scanning_top_bar)
    TextView mScanningTopBar;
    @Bind(R.id.no_devices_found_view)
    TextView mNoDevicesFound;

    ArrayList<Master> mMasters;
    MasterAdapter mAdapter;

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
        mMasters = new ArrayList<>();
        mAdapter = new MasterAdapter(getActivity(), R.id.door_name_list_item, mMasters);
        mDevicesList.setAdapter(mAdapter);
    }

    private void authenticateUser(){
        mCurrentUser = User.getLoggedUser((MainActivity) getActivity());
    }

    private void setListeners(){
        mDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String doorUuid = mMasters.get(position).getUUID();
                Intent intent = new Intent(getActivity(), DoorActivity.class);
                intent.putExtra(Master.UUID, doorUuid);
                intent.putExtra(MainActivity.MASTER_NAME_EXTRA, mMasters.get(position).getName());
                intent.putExtra(MainActivity.SCANNED_DOOR_EXTRA, true);
                startActivity(intent);
            }
        });
    }

    public void addDevice(Master master){
        mNoDevicesFound.setVisibility(TextView.GONE);
        mMasters.add(master);
        ((BaseAdapter) mDevicesList.getAdapter()).notifyDataSetChanged();

    }

    public void stopScanning(){
        mScanningTopBar.setVisibility(TextView.GONE);
    }





}
