package ru.rian.riamessenger.services;
//TODO: Update this so that UI activities can just get references to the service
// rather than using AIDL
//TODO: Above seems to be done and working, but now need to update methods
// to just return asmack objects such as Roster rather than the simple wrappers

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
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
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import bolts.Continuation;
import bolts.Task;
import de.greenrobot.event.EventBus;
import ru.rian.riamessenger.RiaBaseApplication;
import ru.rian.riamessenger.common.RiaConstants;
import ru.rian.riamessenger.common.RiaEventBus;
import ru.rian.riamessenger.di.AppSystemModule;
import ru.rian.riamessenger.di.DaggerXmppServiceComponent;
import ru.rian.riamessenger.di.XmppModule;
import ru.rian.riamessenger.di.XmppServiceComponent;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.riaevents.connection.InternetConnEvent;
import ru.rian.riamessenger.riaevents.request.RiaMessageEvent;
import ru.rian.riamessenger.riaevents.request.RiaServiceEvent;
import ru.rian.riamessenger.riaevents.response.XmppErrorEvent;
import ru.rian.riamessenger.utils.NetworkStateManager;
import ru.rian.riamessenger.xmpp.SmackConnectionListener;
import ru.rian.riamessenger.xmpp.SmackRosterListener;
import ru.rian.riamessenger.xmpp.SmackRosterLoadedListener;
import ru.rian.riamessenger.xmpp.XmppMessageManager;


public class RiaXmppService extends Service {

    private static RiaXmppService instance = null;

    /*public static boolean isInstanceCreated() {
        return instance != null;
    }*/

    private XmppServiceComponent xmppServiceComponent;
    private static Context mContext = null;

    public static XmppServiceComponent component() {
        return getContext().xmppServiceComponent;
    }

    public RiaXmppService() {
        super();
        Log.i("RiaService", "RiaService()");
        RiaBaseApplication.component().inject(this);
    }

    @Inject
    UserAppPreference userAppPreference;


    private Handler xmppConnectingHandler = new Handler();
    private Runnable xmppConnectingRunnable = new Runnable() {
        @Override
        public void run() {
            if (roster == null || !roster.isLoaded()) {
                loadRosterSync();
            }

        }
    };

    AbstractXMPPConnection xmppConnection;
    XmppMessageManager xmppMessageManager;
    //OfflineMessageManager offlineMessageManager;

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
        if (DoLoginAndPassExist() && NetworkStateManager.isNetworkAvailable(this)) {
            connect();
        }
    }

    public void sendMessage(final String jid, final String messageText) {
        if (xmppMessageManager != null) {
            xmppMessageManager.sendMessage(jid, messageText);
        }
    }

    public void loadRosterSync() {
        Log.i("RiaService", "loadRosterSync ()");

        xmppConnectingHandler.removeCallbacks(xmppConnectingRunnable);
        xmppConnectingHandler.postDelayed(xmppConnectingRunnable, RiaConstants.GETTING_ROSTER_NEXT_TRY_TIME_OUT);

        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() {
                if (isAuthenticated()) {
                    try {
                        Log.i("RiaService", "try to load again" + rosterConnectingTryCounter++);
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
        Log.i("RiaService", "connect ()");
        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() {
                doConnect();
                return null;
            }
        }).continueWith(new Continuation<Object, Void>() {
            public Void then(Task<Object> object) throws Exception {
                loadRosterSync();
                return null;
            }
        }, Task.BACKGROUND_EXECUTOR);
    }

    boolean DoLoginAndPassExist() {
        return !TextUtils.isEmpty(userAppPreference.getLoginStringKey()) && !TextUtils.isEmpty(userAppPreference.getPassStringKey());
    }

    synchronized private void doConnect() {
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
            configBuilder.setSendPresence(false);

            configBuilder.setHost(RiaConstants.XMPP_SERVER_ADDRESS);


            xmppConnection = new XMPPTCPConnection(configBuilder.build());
            xmppConnection.addConnectionListener(new SmackConnectionListener(userAppPreference));

            roster = Roster.getInstanceFor(xmppConnection);
            roster.setRosterLoadedAtLogin(false);

            roster.addRosterLoadedListener(new SmackRosterLoadedListener(this, userAppPreference));
            roster.addRosterListener(new SmackRosterListener());

            // Connect to the server
            xmppConnection.connect();
            xmppConnection.setPacketReplyTimeout(RiaConstants.CONNECTING_TIME_OUT);
            xmppConnection.login();

            //offlineMessageManager = new OfflineMessageManager(xmppConnection);
            xmppMessageManager = new XmppMessageManager(this, xmppConnection);

            //Presence presence = new Presence(Presence.Type.available);
            //xmppConnection.sendStanza(presence);

            Log.i("RiaService", "Presence.Type.available");

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

    public void onEvent(RiaMessageEvent event) {
        sendMessage(event.getJid(), event.getMessage());
    }

    public void onEvent(RiaServiceEvent event) {

        switch (event.getEventId()) {
            case SIGN_IN:
                Log.i("RiaService", "SIGN_IN");
                if (isAuthenticated()) {
                    RiaEventBus.post(XmppErrorEvent.State.EAuthenticated);
                } else {
                    connectAndSingIn();
                }
                break;
            case SIGN_OUT:
                Log.i("RiaService", "SIGN_OUT");
                stopSelf();
                break;
           /* case GET_ROSTER:
                if (!isConnecting()) {
                    Roster roster = getRoster();
                    if (roster != null) {
                        if (roster.isLoaded()) {
                            RiaEventBus.post(XmppErrorEvent.State.EDbUpdated);
                        } else {
                            loadRosterSync();
                        }
                    }
                }
                break;*/
        }
    }


    static RiaXmppService getContext() {
        return (RiaXmppService) mContext;
    }


    private static final int NOTIFICATION_CONNECTION_STATUS = 1;
    //  private int mClientCount = 0;


    public NotificationManager getNotifyManager() {
        return (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void onEvent(final InternetConnEvent internetConnEvent) {
        if (internetConnEvent.isConnectionAvailable()) {
            if(!isAuthenticated()) {
                connectAndSingIn();
            }
        }
        else {
            NetworkStateManager.setCurrentUserPresence(new Presence(Presence.Type.unavailable), userAppPreference.getJidStringKey());
        }
    }

    @Override
    public void onCreate() {
        Log.i("RiaService", "onCreate");
        EventBus.getDefault().register(this);
        mContext = this;
        xmppServiceComponent = DaggerXmppServiceComponent.builder()
                .appSystemModule(new AppSystemModule(getApplication()))
                .xmppModule(new XmppModule(this, userAppPreference))
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.i("RiaService", "onStartCommand()");

        onStartService();

        return START_STICKY_COMPATIBILITY;
    }

    /**
     * If the service starts, it means that we don't have yet a connection, roster and so on..
     * The method checks login and password. If login and password exist it starts connecting ,
     * if it is not - it sends auth event to client (probably we don't need it)
     */
    void onStartService() {
        connectAndSingIn();
    }

    /*void postAuthEvent() {
        EventBus.getDefault().post(new AuthClientEvent(isAuthenticated(), isConnected()));
    }*/

    @Override
    public void onDestroy() {
        Log.i("RiaService", "onDestroy");
        mContext = null;
        //userAppPreference.setRiaXmppServiceStartedFlag(false);
        EventBus.getDefault().unregister(this);
        getNotifyManager().cancel(NOTIFICATION_CONNECTION_STATUS);

        /*synchronized (mActiveNotifications) {
            Iterator<Integer> i = mActiveNotifications.iterator();
            while (i.hasNext()) {
                //It's possible this has already been cleared from the user
                //clicking on the notification, but clearing it here
                //does not hurt anything and is simpler than deleting it
                //out of the Set when it is clicked.
                Integer notificationId = (Integer) i.next();
                getNotifyManager().cancel(notificationId);
            }
            mActiveNotifications.clear();
        }*/
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
