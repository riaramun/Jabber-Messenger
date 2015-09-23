package ru.rian.riamessenger.xmpp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import lombok.RequiredArgsConstructor;
import ru.rian.riamessenger.ConversationActivity;
import ru.rian.riamessenger.R;
import ru.rian.riamessenger.model.MessageContainer;
import ru.rian.riamessenger.model.RosterEntryModel;
import ru.rian.riamessenger.services.RiaXmppService;
import ru.rian.riamessenger.utils.DbHelper;
import ru.rian.riamessenger.utils.RiaTextUtils;

/**
 * Created by Roman on 6/28/2015.
 */
@RequiredArgsConstructor
public class SendMsgBroadcastReceiver extends BroadcastReceiver {

    public void sendOrderedBroadcastIntent(MessageContainer messageContainer) {
        try {
            Intent broadcastIntent = new Intent();
            broadcastIntent.putExtra(ConversationActivity.ARG_TO_JID, messageContainer.toJid);
            broadcastIntent.putExtra(ConversationActivity.ARG_FROM_JID, messageContainer.fromJid);
            // Send broadcast and expect result back.  If no result then
            // then an appropriate activity is not alive and we should show a notification
            context.sendOrderedBroadcast(broadcastIntent, null, this, null,
                    Activity.RESULT_CANCELED, null, null);
        } catch (NullPointerException e) {
            Log.i(RiaXmppService.TAG, "context null, it seems the service is killed");
        }
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
            try {
                //JmMessage message = null;
                Bundle bundle = intent.getExtras();
                String jid_from = bundle.getString(ConversationActivity.ARG_FROM_JID);
                String jid_to = bundle.getString(ConversationActivity.ARG_TO_JID);
                MessageContainer messageContainer = DbHelper.getLastMessageFrom(jid_from, jid_to);
                RosterEntryModel rosterEntryModel = DbHelper.getRosterEntryByBareJid(jid_from);
                String messageFromPref = arg0.getString(R.string.message_from);
                //  queueMessage(messageFrom, message);
                String messageText = messageContainer.body;
                int messageLength = NOTIFICATION_MAX_LENGTH;
                if (messageText.length() <= messageLength) {
                    messageLength = messageText.length();
                }
                CharSequence contentText = messageText.subSequence(0, messageLength);

                Intent resultIntent = new Intent(arg0, ConversationActivity.class);
                resultIntent.putExtra(ConversationActivity.ARG_TO_JID, messageContainer.fromJid);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(arg0);
                stackBuilder.addParentStack(ConversationActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_ONE_SHOT);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(arg0);
                builder.setContentIntent(resultPendingIntent);

                NotificationManager mNotificationManager =
                        (NotificationManager) arg0.getSystemService(Context.NOTIFICATION_SERVICE);
                Notification notification = new NotificationCompat.Builder(arg0)
                        .setContentTitle(messageFromPref + RiaTextUtils.capFirst(rosterEntryModel.name))
                        .setContentText(contentText)
                        .setTicker(arg0.getString(R.string.new_message))
                        .setWhen(System.currentTimeMillis())
                        .setContentIntent(resultPendingIntent)
                                //.setDefaults(Notification.DEFAULT_SOUND)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.push_icon)
                        .build();
                mNotificationManager.notify(0, notification);
            } catch (NullPointerException e) {
                Log.i(RiaXmppService.TAG, "context null, it seems our service is killed");
            }
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
