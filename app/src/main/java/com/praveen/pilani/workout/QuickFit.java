package com.praveen.pilani.workout;

import android.app.Application;

import timber.log.Timber;


public class QuickFit extends Application {

    @Override
    public void onCreate() {
        super.onCreate();


        startService(FitActivityService.getIntentSetPeriodicSync(getApplicationContext()));

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
