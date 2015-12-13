package com.incrementaventures.okey.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.incrementaventures.okey.Activities.MainActivity;
import com.incrementaventures.okey.Models.Master;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.Slave;
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
    @Bind(R.id.permission_name)
    TextView mPermissionNameView;

    private CharSequence mPermissionTypes[];
    private String mEndDate;
    private String mStartDate;
    private String mEndHour;
    private String mStartHour;
    private String mKey;
    private String mPermissionName;
    private int mOldSlaveId;
    private Master mSelectedMaster;
    private ArrayList<Master> mMasters;
    private Slave mSelectedSlave;
    private ArrayList<Slave> mSlaves;

    public ModifyPermissionFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_new_permission, container, false);
        ButterKnife.bind(this, v);
        getArgumentsData();
        setMasters();
        setSlaves();
        setListeners();
        mPermissionTypes =  new CharSequence[]{
                getResources().getString(R.string.permission_type_admin),
                getResources().getString(R.string.permission_type_permanent),
                getResources().getString(R.string.permission_type_temporal)
        };
        return v;
    }

    private void setMasters() {
        mMasters = Master.getMasters();
        if (mSelectedMaster == null && mMasters.size() > 0) {
            mSelectedMaster = mMasters.get(0);
        }
    }

    private void setSlaves() {
        mSlaves = new ArrayList<>(mSelectedMaster.getSlaves());
        if (mSelectedSlave == null && mSlaves.size() > 0) {
            mSelectedSlave = mSlaves.get(0);
        }
        else {
            mSelectedSlaveView.setText(R.string.no_slaves_found_yet);
        }
    }

    private void getArgumentsData() {
        mKey = getArguments().getString(Permission.KEY);
        mSelectedMaster = Master.getMaster(getArguments().getString(Master.UUID));
        if (mSelectedMaster != null) {
            mSelectedMasterView.setText(mSelectedMaster.getName());
        }
        mSelectedSlave = Slave.getSlave(getArguments().getString(Slave.UUID));
        if (mSelectedSlave != null) {
            mSelectedSlaveView.setText(mSelectedSlave.getName());
        }
        String oldSlave = String.valueOf(getArguments().getInt(PERMISSION_OLD_SLAVE, -1));
        mOldSlaveId = Integer.valueOf(oldSlave);
        mPermissionName = getArguments().getString(Permission.NAME);
        if (!TextUtils.isEmpty(mPermissionName)) {
            mPermissionNameView.setText(mPermissionName);
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
                        if (mPermissionTypes[which].equals(getResources()
                                .getString(R.string.permission_type_temporal))) {
                            mDueDateLayout.setVisibility(LinearLayout.VISIBLE);
                            mDueHourLayout.setVisibility(LinearLayout.VISIBLE);
                            mStartDateLayout.setVisibility(LinearLayout.VISIBLE);
                            mStartHourLayout.setVisibility(LinearLayout.VISIBLE);
                        } else{
                            mDueDateLayout.setVisibility(LinearLayout.GONE);
                            mDueHourLayout.setVisibility(LinearLayout.GONE);
                        }
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
                        mEndDateView.setText(mEndDate);
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
                        mStartDateView.setText(mStartDate);
                    }
                };
                newFragment.show(getActivity().getFragmentManager(), "datePicker");
            }
        });

        mNewPermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra(PERMISSION_TYPE, mPermissionTypeView.getText().toString());
                data.putExtra(PERMISSION_START_DATE, mStartDateView.getText().toString());
                data.putExtra(PERMISSION_START_HOUR, mStartHourView.getText().toString());
                data.putExtra(PERMISSION_END_DATE, mEndDateView.getText().toString());
                data.putExtra(PERMISSION_END_HOUR, mEndHourView.getText().toString());
                data.putExtra(PERMISSION_KEY, mKey);
                data.putExtra(PERMISSION_OLD_SLAVE, mOldSlaveId);
                data.putExtra(PERMISSION_NEW_SLAVE, mSelectedSlaveView.getText().toString());
                data.putExtra(MainActivity.SCANNED_DOOR_EXTRA, false);
                getActivity().setResult(Activity.RESULT_OK, data);
                getActivity().finish();
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
                builder.setTitle(R.string.doors);
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
        });
    }

}
