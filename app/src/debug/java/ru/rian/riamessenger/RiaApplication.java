package ru.rian.riamessenger;

import android.content.Context;
import android.os.StrictMode;

import timber.log.Timber;

public class RiaApplication extends RiaBaseApplication {

    public static Context mContext = null;

    @Override
    public void onCreate() {
        super.onCreate();
        configureStrictMode();
        configureLogging();
        mContext = getApplicationContext();
    }

    private void configureLogging() {
        Timber.plant(new Timber.DebugTree());
    }

    private void configureStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().penaltyDeathOnNetwork().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().detectActivityLeaks().penaltyLog().build());
    }
}