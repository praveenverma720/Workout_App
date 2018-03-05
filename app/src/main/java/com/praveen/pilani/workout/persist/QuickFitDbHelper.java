package com.praveen.pilani.workout.persist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.praveen.pilani.workout.persist.QuickFitContract.ScheduleEntry;
import com.praveen.pilani.workout.persist.QuickFitContract.SessionEntry;
import com.praveen.pilani.workout.persist.QuickFitContract.WorkoutEntry;

import timber.log.Timber;


public class QuickFitDbHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "workout.db";
    static final int DATABASE_VERSION = 9;

    public QuickFitDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    public void onCreate(SQLiteDatabase database) {
        onUpgrade(database, 0, DATABASE_VERSION);
    }

    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion) {
        Timber.w("Upgrading database from version %d to %d", oldVersion, newVersion);

        for (int versionStep = oldVersion + 1; versionStep <= newVersion; versionStep++) {
            upgradeStep(database, versionStep);
        }
    }

    private void upgradeStep(SQLiteDatabase database, int newVersion) {
        if (newVersion <= 5) {
            // version 5 was the first version in the wild
            database.execSQL("DROP TABLE IF EXISTS " + WorkoutEntry.TABLE_NAME);
            database.execSQL("DROP TABLE IF EXISTS " + SessionEntry.TABLE_NAME);

            final String createWorkout = "CREATE TABLE " + WorkoutEntry.TABLE_NAME + " ( " +
                    WorkoutEntry.COL_ID + " INTEGER PRIMARY KEY, " +
                    WorkoutEntry.COL_ACTIVITY_TYPE + " TEXT NOT NULL, " +
                    WorkoutEntry.COL_DURATION_MINUTES + " INTEGER NOT NULL, " +
                    WorkoutEntry.COL_LABEL + " TEXT NULL, " +
                    WorkoutEntry.COL_CALORIES + " INTEGER NULL " +
                    ")";
            database.execSQL(createWorkout);

            final String createSession = "CREATE TABLE " + SessionEntry.TABLE_NAME + " ( " +
                    SessionEntry._ID + " INTEGER PRIMARY KEY, " +
                    SessionEntry.ACTIVITY_TYPE + " TEXT NOT NULL, " +
                    SessionEntry.START_TIME + " INTEGER NOT NULL, " +
                    SessionEntry.END_TIME + " INTEGER NOT NULL, " +
                    SessionEntry.STATUS + " TEXT NOT NULL, " +
                    SessionEntry.NAME + " TEXT NULL, " +
                    SessionEntry.CALORIES + " INTEGER NULL " +
                    ")";
            database.execSQL(createSession);
            return;
        }
        if (newVersion == 6) {
            final String createSchedule = "CREATE TABLE " + ScheduleEntry.TABLE_NAME + " ( " +
                    ScheduleEntry.COL_ID + " INTEGER PRIMARY KEY, " +
                    ScheduleEntry.COL_WORKOUT_ID + " INTEGER NOT NULL REFERENCES " + WorkoutEntry.TABLE_NAME + "(" + WorkoutEntry.COL_ID + ") ON DELETE CASCADE, " +
                    ScheduleEntry.COL_DAY_OF_WEEK + " TEXT NOT NULL, " +
                    ScheduleEntry.COL_HOUR + " INTEGER NOT NULL, " +
                    ScheduleEntry.COL_MINUTE + " INTEGER NOT NULL " +
                    ")";
            database.execSQL(createSchedule);
            return;
        }
        if (newVersion == 7) {
            // version 6 was never in the wild
            database.execSQL("DROP TABLE " + ScheduleEntry.TABLE_NAME);
            final String createSchedule = "CREATE TABLE " + ScheduleEntry.TABLE_NAME + " ( " +
                    ScheduleEntry.COL_ID + " INTEGER PRIMARY KEY, " +
                    ScheduleEntry.COL_WORKOUT_ID + " INTEGER NOT NULL REFERENCES " + WorkoutEntry.TABLE_NAME + "(" + WorkoutEntry.COL_ID + ") ON DELETE CASCADE, " +
                    ScheduleEntry.COL_DAY_OF_WEEK + " TEXT NOT NULL, " +
                    ScheduleEntry.COL_HOUR + " INTEGER NOT NULL, " +
                    ScheduleEntry.COL_MINUTE + " INTEGER NOT NULL, " +
                    ScheduleEntry.COL_NEXT_ALARM_MILLIS + " INTEGER NOT NULL " +
                    ")";
            database.execSQL(createSchedule);
            return;
        }
        if (newVersion == 8) {
            // version 7 was never in the wild
            database.execSQL("DROP TABLE " + ScheduleEntry.TABLE_NAME);
            final String createSchedule = "CREATE TABLE " + ScheduleEntry.TABLE_NAME + " ( " +
                    ScheduleEntry.COL_ID + " INTEGER PRIMARY KEY, " +
                    ScheduleEntry.COL_WORKOUT_ID + " INTEGER NOT NULL REFERENCES " + WorkoutEntry.TABLE_NAME + "(" + WorkoutEntry.COL_ID + ") ON DELETE CASCADE, " +
                    ScheduleEntry.COL_DAY_OF_WEEK + " TEXT NOT NULL, " +
                    ScheduleEntry.COL_HOUR + " INTEGER NOT NULL, " +
                    ScheduleEntry.COL_MINUTE + " INTEGER NOT NULL, " +
                    ScheduleEntry.COL_NEXT_ALARM_MILLIS + " INTEGER NOT NULL, " +
                    ScheduleEntry.COL_SHOW_NOTIFICATION + " INTEGER NOT NULL " +
                    ")";
            database.execSQL(createSchedule);
            return;
        }
        if (newVersion == 9) {
            // version 9 was never in the wild
            database.execSQL("DROP TABLE " + ScheduleEntry.TABLE_NAME);
            final String createSchedule = "CREATE TABLE " + ScheduleEntry.TABLE_NAME + " ( " +
                    ScheduleEntry.COL_ID + " INTEGER PRIMARY KEY, " +
                    ScheduleEntry.COL_WORKOUT_ID + " INTEGER NOT NULL REFERENCES " + WorkoutEntry.TABLE_NAME + "(" + WorkoutEntry.COL_ID + ") ON DELETE CASCADE, " +
                    ScheduleEntry.COL_DAY_OF_WEEK + " TEXT NOT NULL, " +
                    ScheduleEntry.COL_HOUR + " INTEGER NOT NULL, " +
                    ScheduleEntry.COL_MINUTE + " INTEGER NOT NULL, " +
                    ScheduleEntry.COL_NEXT_ALARM_MILLIS + " INTEGER NOT NULL, " +
                    ScheduleEntry.COL_SHOW_NOTIFICATION + " INTEGER NOT NULL DEFAULT " + ScheduleEntry.SHOW_NOTIFICATION_NO +
                    ")";
            database.execSQL(createSchedule);
            return;
        }
    }
}
