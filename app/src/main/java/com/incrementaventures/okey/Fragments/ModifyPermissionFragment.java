package com.incrementaventures.okey.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.incrementaventures.okey.Activities.MainActivity;
import com.incrementaventures.okey.Activities.ModifyPermissionActivity;
import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class ModifyPermissionFragment extends Fragment {

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

    @Bind(R.id.new_permission_button)
    Button mNewPermissionButton;

    @Bind(R.id.permission_type_new)
    TextView mPermissionTypeView;

    @Bind(R.id.permission_slave)
    EditText mPermissionSlave;

    private CharSequence mPermissionTypes[];
    private String mEndDate;
    private String mStartDate;
    private String mEndHour;
    private String mStartHour;
    private String mKey;
    private int mOldSlaveId;

    public ModifyPermissionFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_new_permission, container, false);
        ButterKnife.bind(this, v);
        mKey = getActivity().getIntent().getStringExtra(Permission.KEY);
        mOldSlaveId = Integer.valueOf(getActivity().getIntent()
                .getStringExtra(ModifyPermissionActivity.PERMISSION_OLD_SLAVE));
        setListeners();

        mPermissionTypes =  new CharSequence[]{
                getResources().getString(R.string.permission_type_admin),
                getResources().getString(R.string.permission_type_permanent),
                getResources().getString(R.string.permission_type_temporal)
        };
        return v;
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
                        if (mPermissionTypes[which].equals(getResources().getString(R.string.permission_type_temporal))) {
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
                data.putExtra(ModifyPermissionActivity.PERMISSION_TYPE, mPermissionTypeView.getText().toString());
                data.putExtra(ModifyPermissionActivity.PERMISSION_START_DATE, mStartDateView.getText().toString());
                data.putExtra(ModifyPermissionActivity.PERMISSION_START_HOUR, mStartHourView.getText().toString());
                data.putExtra(ModifyPermissionActivity.PERMISSION_END_DATE, mEndDateView.getText().toString());
                data.putExtra(ModifyPermissionActivity.PERMISSION_END_HOUR, mEndHourView.getText().toString());
                data.putExtra(ModifyPermissionActivity.PERMISSION_KEY, mKey);
                data.putExtra(ModifyPermissionActivity.PERMISSION_OLD_SLAVE, mOldSlaveId);
                data.putExtra(ModifyPermissionActivity.PERMISSION_NEW_SLAVE, mPermissionSlave.getText().toString());
                data.putExtra(MainActivity.SCANNED_DOOR_EXTRA, false);
                getActivity().setResult(Activity.RESULT_OK, data);
                getActivity().finish();
            }
        });
    }

}
