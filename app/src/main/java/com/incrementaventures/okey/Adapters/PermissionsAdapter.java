package com.incrementaventures.okey.Adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.R;

import java.util.ArrayList;

import butterknife.Bind;

public class PermissionsAdapter extends ArrayAdapter<Permission> {

    private LayoutInflater mLayoutInflator;

    public PermissionsAdapter(Context context, int resource, ArrayList<Permission> objects){
        super(context, resource, objects);
        mLayoutInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Permission permission = getItem(position);

        if (view == null){
            view = mLayoutInflator.inflate(R.layout.permission_list_item, parent, false);
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

            }
        });
        TextView slaveView = (TextView) view.findViewById(R.id.permission_slave_edit);
        // TODO: cambiar esto a el real id del esclavo
        slaveView.setText("0");

        return view;
    }
}
