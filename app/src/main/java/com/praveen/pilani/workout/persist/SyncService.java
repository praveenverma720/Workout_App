package com.praveen.pilani.workout.persist;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SyncService extends Service {
    // Object to use as a thread-safe lock
    private static final Object syncAdapterLock = new Object();
    // Storage for an instance of the sync adapter
    private static QuickFitSyncAdapter syncAdapter = null;

    /*
     * Instantiate the sync adapter object.
     */
    @Override
    public void onCreate() {

        synchronized (syncAdapterLock) {
            if (syncAdapter == null) {
                syncAdapter = new QuickFitSyncAdapter(getApplicationContext());
            }
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        /*
         * Get the object that allows external processes
         * to call onPerformSync(). The object is created
         * in the base class code when the SyncAdapter
         * constructors call super()
         */
        return syncAdapter.getSyncAdapterBinder();
    }
}
