package ru.rian.riamessenger.xmpp;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.DomainpartJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;
import de.greenrobot.event.EventBus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.rian.riamessenger.common.RiaConstants;
import ru.rian.riamessenger.common.RiaEventBus;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.riaevents.request.RiaServiceEvent;
import ru.rian.riamessenger.riaevents.response.XmppErrorEvent;

/**
 * Created by Roman on 7/8/2015.
 */

@RequiredArgsConstructor
public class SmackWrapper {

    final Context context;
    final UserAppPreference userAppPreference;

    private Handler xmppConnectingHandler = new Handler();
    private Runnable xmppConnectingRunnable = new Runnable() {
        @Override
        public void run() {
            if (roster == null || !roster.isLoaded()) {
                rosterConnectingTryCounter++;
                Log.i("SmackWrapper", "try to connect again = " + rosterConnectingTryCounter);
                RiaEventBus.post("try to connect again" + rosterConnectingTryCounter);
                loadRosterSync();
            }

        }
    };

    /*
    SmackRosterListener smackRosterListener;
    SmackRosterLoadedListener smackRosterLoadedListener;
    SmackConnectionListener smackConnectionListener;
    */
    AbstractXMPPConnection xmppConnection;
    XmppMessageManager xmppMessageManager;
    @Getter
    Roster roster;

    public boolean isConnected() {
        boolean connected = false;
        if (xmppConnection != null) {
            connected = xmppConnection.isConnected();
        }
        Log.i("RiaService", "connected = " + connected);
        return connected;
    }

    public boolean isAuthenticated() {
        boolean authenticated = false;
        Log.i("RiaService", "xmppConnection = " + xmppConnection);
        if (xmppConnection != null) {
            authenticated = xmppConnection.isAuthenticated();
        }
        Log.i("RiaService", "authenticated = " + authenticated);
        return authenticated;
    }

    int rosterConnectingTryCounter = 0;

    public void connectAndSingIn() {
        if (DoLoginAndPassExist()) {
            connect();
        }
    }

    public void sendMessage(final String jid, final String messageText) {
        if (xmppMessageManager != null) {
            xmppMessageManager.sendMessage(jid, messageText);
        }
    }

    public void loadRosterSync() {
        Log.i("SmackWrapper", "loadRosterSync ()");

        xmppConnectingHandler.removeCallbacks(xmppConnectingRunnable);
        xmppConnectingHandler.postDelayed(xmppConnectingRunnable, RiaConstants.GETTING_ROSTER_NEXT_TRY_TIME_OUT);

        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() {
                if (isAuthenticated()) {
                    try {
                        RiaEventBus.post("try to connect again" + rosterConnectingTryCounter++);
                        roster.reload();
                    } catch (SmackException.NotLoggedInException e) {
                        e.printStackTrace();
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    connect();
                }
                return null;
            }
        });
    }


    void connect() {
        Log.i("SmackWrapper", "connect ()");
        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() {
                isConnecting = true;
                doConnect();
                return null;
            }
        }).continueWith(new Continuation<Object, Void>() {
            public Void then(Task<Object> object) throws Exception {
                isConnecting = false;
                loadRosterSync();
                return null;
            }
        }, Task.BACKGROUND_EXECUTOR);
    }

    /* isConnecting = false;
                    xmppConnectingHandler.removeCallbacks(xmppConnectingRunnable);
                    if (DoLoginAndPassExist()) {
                        xmppConnectingHandler.postDelayed(xmppConnectingRunnable, RiaConstants.CONNECTING_TIME_OUT);
                    }
                    return null;*/
    boolean DoLoginAndPassExist() {
        if (!TextUtils.isEmpty(userAppPreference.getLoginStringKey()) && !TextUtils.isEmpty(userAppPreference.getPassStringKey())) {
            return true;
        }
        return false;
    }

    @Getter
    boolean isConnecting = false;

    private void doConnect() {
        try {

            final String username = userAppPreference.getLoginStringKey();
            final String password = userAppPreference.getPassStringKey();

            //QueryHelper.setOfflineStatus();
            // Create the configuration for this new connection
            XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
            configBuilder.setUsernameAndPassword(username, password);
            configBuilder.setResource(RiaConstants.XMPP_RESOURCE_NAME);

            DomainBareJid serviceName = JidCreate.domainBareFrom(RiaConstants.XMPP_SERVICE_NAME);

            configBuilder.setServiceName(serviceName);
            configBuilder.setPort(5222);
            configBuilder.setDebuggerEnabled(true);
            configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
            configBuilder.setSendPresence(true);

            configBuilder.setHost(RiaConstants.XMPP_SERVER_ADDRESS);


            xmppConnection = new XMPPTCPConnection(configBuilder.build());
            xmppConnection.addConnectionListener(new SmackConnectionListener(userAppPreference));

            roster = Roster.getInstanceFor(xmppConnection);
            roster.setRosterLoadedAtLogin(false);

            roster.addRosterLoadedListener(new SmackRosterLoadedListener(context));
            roster.addRosterListener(new SmackRosterListener());

            // Connect to the server
            xmppConnection.connect();
            xmppConnection.setPacketReplyTimeout(RiaConstants.CONNECTING_TIME_OUT);
            xmppConnection.login();

            xmppMessageManager = new XmppMessageManager(xmppConnection);

            Presence presence = new Presence(Presence.Type.available);
            xmppConnection.sendStanza(presence);

            Log.i("SmackWrapper", "Presence.Type.available");

        } catch (SmackException e) {
            RiaEventBus.post("doConnect!:" + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMPPException e) {
            e.printStackTrace();
            userAppPreference.setLoginStringKey("");
            userAppPreference.setPassStringKey("");
            RiaEventBus.post(XmppErrorEvent.State.EAuthenticationFailed);
        }
    }

    public void sendMessage(int chatType, long chatId, String messageText) {
        switch (chatType) {
            case ChatConstants.SINGLE_CHAT_STATE:
                /*if (mMessageManager != null) {
                    mMessageManager.sendMessage(chatId, messageText);
                }*/
                break;

            case ChatConstants.MULTICHAT_CHAT_STATE:
                /*if(mMucManager != null){
                    mMucManager.sendMessage(chatId, messageText);
                }*/
                break;
        }
    }

    public void addUser(String jid, String name, String group) {
        /*if(mRosterManager!= null){
            mRosterManager.addUser( jid, name, group);
        }*/
    }

    // delete user
    public void deleteUserFromRoster(long userId) {
        /*if(mRosterManager != null){
            mRosterManager.deleteUserFromRoster(userId);
        }*/
    }

    // MUC
    public void joinMuc(String host, String room, String nickname, String password) {
        /*if(mMucManager != null){
            mMucManager.addJoinMuc(host,room, nickname, password);
        }*/
    }

    /**
     * need call when leave chat
     */
    public void leaveMuc() {
        /*if(mMucManager != null){
            mMucManager.leaveMuc();
        }*/
    }
}
