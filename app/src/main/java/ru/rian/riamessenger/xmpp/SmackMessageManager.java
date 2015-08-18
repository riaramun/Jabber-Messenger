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
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.List;
import java.util.concurrent.Callable;

import bolts.Task;
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
    AbstractXMPPConnection xmppConnection;

    //HashMap<String, Chat> mPrivateChats;
    final Context context;

    UserAppPreference userAppPreference;
    SendMsgBroadcastReceiver sendMsgBroadcastReceiver;
    DeliveryReceiptManager deliveryReceiptManager;

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
        List<MessageContainer> messages = DbHelper.getAllNotSentMessages(userAppPreference.getJidStringKey());
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

    public void sendMessageToServer(final String jidTo, final String messageText) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = createMessage(jidTo, messageText);
                DbHelper.addMessageToDb(message, jidTo, true);
                doSendMessage(message);
            }
        }).start();
    }

    Message createMessage(MessageContainer messageContainer) {
        EntityJid entityJidTo = null;
        EntityJid entityJidFrom = null;
        try {
            entityJidTo = JidCreate.bareFrom(messageContainer.toJid).asEntityJidIfPossible();
            entityJidFrom = JidCreate.bareFrom(messageContainer.fromJid).asEntityJidIfPossible();
            ;
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        Message message = new Message();
        message.setBody(messageContainer.body);
        message.setFrom(entityJidFrom);
        message.setTo(entityJidTo);
        message.setStanzaId(messageContainer.stanzaID);
        message.setType(Message.Type.chat);
        return message;
    }

    Message createMessage(String jidTo, String messageText) {
        EntityJid entityJidTo = null;
        EntityJid entityJidFrom = null;
        try {
            entityJidTo = JidCreate.bareFrom(jidTo).asEntityJidIfPossible();
            entityJidFrom = JidCreate.bareFrom(userAppPreference.getJidStringKey()).asEntityJidIfPossible();
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        Message message = new Message();
        message.setBody(messageText);
        message.setFrom(entityJidFrom);
        message.setTo(entityJidTo);
        message.setStanzaId(StanzaIdUtil.newStanzaId());
        message.setType(Message.Type.chat);
        return message;
    }

    private void doSendMessage(Message message) {
        try {
            final String jidTo = message.getTo().asEntityBareJidIfPossible().toString();
            //TODO remove it after debug
            if (jidTo.contains("lebedenko")
                    || jidTo.contains("sazonov")
                    || jidTo.contains("koltsov")
                    || jidTo.contains("skurzhansky")
                    || jidTo.contains("pronkin")) {
                DeliveryReceiptRequest.addTo(message);
                xmppConnection.sendStanza(message);
                Log.i(TAG, "send msg id = " + message.getStanzaId());
            }
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
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
    public void onReceiptReceived(Jid fromJid, Jid toJid, String receiptId, Stanza receipt) {
        Log.i(TAG, "on chat created receiptId = " + receiptId + " receipt = " + receipt.toString());
        MessageContainer messageContainer = DbHelper.getMessageByReceiptId(receiptId);
        if (messageContainer != null) {
            messageContainer.isSent = true;
            messageContainer.save();
        }
    }

    @Override
    public void processPacket(Stanza packet) throws SmackException.NotConnectedException, InterruptedException {
        final Message message = (Message) packet;
        // store message to DB
        if (!TextUtils.isEmpty(message.getBody())) {
            Log.i(TAG, "received msg id= " + message.getStanzaId());
            Task.callInBackground(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    MessageContainer messageContainer = DbHelper.addMessageToDb(message, message.getFrom().asEntityBareJidIfPossible().toString(), false);
                    if (SysUtils.isApplicationBroughtToBackground(context)) {
                        sendMsgBroadcastReceiver.sendOrderedBroadcastIntent(messageContainer);
                    } else {
                        //Message loader is not restarted immediately for unknown reason, so we do it by this event
                        RiaEventBus.post(XmppErrorEvent.State.EMessageReceived);
                    }
                    return null;
                }
            });
        } else {
            Log.i(TAG, "on null message from chat id = " + message.getThread()
                    + " from " + message.getFrom().asEntityBareJidIfPossible().toString()
                    + " to " + message.getTo().asEntityBareJidIfPossible().toString());
        }
    }
}
