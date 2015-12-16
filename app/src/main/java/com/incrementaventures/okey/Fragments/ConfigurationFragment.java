package com.incrementaventures.okey.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.incrementaventures.okey.Models.Master;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ConfigurationFragment extends Fragment {
    @Bind(R.id.ok_button)
    ImageButton mOkButton;
    @Bind(R.id.cancel_button)
    ImageButton mCancelButton;
    @Bind(R.id.new_0key_name)
    EditText mName;
    @Bind(R.id.new_0key_factory_password)
    EditText mFactoryPassword;
    @Bind(R.id.new_0key_location)
    EditText mLocation;
    @Bind(R.id.new_0key_new_password)
    EditText mNewPassword;

    String mMasterId;

    OnMasterConfigurationListener mListener;

    public interface OnMasterConfigurationListener {
        void onConfigureMasterClick(String permissionKey, String defaultKey, Master master);
    }

    public ConfigurationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_configuration, container, false);
        ButterKnife.bind(this, view);
        mMasterId = getArguments().getString(Master.ID);
        return view;
    }

    @OnClick(R.id.ok_button)
    public void onOkButtonClick() {
        Master master  = Master.create(mMasterId, mName.getText().toString(),
                User.getLoggedUser().getUUID());
        mListener.onConfigureMasterClick(mNewPassword.getText().toString(),
                mFactoryPassword.getText().toString(), master);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnMasterConfigurationListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnMasterConfigurationListener");
        }
    }
}
