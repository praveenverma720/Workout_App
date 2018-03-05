package com.praveen.pilani.workout.ui;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

/**
 * Base class for activities displaying dialog fragments.
 */
public abstract class DialogActivity extends AppCompatActivity {
    private static final String TAG_DIALOG = "dialog";

    protected void showDialog(DialogFragment dialogFragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag(TAG_DIALOG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        dialogFragment.show(ft, TAG_DIALOG);
    }
}
