package com.praveen.pilani.workout.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;


public class TimeDialogFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private static final String KEY_SCHEDULE_ID = "scheduleId";
    private static final String KEY_OLD_HOUR = "oldHour";
    private static final String KEY_OLD_MINUTE = "oldMinute";

    private OnFragmentInteractionListener listener;

    public TimeDialogFragment() {
        // It's a fragment, it needs a default constructor
    }

    public static TimeDialogFragment newInstance(long scheduleId, int oldHour, int oldMinute) {
        TimeDialogFragment fragment = new TimeDialogFragment();
        Bundle args = new Bundle();
        args.putLong(KEY_SCHEDULE_ID, scheduleId);
        args.putInt(KEY_OLD_HOUR, oldHour);
        args.putInt(KEY_OLD_MINUTE, oldMinute);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnFragmentInteractionListenerProvider) {
            listener = ((OnFragmentInteractionListenerProvider) activity).getOnTimeDialogFragmentInteractionListener();
        } else {
            throw new IllegalArgumentException(activity.toString() + " must implement OnFragmentInteractionListenerProvider");
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
        int hour = getArguments().getInt(KEY_OLD_HOUR);
        int minute = getArguments().getInt(KEY_OLD_MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        listener.onTimeChanged(getArguments().getLong(KEY_SCHEDULE_ID), hourOfDay, minute);
    }

    interface OnFragmentInteractionListenerProvider {
        OnFragmentInteractionListener getOnTimeDialogFragmentInteractionListener();
    }

    interface OnFragmentInteractionListener {
        void onTimeChanged(long scheduleId, int newHour, int newMinute);
    }

}
