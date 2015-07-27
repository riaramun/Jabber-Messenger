package ru.rian.riamessenger.di;

import dagger.Module;
import dagger.Provides;
import ru.rian.riamessenger.listeners.ContactsListClickListener;

/**
 * Created by Roman on 7/21/2015.
 */
@Module
public class AppClickHandlersModule {

    @Provides
    ContactsListClickListener provideContactsListClickListener() {
        return new ContactsListClickListener();
    }
}
