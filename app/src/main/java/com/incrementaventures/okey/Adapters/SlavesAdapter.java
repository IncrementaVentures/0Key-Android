package com.incrementaventures.okey.Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.incrementaventures.okey.Fragments.DoorFragment;
import com.incrementaventures.okey.Models.Master;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.Slave;
import com.incrementaventures.okey.R;

import java.util.List;

public class SlavesAdapter extends ArrayAdapter<Slave> implements PopupMenu.OnMenuItemClickListener {
    LayoutInflater mLayoutInflater;
    private Slave mSelectedSlave;
    private Master mMaster;
    private Permission mPermission;
    private DoorFragment.OnSlaveSelectedListener mListener;

    public SlavesAdapter(Context context, int resource, List<Slave> objects, Master master){
        super(context, resource, objects);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMaster = master;
        mPermission = mMaster.getPermission();
        mListener = (DoorFragment.OnSlaveSelectedListener) context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        final Slave slave = getItem(position);

        if (view == null){
            view = mLayoutInflater.inflate(R.layout.slave_list_item, parent, false);
        }

        TextView slaveNameView = (TextView) view.findViewById(R.id.slave_name);
        slaveNameView.setText(slave.getName());

        ImageButton moreOptionsButton = (ImageButton) view.findViewById(R.id.more_options_button);
        moreOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedSlave = slave;
                PopupMenu popup = new PopupMenu(getContext(), v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_more_options_slave, popup.getMenu());
                popup.setOnMenuItemClickListener(SlavesAdapter.this);
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

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        mPermission = mMaster.getPermission();
        switch (item.getItemId()) {
            case R.id.action_read_my_permission:
                mListener.readMyPermissionSelected(mMaster, mSelectedSlave, mPermission.getKey());
                return true;
            case R.id.action_read_all_permissions:
                mListener.readAllPermissionsSelected(mMaster, mSelectedSlave, mPermission.getKey());
                return true;
            case R.id.action_open_when_close:
                mListener.openWhenCloseSelected(mMaster, mSelectedSlave, mPermission.getKey());
                return true;
            case R.id.action_edit_slave:
                Toast.makeText(getContext(), "Not implemented yet", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }
    }

    // numero de tipos de items
    @Override
    public int getViewTypeCount() {
        return 1;
    }

}
