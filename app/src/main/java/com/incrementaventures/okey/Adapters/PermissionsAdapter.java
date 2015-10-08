package com.incrementaventures.okey.Adapters;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.incrementaventures.okey.Activities.DoorActivity;
import com.incrementaventures.okey.Activities.ModifyPermissionActivity;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.R;

import java.util.ArrayList;

public class PermissionsAdapter extends ArrayAdapter<Permission> {

    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private DoorActivity mDoorActivity;

    public PermissionsAdapter(Context context, int resource, ArrayList<Permission> objects) {
        super(context, resource, objects);
        mContext = context;
        mDoorActivity = (DoorActivity) context;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        final Permission permission = getItem(position);

        if (view == null){
            view = mLayoutInflater.inflate(R.layout.permission_list_item, parent, false);
        }

        TextView permissionTypeView = (TextView) view.findViewById(R.id.permission_type_edit);
        permissionTypeView.setText(permission.getType());

        TextView startDateView = (TextView) view.findViewById(R.id.permission_start_date_edit);
        startDateView.setText(permission.getStartDate());

        TextView endDateView = (TextView) view.findViewById(R.id.permission_end_date_edit);
        endDateView.setText(permission.getEndDate());

        Button deleteButton = (Button) view.findViewById(R.id.delete_permission_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        Button editButton = (Button) view.findViewById(R.id.edit_permission_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ModifyPermissionActivity.class);
                intent.putExtra(DoorActivity.REQUEST_CODE, DoorActivity.EDIT_PERMISSION_REQUEST);
                intent.putExtra(Permission.KEY, permission.getKey());
                intent.putExtra(
                        ModifyPermissionActivity.PERMISSION_OLD_SLAVE, permission.getSlaveId());
                mDoorActivity.startActivityForResult(intent, DoorActivity.EDIT_PERMISSION_REQUEST);
            }
        });
        TextView slaveView = (TextView) view.findViewById(R.id.permission_slave_edit);

        slaveView.setText(permission.getSlaveId());

        return view;
    }
}
