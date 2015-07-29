package ru.rian.riamessenger.xmpp;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import lombok.AllArgsConstructor;
import ru.rian.riamessenger.common.RiaEventBus;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.riaevents.response.XmppErrorEvent;

/**
 * Created by Roman on 7/12/2015.
 */
@AllArgsConstructor
public class SmackConnectionListener implements ConnectionListener {


    final UserAppPreference userAppPreference;

    @Override
    public void connected(XMPPConnection connection) {
        RiaEventBus.post(XmppErrorEvent.State.EConnected);
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        RiaEventBus.post(XmppErrorEvent.State.EAuthenticated);
        userAppPreference.setJidStringKey(connection.getUser().asBareJidString());
    }

    @Override
    public void connectionClosed() {
        RiaEventBus.post(XmppErrorEvent.State.EConnectionClosed);
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        RiaEventBus.post(XmppErrorEvent.State.EConnectionClosed);
    }

    @Override
    public void reconnectionSuccessful() {
        RiaEventBus.post(XmppErrorEvent.State.EReconnected);
    }

    @Override
    public void reconnectingIn(int seconds) {

    }

    @Override
    public void reconnectionFailed(Exception e) {
        RiaEventBus.post(XmppErrorEvent.State.EReconnectionFailed);
    }
}
