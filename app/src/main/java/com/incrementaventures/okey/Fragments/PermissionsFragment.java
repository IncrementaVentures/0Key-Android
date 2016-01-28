package com.incrementaventures.okey.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
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
public class PermissionsFragment extends Fragment implements ToolbarFragment {
    public static final String PERMISSIONS_DATA_EXTRA = "permissions_data_extra";
    public static final String TAG = "permissions_fragment_tag";

    @Bind(R.id.permissions_list_view)
    ListView mPermissionsView;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    private ArrayList<Permission> mPermissions;
    private PermissionAdapter mPermissionsAdapter;
    private Master mMaster;
    private OnPermissionAdapterListener mListener;
    private Menu mOptionsMenu;

    public interface OnPermissionAdapterListener {
        void onModifyPermissionAdapterClicked(Permission permission);
        void onDeletePermissionAdapterClicked(Permission permission);
        void onBackPressed();
        void addNewPermissionClicked();
        void checkNewPermissions();
        void showAddPermissionButton();
        void showRefreshButton();
    }

    public PermissionsFragment() {
        // Required empty public constructor
    }

    @Override
    public int getMenuResource() {
        if (User.getLoggedUser().getAdminPermission(mMaster) != null) {
            return R.menu.menu_permissions_admin;
        } else {
            return R.menu.menu_permissions_no_admin;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_permissions, container, false);
        ButterKnife.bind(this, view);
        setPermissions();
        setToolbar();
        if (getContext() == null) return view;
        mPermissionsAdapter = new PermissionAdapter(getContext(), R.layout.permission_list_item,
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

    private void setToolbar() {
        if (getActivity() == null) return;
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
        }
        mToolbar.setTitle(mMaster.getName());
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_36dp);
        mToolbar.findViewById(R.id.logo_toolbar).setVisibility(ImageView.GONE);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onBackPressed();
            }
        });
        //if (User.getLoggedUser().getAdminPermission(mMaster) != null) {
        //    showAddPermissionButton();
        //}
        /// showRefreshPermissionsButton();
    }

    private void showAddPermissionButton() {
        mListener.showAddPermissionButton();
        //mOptionsMenu.findItem(R.id.action_add_permission).setVisible(true);
    }

    private void showRefreshPermissionsButton() {
        mListener.showRefreshButton();
        //mOptionsMenu.findItem(R.id.action_refresh_data).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        if (id == android.R.id.home) {
            //mListener.onBackPressed();
            //return true;
        } else if (id == R.id.action_add_permission) {
            //mListener.addNewPermissionClicked();
        } else if (id == R.id.action_refresh_data) {
            //if (getView() != null) {
            //    Snackbar.make(getView(), R.string.syncing_data, Snackbar.LENGTH_LONG).show();
            //}
            //mListener.checkNewPermissions();
        }
        return super.onOptionsItemSelected(item);
    }


    private void setPermissions() {
        if (getActivity() == null ||((AppCompatActivity)getActivity()).getSupportActionBar() == null)
            return;
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (getArguments() == null ) {
            actionBar.setTitle(User.getLoggedUser().getName());
            mPermissions = new ArrayList<>();
            ArrayList<Master> masters = Master.getMasters();
            if (masters == null || masters.size() == 0)
                return;
            for (Master master : masters) {
                mPermissions.addAll(User.getLoggedUser().getInterestedPermissions(master));
            }
        } else {
            mMaster = Master.getMaster(getArguments().getString(Master.ID), User.getLoggedUser().getId());
            if (mMaster == null) {
                return;
            }
            actionBar.setTitle(mMaster.getName());
            mPermissions = User.getLoggedUser().getInterestedPermissions(mMaster);
        }
        mPermissions = Permission.orderByUserName(mPermissions);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!NetworkingUtils.isOnline(getActivity())) {
            Snackbar.make(getView(), R.string.no_internet_connection, Snackbar.LENGTH_LONG).show();
        }
    }

    public void onPermissionsReceived(ArrayList<Permission> permissions, final boolean newPermissions) {
        if (mPermissions == null) {
            mPermissions = new ArrayList<>();
        }
        for (int i = 0; i < permissions.size(); i++) {
            if (mPermissions.contains(permissions.get(i))) {
                mPermissions.remove(mPermissions.indexOf(permissions.get(i)));
                mPermissions.add(i, permissions.get(i));
            } else {
                mPermissions.add(permissions.get(i));
            }
        }
        mPermissions = Permission.orderByUserName(mPermissions);

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
