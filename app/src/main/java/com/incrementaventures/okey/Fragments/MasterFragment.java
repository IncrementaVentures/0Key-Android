package com.incrementaventures.okey.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.incrementaventures.okey.Models.Master;
import com.incrementaventures.okey.Models.Nameable;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.Slave;
import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MasterFragment extends Fragment implements Master.OnNetworkResponseListener {
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
    @Bind(R.id.open_button)
    ImageButton mOpenButton;

    @Bind(R.id.show_permissions_button)
    Button mShowPermissionsButton;


    private ArrayList<Master> mMasters;
    private int mSelectedMasterIndex;
    private HashMap<Integer, Permission> mPermissions;
    private ArrayList<Slave> mSlaves;
    private boolean mScannedDoor;
    private OnSlaveSelectedListener mSlaveSelectionListener;
    private int mSelectedSlaveIndex;

    public interface OnSlaveSelectedListener {
        void openDoorSelected(Master master, Slave slave);
        void readMyPermissionSelected(Master master, Slave slave, String permissionKey);
        void readAllPermissionsSelected(Master master, Slave slave, String permissionKey);
        void openWhenCloseSelected(Master master, Slave slave, String permissionKey);
    }

    public MasterFragment() { }

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
        return v;
    }

    private void setSlaves() {
        if (mMasters == null || mMasters.size() == 0) return;
        mSlaves = new ArrayList<>(mMasters.get(mSelectedMasterIndex).getSlaves());
        if (mSlaves.size() > 0) {
            mSelectedSlaveIndex = 0;
        } else {
            if (mPermissions != null && mPermissions.size() > 0) {
                TreeMap<Integer, Permission> treeMap = new TreeMap<>(mPermissions);
                User.getLoggedUser().getSlaves(mMasters.get(mSelectedMasterIndex), treeMap.firstEntry().getValue().getKey());
            }
        }
    }

    private void setMasters() {
        Master.fetchMasters(this);
        setLocalMasters();
    }

    private void setLocalMasters() {
        mMasters = Master.getMasters();
        if (mMasters.size() > 0) {
            mSelectedMasterIndex = 0;
        }
    }

    @Override
    public void onSlavesReceived(ArrayList<Slave> slaves) {
        if (slaves == null || slaves.size() == 0)
            return;
        if (mSlaves == null)
            mSlaves = new ArrayList<>();
        mSlaves.addAll(slaves);
        if (mSlaveNameAdapter == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setSlaveHolderAdapter();
                mSlaveNameAdapter.notifyDataSetChanged();
                setUI();
                Snackbar.make(getView(), R.string.slaves_added, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onMastersReceived(ArrayList<Master> masters) {
        if (masters == null || masters.size() == 0)
            return;
        if (mMasters == null || mMasters.size() == 0) {
            mMasters = new ArrayList<>();
            mMasters.addAll(masters);
            mSelectedMasterIndex = 0;
            mPermissions = mMasters.get(mSelectedMasterIndex).getPermissions(User.getLoggedUser());
        }
        if (mMasterNameAdapter == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setMasterHolderAdapter();
                mMasterNameAdapter.notifyDataSetChanged();
                setUI();
            }
        });
    }

    /**
     * Add the master to the list and set again the master holder adapter.
     * @param master
     */
    @Override
    public void onMasterReceived(Master master) {
        mMasters.add(master);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setMasterHolderAdapter();
            }
        });
    }

    private void setNameableHolderAdapters() {
        setMasterHolderAdapter();
        setSlaveHolderAdapter();
    }

    ViewPager.OnPageChangeListener mMasterPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

        @Override
        public void onPageSelected(int position) {
            mSelectedMasterIndex = position;
            setSlaves();
            setSlaveHolderAdapter();
        }

        @Override
        public void onPageScrollStateChanged(int state) { }
    };

    private void setMasterHolderAdapter() {
        mMasterNameAdapter = new TextViewPagerAdapter(getChildFragmentManager(),
                mMasters);
        mMasterNameContainer.setAdapter(mMasterNameAdapter);
        mMasterNameContainer.addOnPageChangeListener(mMasterPageChangeListener);
    }

    private void setSlaveHolderAdapter() {
        mSlaveNameAdapter = new TextViewPagerAdapter(getChildFragmentManager(),
                mSlaves);
        mSlaveNameContainer.setAdapter(mSlaveNameAdapter);
    }

    private void setUI() {
        if (mSlaves == null || mSlaves.size() <= 1) {
            mLeftArrowSlave.setVisibility(ImageButton.GONE);
            mRightArrowSlave.setVisibility(ImageButton.GONE);
        } else {
            mLeftArrowSlave.setVisibility(ImageButton.VISIBLE);
            mRightArrowSlave.setVisibility(ImageButton.VISIBLE);
        }
        if (mMasters == null || mMasters.size() == 0) {
            mLeftArrowMaster.setVisibility(ImageButton.GONE);
            mRightArrowMaster.setVisibility(ImageButton.GONE);
            mShowPermissionsButton.setVisibility(Button.GONE);
        } else if (mMasters.size() <= 1) {
            mLeftArrowMaster.setVisibility(ImageButton.GONE);
            mRightArrowMaster.setVisibility(ImageButton.GONE);
            mShowPermissionsButton.setVisibility(Button.GONE);
        } else {
            mLeftArrowMaster.setVisibility(ImageButton.VISIBLE);
            mRightArrowMaster.setVisibility(ImageButton.VISIBLE);
            mShowPermissionsButton.setVisibility(Button.VISIBLE);
        }
    }

    @OnClick(R.id.open_button)
    public void openDoorClicked() {
        if (mMasters != null && mMasters.size() > 0 && mSlaves != null && mSlaves.size() > 0){
            User.getLoggedUser().openDoor(mMasters.get(mSelectedMasterIndex),
                    mSlaves.get(mSelectedSlaveIndex));
        }
    }

    @OnClick(R.id.left_arrow_master)
    public void leftArrowMasterClicked() {
        mSelectedMasterIndex =
                moveViewPagerLeft(mMasterNameContainer, mSelectedMasterIndex);
        setSlaves();
        setSlaveHolderAdapter();
    }

    @OnClick(R.id.right_arrow_master)
    public void rightArrowMasterClicked() {
        mSelectedMasterIndex =
                moveViewPagerRight(mMasterNameContainer, mSelectedMasterIndex, mMasters.size());
        setSlaves();
        setSlaveHolderAdapter();
    }

    @OnClick(R.id.left_arrow_slave)
    public void leftArrowSlaveClicked() {
        mSelectedSlaveIndex =
                moveViewPagerLeft(mSlaveNameContainer, mSelectedSlaveIndex);
    }

    @OnClick(R.id.left_arrow_slave)
    public void rightArrowSlaveClicked() {
        mSelectedSlaveIndex =
                moveViewPagerRight(mSlaveNameContainer, mSelectedSlaveIndex, mSlaves.size());
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
        if (mMasters == null || mMasters.size() == 0) return;
        mPermissions = mMasters.get(mSelectedMasterIndex).getPermissions(User.getLoggedUser());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mSlaveSelectionListener = (OnSlaveSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnSlaveSelectedListener");
        }
    }

    public void masterNetworkFound(Master master) {
        mMasters.add(master);
        if (mMasters.size() == 1) {
            mSelectedMasterIndex = 0;
            setSlaves();
            setPermissions();
            setNameableHolderAdapters();
        }
    }

    public Master getSelectedMaster() {
        return (mMasters != null && mMasters.size() > 0) ? mMasters.get(mSelectedMasterIndex) : null;
    }

    public Slave getSelectedSlave() {
        return (mSlaves != null && mSlaves.size() > 0) ? mSlaves.get(mSelectedSlaveIndex) : null;
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
            if (mObjects == null) return 0;
            return mObjects.size();
        }
    }
}
