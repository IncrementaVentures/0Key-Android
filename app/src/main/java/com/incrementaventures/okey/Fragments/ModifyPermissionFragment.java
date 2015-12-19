package com.incrementaventures.okey.Fragments;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.incrementaventures.okey.Models.Master;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.Slave;
import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.Networking.NetworkingUtils;
import com.incrementaventures.okey.R;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class ModifyPermissionFragment extends Fragment {
    public static final String TAG = "modify_permission_fragment_tag";
    public static final String PERMISSION_TYPE = "permission_type";
    public static final String PERMISSION_START_DATE = "permission_start_date";
    public static final String PERMISSION_START_HOUR = "permission_start_hour";
    public static final String PERMISSION_END_DATE = "permission_end_date";
    public static final String PERMISSION_END_HOUR = "permission_end_hour";
    public static final String PERMISSION_KEY = "permission_key";
    public static final String PERMISSION_NEW_SLAVE = "permission_new_slave";
    public static final String PERMISSION_OLD_SLAVE = "permission_old_slave";

    @Bind(R.id.permission_type_layout)
    LinearLayout mPermissionTypeLayout;
    @Bind(R.id.due_date_layout)
    LinearLayout mDueDateLayout;
    @Bind(R.id.due_hour_layout)
    LinearLayout mDueHourLayout;
    @Bind(R.id.start_date_layout)
    LinearLayout mStartDateLayout;
    @Bind(R.id.start_hour_layout)
    LinearLayout mStartHourLayout;
    @Bind(R.id.end_date_new)
    TextView mEndDateView;
    @Bind(R.id.end_hour_new)
    TextView mEndHourView;
    @Bind(R.id.start_date_new)
    TextView mStartDateView;
    @Bind(R.id.start_hour_new)
    TextView mStartHourView;
    @Bind(R.id.ok_button)
    ImageButton mNewPermissionButton;
    @Bind(R.id.permission_type_new)
    TextView mPermissionTypeView;
    @Bind(R.id.permission_slave)
    TextView mSelectedSlaveView;
    @Bind(R.id.selected_master)
    TextView mSelectedMasterView;
    @Bind(R.id.permission_email)
    TextView mPermissionEmailView;
    @Bind(R.id.modify_permission_screen_title)
    TextView mScreenTitle;
    @Bind(R.id.slave_title)
    TextView mSlaveTitle;
    @Bind(R.id.separator2)
    View mSeparator2;
    @Bind(R.id.separator3)
    View mSeparator3;

    private CharSequence mPermissionTypes[];
    private String mEndDate;
    private String mStartDate;
    private String mEndHour;
    private String mStartHour;
    private String mKey;
    private String mPermissionName;
    private Permission mToEditPermission;
    private int mOldSlaveId;
    private Master mSelectedMaster;
    private ArrayList<Master> mMasters;
    private Slave mSelectedSlave;
    private ArrayList<Slave> mSlaves;

    private OnPermissionModifiedListener mPermissionModifiedListener;

    public interface OnPermissionModifiedListener {
        void onCreatePermissionClicked(Permission permission, String userKey);
        void onModifyPermissionClicked(Permission toEditPermission, int oldSlaveId,
                                       String userKey, String doorId);
        void onDeletePermissionClicked(Permission permission, String userKey);
    }

    public ModifyPermissionFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_modify_permission, container, false);
        ButterKnife.bind(this, v);
        hideToolbar();
        getArgumentsData();
        setMasters();
        setSlaves();
        setListeners();
        setUi();
        mPermissionTypes =  new CharSequence[]{
                getResources().getString(R.string.permission_type_admin),
                getResources().getString(R.string.permission_type_permanent),
                getResources().getString(R.string.permission_type_temporal)
        };
        return v;
    }

    private void setMasters() {
        if (mSelectedMaster != null) {
            mSelectedMasterView.setText(mSelectedMaster.getName());
            mSelectedSlave = Slave.getSlave(mSelectedMaster.getId(), getArguments().getInt(Slave.ID));
            if (mSelectedSlave != null) {
                mSelectedSlaveView.setText(mSelectedSlave.getName());
            }
        }
        mMasters = Master.getMasters();
        if (mMasters.size() > 0 && mSelectedMaster != null) {
            mSelectedMaster = mMasters.get(0);
            mSelectedMasterView.setText(mSelectedMaster.getName());
        }

    }

    private void setSlaves() {
        mSlaves = new ArrayList<>(mSelectedMaster.getSlaves());
        if (mSlaves.size() == 0) {
            mSelectedSlave = null;
            mSelectedSlaveView.setText(R.string.no_slaves_found_yet);
            mSelectedSlaveView.setClickable(false);
        } else {
            mSelectedSlave = mSlaves.get(0);
        }
    }

    private void getArgumentsData() {
        mKey = getArguments().getString(Permission.KEY);
        if (mKey != null) {
            mScreenTitle.setText(R.string.edit_permission);
        }
        User user = User.getLoggedUser();
        mSelectedMaster = Master.getMaster(getArguments().getString(Master.ID), user.getId());
        String oldSlave = String.valueOf(getArguments().getInt(PERMISSION_OLD_SLAVE, -1));
        mOldSlaveId = Integer.valueOf(oldSlave);
        mPermissionName = getArguments().getString(Permission.NAME);
        if (getArguments().getString(Permission.OBJECT_ID) != null) {
            mToEditPermission = Permission.getPermission(getArguments().getString(Permission.OBJECT_ID));
            String startDate = mToEditPermission.getStartDate();
            String endDate = mToEditPermission.getEndDate();
            mStartHourView.setText(startDate.substring(startDate.indexOf('T') + 1, startDate.length()));
            mEndHourView.setText(endDate.substring(endDate.indexOf('T') + 1, endDate.length()));
            mStartDateView.setText(Permission.getFormattedDate(startDate));
            mEndDateView.setText(Permission.getFormattedDate(endDate));
            mPermissionTypeView.setText(mToEditPermission.getType());
        } else {
            mStartDateView.setText(Permission.getFormattedDate("2015-01-01T00:01"));
            mEndDateView.setText(Permission.getFormattedDate("2015-01-01T00:01"));
        }
        if (!TextUtils.isEmpty(mPermissionName)) {
            mPermissionEmailView.setText(mPermissionName);
        }
    }

    private void setUi() {
        if (mPermissionTypeView.getText().toString().equals(getResources()
                .getString(R.string.permission_type_temporal))) {
            mDueDateLayout.setVisibility(LinearLayout.VISIBLE);
            mDueHourLayout.setVisibility(LinearLayout.VISIBLE);
            mStartDateLayout.setVisibility(LinearLayout.VISIBLE);
            mStartHourLayout.setVisibility(LinearLayout.VISIBLE);
            mSlaveTitle.setVisibility(TextView.VISIBLE);
            mSelectedSlaveView.setVisibility(TextView.VISIBLE);
            mSeparator2.setVisibility(View.VISIBLE);
            mSeparator3.setVisibility(View.VISIBLE);
        } else if (mPermissionTypeView.getText().toString().equals(getResources()
                .getString(R.string.permission_type_permanent))){
            mDueDateLayout.setVisibility(LinearLayout.GONE);
            mDueHourLayout.setVisibility(LinearLayout.GONE);
            mSlaveTitle.setVisibility(TextView.VISIBLE);
            mSelectedSlaveView.setVisibility(TextView.VISIBLE);
            mSeparator2.setVisibility(View.GONE);
            mSeparator3.setVisibility(View.VISIBLE);
        } else if (mPermissionTypeView.getText().toString().equals(getResources()
                .getString(R.string.permission_type_admin))){
            mSlaveTitle.setVisibility(TextView.GONE);
            mSelectedSlaveView.setVisibility(TextView.GONE);
            mDueDateLayout.setVisibility(LinearLayout.GONE);
            mDueHourLayout.setVisibility(LinearLayout.GONE);
            mSeparator2.setVisibility(View.GONE);
            mSeparator3.setVisibility(View.GONE);
        }
    }

    private void setListeners(){
        mPermissionTypeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.permission_type);
                builder.setItems(mPermissionTypes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPermissionTypeView.setText(mPermissionTypes[which]);
                        setUi();
                    }
                });
                builder.show();
            }
        });

        mDueHourLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerFragment(){
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String hour = String.valueOf(hourOfDay);
                        String min = String.valueOf(minute);
                        if (hour.length() == 1) hour = "0" + hour;
                        if (min.length() == 1) min = "0" + min;
                        mEndHour = hour + ":" + min;
                        mEndHourView.setText(mEndHour);
                    }
                };
                newFragment.show(getActivity().getFragmentManager(), "datePicker");
            }
        });

        mStartHourLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerFragment(){
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String hour = String.valueOf(hourOfDay);
                        String min = String.valueOf(minute);
                        if (hour.length() == 1) hour = "0" + hour;
                        if (min.length() == 1) min = "0" + min;
                        mStartHour = hour + ":" + min;
                        mStartHourView.setText(mStartHour);
                    }
                };
                newFragment.show(getActivity().getFragmentManager(), "datePicker");
            }
        });

        mDueDateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment(){
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        String yearString = String.valueOf(year);
                        String monthString = String.valueOf(month + 1);
                        String dayString = String.valueOf(day);

                        if (monthString.length() == 1 ) monthString = "0" + monthString;
                        if (dayString.length() == 1 ) dayString = "0" + dayString;
                        mEndDate = yearString + "-"
                                + monthString + "-"
                                + dayString;
                        mEndDateView.setText(Permission.getFormattedDate(mEndDate + "T00:00"));
                    }
                };
                newFragment.show(getActivity().getFragmentManager(), "datePicker");
            }
        });

        mStartDateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment(){
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        String yearString = String.valueOf(year);
                        String monthString = String.valueOf(month + 1);
                        String dayString = String.valueOf(day);

                        if (monthString.length() == 1 ) monthString = "0" + monthString;
                        if (dayString.length() == 1 ) dayString = "0" + dayString;
                        mStartDate = yearString + "-"
                                + monthString + "-"
                                + dayString;
                        mStartDateView.setText(Permission.getFormattedDate(mStartDate + "T00:00"));
                    }
                };
                newFragment.show(getActivity().getFragmentManager(), "datePicker");
            }
        });

        mNewPermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkingUtils.isOnline(getContext())) {
                    Snackbar.make(getView(), R.string.no_internet_connection, Snackbar.LENGTH_LONG ).show();
                    return;
                }
                User user = User.getUser(mPermissionEmailView.getText().toString());
                if (user == null) {
                    Snackbar.make(getView(), getString(R.string.invalid_email),
                            Snackbar.LENGTH_LONG ).show();
                    return;
                }
                user.saveLocal();
                int slaveId = 0;
                if (mSelectedSlave != null) {
                    slaveId = mSelectedSlave.getId();
                }
                if (Permission.getType(mPermissionTypeView.getText().toString()) != Permission.ADMIN_PERMISSION
                        && (mSlaves == null || mSlaves.size() == 0)) {
                    Snackbar.make(getView(), R.string.master_without_slaves, Snackbar.LENGTH_LONG ).show();
                    return;
                }
                Permission adminPermission = User.getLoggedUser().getAdminPermission(mSelectedMaster);
                if (adminPermission == null) {
                    Snackbar.make(getView(), R.string.no_permission, Snackbar.LENGTH_LONG).show();
                    return;
                }
                String userKey = adminPermission.getKey();
                String startDate =  Permission.getDefaultDateString(mStartDateView.getText().toString())
                        + "T" + mStartHourView.getText().toString();
                String endDate =  Permission.getDefaultDateString(mEndDateView.getText().toString())
                        + "T" + mEndHourView.getText().toString();
                if (TextUtils.isEmpty(mKey)) {
                    Permission permission = Permission.create(user,
                            mSelectedMaster,
                            Permission.getType(mPermissionTypeView.getText().toString()),
                            "",
                            startDate,
                            endDate,
                            slaveId);

                    mPermissionModifiedListener.onCreatePermissionClicked(permission, userKey);
                // If editing permission
                } else {
                    mToEditPermission.setType(Permission.getType(mPermissionTypeView.getText().toString()));
                    mToEditPermission.setStartDate(startDate);
                    mToEditPermission.setEndDate(endDate);
                    mToEditPermission.setSlaveId(slaveId);
                    mPermissionModifiedListener.onModifyPermissionClicked(mToEditPermission, mOldSlaveId,
                            userKey, mSelectedMaster.getId());
                }

            }
        });

        mSelectedSlaveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.doors);
                final CharSequence[] slavesNames = new CharSequence[mSlaves.size()];
                for (int i = 0; i < mSlaves.size(); i++) {
                    slavesNames[i] = mSlaves.get(i).getName();
                }
                builder.setItems(slavesNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSelectedSlave = mSlaves.get(which);
                        mSelectedSlaveView.setText(slavesNames[which]);
                    }
                });
                builder.show();
            }
        });

        mSelectedMasterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        setSlaves();
                    }
                });
                builder.show();
            }
        });
    }


    private void hideToolbar() {
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if ( actionBar != null) {
            actionBar.hide();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mPermissionModifiedListener = (OnPermissionModifiedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnPermissionModifiedListener");
        }
    }
}
