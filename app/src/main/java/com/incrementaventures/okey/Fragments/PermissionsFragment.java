package com.incrementaventures.okey.Fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.incrementaventures.okey.Adapters.PermissionsAdapter;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.Slave;
import com.incrementaventures.okey.R;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class PermissionsFragment extends Fragment {
    public static final String PERMISSIONS_DATA_EXTRA = "permissions_data_extra";

    @Bind(R.id.permissions_list_view)
    ListView mPermissionsView;

    private ArrayList<Permission> mPermissions;
    private PermissionsAdapter mPermissionsAdapter;

    public PermissionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_permissions, container, false);
        ButterKnife.bind(this, view);
        setPermissions();
        mPermissionsAdapter = new PermissionsAdapter(getActivity(), R.layout.permission_list_item,
                mPermissions);
        mPermissionsView.setAdapter(mPermissionsAdapter);
        return view;
    }

    private void setPermissions(){
        ArrayList<String> permissionsAsString =
                getArguments().getStringArrayList(PERMISSIONS_DATA_EXTRA);
        mPermissions = new ArrayList<>();
        for (String permissionData : permissionsAsString){
            String[] data = permissionData.split(" ");
            mPermissions.add(Permission.create( null,
                                                null,
                                                Integer.valueOf(data[2]),
                                                data[0],
                                                data[3],
                                                data[4]));
        }
    }
}
