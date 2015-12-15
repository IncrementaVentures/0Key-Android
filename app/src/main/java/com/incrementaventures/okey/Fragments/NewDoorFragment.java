package com.incrementaventures.okey.Fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.incrementaventures.okey.Models.Master;
import com.incrementaventures.okey.Models.Slave;
import com.incrementaventures.okey.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewDoorFragment extends Fragment {
    @Bind(R.id.selected_master)
    TextView mSelectedMasterView;

    ArrayList<Master> mMasters;
    Master mSelectedMaster;
    OnPairRequestedListener mListener;

    public interface OnPairRequestedListener {
        void onPairRequested(Master master, int slaveId);
    }

    public NewDoorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_door, container, false);
        ButterKnife.bind(this, view);
        mMasters = Master.getMasters();
        if (mMasters != null && mMasters.size() > 0) {
            mSelectedMasterView.setText(mMasters.get(0).getName());
            mSelectedMaster = mMasters.get(0);
        }
        return view;
    }

    @OnClick(R.id.selected_master)
    public void onSelectedMasterViewClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.masters);
        final CharSequence[] mastersNames = new CharSequence[mMasters.size()];
        for (int i = 0; i < mMasters.size(); i++) {
            mastersNames[i] = mMasters.get(i).getName();
        }
        builder.setItems(mastersNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSelectedMaster = mMasters.get(which);
                mSelectedMasterView.setText(mastersNames[which]);
            }
        });
        builder.show();
    }

    @OnClick(R.id.ok_button)
    public void onOkButtonClick() {
        if (mSelectedMaster == null) {
            Snackbar.make(getView(), R.string.no_masters_yet, Snackbar.LENGTH_LONG).show();
            return;
        }
        List<Slave> slaves = mSelectedMaster.getSlaves();
        int newSlave = 1;
        if (slaves != null) {
            newSlave = slaves.size() + 1;
        }
        Snackbar.make(getView(), R.string.pairing_slave, Snackbar.LENGTH_LONG).show();
        mListener.onPairRequested(mSelectedMaster, newSlave);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnPairRequestedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass().getName() +
                    " must implement OnPairRequestedListener");
        }
    }
}
