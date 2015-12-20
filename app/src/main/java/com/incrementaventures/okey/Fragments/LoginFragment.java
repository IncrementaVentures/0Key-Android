package com.incrementaventures.okey.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
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
import com.parse.ParseUser;
import com.parse.SaveCallback;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoginFragment extends Fragment implements User.OnParseUserLoginResponse {

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
        setUI();
        setListeners();
        return v;
    }

    private void setUI() {
        mLoginButton.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.turquoise),
                PorterDuff.Mode.MULTIPLY);
        mToCreateButton.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.turquoise),
                PorterDuff.Mode.MULTIPLY);
        mLoginEmail.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.white),
                PorterDuff.Mode.SRC_ATOP);
        mLoginPassword.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.white),
                PorterDuff.Mode.SRC_ATOP);
    }

    private void setListeners(){
        // Button go to create account
        mToCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create new fragment and transaction
                CreateAccountFragment newFragment = new CreateAccountFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.auth_container, newFragment);
                transaction.addToBackStack(null);
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
    public void userLoggedIn(final ParseUser parseUser) {
        if (mProgressDialog != null) mProgressDialog.dismiss();
        if (parseUser.getEmail().equals("")) {
            parseUser.setEmail(parseUser.getString(User.BACKUP_EMAIL));
            parseUser.saveInBackground();
        }
        if (parseUser.getBoolean(User.EMAIL_VERIFIED)) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            getActivity().finish();
        } else {
            Snackbar snackbar = Snackbar.make(getView(), R.string.not_verified, Snackbar.LENGTH_LONG)
                    .setAction(R.string.resend_verification_email, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            resendVerificationEmail(parseUser);
                        }
                    }).setCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                ParseUser.logOutInBackground();
                            }
                        }
                    });
            snackbar.show();
        }
    }

    private void resendVerificationEmail(final ParseUser parseUser) {
        parseUser.put(User.BACKUP_EMAIL, parseUser.getEmail());
        parseUser.setEmail("");
        parseUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    parseUser.setEmail(parseUser.getString(User.BACKUP_EMAIL));
                    try {
                        parseUser.save();
                        ParseUser.logOutInBackground();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void authError(ParseException e) {
        if (mProgressDialog != null) mProgressDialog.dismiss();
        Toast.makeText(getActivity().getApplicationContext(), R.string.auth_error,
                Toast.LENGTH_SHORT).show();
    }
}
