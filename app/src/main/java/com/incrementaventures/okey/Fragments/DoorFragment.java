package com.incrementaventures.okey.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.incrementaventures.okey.Activities.MainActivity;
import com.incrementaventures.okey.Adapters.SlavesAdapter;
import com.incrementaventures.okey.Models.Master;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.Slave;
import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.R;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DoorFragment extends Fragment {

    @Bind(R.id.slaves_door_fragment)
    ListView mSlavesListView;
    @Bind(R.id.no_slaves_yet)
    TextView mNoSlavesView;

    private Master mMaster;
    private Permission mPermission;
    private List<Slave> mSlaves;
    private SlavesAdapter mSlavesAdapter;
    private boolean mScannedDoor;
    private OnSlaveSelectedListener mListener;

    public interface OnSlaveSelectedListener{
        void openDoorSelected(Master master, Slave slave);
        void readMyPermissionSelected(Master master, Slave slave, String permissionKey);
        void readAllPermissionsSelected(Master master, Slave slave, String permissionKey);
        void openWhenCloseSelected(Master master, Slave slave, String permissionKey);
    }

    public DoorFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_door, container, false);
        ButterKnife.bind(this, v);
        mScannedDoor = getActivity().getIntent().getExtras().getBoolean(MainActivity.SCANNED_DOOR_EXTRA);
        mMaster = getMaster();
        setPermission();
        setUI();
        setListeners();
        return v;
    }

    private void setUI(){
        if (mPermission != null){
            mSlaves = mMaster.getSlaves();
            if (mSlaves == null || mSlaves.size() == 0) {
                mSlaves = new ArrayList<>();
                mNoSlavesView.setVisibility(TextView.VISIBLE);
            } else mNoSlavesView.setVisibility(TextView.GONE);

            mSlavesAdapter = new SlavesAdapter(getActivity(), R.layout.slave_list_item, mSlaves, mMaster);
            mSlavesListView.setAdapter(mSlavesAdapter);
        }
    }

    private void setListeners(){
        mSlavesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.openDoorSelected(mMaster, mSlaves.get(position));
            }
        });
    }

    private Master getMaster(){
        if (mScannedDoor){
            String name = getActivity().getIntent().getExtras().getString(MainActivity.MASTER_NAME_EXTRA);
            return Master.create(name, "");
        }
        ParseQuery query = new ParseQuery(Master.MASTER_CLASS_NAME);
        query.fromLocalDatastore();
        query.whereEqualTo(Master.UUID, getActivity().getIntent().getExtras().getString(Master.UUID));
        try {
            ParseObject doorParse = query.getFirst();
            return Master.create(doorParse);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setPermission(){
        mPermission = mMaster.getPermission();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
             mListener = (OnSlaveSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnSlaveSelectedListener");
        }
    }

    public void addSlave(final ArrayList<HashMap<String,String>> slavesData){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSlaves == null) {
                    mSlaves = new ArrayList<>();
                }
                if (slavesData.size() != 0) {
                    mNoSlavesView.setVisibility(TextView.GONE);
                }
                for (HashMap<String, String> slaveData : slavesData) {
                    final Slave slave = Slave.create(mMaster.getUUID(),
                            slaveData.get(Slave.ID),
                            Integer.valueOf(slaveData.get(Slave.TYPE)),
                            Integer.valueOf(slaveData.get(Slave.ID)));
                    if (!mSlaves.contains(slave)) {
                        slave.save();
                        mSlaves.add(slave);
                    }
                }
                mSlavesAdapter = new SlavesAdapter(getActivity(), R.layout.slave_list_item, mSlaves,
                        mMaster);
                mSlavesListView.setAdapter(mSlavesAdapter);
                ((BaseAdapter) mSlavesListView.getAdapter()).notifyDataSetChanged();
            }
        });
    }

    public void refreshPermissionView(Permission permission) {
        // TODO: 31-10-2015 Refresh slaves ListView with new information
        // mSlavesAdapter.getSelectedSlave();

    }
}
