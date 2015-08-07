package com.incrementaventures.okey.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.incrementaventures.okey.Models.Slave;
import com.incrementaventures.okey.R;

import java.util.List;

public class SlavesAdapter extends ArrayAdapter<Slave> {
    LayoutInflater mLayoutInflater;

    public SlavesAdapter(Context context, int resource, List<Slave> objects){
        super(context, resource, objects);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Slave slave = getItem(position);

        if (view == null){
            view = mLayoutInflater.inflate(R.layout.slave_list_item, parent, false);
        }

        TextView doorName = (TextView) view.findViewById(R.id.slave_name);
        doorName.setText(slave.getName());
        return view;
    }

    // numero de tipos de items
    @Override
    public int getViewTypeCount() {
        return 1;
    }

}
