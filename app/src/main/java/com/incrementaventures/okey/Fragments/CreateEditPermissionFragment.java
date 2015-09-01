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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.incrementaventures.okey.Activities.MainActivity;
import com.incrementaventures.okey.Activities.CreateEditPermissionActivity;
import com.incrementaventures.okey.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class CreateEditPermissionFragment extends Fragment {

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

    private CharSequence mPermissionTypes[];
    private String mEndDate;
    private String mStartDate;
    private String mEndHour;
    private String mStartHour;


    public CreateEditPermissionFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_new_permission, container, false);
        ButterKnife.bind(this, v);
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
                        mEndDate = String.valueOf(year) + "-"
                                + "0" + String.valueOf(month)
                                + "-" + "0" + String.valueOf(day);
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
                        mStartDate = String.valueOf(year) + "-"
                                + "0" + String.valueOf(month)
                                + "-" + "0" + String.valueOf(day);
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
                data.putExtra(CreateEditPermissionActivity.PERMISSION_TYPE, mPermissionTypeView.getText().toString());
                data.putExtra(CreateEditPermissionActivity.PERMISSION_START_DATE, mStartDateView.getText().toString());
                data.putExtra(CreateEditPermissionActivity.PERMISSION_START_HOUR, mStartHourView.getText().toString());
                data.putExtra(CreateEditPermissionActivity.PERMISSION_END_DATE, mEndDateView.getText().toString());
                data.putExtra(CreateEditPermissionActivity.PERMISSION_END_HOUR, mEndHourView.getText().toString());
                data.putExtra(MainActivity.SCANNED_DOOR_EXTRA, false);
                getActivity().setResult(Activity.RESULT_OK, data);
                getActivity().finish();
            }
        });
    }

}
