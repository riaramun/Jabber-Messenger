package ru.rian.riamessenger.di;

import dagger.Module;
import dagger.Provides;
import ru.rian.riamessenger.listeners.ContactsListClickListener;
import ru.rian.riamessenger.listeners.RoomsListClickListener;

/**
 * Created by Roman on 7/21/2015.
 */
@Module
class AppClickHandlersModule {

    @Provides
    ContactsListClickListener provideContactsListClickListener() {
        return new ContactsListClickListener();
    }

    @Provides
    RoomsListClickListener provideRoomsListClickListener() {
        return new RoomsListClickListener();
    }
}
