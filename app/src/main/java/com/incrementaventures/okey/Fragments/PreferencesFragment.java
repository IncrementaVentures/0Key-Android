package com.incrementaventures.okey.Fragments;


import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.incrementaventures.okey.Models.User;
import com.incrementaventures.okey.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PreferencesFragment extends Fragment {
    public static String TAG = "preferences_fragment";
    @Bind(R.id.user_name)
    EditText mUserName;
    @Bind(R.id.user_email)
    EditText mUserEmail;
    @Bind(R.id.user_birthday)
    TextView mUserBirthday;
    @Bind(R.id.user_password)
    EditText mUserPassword;
    @Bind(R.id.user_sex_group)
    RadioGroup mUserSexGroup;
    @Bind(R.id.user_sex_male)
    RadioButton mUserMale;
    @Bind(R.id.user_sex_female)
    RadioButton mUserFemale;
    @Bind(R.id.menu_button)
    ImageButton mMenuButton;
    @Bind(R.id.ok_button)
    ImageButton mOkButton;

    private User mUser;
    private MenuFragment.OnMenuButtonClicked mClickToolbarMenuListener;

    public PreferencesFragment() {
        // Required empty public constructor
    }

    View.OnClickListener mRefreshSettingsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_preferences, container, false);
        ButterKnife.bind(this, view);
        mUser = User.getLoggedUser();
        mUserName.setText(mUser.getName());
        mUserEmail.setText(mUser.getEmail());
        String birthday = mUser.getBirthday();
        if (birthday.indexOf('T') > 0) {
            birthday = birthday.substring(0, birthday.indexOf('T'));
        }
        mUserBirthday.setText(birthday);
        mUserPassword.setText("secret key");
        mOkButton.setOnClickListener(mRefreshSettingsListener);
        if (mUser.isMale()) {
            mUserMale.setSelected(true);
        } else {
            mUserFemale.setSelected(true);
        }
        return view;
    }

    @OnClick(R.id.user_birthday)
    public void onBirthdayTextClick() {
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
                mUserBirthday.setText(birthday);
            }
        };
        newFragment.show(getActivity().getFragmentManager(), "datePicker");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mClickToolbarMenuListener = (MenuFragment.OnMenuButtonClicked) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnMenuButtonClicked");
        }    }
}
