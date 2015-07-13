package ru.rian.riamessenger.services;

import android.app.Activity;
import android.content.Intent;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.roster.RosterEntry;

import javax.inject.Inject;

import ru.rian.riamessenger.xmpp.SendMsgBroadcastReceiver;

/**
 * Created by Roman on 7/7/2015.
 */
public class ChatMessageListenerWrap implements ChatMessageListener {

    @Inject
    SendMsgBroadcastReceiver sendMessageResultReceiver;

    @Override
    public void processMessage(Chat chat, Message message) {
        if (message.getType() == Message.Type.chat) {

            /*RosterEntry re = findRosterEntryByUser(chat.getParticipant());
            //TODO: Handle chat from people NOT in the roster
            if (re != null) {
                Intent i = new Intent();
                i.setAction("jm.android.jmxmpp.INCOMING_MESSAGE");
                // A bit redundant due to the JmMessage but for now
                // just make it work, it's not a huge problem
                i.putExtra("from", re.getUser());
                String sender = (re.getName() != null) ?
                        re.getName() : re.getUser();
                i.putExtra("message",
                        new JmMessage(sender, message.getBody()));
                // Send broadcast and expect result back.  If no result then
                // then an appropriate activity is not alive and we should show a notification

                RiaXmppService.getContext().sendOrderedBroadcast(i, null, sendMessageResultReceiver, null,
                        Activity.RESULT_CANCELED, null, null);
            } else {
                //What to do if they're not on the roster?
            }*/
        }
    }

    RosterEntry findRosterEntryByUser(String user) {
        /*String temp = user.split("/", 2)[0];
        Iterator<RosterEntry> i = RiaService.getContext().getRoster().getEntries().iterator();
        while (i.hasNext()) {
            RosterEntry current = i.next();
            if (current.getUser().equals(temp)) {
                return current;
            }
        }*/
        return null;
    }
}
