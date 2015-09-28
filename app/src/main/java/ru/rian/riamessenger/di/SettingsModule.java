package ru.rian.riamessenger.di;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.rian.riamessenger.prefs.UserAppPreference;

/**
 * Created by Roman on 6/19/2015.
 */
@Module
class SettingsModule {
    @Provides
    @Singleton
    UserAppPreference provideUserAppPreference() {
        return new UserAppPreference();
    }
}
