package ru.rian.riamessenger.xmpp;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;
import android.util.SparseArray;


import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;

import java.util.Date;

import ru.rian.riamessenger.model.MessageContainer;
import ru.rian.riamessenger.model.RosterEntryModel;
import ru.rian.riamessenger.utils.DbHelper;
import ru.rian.riamessenger.utils.XmppUtils;

/**
 * Created by grigoriy on 29.06.15.
 */
public class XmppMessageManager implements MessageListener
        , ChatMessageListener
        , ChatManagerListener {

    private static final String TAG = "XmppChat.MessageManager";
    private Context mContext;
    private AbstractXMPPConnection mConnection;
    private ChatManager mChatManager;
    private SparseArray mPrivateChats;

    public XmppMessageManager(Context context, AbstractXMPPConnection connection) {
        mContext = context;
        mConnection = connection;

        // setup chat manager
        mChatManager = ChatManager.getInstanceFor(mConnection);
        if (mChatManager != null) {
            mChatManager.addChatListener(this);
        }
        mPrivateChats = new SparseArray();
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
        RosterEntryModel rosterEntryModel = DbHelper.getRosterEntryByBareJid(jid);
        if (rosterEntryModel != null) {
            long userId = rosterEntryModel.getId();
            mPrivateChats.put((int) userId, chat);
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

        RosterEntryModel rosterEntryModel = DbHelper.getRosterEntryByBareJid(chat.getParticipant().asBareJid().toString());

        // get user id
        long userId = rosterEntryModel.getId();

        // store message to DB
        MessageContainer messageContainer = new MessageContainer(userId, userId, new Date().getTime(),
                true, true
                , message.getBody()
                , message.getSubject());


    }

    public void sendMessage(final long userId, final String messageText) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doSendMessage(userId, messageText);
            }
        }).start();
    }

    private void doSendMessage(long userId, String messageText) {

       /*if (mConnection.isAuthenticated()) {
            Chat currentChat = (Chat) mPrivateChats.get((int) userId);
            if (currentChat == null) {
                String jid = QueryHelper.getJidById(userId);
                currentChat = mChatManager.createChat(jid);
                mPrivateChats.put((int) userId, currentChat);

            }
            try {
                currentChat.sendMessage(messageText);
                QueryHelper.storeMessage(ChatConstants.CURRENT_LOCAL_USER_ID, userId, new Date(), messageText, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
    }


}
