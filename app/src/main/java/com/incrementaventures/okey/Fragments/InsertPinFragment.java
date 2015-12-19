package com.incrementaventures.okey.Fragments;


import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.incrementaventures.okey.R;
import com.incrementaventures.okey.Views.Adapters.CharacterCountWatcher;


public class InsertPinFragment extends DialogFragment {

    EditText pinEditText;
    PinDialogListener mListener;
    AlertDialog mDialog;

    public interface PinDialogListener {
        void onPinDialogPositiveClick(String pin);
        void onPinDialogNegativeClick();
    }

    DialogInterface.OnShowListener mOnShowListener = new DialogInterface.OnShowListener() {
        @Override
        public void onShow(DialogInterface dialogInterface) {
            Button okButton = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences sharedPref =
                            PreferenceManager.getDefaultSharedPreferences(getActivity());
                    if (!sharedPref.getString("protect_pin", "")
                            .equals(pinEditText.getText().toString())) {
                        Snackbar.make(v, R.string.pin_incorrect, Snackbar.LENGTH_SHORT).show();
                    } else {
                        mListener.onPinDialogPositiveClick(pinEditText.getText().toString());
                        mDialog.dismiss();
                    }
                }
            });
        }
    };

    public InsertPinFragment() {
        // Required empty public constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setTitle(R.string.enter_pin);
        View view = inflater.inflate(R.layout.dialog_pin, null);
        pinEditText = (EditText) view.findViewById(R.id.pin);
        TextInputLayout pinInput = (TextInputLayout) view.findViewById(R.id.pin_input);
        pinInput.getEditText().addTextChangedListener(new CharacterCountWatcher(pinInput, 4, 4));
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.okey, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) { }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onPinDialogNegativeClick();
                        InsertPinFragment.this.getDialog().cancel();
                    }
                });
        mDialog = builder.create();
        mDialog.setOnShowListener(mOnShowListener);
        return mDialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (PinDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

}
