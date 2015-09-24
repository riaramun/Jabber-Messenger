package ru.rian.riamessenger;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;

import com.activeandroid.ActiveAndroid;

import ru.rian.riamessenger.di.AppComponent;
import ru.rian.riamessenger.di.AppSystemModule;
import ru.rian.riamessenger.di.DaggerAppComponent;

//import ru.rian.riamessenger.di.DaggerAppComponent;


public abstract class RiaBaseApplication extends Application {

    private static Context mContext = null;
    private AppComponent appComponent;

    public static AppComponent component() {
        return ((RiaBaseApplication) mContext).appComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ActiveAndroid.initialize(this);
        mContext = getApplicationContext();
        appComponent = DaggerAppComponent.builder()
                .appSystemModule(new AppSystemModule(this))
                .build();
    }

    @Override
    public void onTrimMemory(final int level) {
        super.onTrimMemory(level);
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            // Get called every-time when application went to background.
        }
    }
}
