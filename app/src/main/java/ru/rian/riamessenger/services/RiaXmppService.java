package ru.rian.riamessenger.services;
//TODO: Update this so that UI activities can just get references to the service
// rather than using AIDL
//TODO: Above seems to be done and working, but now need to update methods
// to just return asmack objects such as Roster rather than the simple wrappers

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.roster.Roster;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import lombok.Getter;
import ru.rian.riamessenger.RiaBaseApplication;
import ru.rian.riamessenger.di.DaggerXmppServiceComponent;
import ru.rian.riamessenger.di.SystemServicesModule;
import ru.rian.riamessenger.di.XmppModule;
import ru.rian.riamessenger.di.XmppServiceComponent;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.riaevents.client.AuthClientEvent;
import ru.rian.riamessenger.riaevents.client.RosterClientEvent;
import ru.rian.riamessenger.riaevents.service.RiaServiceEvent;
import ru.rian.riamessenger.xmpp.SmackWrapper;


public class RiaXmppService extends Service implements ConnectionListener {

    private static RiaXmppService instance = null;

    public static boolean isInstanceCreated() {
        return instance != null;
    }

    private XmppServiceComponent xmppServiceComponent;
    private static Context mContext = null;

    public static XmppServiceComponent component() {
        return getContext().xmppServiceComponent;
    }

    public RiaXmppService() {
        super();
        Log.i("RiaService", "RiaService()");
        RiaBaseApplication.component().inject(this);
        smackWrapper = new SmackWrapper(this, this, userAppPreference);
    }

    @Inject
    UserAppPreference userAppPreference;


    final SmackWrapper smackWrapper;


    public void onEvent(RiaServiceEvent event) {

        switch (event.getEventId()) {
            case SIGN_IN:
                Log.i("RiaService", "SIGN_IN");
                if (smackWrapper.isAuthenticated()) {
                    postAuthEvent();
                } else {
                    smackWrapper.connectAndSingIn();
                }
                break;
            case SIGN_OUT:
                Log.i("RiaService", "SIGN_OUT");
                stopSelf();
                break;
            case GET_ROSTER:
                if (!smackWrapper.isConnecting()) {
                    Roster roster = smackWrapper.getRoster();
                    if (roster != null) {
                        if (roster.isLoaded()) {
                            EventBus.getDefault().postSticky(new RosterClientEvent(RosterClientEvent.RiaEvent.DB_UPDATED));
                        } else {
                            if (smackWrapper.isAuthenticated()) {
                                try {
                                    roster.reload();
                                } catch (SmackException.NotLoggedInException e) {
                                    e.printStackTrace();
                                } catch (SmackException.NotConnectedException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                onStartService();
                            }
                        }
                    }
                }
                break;
        }
    }


    static RiaXmppService getContext() {
        return (RiaXmppService) mContext;
    }


    private static final int NOTIFICATION_CONNECTION_STATUS = 1;
    private int mClientCount = 0;


    public NotificationManager getNotifyManager() {
        return (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Getter
    private Set<Integer> mActiveNotifications =
            Collections.synchronizedSet(new LinkedHashSet<Integer>());

    @Override
    public void onCreate() {
        Log.i("RiaService", "onCreate");
        EventBus.getDefault().register(this);
        mContext = this;
        xmppServiceComponent = DaggerXmppServiceComponent.builder()
                .systemServicesModule(new SystemServicesModule(getApplication()))
                .xmppModule(new XmppModule(this, this, userAppPreference))
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.i("RiaService", "onStartCommand()");

        onStartService();

        return START_STICKY;
    }

    /**
    If the service starts, it means that we don't have yet a connection, roster and so on..
    The method cheks login and password. If login and password exist it starts connecting ,
    if it is not - it sends auth event to client (probably we don't need it)
    */
    void onStartService() {
        if (smackWrapper.connectAndSingIn()) {
        } else {
            //postAuthEvent();
        }
    }

    void postAuthEvent() {
        EventBus.getDefault().post(new AuthClientEvent(smackWrapper.isAuthenticated(), smackWrapper.isConnected()));
    }

    @Override
    public void onDestroy() {
        Log.i("RiaService", "onDestroy");
        mContext = null;
        userAppPreference.setRiaXmppServiceStartedFlag(false);
        EventBus.getDefault().unregister(this);
        getNotifyManager().cancel(NOTIFICATION_CONNECTION_STATUS);

        synchronized (mActiveNotifications) {
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
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void connected(XMPPConnection connection) {
        Log.i("RiaService", "connected()");
        postAuthEvent();
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        Log.i("RiaService", "authenticated()");
        postAuthEvent();
    }

    //Listener interfaces
    @Override
    public void connectionClosed() {
        Log.i("RiaService", "connectionClosed()");
        postAuthEvent();
    }

    @Override
    public void connectionClosedOnError(Exception arg0) {
       /*
        //TODO: strings here from strings.xml
        //TODO: Intent to stop reconnection attempts does not exist
        //or do anything yet
        int icon = android.R.drawable.sym_action_chat;
        CharSequence tickerText = "jmXMPP Connection Lost";
        long when = System.currentTimeMillis();
        Notification notification = new Notification(icon, tickerText, when);
        notification.flags |= Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        Intent i = new Intent();
        PendingIntent contentIntent = PendingIntent.getActivity(
                RiaXmppService.this, 0, i, 0);
        notification.setLatestEventInfo(getApplicationContext(),
                "jmXMPP Reconnecting", "Click to cancel", contentIntent);
        getNotifyManager().notify(NOTIFICATION_CONNECTION_STATUS, notification);*/
    }

    @Override
    public void reconnectingIn(int arg0) {
        Log.i("RiaService", "reconnectingIn arg0 =" + arg0);
    }

    @Override
    public void reconnectionFailed(Exception arg0) {
        Log.i("RiaService", "reconnectionFailed arg0 =" + arg0);
    }

    @Override
    public void reconnectionSuccessful() {

    }


    /*private void displayServiceNotification() {
        int icon = android.R.drawable.sym_action_chat;
        CharSequence tickerText = "XMPP Service Started";
        long when = System.currentTimeMillis();

        Notification notification = new Notification(icon, tickerText, when);
        notification.flags |=
                Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        Intent i = new Intent("ru.rian.riamessenger.services.STOP_CONNECTION_SERVICE");
        PendingIntent contentIntent = PendingIntent.getBroadcast(
                RiaXmppService.this, 0, i, 0);
        notification.setLatestEventInfo(getApplicationContext(),
                "jmXMPP Connected", "Click to disconnect", contentIntent);
        getNotifyManager().notify(NOTIFICATION_CONNECTION_STATUS, notification);
    }*/

    // @Inject
    // private Roster roster;

    // private ConnectionConfiguration mConnConfig;
    // Test return reference to the service directly
   /* private final IBinder myBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public RiaService getService() {
            return RiaService.this;
        }
    }*/


    /*@Override
    public IBinder onBind(Intent arg0) {
        Log.i("RiaService", "onBind");
        displayServiceNotification();
        ++mClientCount;
        return myBinder;
    }

    @Override
    public boolean onUnbind(Intent arg0) {
        Log.i("RiaService", "onUnbind");
        --mClientCount;
        return true;
    }*/

/*


    @Inject
    RiaRosterListener riaRosterListener;

    @Inject
    ChatMessageListenerWrap chatMessageListenerWrap;

    ChatManagerListener riaChatManagerListener = new ChatManagerListener() {
        @Override
        public void chatCreated(Chat chat, boolean createdLocally) {
            chat.addMessageListener(chatMessageListenerWrap);
        }
    };

    @Getter
    XMPPTCPConnection xmpptcpConnection;

    @Getter
    Roster roster;

    public interface SignInListener {
        public void onSignedIn(boolean isConnected, boolean isAuthenticated);
    }

    public void signIn(final Context context, final SignInListener signInListener, final RosterLoadedListener riaRosterLoadedListener) {

        final String username = userAppPreference.getLoginStringKey();
        final String password = userAppPreference.getPassStringKey();

        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() {
                try {
                    final XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
                    configBuilder.setServiceName(RiaConstants.XMPP_SERVICE_NAME);
                    configBuilder.setHost(RiaConstants.XMPP_SERVER_ADDRESS);
                    configBuilder.setResource("test");
                    configBuilder.setDebuggerEnabled(true);
                    configBuilder.setPort(5222);
                    configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);

                    xmpptcpConnection = new XMPPTCPConnection(configBuilder.build());

                    if (!xmpptcpConnection.isConnected()) {
                        xmpptcpConnection.connect();
                    }
                    if (!xmpptcpConnection.isAuthenticated()) {
                        xmpptcpConnection.login(username, password);
                    }
                    roster = Roster.getInstanceFor(xmpptcpConnection);
                    roster.addRosterListener(riaRosterListener);
                    roster.addRosterLoadedListener(riaRosterLoadedListener);

                    ChatManager.getInstanceFor(xmpptcpConnection).addChatListener(riaChatManagerListener);
                    System.out.println("Logged in!");

                } catch (Exception e) {
                    xmpptcpConnection.disconnect();
                    e.printStackTrace();
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                }
                return null;
            }
        }).continueWith(new Continuation<Object, Void>() {
            public Void then(Task<Object> object) throws Exception {
                signInListener.onSignedIn(isConnected(), isAuthenticated());
                if (!isConnected()) {
                    Toast.makeText(context, context.getText(R.string.no_response_server), Toast.LENGTH_LONG).show();
                } else if (!isAuthenticated()) {
                    Toast.makeText(context, context.getText(R.string.sign_in_error), Toast.LENGTH_LONG).show();
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    public void sendMessage(String to, String message) throws XMPPException {
        //Why don't I have map of Chat objects?  Seems like I tried once and it
        //would not work for some reason.  Try again and comment the issue
        //if there is one
        Chat chat = ChatManager.getInstanceFor(xmpptcpConnection).createChat(to,
                null);
        try {
            chat.sendMessage(message);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }
    /*if (intent != null) {
            displayServiceNotification();
            registerReceiver(new BroadcastReceiver() {
                                 @Override
                                 public void onReceive(Context context, Intent intent) {
                                     stopSelf();
                                 }
                             },
                    new IntentFilter("ru.rian.riamessenger.services.STOP_CONNECTION_SERVICE"));
        }*/
}
