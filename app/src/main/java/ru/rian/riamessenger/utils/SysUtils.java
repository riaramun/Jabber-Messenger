package ru.rian.riamessenger.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

/**
 * Created by Roman on 7/9/2015.
 */
public class SysUtils {

    static public boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        boolean isRunning = false;
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                isRunning = true;
            }
        }
        Log.i("RiaService", "isRunning = " + isRunning);
        return isRunning;
    }
}
