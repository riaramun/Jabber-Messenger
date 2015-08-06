package ru.rian.riamessenger.xmpp;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import java.util.List;
import java.util.concurrent.Callable;

import bolts.Task;
import ru.rian.riamessenger.ConversationActivity;
import ru.rian.riamessenger.model.MessageContainer;
import ru.rian.riamessenger.utils.DbHelper;

/**
 * Created by grigoriy on 29.06.15.
 */
public class XmppMessageManager implements MessageListener
        , ChatMessageListener
        , ChatManagerListener {

    static final String TAG = "XmppChat.MessageManager";
    AbstractXMPPConnection mConnection;
    ChatManager mChatManager;
    HashMap<String, Chat> mPrivateChats;
    final Context context;

    SendMsgBroadcastReceiver sendMsgBroadcastReceiver;

    public XmppMessageManager(Context context, AbstractXMPPConnection connection) {
        mConnection = connection;
        sendMsgBroadcastReceiver = new SendMsgBroadcastReceiver(context);
        mChatManager = ChatManager.getInstanceFor(mConnection);
        if (mChatManager != null) {
            mChatManager.addChatListener(this);

        }
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

        Log.d(TAG, "on chat created " + createdLocally);
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
        Log.d(TAG, "on received message");
    }

    private boolean isApplicationBroughtToBackground() {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * event when incoming message from chat
     *
     * @param chat
     * @param message
     */

    @Override
    public void processMessage(Chat chat, final Message message) {
        Log.d(TAG, "on message from chat");
        // store message to DB
        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                MessageContainer messageContainer = DbHelper.addMessageToDb(message);
                if (isApplicationBroughtToBackground()) {
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.putExtra(ConversationActivity.ARG_TO_JID, messageContainer.toJid);
                    broadcastIntent.putExtra(ConversationActivity.ARG_FROM_JID, messageContainer.fromJid);
                    // Send broadcast and expect result back.  If no result then
                    // then an appropriate activity is not alive and we should show a notification
                    context.sendOrderedBroadcast(broadcastIntent, null, sendMsgBroadcastReceiver, null,
                            Activity.RESULT_CANCELED, null, null);
                }
                return null;
            }
        });


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
                if (jid.contains("lebedenko") || jid.contains("skurzhansky")) {

                    currentChat.sendMessage(messageText);
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
