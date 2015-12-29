package com.incrementaventures.okey.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.Networking.NetworkingUtils;
import com.incrementaventures.okey.Views.Adapters.PermissionAdapter;
import com.incrementaventures.okey.Models.Master;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.R;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class PermissionsFragment extends Fragment {
    public static final String PERMISSIONS_DATA_EXTRA = "permissions_data_extra";
    public static final String TAG = "permissions_fragment_tag";

    @Bind(R.id.permissions_list_view)
    ListView mPermissionsView;

    private ArrayList<Permission> mPermissions;
    private PermissionAdapter mPermissionsAdapter;
    private Master mMaster;
    private OnPermissionAdapterListener mListener;

    public interface OnPermissionAdapterListener {
        void onModifyPermissionAdapterClicked(Permission permission);
        void onDeletePermissionAdapterClicked(Permission permission);
    }

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
        mPermissionsAdapter = new PermissionAdapter(getActivity(), R.layout.permission_list_item,
            mPermissions, mListener);
        mPermissionsView.setAdapter(mPermissionsAdapter);
        mPermissionsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.onModifyPermissionAdapterClicked(mPermissions.get(position));
            }
        });
        return view;
    }

    private void setPermissions() {
        mMaster = Master.getMaster(getArguments().getString(Master.ID), User.getLoggedUser().getId());
        if (mMaster == null) return;
        mPermissions = mMaster.getAllPermissions();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!NetworkingUtils.isOnline(getActivity())) {
            Snackbar.make(getView(), R.string.no_internet_connection, Snackbar.LENGTH_LONG).show();
        }
    }

    public void onPermissionsReceived(ArrayList<Permission> permissions, final boolean newPermissions) {
        mPermissions.clear();
        for (Permission permission : permissions) {
            if (permission.getMasterId().equals(mMaster.getId())) {
                mPermissions.add(permission);
            }
        }
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (newPermissions && getView() != null) {
                        Snackbar.make(getView(), R.string.new_virtual_keys, Snackbar.LENGTH_LONG).show();
                    }
                    mPermissionsAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnPermissionAdapterListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnPermissionAdapterListener");
        }
    }
}
