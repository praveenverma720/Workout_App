package com.praveen.pilani.workout.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.praveen.pilani.workout.R;


public class DurationMinutesDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String KEY_WORKOUT_ID = "workoutId";
    private static final String KEY_OLD_VALUE = "oldValue";

    private OnFragmentInteractionListener listener;

    public DurationMinutesDialogFragment() {
        // It's a fragment, it needs a default constructor
    }

    public static DurationMinutesDialogFragment newInstance(long workoutId, int oldValue) {
        DurationMinutesDialogFragment fragment = new DurationMinutesDialogFragment();
        Bundle args = new Bundle();
        args.putLong(KEY_WORKOUT_ID, workoutId);
        args.putInt(KEY_OLD_VALUE, oldValue);
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
        @SuppressLint("InflateParams") View dialogContent = LayoutInflater.from(getContext()).inflate(R.layout.dialog_duration_minutes, null);
        NumberPicker numberPicker = ((NumberPicker) dialogContent.findViewById(R.id.duration_mins_picker));
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(9999);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setValue(getArguments().getInt(KEY_OLD_VALUE));

        return new AlertDialog.Builder(getContext())
                .setView(dialogContent)
                .setTitle(R.string.title_workout_duration)
                .setPositiveButton(R.string.button_done_workout_duration, this)
                .setNegativeButton(R.string.cancel, this)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                NumberPicker numberPicker = ((NumberPicker) getDialog().findViewById(R.id.duration_mins_picker));
                numberPicker.clearFocus();
                int newVal = numberPicker.getValue();
                listener.onDurationChanged(getArguments().getLong(KEY_WORKOUT_ID), newVal);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                break;
            default:
                throw new IllegalStateException("No such button.");
        }
    }

    interface OnFragmentInteractionListener {
        void onDurationChanged(long workoutId, int newValue);
    }
}
