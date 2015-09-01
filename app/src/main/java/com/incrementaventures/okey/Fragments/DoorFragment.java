package com.incrementaventures.okey.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
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
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DoorFragment extends Fragment implements PopupMenu.OnMenuItemClickListener{

    @Bind(R.id.permission_type)
    TextView mPermissionTypeView;
    @Bind(R.id.end_date)
    TextView mEndDateView;
    @Bind(R.id.end_date_text_view)
    TextView mEndDateStaticView;
    @Bind(R.id.slaves_door_fragment)
    ListView mSlavesListView;

    private Master mMaster;
    private Permission mPermission;

    private List<Slave> mSlaves;
    private SlavesAdapter mSlavesAdapter;

    private Slave mSelectedSlave;

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

        setUI(inflater);
        setListeners();

        return v;
    }

    private void setUI(final LayoutInflater inflater){
        if (mPermission != null){
            mSlaves = mMaster.getSlaves();
            if (mSlaves == null) mSlaves = new ArrayList<>();
            mSlavesAdapter = new SlavesAdapter(getActivity(), R.layout.slave_list_item, mSlaves){
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = convertView;
                    final Slave slave = getItem(position);

                    if (view == null){
                        view = inflater.inflate(R.layout.slave_list_item, parent, false);
                    }

                    TextView doorName = (TextView) view.findViewById(R.id.slave_name);
                    doorName.setText(slave.getName());

                    ImageButton moreOptionsButton = (ImageButton) view.findViewById(R.id.more_options_button);
                    moreOptionsButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mSelectedSlave = slave;
                            PopupMenu popup = new PopupMenu(getContext(), v);
                            MenuInflater inflater = popup.getMenuInflater();
                            inflater.inflate(R.menu.menu_more_options_slave, popup.getMenu());
                            popup.setOnMenuItemClickListener(DoorFragment.this);
                            popup.show();
                        }
                    });

                    LinearLayout openButton = (LinearLayout) view.findViewById(R.id.open_slave_layout);
                    openButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mListener.openDoorSelected(mMaster, slave);
                        }
                    });
                    return view;
                }
            };
            mSlavesListView.setAdapter(mSlavesAdapter);
            mPermissionTypeView.setText(mPermission.getType());
            mEndDateView.setText(mPermission.getEndDate());
        } else{
            mPermissionTypeView.setText(R.string.permission_type_unknown);
            mEndDateView.setVisibility(TextView.GONE);
            mEndDateStaticView.setVisibility(TextView.GONE);
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

    public void createPermission(User user, String key, int type){

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_read_my_permission:
                mListener.readMyPermissionSelected(mMaster, mSelectedSlave, mPermission.getKey());
                return true;
            case R.id.action_read_all_permissions:
                mListener.readAllPermissionsSelected(mMaster, mSelectedSlave, mPermission.getKey());
                return true;
            case R.id.action_open_when_close:
                mListener.openWhenCloseSelected(mMaster, mSelectedSlave, mPermission.getKey());
            default:
                return false;
        }
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

    public void addSlave(final String id, final String type, final String name){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Slave s = Slave.create(mMaster.getUUID(), name, Integer.valueOf(type), Integer.valueOf(id));
                if (mSlaves == null) {
                    mSlaves = new ArrayList<>();
                }
                mSlaves.add(s);
                mSlavesAdapter = new SlavesAdapter(getActivity(), R.layout.slave_list_item, mSlaves);
                ((BaseAdapter) mSlavesListView.getAdapter()).notifyDataSetChanged();
                s.save();
            }
        });

    }



}
