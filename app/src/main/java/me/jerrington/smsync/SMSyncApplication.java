package me.jerrington.smsync;

import android.app.Application;
import android.os.Handler;

import timber.log.Timber;

public class SMSyncApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());

        getContentResolver().registerContentObserver(
                SMSSentObserver.CONTENT_SMS,
                true,
                new SMSSentObserver(getApplicationContext(), new Handler())
        );
        Timber.d("Registered content observer.");
    }
}
