package com.incrementaventures.okey.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.incrementaventures.okey.Models.Master;
import com.incrementaventures.okey.R;

import java.util.ArrayList;


public class MastersAdapter extends ArrayAdapter<Master> {

    private LayoutInflater mLayoutInflator;

    public MastersAdapter(Context context, int resource, ArrayList<Master> objects){
        super(context, resource, objects);
        mLayoutInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Master master = getItem(position);

        if (view == null){
            view = mLayoutInflator.inflate(R.layout.master_list_item, parent, false);
        }

        TextView doorName = (TextView) view.findViewById(R.id.door_name_list_item);
        doorName.setText(master.getName());

        TextView doorDescription = (TextView) view.findViewById(R.id.door_description_list_item);
        doorDescription.setText(master.getDescription());
        if (master.getDescription() == null || master.getDescription().equals("")){
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
