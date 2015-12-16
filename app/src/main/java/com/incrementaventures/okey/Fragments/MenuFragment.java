package com.incrementaventures.okey.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class MenuFragment extends Fragment {
    public static final String TAG = "menu_fragment_tag";

    @Bind(R.id.user_name)
    TextView mUserName;
    @Bind(R.id.user_email)
    TextView mUserEmail;
    @Bind(R.id.go_home)
    TextView mGoHome;
    @Bind(R.id.add_new_0key)
    TextView mAddNew0key;
    @Bind(R.id.add_new_door)
    TextView mAddNewDoor;
    @Bind(R.id.add_new_permission)
    TextView mAddNewPermission;
    @Bind(R.id.settings)
    TextView mSettings;


    public interface OnMenuButtonClicked {
        void onMenuClick();
    }

    public MenuFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        hideToolbar();
        ButterKnife.bind(this, view);
        mUserName.setText(User.getLoggedUser().getName());
        mUserEmail.setText(User.getLoggedUser().getEmail());
        return view;
    }

    private void hideToolbar() {
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if ( actionBar != null) {
            actionBar.hide();
        }
    }
}
