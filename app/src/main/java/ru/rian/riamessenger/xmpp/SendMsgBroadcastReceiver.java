package ru.rian.riamessenger.xmpp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;

import lombok.RequiredArgsConstructor;
import lombok.val;
import ru.rian.riamessenger.ConversationActivity;
import ru.rian.riamessenger.R;
import ru.rian.riamessenger.model.MessageContainer;
import ru.rian.riamessenger.model.RosterEntryModel;
import ru.rian.riamessenger.utils.DbHelper;
import ru.rian.riamessenger.utils.RiaTextUtils;

/**
 * Created by Roman on 6/28/2015.
 */
@RequiredArgsConstructor
public class SendMsgBroadcastReceiver extends BroadcastReceiver {

    public void sendOrderedBroadcastIntent(MessageContainer messageContainer) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.putExtra(ConversationActivity.ARG_TO_JID, messageContainer.toJid);
        broadcastIntent.putExtra(ConversationActivity.ARG_FROM_JID, messageContainer.fromJid);
        // Send broadcast and expect result back.  If no result then
        // then an appropriate activity is not alive and we should show a notification
        context.sendOrderedBroadcast(broadcastIntent, null, this, null,
                Activity.RESULT_CANCELED, null, null);
    }

    final static int NOTIFICATION_MAX_LENGTH = 10;
    //Stores messages when there was no proper BroadcastReceiver
    //so that a ChatView can display them later
    //this may need revisited to deal properly with MUC
    //uses who our conversation is with as the key and then uses JmMessage
    //because the actual message being stored could be from ourselves TO the person
    //we are chatting with.  This info is not stored with the message, it is tracked
    //by who the chat session is with, so using a simple class to track this
    //  private HashMap<String, List<JmMessage>> mQueuedMessages = new HashMap<String, List<JmMessage>>();

    final Context context;

    @Override
    public void onReceive(Context arg0, Intent intent) {
        if (getResultCode() != Activity.RESULT_OK) {

            //JmMessage message = null;
            Bundle bundle = intent.getExtras();
            String jid_from = bundle.getString(ConversationActivity.ARG_FROM_JID);
            String jid_to = bundle.getString(ConversationActivity.ARG_TO_JID);
            MessageContainer messageContainer = DbHelper.getLastMessageFrom(jid_from, jid_to);
            RosterEntryModel rosterEntryModel = DbHelper.getRosterEntryByBareJid(jid_from);
            String messageFromPref = context.getString(R.string.message_from);
            //  queueMessage(messageFrom, message);
            String messageText = messageContainer.body;
            int messageLength = NOTIFICATION_MAX_LENGTH;
            if (messageText.length() <= messageLength) {
                messageLength = messageText.length();
            }
            CharSequence contentText = messageText.subSequence(0, messageLength);

            /*Intent notificationIntent = new Intent(context, ConversationActivity.class);

            Bundle notificationBundle = new Bundle();
            notificationBundle.putString(ConversationActivity.ARG_FROM_JID, messageContainer.toJid);
            notificationIntent.putExtras(notificationBundle);*/
            //myIntent.putExtra(ConversationActivity.ARG_TO_JID, messageContainer.fromJid);
            //  notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
            //         Intent.FLAG_ACTIVITY_SINGLE_TOP);


            Intent notificationIntent = new Intent(context, ConversationActivity.class);
            notificationIntent.putExtra(ConversationActivity.ARG_TO_JID, messageContainer.fromJid);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);


            val notification = new NotificationCompat.Builder(context)
                    .setContentTitle(messageFromPref + RiaTextUtils.capFirst(rosterEntryModel.name))
                    .setContentText(contentText)
                    .setTicker(context.getString(R.string.new_message))
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(pendingIntent)
                            //.setDefaults(Notification.DEFAULT_SOUND)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.push_icon)
                    .build();
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            //notification.setLatestEventInfo(context, title, message, pIntent);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(0, notification);


            //Notification notification = new Notification(icon, tickerText, when);
            //notification.flags = notification.defaults;
            //notification.flags |= Notification.FLAG_AUTO_CANCEL;


            // Uri uri = Uri.parse("ru.rian.riamessenger.ConversationActivity");

            //Intent activityIntent = new Intent("android.intent.action.VIEW", uri);

            //Intent activityIntent = new Intent("android.intent.action.VIEW");
            //activityIntent.setClassName("ru.rian.riamessenger", "ru.rian.riamessenger.ConversationActivity");
            //activityIntent.putExtra("participant", messageFrom);
            //activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //context.startActivity(activityIntent);


            //PendingIntent contentIntent = PendingIntent.getActivity(
            //        context, 0, activityIntent, 0);
            // notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

            //Use the hashcode of the message sender's user id
            //so that there's a unique id per message source.  This way
            //there will be a new notification per new source, but if that source
            //sends multiple messages, the notification for them will just update
            //with the latest message
            //This needs to be an int due to NotificationManager.notify() so the
            //hashcode is used rather than the username.
            //int notificationId = messageFrom.getUser().hashCode();

            /*synchronized (((RiaXmppService) context).getMActiveNotifications()) {
                ((RiaXmppService) context).getMActiveNotifications().add(notificationId);
            }*/
        }
    }

    /*private void queueMessage(String user, JmMessage message) {
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
    }*/
}
