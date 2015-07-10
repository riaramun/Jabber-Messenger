package ru.rian.riamessenger.di;


import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.rian.riamessenger.utils.D2ECollectionUtils;

@Module
public class D2EUtilsModule {

    @Provides
    @Singleton
    D2ECollectionUtils provideStringUtils() {
        return new D2ECollectionUtils();
    }
}
