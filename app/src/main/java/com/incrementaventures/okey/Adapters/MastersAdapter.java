package com.incrementaventures.okey.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.incrementaventures.okey.Models.Master;
import com.incrementaventures.okey.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class MastersAdapter extends ArrayAdapter<Master> {

    private LayoutInflater mLayoutInflater;

    public MastersAdapter(Context context, int resource, ArrayList<Master> objects){
        super(context, resource, objects);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Master master = getItem(position);

        if (view == null){
            view = mLayoutInflater.inflate(R.layout.master_list_item, parent, false);
            ImageView imageView = (ImageView) view.findViewById(R.id.master_image);
            //Glide.with(mLayoutInflater.getContext()).load(R.drawable.house).into(imageView);
            Picasso.with(mLayoutInflater.getContext()).load(R.drawable.house).into(imageView);
        }
        TextView doorName = (TextView) view.findViewById(R.id.door_name_list_item);
        doorName.setText(master.getDescription());
        return view;
    }

    // numero de tipos de items
    @Override
    public int getViewTypeCount() {
        return 1;
    }

}
