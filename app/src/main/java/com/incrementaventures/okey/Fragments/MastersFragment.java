package com.incrementaventures.okey.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.incrementaventures.okey.Models.Master;
import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class MastersFragment extends Fragment implements ToolbarFragment {
    public final static String TAG = "masters_fragment";

    @Bind(R.id.masters_view)
    ListView mMastersView;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    private ArrayList<Master> mMasters;
    private ArrayAdapter<Master> mAdapter;
    private OnMastersFragmentListener mListener;

    public interface OnMastersFragmentListener {
        void onShowPermissions(Master master);
        void onBackPressed();
    }

    public MastersFragment() {
        // Required empty public constructor
    }

    @Override
    public int getMenuResource() {
        return R.menu.menu_masters;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_masters, container, false);
        ButterKnife.bind(this, view);
        setToolbar();
        mMasters = Master.getMasters();
        mAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, mMasters);
        mMastersView.setAdapter(mAdapter);
        mMastersView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openPermissionsFragment(mMasters.get(position));
            }
        });
        return view;
    }

    private void setToolbar() {
        if (getActivity() == null) return;
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar()
                    .setDisplayShowHomeEnabled(true);
        }
        mToolbar.setTitle(User.getLoggedUser().getName());
        mToolbar.findViewById(R.id.logo_toolbar).setVisibility(ImageView.GONE);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_36dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onBackPressed();
            }
        });
    }

    private void openPermissionsFragment(Master master) {
        mListener.onShowPermissions(master);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnMastersFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnMastersFragmentListener");
        }
    }
}
