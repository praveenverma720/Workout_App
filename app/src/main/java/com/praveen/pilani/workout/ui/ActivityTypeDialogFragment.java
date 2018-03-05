package com.praveen.pilani.workout.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.praveen.pilani.workout.R;
import com.praveen.pilani.workout.model.FitActivity;
import com.praveen.pilani.workout.util.Arrays;

public class ActivityTypeDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String KEY_WORKOUT_ID = "workoutId";
    private static final String KEY_OLD_VALUE_KEY = "oldValue";

    private OnFragmentInteractionListener listener;
    private int checkedItemPosition;
    private FitActivity[] fitActivities;

    public ActivityTypeDialogFragment() {
        // It's a fragment, it needs a default constructor
    }

    public static ActivityTypeDialogFragment newInstance(long workoutId, FitActivity oldValue) {
        ActivityTypeDialogFragment fragment = new ActivityTypeDialogFragment();
        Bundle args = new Bundle();
        args.putLong(KEY_WORKOUT_ID, workoutId);
        args.putString(KEY_OLD_VALUE_KEY, oldValue.key);
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
        fitActivities = FitActivity.all(getResources());
        java.util.Arrays.sort(fitActivities, (left, right) -> left.displayName.compareToIgnoreCase(right.displayName));
        //noinspection ConstantConditions
        checkedItemPosition = Arrays.firstIndexOf(fitActivities, FitActivity.fromKey(getArguments().getString(KEY_OLD_VALUE_KEY), getResources()));


        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.title_workout_activityType)
                .setSingleChoiceItems(Arrays.map(fitActivities, String[].class, fitAct -> fitAct.displayName), checkedItemPosition, this)
                .setPositiveButton(R.string.button_done_workout_activityType, this)
                .setNegativeButton(R.string.cancel, this)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                if (listener != null) {
                    listener.onActivityTypeChanged(getArguments().getLong(KEY_WORKOUT_ID), fitActivities[checkedItemPosition].key);
                }
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                break;
            default:
                checkedItemPosition = which;
                break;
        }

    }


    interface OnFragmentInteractionListener {
        void onActivityTypeChanged(long workoutId, String newActivityTypeKey);
    }
}
