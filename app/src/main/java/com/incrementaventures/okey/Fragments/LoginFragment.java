package com.incrementaventures.okey.Fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.incrementaventures.okey.Activities.MainActivity;
import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.R;
import com.parse.ParseException;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoginFragment extends Fragment implements User.OnParseUserResponse{

    @Bind(R.id.create_account_in_login_button)
    Button mToCreateButton;
    @Bind(R.id.login_button)
    Button mLoginButton;
    @Bind(R.id.login_email)
    EditText mLoginEmail;
    @Bind(R.id.login_password)
    EditText mLoginPassword;

    LoginFragment thisFragment = this;
    ProgressDialog mProgressDialog;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, v);
        setListeners();
        return v;
    }

    private void setListeners(){
        // Button go to create account
        mToCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create new fragment and transaction
                CreateAccountFragment newFragment = new CreateAccountFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack if needed
                transaction.replace(R.id.auth_container, newFragment);
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
            }
        });

        //Button login
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = mLoginPassword.getText().toString();
                String email = mLoginEmail.getText().toString();
                User.logIn(thisFragment, email, password);
                mProgressDialog = ProgressDialog.show(getActivity(), null, getResources().getString(R.string.logging));
            }
        });
    }

    @Override
    public void userSignedUp() {  }

    @Override
    public void userLoggedIn() {
        if (mProgressDialog != null) mProgressDialog.dismiss();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void authError(ParseException e) {
        if (mProgressDialog != null) mProgressDialog.dismiss();
        Toast.makeText(getActivity().getApplicationContext(), R.string.auth_error, Toast.LENGTH_SHORT).show();
    }
}
