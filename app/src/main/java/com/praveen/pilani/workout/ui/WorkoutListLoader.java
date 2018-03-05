

package com.praveen.pilani.workout.ui;

import android.content.Context;
import android.content.CursorLoader;

import com.praveen.pilani.workout.persist.QuickFitContentProvider;
import com.praveen.pilani.workout.persist.QuickFitContract.WorkoutEntry;


public class WorkoutListLoader extends CursorLoader {
    public WorkoutListLoader(Context context) {
        super(
                context,
                QuickFitContentProvider.getUriWorkoutsList(),
                WorkoutEntry.COLUMNS_FULL,
                null,
                null,
                WorkoutEntry.WORKOUT_ID + " ASC, " + WorkoutEntry.SCHEDULE_ID + " ASC"
        );
    }
}
