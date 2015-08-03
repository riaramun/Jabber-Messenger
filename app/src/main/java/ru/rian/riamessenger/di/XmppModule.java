package ru.rian.riamessenger.di;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import lombok.AllArgsConstructor;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.xmpp.SmackRosterListener;
import ru.rian.riamessenger.xmpp.SendMsgBroadcastReceiver;

/**
 * Created by Roman on 6/19/2015.
 */

@AllArgsConstructor
@Module
public class XmppModule {

    private final Context context;
    final UserAppPreference userAppPreference;

    @Provides
    @Singleton
    SendMsgBroadcastReceiver provideSendMsgBroadcastReceiver() {
        return new SendMsgBroadcastReceiver(context);
    }


    @Provides
    @Singleton
    SmackRosterListener provideRiaRosterListener() {
        return new SmackRosterListener();
    }
}
