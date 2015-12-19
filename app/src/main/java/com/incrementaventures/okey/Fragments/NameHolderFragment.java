package com.incrementaventures.okey.Fragments;


import android.content.Context;
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
    TextView mName;
    @Bind(R.id.ic_house)
    ImageView mHouseIcon;
    View mView;

    public interface OnTextHolderFragmentClick {
        void onMasterNameClick();
        void onSlaveNameClick();
    }

    OnTextHolderFragmentClick mListener;

    public NameHolderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_name_holder, container, false);
        ButterKnife.bind(this, mView);
        if (!getArguments().getBoolean(SHOW_ICON)) {
            mHouseIcon.setVisibility(ImageView.GONE);
            mName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onSlaveNameClick();
                }
            });
        } else {
            mName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onMasterNameClick();
                }
            });
        }
        mName.setText(getArguments().getString(Master.NAME));
        return mView;
    }

    public void setText(String text) {
        mName.setText(text);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnTextHolderFragmentClick) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass().getSimpleName()
                    + " must implement OnTextHolderFragmentClick");
        }
    }
}
