package com.incrementaventures.okey.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.incrementaventures.okey.Activities.DoorActivity;
import com.incrementaventures.okey.Activities.MainActivity;
import com.incrementaventures.okey.Activities.ModifyPermissionActivity;
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

public class MasterFragment extends Fragment {

    @Bind(R.id.no_slaves_yet)
    TextView mNoSlavesView;
    @Bind(R.id.add_permission_button)
    ImageButton mAddPermissionButton;
    @Bind(R.id.right_arrow_master)
    ImageButton mRightArrowMaster;
    @Bind(R.id.left_arrow_master)
    ImageButton mLeftArrowMaster;
    @Bind(R.id.master_name)
    TextView mNameMasterView;

    private Master mMaster;
    private HashMap<Integer, Permission> mPermissions;
    private List<Slave> mSlaves;
    private SlavesAdapter mSlavesAdapter;
    private boolean mScannedDoor;
    private OnSlaveSelectedListener mSlaveSelectionListener;
    private OnChangeMasterListener mChangeMasterListener;
    private Slave mSelectedSlave;

    public interface OnSlaveSelectedListener {
        void openDoorSelected(Master master, Slave slave);
        void readMyPermissionSelected(Master master, Slave slave, String permissionKey);
        void readAllPermissionsSelected(Master master, Slave slave, String permissionKey);
        void openWhenCloseSelected(Master master, Slave slave, String permissionKey);
    }

    public interface OnChangeMasterListener {
        void onMoveRight();
        void onMoveLeft();
    }

    public MasterFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_master, container, false);
        ButterKnife.bind(this, v);
        mScannedDoor = getArguments().getBoolean(MainActivity.SCANNED_DOOR_EXTRA);
        mMaster = getMaster();
        setPermissions();
        setUI();
        setListeners();
        return v;
    }

    private void setUI() {
        mNameMasterView.setText(mMaster.getName());
        if (mPermissions != null){
            mSlaves = mMaster.getSlaves();
            if (mSlaves == null || mSlaves.size() == 0) {
                mSlaves = new ArrayList<>();
                mNoSlavesView.setVisibility(TextView.VISIBLE);
            } else mNoSlavesView.setVisibility(TextView.GONE);

        }
    }

    private void setListeners(){
        mAddPermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Permission permission =
                        mSelectedSlave.getPermission(User.getLoggedUser());
                if (mMaster != null && mMaster.getPermissions() != null && permission.isAdmin()) {
                    Intent intent = new Intent(getActivity(), ModifyPermissionActivity.class);
                    intent.putExtra(DoorActivity.REQUEST_CODE, DoorActivity.NEW_PERMISSION_REQUEST);
                    intent.putExtra(Permission.KEY, permission.getKey());
                    startActivityForResult(intent, DoorActivity.NEW_PERMISSION_REQUEST);
                }
                else {
                    Toast.makeText(getActivity(), R.string.you_are_not_admin, Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        mLeftArrowMaster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChangeMasterListener.onMoveLeft();
            }
        });

        mRightArrowMaster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChangeMasterListener.onMoveRight();
            }
        });

        mNameMasterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChangeMasterListener.onMoveRight();
            }
        });
    }

    private Master getMaster(){
        if (mScannedDoor){
            String name = getArguments().getString(MainActivity.MASTER_NAME_EXTRA);
            return Master.create(name, "");
        }
        ParseQuery query = new ParseQuery(Master.MASTER_CLASS_NAME);
        query.fromLocalDatastore();
        query.whereEqualTo(Master.UUID, getArguments().getString(Master.UUID));
        try {
            ParseObject doorParse = query.getFirst();
            return Master.create(doorParse);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setPermissions(){
        mPermissions = mMaster.getPermissions();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mSlaveSelectionListener = (OnSlaveSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnSlaveSelectedListener");
        }
        try {
            mChangeMasterListener = (OnChangeMasterListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnChangeMasterListener");
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
            }
        });
    }

    public void refreshPermissionView(Permission permission) {
        // TODO: 31-10-2015 Refresh slaves ListView with new information
        // mSlavesAdapter.getSelectedSlave();

    }
}
