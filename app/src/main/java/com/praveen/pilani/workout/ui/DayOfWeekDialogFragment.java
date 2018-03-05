package com.praveen.pilani.workout.ui;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.praveen.pilani.workout.R;
import com.praveen.pilani.workout.model.DayOfWeek;
import com.praveen.pilani.workout.util.Arrays;


public class DayOfWeekDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String KEY_SCHEDULE_ID = "scheduleId";
    private static final String KEY_OLD_VALUE = "oldValue";

    private OnFragmentInteractionListener listener;
    private DayOfWeek[] week;

    private int checkedItemPosition;

    public DayOfWeekDialogFragment() {
        // It's a fragment, it needs a default constructor
    }

    public static DayOfWeekDialogFragment newInstance(long objectId, DayOfWeek oldValue) {
        DayOfWeekDialogFragment fragment = new DayOfWeekDialogFragment();
        Bundle args = new Bundle();
        args.putLong(KEY_SCHEDULE_ID, objectId);
        args.putParcelable(KEY_OLD_VALUE, oldValue);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnFragmentInteractionListenerProvider) {
            listener = ((OnFragmentInteractionListenerProvider) activity).getOnDayOfWeekDialogFragmentInteractionListener();
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
        week = DayOfWeek.getWeek();
        //noinspection ConstantConditions
        checkedItemPosition = Arrays.firstIndexOf(week, getArguments().getParcelable(KEY_OLD_VALUE));

        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.title_schedule_dayOfWeek)
                .setSingleChoiceItems(Arrays.map(week, String[].class, dayOfWeek -> getResources().getString(dayOfWeek.fullNameResId)), checkedItemPosition, this)
                .setPositiveButton(R.string.button_done_schedule_dayOfWeek, this)
                .setNegativeButton(R.string.cancel, this)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                if (listener != null) {
                    listener.onListItemChanged(getArguments().getLong(KEY_SCHEDULE_ID), week[checkedItemPosition]);
                }
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                break;
            default:
                checkedItemPosition = which;
                break;
        }

    }

    interface OnFragmentInteractionListenerProvider {
        OnFragmentInteractionListener getOnDayOfWeekDialogFragmentInteractionListener();
    }

    interface OnFragmentInteractionListener {
        void onListItemChanged(long objectId, DayOfWeek newValue);
    }
}
