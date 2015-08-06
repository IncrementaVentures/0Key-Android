package com.incrementaventures.okey.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.incrementaventures.okey.Activities.MainActivity;
import com.incrementaventures.okey.Models.Door;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.R;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DoorFragment extends Fragment {

    @Bind(R.id.permission_type)
    TextView mPermissionTypeView;
    @Bind(R.id.end_date)
    TextView mEndDateView;
    @Bind(R.id.end_date_text_view)
    TextView mEndDateStaticView;

    private Door mDoor;
    private Permission mPermission;
    private String endDate;
    private boolean mScannedDoor;

    public DoorFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_door, container, false);
        ButterKnife.bind(this, v);

        mScannedDoor = getActivity().getIntent().getExtras().getBoolean(MainActivity.SCANNED_DOOR_EXTRA);

        mDoor = getDoor();
        setPermission();

        setUI();


        return v;
    }

    private void setUI(){
        if (mPermission != null){
            mPermissionTypeView.setText(mPermission.getType());
            mEndDateView.setText(mPermission.getEndDate());
        } else{
            mPermissionTypeView.setText(R.string.permission_type_unknown);
            mEndDateView.setVisibility(TextView.GONE);
            mEndDateStaticView.setVisibility(TextView.GONE);
        }
    }

    private Door getDoor(){
        if (mScannedDoor){
            String name = getActivity().getIntent().getExtras().getString(MainActivity.DOOR_NAME_EXTRA);
            return Door.create(name, "");
        }

        ParseQuery query = new ParseQuery(Door.DOOR_CLASS_NAME);
        query.fromLocalDatastore();
        query.whereEqualTo(Door.UUID, getActivity().getIntent().getExtras().getString(Door.UUID));

        try {
            ParseObject doorParse = query.getFirst();
            return Door.create(doorParse);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setPermission(){
        mPermission = mDoor.getPermission();
    }

    public void createPermission(User user, String key, int type){

    }
}
