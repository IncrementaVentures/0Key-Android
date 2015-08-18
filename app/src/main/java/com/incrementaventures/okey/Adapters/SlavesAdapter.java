package com.incrementaventures.okey.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;

import com.incrementaventures.okey.Models.Slave;

import java.util.List;

public class SlavesAdapter extends ArrayAdapter<Slave> {
    LayoutInflater mLayoutInflater;

    public SlavesAdapter(Context context, int resource, List<Slave> objects){
        super(context, resource, objects);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    // numero de tipos de items
    @Override
    public int getViewTypeCount() {
        return 1;
    }

}
