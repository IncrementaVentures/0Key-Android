package com.incrementaventures.okey.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.incrementaventures.okey.Models.Door;
import com.incrementaventures.okey.R;

import java.util.ArrayList;


public class DoorsAdapter extends ArrayAdapter<Door> {

    private LayoutInflater mLayoutInflator;

    public DoorsAdapter(Context context, int resource, ArrayList<Door> objects){
        super(context, resource, objects);
        mLayoutInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Door door = getItem(position);

        if (view == null){
            view = mLayoutInflator.inflate(R.layout.door_list_item, parent, false);
        }

        TextView doorName = (TextView) view.findViewById(R.id.door_name_list_item);
        doorName.setText(door.getName());

        TextView doorDescription = (TextView) view.findViewById(R.id.door_description_list_item);
        doorDescription.setText(door.getDescription());
        if (door.getDescription() == null || door.getDescription().equals("")){
            doorDescription.setText("No description");
        }
        return view;
    }

    // numero de tipos de items
    @Override
    public int getViewTypeCount() {
        return 1;
    }

}
