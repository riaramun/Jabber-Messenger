package ru.rian.riamessenger.xmpp;

import android.content.Context;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.bytestreams.ibb.provider.DataPacketProvider;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jivesoftware.smackx.offline.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.offline.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.xdata.Form;

import java.util.List;

import lombok.AllArgsConstructor;
import ru.rian.riamessenger.common.RiaEventBus;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.riaevents.response.XmppErrorEvent;
import ru.rian.riamessenger.utils.DbHelper;
import ru.rian.riamessenger.utils.NetworkStateManager;

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
    public void authenticated(final XMPPConnection connection, boolean resumed) {
        RiaEventBus.post(XmppErrorEvent.State.EAuthenticated);
        //add current user entry to track his presence via loader
        userAppPreference.setJidStringKey(connection.getUser().asBareJidString());
        NetworkStateManager.setCurrentUserPresence(new Presence(Presence.Type.available), userAppPreference.getJidStringKey());
      /*  Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
*/

        //OfflineMessageInfo.Provider offlineProvider  = new OfflineMessageInfo.Provider();

        //ProviderManager.addExtensionProvider("offline", "http://jabber.org/protocol/offline", new OfflineMessageInfo.Provider());
        //ProviderManager.addIQProvider("offline", "http://jabber.org/protocol/offline", new OfflineMessageRequest.Provider());
        try {
            handleOfflineMessages(connection);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void handleOfflineMessages(XMPPConnection connection)throws Exception {
        OfflineMessageManager offlineMessageManager = new OfflineMessageManager(connection);

        if (!offlineMessageManager.supportsFlexibleRetrieval()) {
            //Log.d("Offline messages not supported");
            return;
        }

        if (offlineMessageManager.getMessageCount() == 0) {
            //Log.d("No offline messages found on server");
        } else {
            List<Message> msgs = offlineMessageManager.getMessages();
            for (Message msg : msgs) {

            }
            offlineMessageManager.deleteMessages();
        }
    }

    public int offlinemessagecount(final XMPPConnection connection){
        try {

            ServiceDiscoveryManager manager = ServiceDiscoveryManager
                    .getInstanceFor(connection);
            DiscoverInfo info = manager.discoverInfo(null,
                    "http://jabber.org/protocol/offline");
            Form extendedInfo = Form.getFormFrom(info);
            if (extendedInfo != null) {
                String value = extendedInfo.getField("number_of_messages")
                        .getValues().get(0);

                return Integer.parseInt(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;

    }
    @Override
    public void connectionClosed() {

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
        NetworkStateManager.setCurrentUserPresence(new Presence(Presence.Type.unavailable), userAppPreference.getJidStringKey());
    }
}
