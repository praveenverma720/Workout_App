package com.praveen.pilani.workout.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.praveen.pilani.workout.R;

public class LabelDialogFragment extends DialogFragment implements DialogInterface.OnClickListener, TextView.OnEditorActionListener {

    private static final String KEY_WORKOUT_ID = "workoutId";
    private static final String KEY_OLD_VALUE = "oldValue";

    private OnFragmentInteractionListener listener;

    public LabelDialogFragment() {
        // It's a fragment, it needs a default constructor
    }

    public static LabelDialogFragment newInstance(long workoutId, String oldValue) {
        LabelDialogFragment fragment = new LabelDialogFragment();
        Bundle args = new Bundle();
        args.putLong(KEY_WORKOUT_ID, workoutId);
        args.putString(KEY_OLD_VALUE, oldValue);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) activity;
        } else {
            throw new IllegalArgumentException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View dialogContent = LayoutInflater.from(getContext()).inflate(R.layout.dialog_label, null);
        EditText editText = (EditText) dialogContent.findViewById(R.id.label_input);
        editText.setText(getArguments().getString(KEY_OLD_VALUE));
        editText.setOnEditorActionListener(this);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogContent)
                .setTitle(R.string.title_workout_label)
                .setPositiveButton(R.string.button_done_workout_label, this)
                .setNegativeButton(R.string.cancel, this)
                .create();

        editText.requestFocus();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onDone();
            dismiss();
            return true;
        }
        return false;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                onDone();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                break;
            default:
                throw new IllegalStateException("No such button.");
        }
    }

    private void onDone() {
        EditText editText = (EditText) getDialog().findViewById(R.id.label_input);
        String newVal = editText.getText().toString();
        listener.onLabelChanged(getArguments().getLong(KEY_WORKOUT_ID), newVal.isEmpty() ? null : newVal);
    }

    interface OnFragmentInteractionListener {
        void onLabelChanged(long workoutId, String newValue);
    }
}
