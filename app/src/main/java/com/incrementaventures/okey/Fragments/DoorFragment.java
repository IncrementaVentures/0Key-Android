package com.incrementaventures.okey.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DoorFragment extends Fragment {

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

    private boolean mScannedDoor;

    private OnSlaveSelectedListener mListener;

    public interface OnSlaveSelectedListener{
        void slaveSelected(Master master, Slave slave);
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
            mSlavesAdapter = new SlavesAdapter(getActivity(), R.layout.slave_list_item, mSlaves);
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
                mListener.slaveSelected(mMaster, mSlaves.get(position));
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
             mListener = (OnSlaveSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnSlaveSelectedListener");
        }
    }



}
