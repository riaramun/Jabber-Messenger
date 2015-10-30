package ru.rian.riamessenger.xmpp;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.packet.id.StanzaIdUtil;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.List;
import java.util.concurrent.Callable;

import bolts.Task;
import de.greenrobot.event.EventBus;
import ru.rian.riamessenger.common.RiaEventBus;
import ru.rian.riamessenger.model.MessageContainer;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.riaevents.response.XmppErrorEvent;
import ru.rian.riamessenger.utils.DbHelper;
import ru.rian.riamessenger.utils.SysUtils;

/**
 * Created by grigoriy on 29.06.15.
 */
public class SmackMessageManager implements ReceiptReceivedListener, StanzaListener {

    static final String TAG = "RiaService";
    final AbstractXMPPConnection xmppConnection;
    final Context context;
    final UserAppPreference userAppPreference;
    final SendMsgBroadcastReceiver sendMsgBroadcastReceiver;
    final DeliveryReceiptManager deliveryReceiptManager;

    public SmackMessageManager(Context context, AbstractXMPPConnection connection, SendMsgBroadcastReceiver sendMsgBroadcastReceiver, UserAppPreference userAppPreference) {
        xmppConnection = connection;
        this.sendMsgBroadcastReceiver = sendMsgBroadcastReceiver;
        deliveryReceiptManager = DeliveryReceiptManager.getInstanceFor(xmppConnection);
        deliveryReceiptManager.addReceiptReceivedListener(this);
        deliveryReceiptManager.setAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.always);
        this.userAppPreference = userAppPreference;

        //mPrivateChats = new HashMap();
        this.context = context;

        Roster roster = Roster.getInstanceFor(connection);
        roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);

        xmppConnection.addAsyncStanzaListener(this, new StanzaTypeFilter(Message.class));

    }


    public void sendAllNotSentMessages() {
        List<MessageContainer> messages = DbHelper.getAllNotSentMessages(userAppPreference.getUserStringKey());
        for (final MessageContainer messageContainer : messages) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Message message = createMessage(messageContainer);
                    doSendMessage(message);
                }
            }).start();
        }
    }

    public void sendMessageToServer(final Jid jidTo, final String messageText) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = createMessage(jidTo, messageText);
                DbHelper.addMessageToDb(message, MessageContainer.CHAT_SIMPLE, jidTo, true);
                doSendMessage(message);
            }
        }).start();
    }

    Message createMessage(MessageContainer messageContainer) {
        String entityJidTo = null;
        String entityJidFrom = null;
        entityJidTo = (messageContainer.toJid);
        entityJidFrom = (messageContainer.fromJid);

        Message message = new Message();
        message.setBody(messageContainer.body);
        message.setFrom(entityJidFrom);
        message.setTo(entityJidTo);
        message.setStanzaId(messageContainer.stanzaID);
        message.setType(Message.Type.chat);
        return message;
    }

    Message createMessage(Jid jidTo, String messageText) {
        //  String entityJidTo = null;
        String entityJidFrom = null;
        //entityJidTo = jidTo;
        entityJidFrom = (userAppPreference.getUserStringKey());

        Message message = new Message();
        message.setBody(messageText);
        try {
            message.setFrom(JidCreate.from(entityJidFrom));
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        message.setTo(jidTo);
        message.setStanzaId(StanzaIdUtil.newStanzaId());
        message.setType(Message.Type.chat);
        return message;
    }

    void doSendMessage(Message message) {
        try {
            //final String jidFrom = message.getFrom().asBareJid().toString();
            //final String jidTo = message.getTo().asBareJid().toString();
            //TODO remove it after debug

            /*if (BuildConfig.DEBUG && (jidTo.contains("lebedenko")
                    || jidTo.contains("sazonov")
                    || jidTo.contains("koltsov")
                    || jidTo.contains("skurzhansky")
                    || jidTo.contains("pronkin"))) */
            {
                DeliveryReceiptRequest.addTo(message);
                message.setTo(message.getTo());
                message.setFrom(message.getFrom());
                xmppConnection.sendStanza(message);
                //Log.i(TAG, "send msg id = " + message.getStanzaId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            EventBus.getDefault().post(new XmppErrorEvent(XmppErrorEvent.State.EMessageNotSend));
        }
    }

    public void listeningForMessages() {

        /*PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
        xmppConnection.addPacketListener(new PacketListener() {
            public void processPacket(Packet packet) {
                Message message = (Message) packet;
                if (message.getBody() != null) {
                    String fromName = StringUtils.parseBareAddress(message
                            .getFrom());
                    m_discussionThread.add(fromName + ":");
                    m_discussionThread.add(message.getBody());
                    m_discussionThreadAdapter.notifyDataSetChanged();
                }
            }
        }, filter);

        PacketCollector collector = xmppConnection.createPacketCollector(new MessageTypeFilter(Message.Type.chat));
        PacketCollector collector = getConnection(1).createPacketCollector(
                new MessageTypeFilter(Message.Type.chat));
        PacketFilter filter = new AndFilter(new PacketTypeFilter(Message.class));
        PacketCollector collector = xmppConnection.createPacketCollector(filter);
        while (true) {
            Packet packet = collector.nextResult();
            if (packet instanceof Message) {
                Message message = (Message) packet;
                if (message != null && message.getBody() != null)
                    System.out.println("Received message from "
                            + packet.getFrom() + " : "
                            + (message != null ? message.getBody() : "NULL"));
            }
        }*/
    }


    @Override
    public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
        final Message message = (Message) packet;
        // store message to DB
        if (message.getBodies().size() > 0 && !TextUtils.isEmpty(message.getBody())) {
           Resourcepart resourcepart = message.getFrom().getResourceOrNull();
            if (resourcepart != null && resourcepart.equals(userAppPreference.getFirstSecondName())) {
                return;
            }
            // Log.i(TAG, "received msg id= " + message.getStanzaId());
            Task.callInBackground(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    Boolean isRead = false;
                    MessageContainer messageInDb = DbHelper.getMessageByReceiptId(message.getStanzaId());
                    if (messageInDb != null) {
                        isRead = messageInDb.isRead;
                    }
                    MessageContainer messageContainer = null;
                    int msgType = message.getType() == Message.Type.groupchat ? MessageContainer.CHAT_GROUP : MessageContainer.CHAT_SIMPLE;
                    messageContainer = DbHelper.addMessageToDb(message, msgType, message.getFrom(), isRead);
                    if (messageContainer != null && !isRead && SysUtils.isApplicationBroughtToBackground(context)) {
                        sendMsgBroadcastReceiver.sendOrderedBroadcastIntent(messageContainer);
                    } else {
                        //Message loader is not restarted immediately for unknown reason, so we do it by this event
                        RiaEventBus.post(XmppErrorEvent.State.EMessageReceived);
                    }
                    return null;
                }
            });
        } else {
            // if we get a message without a body, we check it's stanza id,
            // if the stanza id coincides with it, we conclude that it is a confirmation receipt
            MessageContainer messageContainer = DbHelper.getMessageByReceiptId(message.getStanzaId());
            if (messageContainer != null) {
                messageContainer.isSent = true;
                messageContainer.save();
            }
        }
    }


    @Override
    public void onReceiptReceived(Jid fromJid, Jid toJid, String receiptId, Stanza receipt) {
        Log.i(TAG, "on chat created receiptId = " + receiptId + " receipt = " + receipt.toString());
        MessageContainer messageContainer = DbHelper.getMessageByReceiptId(receiptId);
        if (messageContainer != null) {
            messageContainer.isSent = true;
            messageContainer.save();
        }
    }
}
