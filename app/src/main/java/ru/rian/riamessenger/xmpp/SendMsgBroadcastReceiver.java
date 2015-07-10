package ru.rian.riamessenger.xmpp;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import ru.rian.riamessenger.services.JmMessage;
import ru.rian.riamessenger.services.RiaXmppService;

/**
 * Created by Roman on 6/28/2015.
 */
public class SendMsgBroadcastReceiver extends BroadcastReceiver {

    public SendMsgBroadcastReceiver(){
        RiaXmppService.component().inject(this);
    }
    //Stores messages when there was no proper BroadcastReceiver
    //so that a ChatView can display them later
    //this may need revisited to deal properly with MUC
    //uses who our conversation is with as the key and then uses JmMessage
    //because the actual message being stored could be from ourselves TO the person
    //we are chatting with.  This info is not stored with the message, it is tracked
    //by who the chat session is with, so using a simple class to track this
    private HashMap<String, List<JmMessage>> mQueuedMessages = new HashMap<String, List<JmMessage>>();

    @Inject
    Context application;


    @Override
    public void onReceive(Context arg0, Intent intent) {
        if (getResultCode() != Activity.RESULT_OK) {
            //JmRosterEntry messageFrom = null;
            String messageFrom = null;
            JmMessage message = null;
            Bundle data = intent.getExtras();
            if (data != null) {
                messageFrom = data.getString("from");
                message = data.getParcelable("message");
            }
            queueMessage(messageFrom, message);
            String messageText = message.getText();
            int icon = android.R.drawable.sym_action_chat;
            CharSequence tickerText = "New XMPP Message";
            long when = System.currentTimeMillis();
            Notification notification = new Notification(icon, tickerText, when);
            notification.flags = notification.defaults;
            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            CharSequence contentTitle = "New jmXMPP Message from " + messageFrom;
            int messageLength = 15;
            if (messageText.length() <= 15) {
                messageLength = messageText.length();
            }
            CharSequence contentText = messageText.subSequence(0, messageLength);

            Intent i = new Intent("jm.android.jmxmpp.START_CHAT");
            i.setClassName("jm.android.jmxmpp", "jm.android.jmxmpp.ChatView");
            i.putExtra("participant", messageFrom);

            Context context = application.getApplicationContext();
            /*PendingIntent contentIntent = PendingIntent.getActivity(
                    riaServiceConnection.getXmppConnectionService(), 0, i, 0);
            notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

            //Use the hashcode of the message sender's user id
            //so that there's a unique id per message source.  This way
            //there will be a new notification per new source, but if that source
            //sends multiple messages, the notification for them will just update
            //with the latest message
            //This needs to be an int due to NotificationManager.notify() so the
            //hashcode is used rather than the username.
            //int notificationId = messageFrom.getUser().hashCode();
            int notificationId = messageFrom.hashCode();

            RiaService xmppConnectionService = riaServiceConnection.getXmppConnectionService();

            xmppConnectionService.getNotifyManager().notify(notificationId, notification);

            synchronized (xmppConnectionService.getMActiveNotifications()) {
                xmppConnectionService.getMActiveNotifications().add(notificationId);*/
           //}
        }
    }

    private void queueMessage(String user, JmMessage message) {
        if (!mQueuedMessages.containsKey(user)) {
            mQueuedMessages.put(user, new ArrayList<JmMessage>());
        }
        mQueuedMessages.get(user).add(message);
    }

    public void clearQueuedMessages(String from) {
        mQueuedMessages.remove(from);
    }

    public List<JmMessage> getQueuedMessages(String from) {
        if (mQueuedMessages.containsKey(from)) {
            return mQueuedMessages.get(from);
        }
        return null;
    }

    public void addMessagesToQueue(String from, JmMessage[] messages) {
        List<JmMessage> messageList = new ArrayList<JmMessage>();

        if (mQueuedMessages.containsKey(from)) {
            messageList = mQueuedMessages.get(from);
        }

        for (JmMessage message : messages) {
            messageList.add(message);
        }

        mQueuedMessages.put(from, messageList);
    }
}
