package com.incrementaventures.okey.Fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.incrementaventures.okey.Activities.MainActivity;
import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.R;
import com.parse.ParseException;

import butterknife.Bind;
import butterknife.ButterKnife;


public class CreateAccountFragment extends Fragment implements User.OnParseUserResponse{

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
    EditText mBirthday;
    @Bind(R.id.create_account_radio_group)
    RadioGroup mRadioGroupSex;

    View view;
    CreateAccountFragment thisFragment = this;

    private ProgressDialog mProgressDialog;

    public CreateAccountFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_create_account, container, false);
        ButterKnife.bind(this, view);
        setListeners();
        return view;
    }


    private void setListeners(){
        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mName.getText().toString();
                String password = mPassword.getText().toString();
                String email = mEmail.getText().toString();
                String phone = mPhone.getText().toString();
                String birthday = mBirthday.getText().toString();
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
                User.signUp(thisFragment, name, password, email, phone, sex, birthday);
                mProgressDialog = ProgressDialog.show(getActivity(), null, getResources().getString(R.string.logging));

            }
        });
    }

    @Override
    public void userSignedUp() {
        if (mProgressDialog != null) mProgressDialog.dismiss();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void userLoggedIn() {

    }

    @Override
    public void authError(ParseException e) {
        if (mProgressDialog != null) mProgressDialog.dismiss();
        Toast.makeText(getActivity().getApplicationContext(), R.string.auth_error, Toast.LENGTH_SHORT).show();
    }
}
