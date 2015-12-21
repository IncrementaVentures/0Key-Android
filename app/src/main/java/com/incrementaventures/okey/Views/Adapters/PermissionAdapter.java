package com.incrementaventures.okey.Views.Adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.incrementaventures.okey.Fragments.PermissionsFragment;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.Slave;
import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.R;

import java.util.ArrayList;

public class PermissionAdapter extends ArrayAdapter<Permission> {

    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private PermissionsFragment.OnPermissionAdapterListener mListener;

    public PermissionAdapter(Context context, int resource, ArrayList<Permission> objects,
                             PermissionsFragment.OnPermissionAdapterListener listener) {
        super(context, resource, objects);
        mContext = context;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mListener = listener;
    }

    private void setUser(View view, Permission permission) {
        User user = permission.getUser();
        final TextView userNameView = (TextView) view.findViewById(R.id.user_name);
        if (user == null) {
            User.fetchUser(permission.getUserUuid(), new User.OnParseUserGetted() {
                @Override
                public void onUserGetted(User user) {
                    user.saveLocal();
                    userNameView.setText(user.getName());
                }
            });
        } else {
            userNameView.setText(user.getName().substring(0, user.getName().indexOf(' ')));
        }

    }

    private void setPermissionType(View view, Permission permission) {
        TextView permissionTypeView = (TextView) view.findViewById(R.id.permission_type_edit);
        permissionTypeView.setText(permission.getType());
    }

    private void setDates(View view, Permission permission) {
        TextView startDateView = (TextView) view.findViewById(R.id.permission_start_date_edit);
        startDateView.setText(Permission.getFormattedDate(permission.getStartDate()));
        TextView endDateView = (TextView) view.findViewById(R.id.permission_end_date_edit);
        if (permission.getType().equals(mContext.getString(R.string.permission_type_admin)) ||
                permission.getType().equals(mContext.getString(R.string.permission_type_permanent))) {
            endDateView.setVisibility(TextView.GONE);
        } else {
            endDateView.setText(Permission.getFormattedDate(permission.getEndDate()));
            endDateView.setVisibility(TextView.VISIBLE);
        }
    }

    private void setSlave(View view, Permission permission) {
        Slave slave = permission.getSlave();
        TextView slaveView = (TextView) view.findViewById(R.id.permission_slave_edit);
        ImageView slaveImageView = (ImageView) view.findViewById(R.id.slave_image);
        if (slave == null) {
            slaveView.setVisibility(TextView.GONE);
            slaveImageView.setVisibility(ImageView.GONE);
        } else {
            slaveView.setVisibility(TextView.VISIBLE);
            slaveImageView.setVisibility(ImageView.VISIBLE);
            slaveView.setText(String.valueOf(slave.getName()));
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null){
            view = mLayoutInflater.inflate(R.layout.permission_list_item, parent, false);
        }
        final Permission permission = getItem(position);
        setUser(view, permission);
        setPermissionType(view, permission);
        setDates(view, permission);
        setSlave(view, permission);
        return view;
    }
}
