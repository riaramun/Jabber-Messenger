package ru.rian.riamessenger.xmpp;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import bolts.Task;
import ru.rian.riamessenger.model.MessageContainer;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.utils.DbHelper;
import ru.rian.riamessenger.utils.SysUtils;

/**
 * Created by grigoriy on 29.06.15.
 */
public class SmackMessageManager implements MessageListener
        , ChatMessageListener
        , ChatManagerListener
        , ReceiptReceivedListener {

    static final String TAG = "RiaService";
    AbstractXMPPConnection xmppConnection;
    ChatManager mChatManager;
    HashMap<String, Chat> mPrivateChats;
    final Context context;

    UserAppPreference userAppPreference;
    SendMsgBroadcastReceiver sendMsgBroadcastReceiver;
    DeliveryReceiptManager deliveryReceiptManager;

    public SmackMessageManager(Context context, AbstractXMPPConnection connection, SendMsgBroadcastReceiver sendMsgBroadcastReceiver, UserAppPreference userAppPreference) {
        xmppConnection = connection;
        this.sendMsgBroadcastReceiver = sendMsgBroadcastReceiver;

        mChatManager = ChatManager.getInstanceFor(xmppConnection);
        mChatManager.addChatListener(this);

        deliveryReceiptManager = DeliveryReceiptManager.getInstanceFor(xmppConnection);
        deliveryReceiptManager.addReceiptReceivedListener(this);
        deliveryReceiptManager.setAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.always);
        this.userAppPreference = userAppPreference;

        mPrivateChats = new HashMap();
        this.context = context;
    }

    /**
     * event when incoming chat
     *
     * @param chat
     * @param createdLocally
     */
    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {

        Log.i(TAG, "on chat created createdLocally =" + createdLocally);
        if (!createdLocally) {
            String jid = chat.getParticipant().asBareJid().toString();
            mPrivateChats.put(jid, chat);
            chat.addMessageListener(this);
        }
    }

    /**
     * event when incoming message
     *
     * @param message
     */
    @Override
    public void processMessage(Message message) {
        //TODO: send to activity
        Log.i(TAG, "on received message");
    }

    /**
     * event when incoming message from chat
     *
     * @param chat
     * @param message
     */

    @Override
    public void processMessage(Chat chat, final Message message) {
        // store message to DB
        if (!TextUtils.isEmpty(message.getBody())) {
            Log.i(TAG, "on message from chat");
            Task.callInBackground(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    MessageContainer messageContainer = DbHelper.addMessageToDb(message, message.getFrom().asEntityBareJidIfPossible().toString(), false);
                    if (SysUtils.isApplicationBroughtToBackground(context)) {
                        sendMsgBroadcastReceiver.sendOrderedBroadcastIntent(messageContainer);
                    }
                    return null;
                }
            });
        }
        else {
            Log.i(TAG, "on null message from chat thread = " + message.getThread()
                    + " from " + message.getFrom().asEntityBareJidIfPossible().toString()
                    + " to " + message.getTo().asEntityBareJidIfPossible().toString() );
        }
    }

    public void sendAllNotSentMessages() {
        List<MessageContainer> messages = DbHelper.getAllNotSentMessages(userAppPreference.getJidStringKey());
        for (final MessageContainer message : messages) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    doSendMessage(message.toJid, message.body, message.created);
                    message.delete();
                }
            }).start();
        }
    }

    public void sendMessageToServer(final String jid, final String messageText) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doSendMessage(jid, messageText, new Date());
            }
        }).start();
    }

    private void doSendMessage(String jidTo, String messageText, final Date date) {
        Chat currentChat = null;//mPrivateChats.get(jidTo);
        if (/*currentChat == null &&*/ mChatManager != null) {
            try {
                EntityJid entityJid = JidCreate.bareFrom(jidTo);
                currentChat = mChatManager.createChat(entityJid);
                //mPrivateChats.put(jidTo, currentChat);
            } catch (XmppStringprepException e) {
                e.printStackTrace();
            }
        }
        try {
            //TODO remove it after debug
            if (jidTo.contains("lebedenko") || jidTo.contains("skurzhansky") || jidTo.contains("pronkin")) {

                Message message = new Message();
                message.setBody(messageText);
                String messageReceiptId = DeliveryReceiptRequest.addTo(message);

                MessageContainer messageContainer = new MessageContainer();
                messageContainer.body = messageText;
                messageContainer.fromJid = userAppPreference.getJidStringKey();
                messageContainer.toJid = jidTo;
                messageContainer.created = date;
                messageContainer.threadID = jidTo;
                messageContainer.isRead = true;
                messageContainer.receiptId = messageReceiptId;
                messageContainer.save();

                currentChat.sendMessage(message);
            }
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onReceiptReceived(Jid fromJid, Jid toJid, String receiptId, Stanza receipt) {
        Log.i(TAG, "on chat created receiptId = " + receiptId + " receipt = " + receipt.toString());
        MessageContainer messageContainer = DbHelper.getMessageByReceiptId(receiptId);
        messageContainer.isSent = true;
        messageContainer.save();
    }
}
