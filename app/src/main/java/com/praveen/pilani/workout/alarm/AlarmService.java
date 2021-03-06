package com.praveen.pilani.workout.alarm;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v4.app.NotificationCompat.InboxStyle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v7.app.NotificationCompat;
import android.text.format.DateUtils;


import com.praveen.pilani.workout.Constants;
import com.praveen.pilani.workout.FitActivityService;
import com.praveen.pilani.workout.R;
import com.praveen.pilani.workout.model.DayOfWeek;
import com.praveen.pilani.workout.model.FitActivity;
import com.praveen.pilani.workout.persist.QuickFitContentProvider;
import com.praveen.pilani.workout.persist.QuickFitContract.ScheduleEntry;
import com.praveen.pilani.workout.persist.QuickFitContract.WorkoutEntry;
import com.praveen.pilani.workout.persist.QuickFitDbHelper;
import com.praveen.pilani.workout.ui.WorkoutListActivity;
import com.praveen.pilani.workout.util.DateTimes;
import static com.praveen.pilani.workout.Constants.PENDING_INTENT_ALARM_RECEIVER;











import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * A wakeful intent service that handles notifications due to alarms
 * and interaction with the AlarmManager.
 */
public class AlarmService extends IntentService {

    private static final String ACTION_ON_ALARM_RECEIVED = "com.praveen.pilani.workout.alarm.action.ON_ALARM_RECEIVED";
    private static final String ACTION_ON_TIME_CHANGED = "com.praveen.pilani.workout.alarm.action.ON_TIME_CHANGED";
    private static final String ACTION_ON_NEXT_OCC_CHANGED = "com.praveen.pilani.workout.alarm.action.ON_NEXT_OCC_CHANGED";
    private static final String ACTION_ON_NOTIFICATIONS_CANCELED = "com.praveen.pilani.workout.alarm.action.ON_NOTIFICATIONS_CANCELED";
    private static final String ACTION_ON_SNOOZE = "com.praveen.pilani.workout.alarm.action.ON_SNOOZE";
    private static final String ACTION_ON_DID_IT = "com.praveen.pilani.workout.alarm.action.ON_DID_IT";

    private static final String EXTRA_SCHEDULE_ID = "com.praveen.pilani.workout.alarm.extra.SCHEDULE_ID";
    private static final String EXTRA_SCHEDULE_IDS = "com.praveen.pilani.workout.alarm.extra.SCHEDULE_IDS";
    private static final String EXTRA_WORKOUT_ID = "com.praveen.pilani.workout.alarm.WORKOUT_ID";

    private static final String QUERY_SELECT_MIN_NEXT_ALERT = "SELECT " + ScheduleEntry.COL_NEXT_ALARM_MILLIS + " FROM " + ScheduleEntry.TABLE_NAME +
            " ORDER BY " + ScheduleEntry.COL_NEXT_ALARM_MILLIS + " ASC LIMIT 1";

    private final QuickFitDbHelper dbHelper; // TODO: move into content provider

    public AlarmService() {
        super("AlarmService");
        dbHelper = new QuickFitDbHelper(this);
    }


    public static Intent getIntentOnAlarmReceived(Context context) {
        Intent intent = new Intent(context, AlarmService.class);
        intent.setAction(ACTION_ON_ALARM_RECEIVED);
        return intent;
    }


    public static Intent getIntentOnTimeChanged(Context context) {
        Intent intent = new Intent(context, AlarmService.class);
        intent.setAction(ACTION_ON_TIME_CHANGED);
        return intent;
    }


    public static Intent getIntentOnNextOccChanged(Context context) {
        Intent intent = new Intent(context, AlarmService.class);
        intent.setAction(ACTION_ON_NEXT_OCC_CHANGED);
        return intent;
    }

    /**
     * For use by the Notification main actions; marks the schedules as
     * "don't show notification".
     */
    public static Intent getIntentOnNotificationsCanceled(Context context, long[] scheduleIds) {
        Intent intent = new Intent(context, AlarmService.class);
        intent.setAction(ACTION_ON_NOTIFICATIONS_CANCELED);
        intent.putExtra(EXTRA_SCHEDULE_IDS, scheduleIds);
        return intent;
    }

    /**
     * Action of the Notification snooze action.
     */
    private static Intent getIntentOnSnooze(Context context, long scheduleId) {
        Intent intent = new Intent(context, AlarmService.class);
        intent.setAction(ACTION_ON_SNOOZE);
        intent.putExtra(EXTRA_SCHEDULE_ID, scheduleId);
        return intent;
    }

    /**
     * Action of the Notification DidIt action.
     */
    private static Intent getIntentOnDidIt(Context context, long scheduleId, long workoutId) {
        Intent intent = new Intent(context, AlarmService.class);
        intent.setAction(ACTION_ON_DID_IT);
        intent.putExtra(EXTRA_SCHEDULE_ID, scheduleId);
        intent.putExtra(EXTRA_WORKOUT_ID, workoutId);
        return intent;
    }


    @Override
    @WorkerThread
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_ON_ALARM_RECEIVED.equals(action)) {
                handleOnAlarmReceived(intent);
            } else if (ACTION_ON_TIME_CHANGED.equals(action)) {
                handleOnTimeChanged(intent);
            } else if (ACTION_ON_NEXT_OCC_CHANGED.equals(action)) {
                handleOnNextOccChanged();
            } else if (ACTION_ON_NOTIFICATIONS_CANCELED.equals(action)) {
                long[] scheduleIds = intent.getLongArrayExtra(EXTRA_SCHEDULE_IDS);
                handleOnNotificationsCanceled(scheduleIds);
            } else if (ACTION_ON_DID_IT.equals(action)) {
                long scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1);
                long workoutId = intent.getLongExtra(EXTRA_WORKOUT_ID, -1);
                handleOnDidIt(scheduleId, workoutId);
            } else if (ACTION_ON_SNOOZE.equals(action)) {
                long scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1);
                handleOnSnooze(scheduleId);
            } else {
                throw new IllegalArgumentException("Unexpected action " + action);
            }
        }
    }


    @WorkerThread
    private void handleOnAlarmReceived(Intent intent) {
        Timber.d("Handling onAlarmReceived");
        try {
            processOldEvents();
            refreshNotificationDisplay();
            setNextAlarm();
        } finally {
            WakefulBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

    @WorkerThread
    private void handleOnTimeChanged(Intent intent) {
        Timber.d("Handling onTimeChanged");
        try {
            // ignores snooze; time change events should happen only when
            // - user is currently traveling (probably does not care deeply about doing sports)
            // - DST change, deep at night
            // - user wilfully plays around with their system time settings (we're not caring for that)
            recalculateNextOccForAll();
            setNextAlarm();
        } finally {
            WakefulBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

    @WorkerThread
    private void handleOnNextOccChanged() {
        Timber.d("Handling next occurrence changed");
        setNextAlarm();
    }

    @WorkerThread
    private void handleOnNotificationsCanceled(long[] scheduleIds) {
        Timber.d("Handling onNotificationsCanceled");
        setDontShowNotificationForIds(scheduleIds);
    }

    @WorkerThread
    private void handleOnDidIt(long scheduleId, long workoutId) {
        Timber.d("Handling onDidIt");
        startService(FitActivityService.getIntentInsertSession(getApplicationContext(), workoutId));
        setDontShowNotificationForIds(new long[]{scheduleId});
        refreshNotificationDisplay();
    }

    @WorkerThread
    private void handleOnSnooze(long scheduleId) {
        Timber.d("Handling onSnooze");
        String durationMinsStr = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.pref_key_snooze_duration_mins), "60");
        int durationMins = Integer.parseInt(durationMinsStr);
        ContentValues values = new ContentValues(2);
        values.put(ScheduleEntry.COL_NEXT_ALARM_MILLIS, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(durationMins));
        values.put(ScheduleEntry.COL_SHOW_NOTIFICATION, ScheduleEntry.SHOW_NOTIFICATION_NO);
        getContentResolver().update(QuickFitContentProvider.getUriSchedulesId(scheduleId), values, null, null);

        setNextAlarm();
        setDontShowNotificationForIds(new long[]{scheduleId});
        refreshNotificationDisplay();
    }


    /**
     * sets the alarm with the alarm manager for the next occurrence of any scheduled event according
     * to the current db state
     */
    @WorkerThread
    private void setNextAlarm() {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery(QUERY_SELECT_MIN_NEXT_ALERT, null)) {
            // if cursor is empty, no schedules exist, no alarms to set
            if (cursor.moveToFirst()) {
                long nextAlarmMillis = cursor.getLong(cursor.getColumnIndexOrThrow(ScheduleEntry.COL_NEXT_ALARM_MILLIS));

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                PendingIntent alarmReceiverIntent = PendingIntent.getBroadcast(this, PENDING_INTENT_ALARM_RECEIVER, AlarmReceiver.getIntentOnAlarm(this), PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.cancel(alarmReceiverIntent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAlarmMillis, alarmReceiverIntent);
                } else {
                    alarmManager.setWindow(AlarmManager.RTC_WAKEUP, nextAlarmMillis, DateUtils.MINUTE_IN_MILLIS, alarmReceiverIntent);
                }
            }
        }
    }

    /**
     * Updates time for next occurrence and flag that notification is needed in single pass;
     * for all events with next occurrence in the past.
     */
    @WorkerThread
    private void processOldEvents() {
        long now = System.currentTimeMillis();
        Schedule[] schedules;

        try (Cursor pastEvents = getContentResolver().query(
                QuickFitContentProvider.getUriSchedulesList(),
                new String[]{ScheduleEntry.COL_ID, ScheduleEntry.COL_DAY_OF_WEEK, ScheduleEntry.COL_HOUR, ScheduleEntry.COL_MINUTE},
                ScheduleEntry.COL_NEXT_ALARM_MILLIS + "<=?",
                new String[]{Long.toString(now)},
                ScheduleEntry.COL_NEXT_ALARM_MILLIS + " ASC")) {
            if (pastEvents == null) {
                schedules = new Schedule[0];
            } else {
                schedules = new Schedule[pastEvents.getCount()];
                int i = 0;
                while (pastEvents.moveToNext()) {
                    schedules[i] = Schedule.fromRow(pastEvents);
                    i++;
                }
            }
        }

        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            db.beginTransactionNonExclusive();
            try {
                for (Schedule schedule : schedules) {
                    long nextAlarmMillis = DateTimes.getNextOccurrence(now, schedule.dayOfWeek, schedule.hour, schedule.minute);
                    ContentValues contentValues = new ContentValues(2);
                    contentValues.put(ScheduleEntry.COL_NEXT_ALARM_MILLIS, nextAlarmMillis);
                    contentValues.put(ScheduleEntry.COL_SHOW_NOTIFICATION, ScheduleEntry.SHOW_NOTIFICATION_YES);
                    db.update(ScheduleEntry.TABLE_NAME, contentValues, ScheduleEntry.COL_ID + "=?", new String[]{Long.toString(schedule.id)});
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }


    @WorkerThread
    private void refreshNotificationDisplay() {
        try (Cursor toNotify = getContentResolver().query(
                QuickFitContentProvider.getUriWorkoutsList(),
                new String[]{WorkoutEntry.SCHEDULE_ID, WorkoutEntry.WORKOUT_ID, WorkoutEntry.ACTIVITY_TYPE, WorkoutEntry.LABEL, WorkoutEntry.DURATION_MINUTES},
                ScheduleEntry.TABLE_NAME + "." + ScheduleEntry.COL_SHOW_NOTIFICATION + "=?",
                new String[]{Integer.toString(ScheduleEntry.SHOW_NOTIFICATION_YES)},
                null
        )) {
            int count = toNotify == null ? 0 : toNotify.getCount();
            if (count == 0) {
                Timber.d("refreshNotificationDisplay: no events");
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancel(Constants.NOTIFICATION_ALARM);
                return;
            }

            long[] scheduleIds = new long[count];
            int i = 0;
            toNotify.moveToPosition(-1);
            while (toNotify.moveToNext()) {
                scheduleIds[i] = toNotify.getLong(toNotify.getColumnIndex(WorkoutEntry.SCHEDULE_ID));
                i++;
            }

            PendingIntent cancelIntent = PendingIntent.getService(getApplicationContext(), 0, getIntentOnNotificationsCanceled(this, scheduleIds), PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder notification;
            if (count == 1) {
                Timber.d("refreshNotificationDisplay: single event");
                toNotify.moveToFirst();
                notification = notifySingleEvent(toNotify, cancelIntent);
            } else {
                Timber.d("refreshNotificationDisplay: multiple events");
                toNotify.moveToPosition(-1);
                notification = notifyMultipleEvents(toNotify, cancelIntent);
            }


            notification.setDeleteIntent(cancelIntent);
            notification.setAutoCancel(true);
            notification.setPriority(Notification.PRIORITY_HIGH);
            notification.setSmallIcon(R.drawable.ic_stat_quickfit_icon);
            notification.setColor(ContextCompat.getColor(this, R.color.colorPrimary));

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String ringtoneUriStr = preferences.getString(getString(R.string.pref_key_notification_ringtone), null);
            if (ringtoneUriStr == null) {
                notification.setSound(RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION));
            } else if (!ringtoneUriStr.isEmpty()) {
                notification.setSound(Uri.parse(ringtoneUriStr));
            }
            boolean ledOn = preferences.getBoolean(getString(R.string.pref_key_notification_led), true);
            boolean vibrationOn = preferences.getBoolean(getString(R.string.pref_key_notification_vibrate), true);
            notification.setDefaults((ledOn ? Notification.DEFAULT_LIGHTS : 0) | (vibrationOn ? Notification.DEFAULT_VIBRATE : 0));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notification.setCategory(Notification.CATEGORY_ALARM);
            }

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(Constants.NOTIFICATION_ALARM, notification.build());
        }
    }


    @WorkerThread
    @NonNull
    private NotificationCompat.Builder notifySingleEvent(@NonNull Cursor cursor, PendingIntent cancelIntent) {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this);


        FitActivity fitActivity = FitActivity.fromKey(cursor.getString(cursor.getColumnIndex(WorkoutEntry.ACTIVITY_TYPE)), getResources());
        String label = "";
        if (!cursor.isNull(cursor.getColumnIndex(WorkoutEntry.LABEL))) {
            label = cursor.getString(cursor.getColumnIndex(WorkoutEntry.LABEL));
        }
        int durationMinutes = cursor.getInt(cursor.getColumnIndex(WorkoutEntry.DURATION_MINUTES));

        String title = getString(R.string.notification_alarm_title_single, fitActivity.displayName);
        String formattedMinutes = String.format(getResources().getQuantityString(R.plurals.duration_mins_format, durationMinutes), durationMinutes);
        String content = getString(R.string.notification_alarm_content_single, formattedMinutes, label);

        notification.setContentTitle(title);
        notification.setContentText(content);


        long workoutId = cursor.getLong(cursor.getColumnIndex(WorkoutEntry.WORKOUT_ID));

        Intent workoutIntent = new Intent(getApplicationContext(), WorkoutListActivity.class);
        workoutIntent.putExtra(WorkoutListActivity.EXTRA_SHOW_WORKOUT_ID, workoutId);
        workoutIntent.putExtra(WorkoutListActivity.EXTRA_NOTIFICATIONS_CANCEL_INTENT, cancelIntent);
        PendingIntent activityIntent = TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(workoutIntent)
                .getPendingIntent(Constants.PENDING_INTENT_WORKOUT_LIST, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(activityIntent);// open workout list, scroll to workout


        long scheduleId = cursor.getLong(cursor.getColumnIndex(WorkoutEntry.SCHEDULE_ID));

        PendingIntent didItIntent = PendingIntent.getService(
                getApplicationContext(),
                Constants.PENDING_INTENT_DID_IT,
                getIntentOnDidIt(getApplicationContext(), scheduleId, workoutId),
                PendingIntent.FLAG_UPDATE_CURRENT);
        notification.addAction(R.drawable.ic_done_white_24dp, getString(R.string.notification_action_did_it), didItIntent);

        PendingIntent snoozeIntent = PendingIntent.getService(
                getApplicationContext(),
                Constants.PENDING_INTENT_SNOOZE,
                AlarmService.getIntentOnSnooze(this, scheduleId),
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        notification.addAction(R.drawable.ic_alarm_white_24dp, getString(R.string.notification_action_remind_me_later), snoozeIntent);

        return notification;
    }



    @WorkerThread
    @NonNull
    private NotificationCompat.Builder notifyMultipleEvents(@NonNull Cursor cursor, PendingIntent cancelIntent) {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this);

        Intent workoutIntent = new Intent(getApplicationContext(), WorkoutListActivity.class);
        workoutIntent.putExtra(WorkoutListActivity.EXTRA_NOTIFICATIONS_CANCEL_INTENT, cancelIntent);
        PendingIntent activityIntent = TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(workoutIntent)
                .getPendingIntent(Constants.PENDING_INTENT_WORKOUT_LIST, PendingIntent.FLAG_UPDATE_CURRENT);

        notification.setContentIntent(activityIntent);


        InboxStyle inboxStyle = new InboxStyle();
        while (cursor.moveToNext()) {
            FitActivity fitActivity = FitActivity.fromKey(cursor.getString(cursor.getColumnIndex(WorkoutEntry.ACTIVITY_TYPE)), getResources());
            String label = "";
            if (!cursor.isNull(cursor.getColumnIndex(WorkoutEntry.LABEL))) {
                label = cursor.getString(cursor.getColumnIndex(WorkoutEntry.LABEL));
            }
            int durationMinutes = cursor.getInt(cursor.getColumnIndex(WorkoutEntry.DURATION_MINUTES));

            String formattedMinutes = String.format(getResources().getQuantityString(R.plurals.duration_mins_format, durationMinutes), durationMinutes);
            String line = getString(R.string.notification_alarm_content_line_multi, fitActivity.displayName, formattedMinutes, label);
            inboxStyle.addLine(line);
        }

        notification.setContentTitle(getString(R.string.notification_alarm_title_multi));
        notification.setContentText(getString(R.string.notification_alarm_content_summary_multi, cursor.getCount()));

        notification.setStyle(inboxStyle);

        return notification;
    }

    @WorkerThread
    private void setDontShowNotificationForIds(long[] scheduleIds) {
        Timber.d("setDontShowNotificationForIds() called with: scheduleIds = [%s]", Arrays.toString(scheduleIds));
        for (long scheduleId : scheduleIds) {
            ContentValues contentValues = new ContentValues(1);
            contentValues.put(ScheduleEntry.COL_SHOW_NOTIFICATION, ScheduleEntry.SHOW_NOTIFICATION_NO);
            getContentResolver().update(
                    QuickFitContentProvider.getUriSchedulesId(scheduleId),
                    contentValues,
                    null,
                    null
            );
        }
    }


    @WorkerThread
    private void recalculateNextOccForAll() {
        long now = System.currentTimeMillis();
        Schedule[] schedules;

        try (Cursor allSchedules = getContentResolver().query(
                QuickFitContentProvider.getUriSchedulesList(),
                new String[]{ScheduleEntry.COL_ID, ScheduleEntry.COL_DAY_OF_WEEK, ScheduleEntry.COL_HOUR, ScheduleEntry.COL_MINUTE},
                null,
                null,
                null)) {
            if (allSchedules == null) {
                schedules = new Schedule[0];
            } else {
                schedules = new Schedule[allSchedules.getCount()];
                int i = 0;
                while (allSchedules.moveToNext()) {
                    schedules[i] = Schedule.fromRow(allSchedules);
                    i++;
                }
            }
        }

        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            db.beginTransactionNonExclusive();
            try {
                for (Schedule schedule : schedules) {
                    long nextAlarmMillis = DateTimes.getNextOccurrence(now, schedule.dayOfWeek, schedule.hour, schedule.minute);
                    ContentValues contentValues = new ContentValues(1);
                    contentValues.put(ScheduleEntry.COL_NEXT_ALARM_MILLIS, nextAlarmMillis);
                    db.update(ScheduleEntry.TABLE_NAME, contentValues, ScheduleEntry.COL_ID + "=?", new String[]{Long.toString(schedule.id)});
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }


    private static class Schedule {
        public final long id;
        public final DayOfWeek dayOfWeek;
        public final int hour;
        public final int minute;

        private Schedule(long id, DayOfWeek dayOfWeek, int hour, int minute) {
            this.id = id;
            this.dayOfWeek = dayOfWeek;
            this.hour = hour;
            this.minute = minute;
        }

        static Schedule fromRow(Cursor cursor) {
            DayOfWeek dayOfWeek = DayOfWeek.valueOf(cursor.getString(cursor.getColumnIndex(ScheduleEntry.COL_DAY_OF_WEEK)));
            int hour = cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COL_HOUR));
            int minute = cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COL_MINUTE));
            long id = cursor.getLong(cursor.getColumnIndex(ScheduleEntry.COL_ID));

            return new Schedule(id, dayOfWeek, hour, minute);
        }
    }

}
