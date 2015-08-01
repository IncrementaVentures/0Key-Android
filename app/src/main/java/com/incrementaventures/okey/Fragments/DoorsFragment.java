package com.incrementaventures.okey.Fragments;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.incrementaventures.okey.Activities.DoorConfigurationActivity;
import com.incrementaventures.okey.Activities.MainActivity;
import com.incrementaventures.okey.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DoorsFragment extends Fragment {

    @Bind(R.id.new_door_button)
    Button newDoorButton;

    public DoorsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_doors, container, false);
        ButterKnife.bind(this, v);

        newDoorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DoorConfigurationActivity.class);
                startActivityForResult(intent, MainActivity.FIRST_CONFIG);
                // TODO: abrir pantalla para configurar puerta
                // TODO: IMPORTANTE. Cuando usuario random se conecte primera vez con puerta, agregarla a BD
            }
        });

        return v;
    }


}
