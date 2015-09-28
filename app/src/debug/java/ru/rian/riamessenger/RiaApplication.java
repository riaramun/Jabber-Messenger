package ru.rian.riamessenger;

import android.content.Context;
import android.os.StrictMode;

public class RiaApplication extends RiaBaseApplication {

    public static Context mContext = null;

    @Override
    public void onCreate() {
        super.onCreate();
        configureStrictMode();
        mContext = getApplicationContext();
    }

     void configureStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().penaltyDeathOnNetwork().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().detectActivityLeaks().penaltyLog().build());
    }
}