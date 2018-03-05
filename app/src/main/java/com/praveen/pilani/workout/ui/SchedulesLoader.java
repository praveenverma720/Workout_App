package com.praveen.pilani.workout.ui;

import android.content.Context;
import android.support.v4.content.CursorLoader;

import com.praveen.pilani.workout.persist.QuickFitContentProvider;
import com.praveen.pilani.workout.persist.QuickFitContract;

/**
 * Loader for the schedule items belonging to a single workout
 */
public class SchedulesLoader extends CursorLoader {
    public SchedulesLoader(Context context, long workoutId) {
        super(context,
                QuickFitContentProvider.getUriWorkoutsId(workoutId),
                QuickFitContract.WorkoutEntry.COLUMNS_SCHEDULE_ONLY,
                null,
                null,
                null);
    }
}
