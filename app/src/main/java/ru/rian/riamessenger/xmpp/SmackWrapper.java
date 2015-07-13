package ru.rian.riamessenger.xmpp;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.DomainpartJid;
import org.jxmpp.jid.impl.JidCreate;

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
import ru.rian.riamessenger.riaevents.response.XmppErrorEvent;

/**
 * Created by Roman on 7/8/2015.
 */

@RequiredArgsConstructor
public class SmackWrapper {

    final Context context;
    final UserAppPreference userAppPreference;
    //final RosterConnectingTimer rosterConnectingTimer;

    private Handler xmppConnectingHandler = new Handler();
    private Runnable xmppConnectingRunnable = new Runnable() {
        @Override
        public void run() {
            xmppConnectingHandler.removeCallbacks(xmppConnectingRunnable);
            if(!roster.isLoaded()) {
                xmppConnectingHandler.postDelayed(xmppConnectingRunnable, RiaConstants.CONNECTING_TIME_OUT);
                rosterConnectingTryCounter++;
                RiaEventBus.post("try to connect again" + rosterConnectingTryCounter);
                connect();
            }
        }
    };

    volatile AbstractXMPPConnection xmppConnection;

    @Getter
    Roster roster;

    // private XmppMessageManager 				mMessageManager;
    // private XmppRosterManager				mRosterManager;
    //private XmppMucManager					mMucManager;

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
        if (!TextUtils.isEmpty(userAppPreference.getLoginStringKey()) && !TextUtils.isEmpty(userAppPreference.getPassStringKey())) {
            //connect();
            xmppConnectingHandler.removeCallbacks(xmppConnectingRunnable);
            xmppConnectingHandler.postDelayed(xmppConnectingRunnable, RiaConstants.CONNECTING_TIME_OUT);
            connect();
            /*rosterConnectingTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    rosterConnectingTryCounter++;
                    Log.i("SmackWrapper", "try to connect again, couse of roster exception");


                }
            });*/
        }
    }

    void connect() {
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
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
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

            if (xmppConnection != null && xmppConnection.isConnected()) {
                xmppConnection.disconnect();
            }
            xmppConnection = new XMPPTCPConnection(configBuilder.build());

            xmppConnection.addConnectionListener(new SmackConnectionListener());
            xmppConnection.setPacketReplyTimeout(RiaConstants.CONNECTING_TIME_OUT);
            roster = Roster.getInstanceFor(xmppConnection);
            roster.addRosterLoadedListener(new SmackRosterLoadedListener());
            roster.addRosterListener(new SmackRosterListener());


            // Connect to the server
            xmppConnection.connect();
            // Log into the server
            xmppConnection.login();

            //mRosterManager 	= new XmppRosterManager(context, xmppConnection);
            //mMessageManager	= new XmppMessageManager(context, xmppConnection);
            //mMucManager		= new XmppMucManager(context, xmppConnection);

            // Create a new presence. Pass in false to indicate we're unavailable._
            Presence presence = new Presence(Presence.Type.available);
            presence.setStatus("Working");
            // Send the packet (assume we have an XMPPConnection instance called "con").
            xmppConnection.sendStanza(presence);
            //QueryHelper.updateUser(ChatConstants.CURRENT_LOCAL_USER_ID, mLogin+"@"+mServer, mLogin);

            //Log.d(TAG, "on connected");
        } catch (Exception e) {
            RiaEventBus.post(e.getMessage());
            e.printStackTrace();
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
