package ru.rian.riamessenger.xmpp;

import android.content.Intent;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterListener;
import org.jxmpp.jid.Jid;

import java.util.Collection;

import javax.inject.Inject;

import lombok.val;
import ru.rian.riamessenger.RiaApplication;

/**
 * Created by Roman on 6/28/2015.
 */
public class SmackRosterListener implements RosterListener {


    @Inject
    public SmackRosterListener() {

    }

    @Override
    public void entriesAdded(Collection<Jid> addresses) {

    }

    @Override
    public void entriesUpdated(Collection<Jid> addresses) {

    }

    @Override
    public void entriesDeleted(Collection<Jid> addresses) {

    }

    @Override
    public void presenceChanged(Presence presence) {
        // maybe some day null getFrom() will be useful?
        // or maybe it's optional but is always included in real world use
        if (presence.getFrom() != null) {
            Intent i = new Intent("jm.android.jmxmpp.ROSTER_UPDATED");
           /* val xmppConnectionService = riaServiceConnection.getXmppConnectionService();
            if (xmppConnectionService != null)
                xmppConnectionService.sendBroadcast(i);*/
        }
    }
}

