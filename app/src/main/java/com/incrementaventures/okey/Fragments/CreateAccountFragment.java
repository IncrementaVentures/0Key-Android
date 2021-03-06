package com.incrementaventures.okey.Fragments;


import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.incrementaventures.okey.Models.Permission;
import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.Networking.NetworkingUtils;
import com.incrementaventures.okey.R;
import com.parse.ParseException;
import com.parse.ParseUser;

import butterknife.Bind;
import butterknife.ButterKnife;


public class CreateAccountFragment extends Fragment implements User.OnParseUserLoginResponse {

    @Bind(R.id.create_account_email)
    EditText mEmail;
    @Bind(R.id.create_account_name)
    EditText mName;
    @Bind(R.id.create_account_password)
    EditText mPassword;
    @Bind(R.id.create_account_phone)
    EditText mPhone;
    @Bind(R.id.create_account_button)
    Button mCreateButton;
    @Bind(R.id.create_account_birthday)
    TextView mBirthday;
    @Bind(R.id.create_account_radio_group)
    RadioGroup mRadioGroupSex;

    View view;
    CreateAccountFragment thisFragment = this;

    public CreateAccountFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_create_account, container, false);
        ButterKnife.bind(this, view);
        setUI();
        setListeners();
        return view;
    }

    private void setUI() {
        mCreateButton.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.turquoise),
                PorterDuff.Mode.MULTIPLY);
        mEmail.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.white),
                PorterDuff.Mode.SRC_ATOP);
        mPassword.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.white),
                PorterDuff.Mode.SRC_ATOP);
        mBirthday.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.white),
                PorterDuff.Mode.SRC_ATOP);
        mName.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.white),
                PorterDuff.Mode.SRC_ATOP);
        mPhone.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.white),
                PorterDuff.Mode.SRC_ATOP);
    }


    private void setListeners(){
        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mName.getText().toString();
                String password = mPassword.getText().toString();
                String email = mEmail.getText().toString();
                String phone = mPhone.getText().toString();
                String birthday = Permission.getDefaultDateString(mBirthday.getText().toString())
                        + "T00:01";
                int selectedId = mRadioGroupSex.getCheckedRadioButtonId();
                String sex;
                switch (selectedId) {
                    case R.id.create_account_male:
                        sex = User.MALE;
                        break;
                    case R.id.create_account_female:
                        sex = User.FEMALE;
                        break;
                    default:
                        sex = User.MALE;
                }
                if (NetworkingUtils.isOnline(getContext())) {
                    User.signUp(thisFragment, name, password, email, phone, sex, birthday);
                    Snackbar.make(getView(), R.string.creating_account, Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(getView(), R.string.no_internet_connection, Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        mBirthday.setOnClickListener(new View.OnClickListener() {
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
                        String birthday = yearString + "-"
                                + monthString + "-"
                                + dayString;
                        mBirthday.setText(Permission.getFormattedDate(birthday + "T00:01"));
                    }
                };
                newFragment.show(getActivity().getFragmentManager(), "datePicker");
            }
        });
    }

    @Override
    public void userSignedUp() {
        Snackbar.make(getActivity().findViewById(R.id.auth_container), R.string.verification_email_sent,
                Snackbar.LENGTH_LONG).show();
        // Create new fragment and transaction
        LoginFragment newFragment = new LoginFragment();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.auth_container, newFragment);
        transaction.addToBackStack(null);
        // Commit the transaction
        transaction.commit();
    }

    @Override
    public void userLoggedIn(ParseUser parseUser) {

    }

    @Override
    public void authError(ParseException e) {
        if (getView() == null) return;
        Snackbar.make(getView(),
                e.getMessage().substring(0, 1).toUpperCase() + e.getMessage().substring(1),
                Snackbar.LENGTH_SHORT).show();
    }
}
