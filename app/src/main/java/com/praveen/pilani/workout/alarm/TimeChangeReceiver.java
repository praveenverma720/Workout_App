package com.praveen.pilani.workout.alarm;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.HashSet;
import java.util.Set;

public class TimeChangeReceiver extends WakefulBroadcastReceiver {

    private static final Set<String> ALLOWED_ACTIONS = new HashSet<String>() {
        {
            add(Intent.ACTION_TIME_CHANGED);
            add(Intent.ACTION_TIMEZONE_CHANGED);
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ALLOWED_ACTIONS.contains(intent.getAction())) {
            return;
        }
        startWakefulService(context, AlarmService.getIntentOnTimeChanged(context));
    }
}
