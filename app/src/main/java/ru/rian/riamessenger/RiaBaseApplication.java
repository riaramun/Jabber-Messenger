package ru.rian.riamessenger;

import android.app.Application;
import android.content.Context;

import com.activeandroid.ActiveAndroid;

import ru.rian.riamessenger.di.AppComponent;
//import ru.rian.riamessenger.di.DaggerAppComponent;
import ru.rian.riamessenger.di.AppSystemModule;
import ru.rian.riamessenger.di.DaggerAppComponent;


public abstract class RiaBaseApplication extends Application {

    private AppComponent appComponent;

    private static Context mContext = null;

    @Override
    public void onCreate() {
        super.onCreate();
        ActiveAndroid.initialize(this);
        mContext = getApplicationContext();
        appComponent = DaggerAppComponent.builder()
                .appSystemModule(new AppSystemModule(this))
                .build();
    }


    public static AppComponent component() {
        return ((RiaBaseApplication) mContext).appComponent;
    }

}
