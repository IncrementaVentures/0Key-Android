package com.incrementaventures.okey.Fragments;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.incrementaventures.okey.Activities.DoorActivity;
import com.incrementaventures.okey.Activities.MainActivity;
import com.incrementaventures.okey.Adapters.MasterAdapter;
import com.incrementaventures.okey.Bluetooth.BluetoothProtocol;
import com.incrementaventures.okey.Models.Master;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.Slave;
import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.R;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MainFragment extends Fragment {

    @Bind(R.id.master_list_main)
    GridView mMasterList;
    ArrayList<Master> mMasters;
    MasterAdapter mAdapter;
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
        mMasterList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String doorUuid = mMasters.get(position).getUUID();
                Intent intent = new Intent(getActivity(), DoorActivity.class);
                intent.putExtra(Master.UUID, doorUuid);
                intent.putExtra(MainActivity.SCANNED_DOOR_EXTRA, false);
                startActivity(intent);
            }
        });
    }


    private void setUp() {
        mMasters = Master.getMasters();
        mAdapter.notifyDataSetChanged();
    }

    private void setDoorList(){
        mAdapter = new MasterAdapter(getActivity(), R.layout.master_list_item, mMasters);
        mMasterList.setAdapter(mAdapter);
    }


    public void masterNetworkFound(Master master) {
        if (mMasters.size()>0)
            mMasters.add(0, master);
        else
            mMasters.add(master);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
                Toast.makeText(getActivity(),"You have new permissions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createFakeData(){
        if (mMasters.size() == 0){
            Master m1 = Master.create("PSN"
                    , "Casa "+ User.getLoggedUser((MainActivity)getActivity()).getName(), "");
            Master m2 = Master.create("PSN", "Oficina", "");
            m1.save();
            m2.save();
            for (int i = 1; i<6; i++){
                Slave s1 = Slave.create(m1.getUUID(), "Slave "+i, 0, i);
                Slave s2 = Slave.create(m2.getUUID(), "Slave "+i, 0, i);
                s1.save();
                s2.save();
            }
            mMasters.add(m1);
            mMasters.add(m2);
            Time now = new Time();
            now.setToNow();
            Permission p1 = Permission.create(mCurrentUser, m1, 0, "1234",
                    BluetoothProtocol.formatDate(now), Permission.PERMANENT_DATE, 0);
            Permission p2 = Permission.create(mCurrentUser, m2, 0, "1234",
                    BluetoothProtocol.formatDate(now), Permission.PERMANENT_DATE, 0);
            p1.save();
            p2.save();

            mAdapter = new MasterAdapter(getActivity(), R.layout.master_list_item, mMasters);
            mMasterList.setAdapter(mAdapter);
        }
    }
}
