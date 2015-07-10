package ru.rian.riamessenger;

import android.app.Application;
import android.content.Context;

import com.activeandroid.ActiveAndroid;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.rian.riamessenger.di.D2EComponent;
import ru.rian.riamessenger.di.DaggerD2EComponent;
import ru.rian.riamessenger.di.SystemServicesModule;


public abstract class RiaBaseApplication extends Application {

    private D2EComponent d2EComponent;

    private static Context mContext = null;

    @Override
    public void onCreate() {
        super.onCreate();
        ActiveAndroid.initialize(this);
        mContext = getApplicationContext();
        d2EComponent = DaggerD2EComponent.builder()
                .systemServicesModule(new SystemServicesModule(this))
                .build();
    }


    public static D2EComponent component() {
        return ((RiaBaseApplication) mContext).d2EComponent;
    }

}
