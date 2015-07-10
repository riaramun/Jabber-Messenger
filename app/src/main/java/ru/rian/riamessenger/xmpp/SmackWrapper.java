package ru.rian.riamessenger.xmpp;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.activeandroid.ActiveAndroid;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smack.roster.RosterLoadedListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.security.acl.Group;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import bolts.Continuation;
import bolts.Task;
import de.greenrobot.event.EventBus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.rian.riamessenger.model.RosterEntryModel;
import ru.rian.riamessenger.model.RosterGroupModel;
import ru.rian.riamessenger.riaevents.client.RosterClientEvent;
import ru.rian.riamessenger.riaevents.service.ConnectionStatus;
import ru.rian.riamessenger.common.RiaConstants;
import ru.rian.riamessenger.prefs.UserAppPreference;

/**
 * Created by Roman on 7/8/2015.
 */

@RequiredArgsConstructor
public class SmackWrapper {

    final Context context;
    final ConnectionListener connectionListener;
    final UserAppPreference userAppPreference;

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

    final Timer rosterLoaderWaitingTimer = new Timer();

    public boolean connectAndSingIn() {
        //this is work around the smack exception
/*        rosterLoaderWaitingTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                Log.i("SmackWrapper", "try connect again, couse of roster exception");
            }
        }, 100, 6000);
*/
        boolean ret = false;
        if (!TextUtils.isEmpty(userAppPreference.getLoginStringKey()) && !TextUtils.isEmpty(userAppPreference.getPassStringKey())) {
            ret = true;
            connect();
        }
        return ret;
        //end work around
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

    public void saveRosterToDb(final Roster roster) {
        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() {
                doSaveRosterToDb(roster);
                return null;
            }
        }).continueWith(new Continuation<Object, Void>() {
            public Void then(Task<Object> object) throws Exception {
                EventBus.getDefault().postSticky(new RosterClientEvent(RosterClientEvent.RiaEvent.DB_UPDATED));
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    void doSaveRosterToDb(final Roster roster) {
        Log.i("SmackWrapper", "doSaveRosterToDb b");
        try {
            ActiveAndroid.beginTransaction();
            for (RosterGroup rosterGroup : roster.getGroups()) {
                RosterGroupModel rosterGroupModel = new RosterGroupModel();
                rosterGroupModel.name = rosterGroup.getName();
                // rosterGroupModel.remoteId = rosterGroup.getName().hashCode();
                rosterGroupModel.save();

                for (RosterEntry rosterEntry : rosterGroup.getEntries()) {

                    RosterEntryModel rosterEntryModel = new RosterEntryModel();

                    rosterEntryModel.name = rosterEntry.getName();
                    // rosterEntryModel.remoteId = rosterEntry.hashCode();
                    rosterEntryModel.rosterGroupModel = rosterGroupModel;
                    rosterEntryModel.save();
                    //rosterGroupModel.items().add(rosterEntryModel);
                }
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
        Log.i("SmackWrapper", "doSaveRosterToDb e");
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
            configBuilder.setResource("mobile");
            configBuilder.setServiceName("kis-jabber");
            configBuilder.setPort(5222);
            configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
            configBuilder.setSendPresence(true);
            configBuilder.setHost(RiaConstants.XMPP_SERVER_ADDRESS);

            if (xmppConnection != null && xmppConnection.isConnected()) {
                //xmppConnection.disconnect();
                //xmppConnection.removeConnectionListener(connectionListener);
            }
            xmppConnection = new XMPPTCPConnection(configBuilder.build());
            xmppConnection.addConnectionListener(connectionListener);

            roster = Roster.getInstanceFor(xmppConnection);
            roster.addRosterLoadedListener(new RosterLoadedListener() {
                @Override
                public void onRosterLoaded(Roster roster) {
                    //if we've got roster we can cancel our connection timer
                    // rosterLoaderWaitingTimer.cancel();
                    saveRosterToDb(roster);
                }
            });

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
            sendOnConnectedMessage(ConnectionStatus.CONNECTION_STATUS_OK);
            //Log.d(TAG, "on connected");
        } catch (SmackException.ConnectionException e) {
            sendOnConnectedMessage(ConnectionStatus.CONNECTION_STATUS_ERROR_CONNECTION);
        } catch (SmackException.NoResponseException e) {
            sendOnConnectedMessage(ConnectionStatus.CONNECTION_STATUS_ERROR_NORESPONSE);
        } catch (Exception e) {
            e.printStackTrace();
            sendOnConnectedMessage(ConnectionStatus.CONNECTION_STATUS_ERROR_UNKNOWN);
        }
    }

    private void sendOnConnectedMessage(int msg) {

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
