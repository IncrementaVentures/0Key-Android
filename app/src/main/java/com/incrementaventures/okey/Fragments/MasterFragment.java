package com.incrementaventures.okey.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.incrementaventures.okey.Activities.DoorActivity;
import com.incrementaventures.okey.Activities.ModifyPermissionActivity;
import com.incrementaventures.okey.Models.Master;
import com.incrementaventures.okey.Models.Nameable;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.Slave;
import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MasterFragment extends Fragment {
    public static final String TAG = "master_fragment_tag";

    @Bind(R.id.right_arrow_master)
    ImageButton mRightArrowMaster;
    @Bind(R.id.left_arrow_master)
    ImageButton mLeftArrowMaster;

    @Bind(R.id.master_name_container)
    ViewPager mMasterNameContainer;
    private TextViewPagerAdapter mMasterNameAdapter;

    @Bind(R.id.slave_name_container)
    ViewPager mSlaveNameContainer;
    private TextViewPagerAdapter mSlaveNameAdapter;

    @Bind(R.id.right_arrow_slave)
    ImageButton mRightArrowSlave;
    @Bind(R.id.left_arrow_slave)
    ImageButton mLeftArrowSlave;

    private ArrayList<Master> mMasters;
    private Master mSelectedMaster;
    private int mSelectedMasterIndex;
    private HashMap<Integer, Permission> mPermissions;
    private ArrayList<Slave> mSlaves;
    private boolean mScannedDoor;
    private OnSlaveSelectedListener mSlaveSelectionListener;
    private Slave mSelectedSlave;
    private int mSelectedSlaveIndex;

    public interface OnSlaveSelectedListener {
        void openDoorSelected(Master master, Slave slave);
        void readMyPermissionSelected(Master master, Slave slave, String permissionKey);
        void readAllPermissionsSelected(Master master, Slave slave, String permissionKey);
        void openWhenCloseSelected(Master master, Slave slave, String permissionKey);
    }

    public MasterFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_master, container, false);
        ButterKnife.bind(this, v);
        setMasters();
        setSlaves();
        setPermissions();
        setNameableHolderAdapters();
        setUI();
        setListeners();
        return v;
    }


    private void setSlaves() {
        mSlaves = new ArrayList<>(mSelectedMaster.getSlaves());
        if (mSlaves.size() > 0) {
            mSelectedSlave = mSlaves.get(0);
        }
    }

    private void setMasters() {
        mMasters = Master.getMasters();
        if (mMasters.size() > 0) {
            mSelectedMaster = mMasters.get(0);
        }
    }

    private void setNameableHolderAdapters() {
        mMasterNameAdapter = new TextViewPagerAdapter(getChildFragmentManager(),
                mMasters);
        mMasterNameContainer.setAdapter(mMasterNameAdapter);

        mSlaveNameAdapter = new TextViewPagerAdapter(getChildFragmentManager(),
                mSlaves);
        mSlaveNameContainer.setAdapter(mSlaveNameAdapter);
    }

    private void setUI() {
        if (mSlaves == null || mSlaves.size() == 0) {
            mLeftArrowSlave.setVisibility(ImageButton.GONE);
            mRightArrowSlave.setVisibility(ImageButton.GONE);
        }
        if (mMasters == null || mMasters.size() == 0) {
            mLeftArrowSlave.setVisibility(ImageButton.GONE);
            mRightArrowSlave.setVisibility(ImageButton.GONE);
        }
    }

    private void setListeners(){
       /* mAddPermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Permission permission =
                        mSelectedSlave.getPermission(User.getLoggedUser());
                if (mSelectedMaster != null && mSelectedMaster.getPermissions() != null && permission.isAdmin()) {
                    Intent intent = new Intent(getActivity(), ModifyPermissionActivity.class);
                    intent.putExtra(DoorActivity.REQUEST_CODE, DoorActivity.NEW_PERMISSION_REQUEST);
                    intent.putExtra(Permission.KEY, permission.getKey());
                    startActivityForResult(intent, DoorActivity.NEW_PERMISSION_REQUEST);
                } else {
                    Toast.makeText(getActivity(), R.string.you_are_not_admin, Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });*/

        mLeftArrowMaster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedMasterIndex =
                        moveViewPagerLeft(mMasterNameContainer, mSelectedMasterIndex);
            }
        });

        mRightArrowMaster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedMasterIndex =
                        moveViewPagerRight(mMasterNameContainer, mSelectedMasterIndex, mMasters.size());
            }
        });

        mLeftArrowSlave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedSlaveIndex =
                        moveViewPagerLeft(mSlaveNameContainer, mSelectedSlaveIndex);
            }
        });

        mRightArrowSlave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedSlaveIndex =
                        moveViewPagerRight(mSlaveNameContainer, mSelectedSlaveIndex, mSlaves.size());
            }
        });

    }

    private int moveViewPagerRight(ViewPager viewPager, int currentIndex, int listLenght) {
        currentIndex++;
        if (currentIndex >= listLenght) {
            currentIndex = listLenght - 1;
        }
        viewPager.setCurrentItem(currentIndex);
        return currentIndex;
    }

    private int moveViewPagerLeft(ViewPager viewPager, int currentIndex) {
        currentIndex--;
        if (currentIndex < 0) {
            currentIndex = 0;
        }
        viewPager.setCurrentItem(currentIndex);
        return currentIndex;
    }

    private void setPermissions(){
        mPermissions = mSelectedMaster.getPermissions();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mSlaveSelectionListener = (OnSlaveSelectedListener) activity;
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
                for (HashMap<String, String> slaveData : slavesData) {
                    final Slave slave = Slave.create(mSelectedMaster.getUUID(),
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

    public void masterNetworkFound(Master master) {
        mMasters.add(master);
        if (mSelectedMaster == null) {
            mSelectedMaster = master;
            setSlaves();
            setPermissions();
            setNameableHolderAdapters();
        }
    }

    public Master getSelectedMaster() {
        return mSelectedMaster;
    }

    public Slave getSelectedSlave() {
        return mSelectedSlave;
    }

    private class TextViewPagerAdapter extends FragmentStatePagerAdapter {
        ArrayList<? extends Nameable> mObjects;

        public TextViewPagerAdapter(FragmentManager fm, ArrayList<? extends Nameable> objects) {
            super(fm);
            mObjects = objects;
        }

        @Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();
            args.putString(Master.NAME, mObjects.get(position).getName());
            NameHolderFragment nameHolderFragment = new NameHolderFragment();
            nameHolderFragment.setArguments(args);
            return nameHolderFragment;
        }

        @Override
        public int getCount() {
            return mObjects.size();
        }
    }
}
