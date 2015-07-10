package ru.rian.riamessenger.di;


import javax.inject.Singleton;

import dagger.Component;
import ru.rian.riamessenger.common.RiaBaseActivity;

/**
 * This class is in debug/ folder. You can use it to define injects or getters for dependencies only in debug variant
 */
@Singleton
@Component(modules = {SystemServicesModule.class, D2EUtilsModule.class, SettingsModule.class})
public interface D2EComponent extends D2EGraph{
    void inject(RiaBaseActivity baseActivity);
}
