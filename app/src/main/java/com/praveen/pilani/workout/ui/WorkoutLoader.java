
package com.praveen.pilani.workout.ui;

import android.content.Context;
import android.content.CursorLoader;

import com.praveen.pilani.workout.persist.QuickFitContentProvider;
import com.praveen.pilani.workout.persist.QuickFitContract;

/**
 * Loader for a single workout.
 */
public class WorkoutLoader extends CursorLoader {
    public WorkoutLoader(Context context, long workoutId) {
        super(context,
                QuickFitContentProvider.getUriWorkoutsId(workoutId),
                QuickFitContract.WorkoutEntry.COLUMNS_WORKOUT_ONLY,
                null,
                null,
                null);
    }
}
