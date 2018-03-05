package com.praveen.pilani.workout.alarm;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.HashSet;
import java.util.Set;

import timber.log.Timber;


public class AlarmReceiver extends WakefulBroadcastReceiver {
    private static final String ACTION_ALARM = "com.praveen.pilani.workoutaction.ALARM";

    private static final Set<String> ALLOWED_ACTIONS = new HashSet<String>() {
        {
            add(ACTION_ALARM);
            add(Intent.ACTION_BOOT_COMPLETED);
            add("android.intent.action.QUICKBOOT_POWERON");
            add("com.htc.intent.action.QUICKBOOT_POWERON");
        }
    };

    static Intent getIntentOnAlarm(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION_ALARM);
        return intent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Timber.d("received intent with action %s", action);
        if (!ALLOWED_ACTIONS.contains(action)) {
            return;
        }
        startWakefulService(context, AlarmService.getIntentOnAlarmReceived(context));
    }
}
