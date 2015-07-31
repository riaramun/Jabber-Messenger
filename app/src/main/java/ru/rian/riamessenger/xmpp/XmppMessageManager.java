package ru.rian.riamessenger.xmpp;

import android.util.Log;


import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.Date;
import java.util.HashMap;

import ru.rian.riamessenger.model.MessageContainer;
import ru.rian.riamessenger.model.RosterEntryModel;
import ru.rian.riamessenger.utils.DbHelper;

/**
 * Created by grigoriy on 29.06.15.
 */
public class XmppMessageManager implements MessageListener
        , ChatMessageListener
        , ChatManagerListener {

    private static final String TAG = "XmppChat.MessageManager";

    private AbstractXMPPConnection mConnection;
    private ChatManager mChatManager;
    private HashMap<String, Chat> mPrivateChats;

    public XmppMessageManager(AbstractXMPPConnection connection) {
        mConnection = connection;
        // setup chat manager
        mChatManager = ChatManager.getInstanceFor(mConnection);
        if (mChatManager != null) {
            mChatManager.addChatListener(this);

        }
        mPrivateChats = new HashMap();
    }

    /**
     * event when incoming chat
     *
     * @param chat
     * @param createdLocally
     */
    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {

        Log.d(TAG, "on chat created");
        String jid = chat.getParticipant().asBareJid().toString();
        mPrivateChats.put(jid, chat);
        chat.addMessageListener(this);
    }

    /**
     * event when incoming message
     *
     * @param message
     */
    @Override
    public void processMessage(Message message) {
        //TODO: send to activity
        Log.d(TAG, "on received message");
    }


    /**
     * event when incoming message from chat
     *
     * @param chat
     * @param message
     */
    @Override
    public void processMessage(Chat chat, Message message) {

        Log.d(TAG, "on message from chat");
        // store message to DB
        MessageContainer messageContainer = new MessageContainer();
        messageContainer.body = message.getBody();
        messageContainer.toJid = message.getTo().asEntityBareJidIfPossible().toString();
        messageContainer.fromJid = message.getFrom().asEntityBareJidIfPossible().toString();
        messageContainer.threadID = message.getFrom().asEntityBareJidIfPossible().toString();;
        messageContainer.created = new Date();
        messageContainer.save();
    }

    public void sendMessage(final String jid, final String messageText) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doSendMessage(jid, messageText);
            }
        }).start();
    }

    private void doSendMessage(String jid, String messageText) {

        if (mConnection.isAuthenticated()) {
            Chat currentChat = mPrivateChats.get(jid);

            if (currentChat == null) {
                try {
                    EntityJid entityJid = JidCreate.bareFrom(jid);
                    currentChat = mChatManager.createChat(entityJid);
                    mPrivateChats.put(jid, currentChat);
                } catch (XmppStringprepException e) {
                    e.printStackTrace();
                }
            }
            try {
                //TODO remove it after debug
                if(jid.contains("lebedenko") || jid.contains("skurzhansky")) {

                    currentChat.sendMessage(messageText);
                    //RosterEntryModel rosterEntryModel = DbHelper.getRosterEntryByBareJid(jid);
                    MessageContainer messageContainer = new MessageContainer();
                    messageContainer.body = messageText;
                    messageContainer.fromJid = mConnection.getUser().asBareJidString();
                    messageContainer.toJid = jid;
                    messageContainer.created = new Date();
                    messageContainer.threadID = jid;
                    messageContainer.save();

                }
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
