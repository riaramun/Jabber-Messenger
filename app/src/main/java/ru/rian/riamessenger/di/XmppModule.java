package ru.rian.riamessenger.di;

import android.content.Context;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.rian.riamessenger.common.RiaConstants;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.xmpp.SendMsgBroadcastReceiver;
import ru.rian.riamessenger.xmpp.SmackConnectionListener;
import ru.rian.riamessenger.xmpp.SmackMessageManager;
import ru.rian.riamessenger.xmpp.SmackRosterManager;
import ru.rian.riamessenger.xmpp.SmackXmppConnection;

/**
 * Created by Roman on 6/19/2015.
 */



public class XmppModule {
/*
    final Context context;
    final UserAppPreference userAppPreference;


    public XmppModule(Context context, UserAppPreference userAppPreference) {
        this.context = context;
        this.userAppPreference = userAppPreference;
    }

    @Provides
    @Singleton
    SendMsgBroadcastReceiver provideSendMsgBroadcastReceiver() {
        return new SendMsgBroadcastReceiver(context);
    }


    @Provides
    @Singleton
    SmackRosterManager provideSmackRosterManager() {
        return new SmackRosterManager(context, userAppPreference, xmppConnection);
    }

    @Provides
    @Singleton
    SmackXmppConnection provideSmackXmppConnection() {
        return new SmackXmppConnection(xmppConnection, userAppPreference);
    }

    @Provides
    @Singleton
    SmackMessageManager provideXmppMessageManager() {
        return new SmackMessageManager(context, xmppConnection);
    }
*/
}
