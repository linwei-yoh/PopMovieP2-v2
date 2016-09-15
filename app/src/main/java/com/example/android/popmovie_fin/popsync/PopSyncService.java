package com.example.android.popmovie_fin.popsync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class PopSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static PopSyncAdapter sPopSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("PopSyncService", "onCreate - PopSyncService");
        synchronized (sSyncAdapterLock) {
            if (sPopSyncAdapter == null) {
                sPopSyncAdapter = new PopSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sPopSyncAdapter.getSyncAdapterBinder();
    }
}
