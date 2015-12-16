package com.incrementaventures.okey.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.incrementaventures.okey.Models.Master;
import com.incrementaventures.okey.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class NameHolderFragment extends Fragment {
    public static String SHOW_ICON = "show_icon";

    @Bind(R.id.master_name)
    TextView mMasterName;
    @Bind(R.id.ic_house)
    ImageView mHouseIcon;

    public NameHolderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_name_holder, container, false);
        ButterKnife.bind(this, view);
        if (!getArguments().getBoolean(SHOW_ICON)) {
            mHouseIcon.setVisibility(ImageView.GONE);
        }
        mMasterName.setText(getArguments().getString(Master.NAME));
        return view;
    }

}
