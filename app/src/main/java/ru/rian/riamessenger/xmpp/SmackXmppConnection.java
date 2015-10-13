package ru.rian.riamessenger.xmpp;

import android.util.Log;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import lombok.AllArgsConstructor;
import ru.rian.riamessenger.common.RiaConstants;
import ru.rian.riamessenger.common.RiaEventBus;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.riaevents.response.XmppErrorEvent;

/**
 * Created by Roman on 8/10/2015.
 */
@AllArgsConstructor
public class SmackXmppConnection {

    final XMPPTCPConnection xmppConnection;
    final UserAppPreference userAppPreference;

    public static XMPPTCPConnectionConfiguration getConfig(UserAppPreference userAppPreference) {

        XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
        String serviceName = null;
        try {
            serviceName = RiaConstants.XMPP_SERVICE_NAME;
            configBuilder.setResource(RiaConstants.XMPP_RESOURCE_NAME);
            configBuilder.setXmppDomain(JidCreate.domainBareFrom(serviceName));
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }

        //configBuilder.setPort(5222);
        configBuilder.setDebuggerEnabled(true);
        configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        configBuilder.setSendPresence(true);
        configBuilder.setHost(RiaConstants.XMPP_SERVER_ADDRESS);

        return configBuilder.build();
    }

    public boolean isConnected() {
        boolean connected = false;
        if (xmppConnection != null) {
            connected = xmppConnection.isConnected();
        }
        return connected;
    }

    public boolean isAuthenticated() {
        boolean authenticated = false;
        if (xmppConnection != null) {
            authenticated = xmppConnection.isAuthenticated();
        }
        return authenticated;
    }


    public void tryConnectToServer() {
        if (isConnected() || xmppConnection == null) return;
        try {
            Log.i("RiaService", "tryConnectToServer");
            xmppConnection.connect();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("RiaService", e.getMessage());
        }
    }

    public void tryLoginToServer() {
        if (isAuthenticated() || !isConnected()) return;
        try {
            Log.i("RiaService", "tryLoginToServer");
            xmppConnection.login(userAppPreference.getLoginStringKey(), userAppPreference.getPassStringKey());
        } catch (Exception e) {
            if (e.getMessage().contains("No response") || e.getMessage().contains("reply timeout") || e.getMessage().contains("is no longer connected")) {
                //From time to time we ca not login to server because of "No response received within reply timeout"
                //it is a workaround for this situation
            } else {
                userAppPreference.setLoginStringKey("");
                userAppPreference.setPassStringKey("");
                RiaEventBus.post(XmppErrorEvent.State.EAuthenticationFailed);
            }
            Log.i("RiaService", "sign in error, " + e.getMessage());
            e.printStackTrace();
        }
    }
}
