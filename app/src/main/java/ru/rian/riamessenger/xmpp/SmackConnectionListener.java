package ru.rian.riamessenger.xmpp;

import android.content.Context;
import android.util.Log;

import com.activeandroid.ActiveAndroid;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.offline.OfflineMessageManager;

import java.util.List;
import java.util.concurrent.Callable;

import bolts.Task;
import lombok.AllArgsConstructor;
import ru.rian.riamessenger.common.RiaEventBus;
import ru.rian.riamessenger.model.MessageContainer;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.riaevents.response.XmppErrorEvent;
import ru.rian.riamessenger.services.RiaXmppService;
import ru.rian.riamessenger.utils.DbHelper;
import ru.rian.riamessenger.utils.NetworkStateManager;
import ru.rian.riamessenger.utils.SysUtils;
import ru.rian.riamessenger.utils.XmppUtils;

/**
 * Created by Roman on 7/12/2015.
 */
@AllArgsConstructor
public class SmackConnectionListener implements ConnectionListener {


    final Context context;
    final UserAppPreference userAppPreference;
    final SendMsgBroadcastReceiver sendMsgBroadcastReceiver;

    @Override
    public void connected(XMPPConnection connection) {
        Log.i(RiaXmppService.TAG, "EConnected");
        RiaEventBus.post(XmppErrorEvent.State.EConnected);
    }

    @Override
    public void authenticated(final XMPPConnection connection, boolean resumed) {
        RiaEventBus.post(XmppErrorEvent.State.EAuthenticated);
        Log.i(RiaXmppService.TAG, "EAuthenticated");
        //add current user entry to track his presence via loader
        userAppPreference.setAuthStateKey(true);
        userAppPreference.setJidStringKey(connection.getUser().asEntityBareJidString());
        try {
            handleOfflineMessages(connection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        XmppUtils.changeCurrentUserStatus(new Presence(Presence.Type.available), userAppPreference.getJidStringKey(), connection);
        /*
        DeliveryReceiptManager.getInstanceFor(connection).setAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.always);
        DeliveryReceiptManager.getInstanceFor(connection).addReceiptReceivedListener(new ReceiptReceivedListener() {
            @Override
            public void onReceiptReceived(Jid fromJid, Jid toJid, String receiptId, Stanza receipt) {
                Log.i(RiaXmppService.TAG, "receiptId = " + receiptId + " receipt = " + receipt.toString());
            }
        });*/

    }

    /* void getOfflineMessages()
     {
         PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
         this.connection.addPacketListener(new PacketListener() {
             public void processPacket(Packet packet) {

                 Message message = (Message) packet;
                 if (message.getBody() != null) {
                     String fromName = StringUtils.parseBareAddress(message
                             .getFrom());
                     Log.i("XMPPClient", "Got text [" + message.getBody()
                             + "] from [" + fromName + "]");
                     if (fromName.equalsIgnoreCase(matchUserJabberId
                             + "server name")) {


                         // }
                     }
                 }
             }
         }, filter);
     }*/
    public void handleOfflineMessages(XMPPConnection connection) throws Exception {
        final OfflineMessageManager offlineMessageManager = new OfflineMessageManager(connection);

        if (!offlineMessageManager.supportsFlexibleRetrieval()) {
            Log.i(RiaXmppService.TAG, "Offline messages not supported");
            return;
        }
        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                if (offlineMessageManager.getMessageCount() == 0) {
                    Log.i(RiaXmppService.TAG, "No offline messages found on server");
                } else {
                    MessageContainer messageContainer = null;
                    try {
                        ActiveAndroid.beginTransaction();
                        List<Message> messages = offlineMessageManager.getMessages();
                        if (messages.size() > 0) {
                            Log.i(RiaXmppService.TAG, "offline messages = " + messages.size());
                            for (Message msg : messages) {
                                int msgType = MessageContainer.CHAT_SIMPLE;
                                //int msgType = msg.getType() == Message.Type.groupchat ? MessageContainer.CHAT_GROUP : MessageContainer.CHAT_SIMPLE;
                                /*String msgId;
                                if(msgType == MessageContainer.CHAT_SIMPLE) {
                                    msgId = msg.getFrom().asEntityBareJidIfPossible().toString();
                                }
                                else {
                                    msgId = msg.getThread()
                                }*/
                                messageContainer = DbHelper.addMessageToDb(msg, msgType, msg.getFrom().asEntityBareJidIfPossible().toString(), false);
                            }
                        }
                        ActiveAndroid.setTransactionSuccessful();
                    } finally {
                        ActiveAndroid.endTransaction();
                    }
                    if (messageContainer != null && SysUtils.isApplicationBroughtToBackground(context)) {
                        sendMsgBroadcastReceiver.sendOrderedBroadcastIntent(messageContainer);
                    }
                    offlineMessageManager.deleteMessages();
                }
                return null;
            }
        });
    }

    /*class EntrySortBasedOnName implements Comparator {
        public int compare(Object o1, Object o2) {
            val dd1 = (Message) o1;// where FBFriends_Obj is your object class
            val dd2 = (Message) o2;
            return dd1.compareToIgnoreCase(dd2.getName());//where uname is field name
        }
    }*/
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
        userAppPreference.setAuthStateKey(false);
        RiaEventBus.post(XmppErrorEvent.State.EReconnectionFailed);
        NetworkStateManager.setCurrentUserPresence(new Presence(Presence.Type.unavailable), userAppPreference.getJidStringKey());
    }
}
