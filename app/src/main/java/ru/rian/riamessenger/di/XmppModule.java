package ru.rian.riamessenger.di;

import android.content.Context;

import org.jivesoftware.smack.ConnectionListener;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import lombok.AllArgsConstructor;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.services.ChatMessageListenerWrap;
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
/*
    @Provides
    @Singleton
    RiaXMPPConnection provideRiaXMPPConnection() {
        return new RiaXMPPConnection();
    }
*/

    @Provides
    @Singleton
    SendMsgBroadcastReceiver provideSendMsgBroadcastReceiver() {
        return new SendMsgBroadcastReceiver();
    }



    @Provides
    @Singleton
    ChatMessageListenerWrap provideChatMessageListenerWrap() {
        return new ChatMessageListenerWrap();
    }

    @Provides
    @Singleton
    SmackRosterListener provideRiaRosterListener() {
        return new SmackRosterListener();
    }


    /*@Provides
    @Singleton
    SmackWrapper provideSmackWrapper() {
        return new SmackWrapper(context, connectionListener, userAppPreference);
    }*/


}
