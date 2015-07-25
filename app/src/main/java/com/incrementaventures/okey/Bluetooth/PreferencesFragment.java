package com.incrementaventures.okey.Bluetooth;


import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.incrementaventures.okey.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreferencesFragment extends PreferenceFragment {


    public PreferencesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

    }




}
