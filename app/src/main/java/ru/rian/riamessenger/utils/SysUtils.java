package ru.rian.riamessenger.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import java.util.List;

import ru.rian.riamessenger.services.RiaXmppService;

/**
 * Created by Roman on 7/9/2015.
 */
public class SysUtils {

    static public boolean isApplicationBroughtToBackground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    static public boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        return RiaXmppService.IS_STARTED;
        /*boolean isRunning = false;
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                isRunning = true;
            }
        }
        Log.i("RiaService", "isRunning = " + isRunning);
        return isRunning;
        */
    }
}
