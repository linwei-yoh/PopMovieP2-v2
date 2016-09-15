package com.example.android.popmovie_fin.popsync;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class PopAuthenticatorService extends Service {

    private PopAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new PopAuthenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
            return mAuthenticator.getIBinder();
    }
}
