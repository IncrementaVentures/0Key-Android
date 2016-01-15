package com.incrementaventures.okey.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.incrementaventures.okey.Models.Master;
import com.incrementaventures.okey.Models.Nameable;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.Slave;
import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MasterFragment extends Fragment implements Master.OnNetworkResponseListener {
    public static final String TAG = "master_fragment_tag";

    @Bind(R.id.right_arrow_master)
    ImageButton mRightArrowMaster;
    @Bind(R.id.left_arrow_master)
    ImageButton mLeftArrowMaster;

    @Bind(R.id.master_name_container)
    ViewPager mMasterNameContainer;
    private TextViewPagerAdapter mMasterNameAdapter;

    @Bind(R.id.slave_name_container)
    ViewPager mSlaveNameContainer;
    private TextViewPagerAdapter mSlaveNameAdapter;

    @Bind(R.id.right_arrow_slave)
    ImageButton mRightArrowSlave;
    @Bind(R.id.left_arrow_slave)
    ImageButton mLeftArrowSlave;
    @Bind(R.id.open_button)
    ImageButton mOpenButton;
    @Bind(R.id.bottom_text)
    TextView mBottomText;
    @Bind(R.id.share_image)
    ImageView mShareImage;
    @Bind(R.id.share_virtual_key)
    LinearLayout mBottomLayout;

    @Bind(R.id.manage_virtual_keys)
    LinearLayout mShowPermissionsButton;

    private ArrayList<Master> mMasters;
    private int mSelectedMasterIndex;
    private ArrayList<Slave> mSlaves;
    private ArrayList<Permission> mPermissions;
    private OnMasterFragmentListener mMasterFragmentListener;
    private int mSelectedSlaveIndex;
    private View mView;

    public interface OnMasterFragmentListener {
        void shareKeySelected(Master master);
        void get0keySelected();
        void openWhenCloseSelected(Master master, Slave slave, String permissionKey);
    }

    View.OnClickListener mShareKeyListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mMasterFragmentListener.shareKeySelected(mMasters.get(mSelectedMasterIndex));
        }
    };

    View.OnClickListener mGet0keyListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mMasterFragmentListener.get0keySelected();
        }
    };

    public MasterFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_master, container, false);
            ButterKnife.bind(this, mView);
        } else {
            ButterKnife.bind(this, mView);
            return mView;
        }
        Picasso.with(getContext()).load(R.drawable.app_icon_placeholder).into(mOpenButton);
        setData();
        return mView;
    }

    private void setData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                setPermissions();
                setMasters();
                setSlaves();
                setBottomText();
                setNameableHolderAdapters();

                if(getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setUI();
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mView != null) {
            ViewGroup parentViewGroup = (ViewGroup) mView.getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeAllViews();
            }
        }
    }

    private void setSlaves() {
        if (mMasters == null || mMasters.size() == 0) return;
        mSlaves = new ArrayList<>(mMasters.get(mSelectedMasterIndex).getSlaves());
        if (mSlaves.size() > 0) {
            mSelectedSlaveIndex = 0;
        }
    }

    private void setBottomText() {
        if (mMasters == null || mMasters.size() == 0) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        changeBottomTextToGet0key();

                    }
                });
            }
            return;
        }
        if (User.getLoggedUser().getAdminPermission(mMasters.get(mSelectedMasterIndex)) != null) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBottomLayout.setOnClickListener(mShareKeyListener);
                        changeBottomTextToShareKey();
                    }
                });
            }
        } else {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBottomLayout.setOnClickListener(mGet0keyListener);
                        changeBottomTextToGet0key();
                    }
                });
            }
        }
    }

    private void changeBottomTextToGet0key() {
        mShareImage.setVisibility(ImageView.GONE);
        mBottomText.setText(R.string.get_an_0key_exclamation);
        mBottomText.setTypeface(mBottomText.getTypeface(), Typeface.BOLD);
    }

    private void changeBottomTextToShareKey() {
        mShareImage.setVisibility(ImageView.VISIBLE);
        mBottomText.setText(R.string.share_a_virtual_key);
        mBottomText.setTypeface(mBottomText.getTypeface(), Typeface.NORMAL);
    }

    private void setMasters() {
        setLocalMasters();
    }

    private void setLocalMasters() {
        mMasters = new ArrayList<>();
        Master master;
        for (Permission permission : mPermissions) {
            master = permission.getMaster();
            if (master != null && !mMasters.contains(master)) {
                mMasters.add(master);
            }
        }
        if (mMasters.size() > 0) {
            mSelectedMasterIndex = 0;
        }
    }

    @Override
    public void onSlavesReceived(ArrayList<Slave> slaves) {
        if (slaves == null || slaves.size() == 0 || mMasters == null || mMasters.size() == 0)
            return;
        mSlaves = new ArrayList<>();
        for (Slave slave : slaves) {
            if (mMasters.get(mSelectedMasterIndex).getId().equals(slave.getMasterId())) {
                mSlaves.add(slave);
            }
        }
        if (mSlaveNameAdapter == null || getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setSlaveHolderAdapter();
                mSlaveNameAdapter.notifyDataSetChanged();
                setUI();
            }
        });
    }

    @Override
    public void onMastersReceived(ArrayList<Master> masters) {
        if (masters == null || masters.size() == 0)
            return;
        mMasters = new ArrayList<>();
        mMasters.addAll(masters);
        mSelectedMasterIndex = 0;
        if (mMasterNameAdapter == null || getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setMasterHolderAdapter();
                mMasterNameAdapter.notifyDataSetChanged();
                setUI();
            }
        });
    }

    /**
     * Add the master to the list and set again the master holder adapter.
     * @param master
     */
    @Override
    public void onMasterReceived(Master master) {
        mMasters.add(master);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setNameableHolderAdapters();
            }
        });
    }

    private void setNameableHolderAdapters() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setMasterHolderAdapter();
                    setSlaveHolderAdapter();
                }
            });
        }
    }

    ViewPager.OnPageChangeListener mMasterPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

        @Override
        public void onPageSelected(int position) {
            mSelectedMasterIndex = position;
            setSlaves();
            setSlaveHolderAdapter();
            setBottomText();
            setSlavesUI();
        }

        @Override
        public void onPageScrollStateChanged(int state) { }
    };



    private void setMasterHolderAdapter() {
        mMasterNameAdapter = new TextViewPagerAdapter(getChildFragmentManager(),
                mMasters, true);
        mMasterNameContainer.setAdapter(mMasterNameAdapter);
        mMasterNameContainer.addOnPageChangeListener(mMasterPageChangeListener);
        mMasterNameAdapter.notifyDataSetChanged();
    }

    private void setSlaveHolderAdapter() {
        mSlaveNameAdapter = new TextViewPagerAdapter(getChildFragmentManager(),
                mSlaves, false);
        mSlaveNameContainer.setAdapter(mSlaveNameAdapter);
        mSlaveNameAdapter.notifyDataSetChanged();
    }

    private void setSlavesUI() {
        if (mSlaves == null || mSlaves.size() <= 1) {
            mLeftArrowSlave.setVisibility(ImageButton.GONE);
            mRightArrowSlave.setVisibility(ImageButton.GONE);
        } else {
            mLeftArrowSlave.setVisibility(ImageButton.VISIBLE);
            mRightArrowSlave.setVisibility(ImageButton.VISIBLE);
        }
    }

    private void setMasterUI() {
        if (mMasters == null || mMasters.size() == 0) {
            mLeftArrowMaster.setVisibility(ImageButton.GONE);
            mRightArrowMaster.setVisibility(ImageButton.GONE);
        } else if (mMasters.size() <= 1) {
            mLeftArrowMaster.setVisibility(ImageButton.GONE);
            mRightArrowMaster.setVisibility(ImageButton.GONE);
        } else {
            mLeftArrowMaster.setVisibility(ImageButton.VISIBLE);
            mRightArrowMaster.setVisibility(ImageButton.VISIBLE);
        }
    }

    private void setUI() {
        setSlavesUI();
        setMasterUI();
    }

    @OnClick(R.id.open_button)
    public void openDoorClicked() {
        if (mMasters != null && mMasters.size() > 0 && mSlaves != null && mSlaves.size() > 0) {
            mOpenButton.setMinimumHeight(mOpenButton.getMeasuredHeight());
            if (getContext() != null) {
                Picasso.with(getContext()).load(R.drawable.gray_app_icon_placeholder).into(mOpenButton);
            }
            mOpenButton.setClickable(false);
            User.getLoggedUser().openDoor(mMasters.get(mSelectedMasterIndex),
                    mSlaves.get(mSelectedSlaveIndex));
        }
    }

    @OnClick(R.id.left_arrow_master)
    public void leftArrowMasterClicked() {
        mSelectedMasterIndex =
                moveViewPagerLeft(mMasterNameContainer, mSelectedMasterIndex);
        setSlaves();
        setBottomText();
        setSlaveHolderAdapter();
        setSlavesUI();
    }

    @OnClick(R.id.right_arrow_master)
    public void rightArrowMasterClicked() {
        mSelectedMasterIndex =
                moveViewPagerRight(mMasterNameContainer, mSelectedMasterIndex, mMasters.size());
        setSlaves();
        setBottomText();
        setSlaveHolderAdapter();
        setSlavesUI();
    }

    @OnClick(R.id.left_arrow_slave)
    public void leftArrowSlaveClicked() {
        mSelectedSlaveIndex =
                moveViewPagerLeft(mSlaveNameContainer, mSelectedSlaveIndex);
    }

    @OnClick(R.id.right_arrow_slave)
    public void rightArrowSlaveClicked() {
        mSelectedSlaveIndex =
                moveViewPagerRight(mSlaveNameContainer, mSelectedSlaveIndex, mSlaves.size());
    }

    public void enableOpenButton(int resId) {
        mOpenButton.setClickable(true);
        if (getContext() != null) {
            Picasso.with(getContext()).load(resId).into(mOpenButton);
        }
    }

    public void onSlaveNameClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final EditText edittext = new EditText(getContext());
        builder.setTitle(R.string.edit_slave_name);
        builder.setView(edittext);
        builder.setPositiveButton(R.string.change_name, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) { }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) { }
        });
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = edittext.getText().toString();
                        if (name.length() > 0) {
                            mSlaves.get(mSelectedSlaveIndex).setName(name);
                            NameHolderFragment fragment = (NameHolderFragment)
                                    (mSlaveNameAdapter.getInstanceItem(mSlaveNameContainer.getCurrentItem()));
                            fragment.setText(name);
                        }
                        dialog.dismiss();
                    }
                });
            }
        });
        dialog.show();
    }

    public void onMasterNameClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final EditText edittext = new EditText(getContext());
        builder.setTitle(R.string.edit_master_name);
        builder.setView(edittext);
        builder.setPositiveButton(R.string.change_name, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) { }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) { }
        });
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = edittext.getText().toString();
                        if (name.length() > 0) {
                            mMasters.get(mSelectedMasterIndex).setName(name);
                            NameHolderFragment fragment = (NameHolderFragment)
                                    (mMasterNameAdapter.getInstanceItem(mMasterNameContainer.getCurrentItem()));
                            fragment.setText(name);
                        }
                        dialog.dismiss();
                    }
                });
            }
        });
        dialog.show();
    }

    private int moveViewPagerRight(ViewPager viewPager, int currentIndex, int listLength) {
        currentIndex++;
        if (currentIndex >= listLength) {
            currentIndex = listLength - 1;
        }
        viewPager.setCurrentItem(currentIndex);
        return currentIndex;
    }

    private int moveViewPagerLeft(ViewPager viewPager, int currentIndex) {
        currentIndex--;
        if (currentIndex < 0) {
            currentIndex = 0;
        }
        viewPager.setCurrentItem(currentIndex);
        return currentIndex;
    }

    private void setPermissions() {
        mPermissions = Permission.getPermissions(User.getLoggedUser().getId());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mMasterFragmentListener = (OnMasterFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnMasterFragmentListener");
        }
    }

    public void masterNetworkFound(Master master) {
        if (master == null) return;
        mMasters.add(master);
        if (mMasters.size() == 1) {
            mSelectedMasterIndex = 0;
            setSlaves();
            setBottomText();
            setPermissions();
            setNameableHolderAdapters();
        }
    }

    public Master getSelectedMaster() {
        return (mMasters != null && mMasters.size() > 0) ? mMasters.get(mSelectedMasterIndex) : null;
    }

    public Slave getSelectedSlave() {
        return (mSlaves != null && mSlaves.size() > 0) ? mSlaves.get(mSelectedSlaveIndex) : null;
    }

    public void addMasterInRange(Master foundMaster) {
        for (int i = 0; i < mMasters.size(); i++) {
            Master master = mMasters.get(i);
            if (master.getId().equals(foundMaster.getId())) {
                // TODO: 21-12-2015  Put master name more dark. Soy darks.
                if ((mMasterNameAdapter.getInstanceItem(i)) != null) {
                    ((NameHolderFragment)mMasterNameAdapter.getInstanceItem(i)).showInRange();
                }
            }
        }
    }

    private class TextViewPagerAdapter extends FragmentStatePagerAdapter {
        ArrayList<? extends Nameable> mObjects;
        private boolean mShowIcon;
        HashMap<Integer, Fragment> mFragments;

        public TextViewPagerAdapter(FragmentManager fm, ArrayList<? extends Nameable> objects,
                                    boolean showIcon) {
            super(fm);
            mObjects = objects;
            mShowIcon = showIcon;
            mFragments = new HashMap<>();
        }

        @Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();
            args.putString(Master.NAME, mObjects.get(position).getName());
            args.putBoolean(NameHolderFragment.SHOW_ICON, mShowIcon);
            NameHolderFragment nameHolderFragment = new NameHolderFragment();
            nameHolderFragment.setArguments(args);
            mFragments.put(position, nameHolderFragment);
            return nameHolderFragment;
        }

        @Override
        public int getCount() {
            if (mObjects == null) return 0;
            return mObjects.size();
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        public Fragment getInstanceItem(int position) {
            return mFragments.get(position);
        }
    }
}
