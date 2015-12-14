package com.incrementaventures.okey.Fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.incrementaventures.okey.R;

import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewDoorFragment extends Fragment {


    public NewDoorFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_door, container, false);
        ButterKnife.bind(this, view);
        return view;
    }


}
