package ru.rian.riamessenger.xmpp;

import android.content.Intent;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterListener;

import java.util.Collection;

import javax.inject.Inject;

import lombok.val;
import ru.rian.riamessenger.RiaApplication;

/**
 * Created by Roman on 6/28/2015.
 */
public class RiaRosterListener implements RosterListener {


    @Inject
    public RiaRosterListener() {
        RiaApplication.component().inject(this);
    }

    /*
        * The way these are implemented could get slow if someone has a huge
        * roster, but for now just send a Broadcast Intent saying there's been
        * a change and views that need it will call the service's getRoster()
        * and update as required.  Updating only the updated entry
        * will require a more complicated object and service structure
        * and likely more duplicated smack objects that are parcelable
        */
    @Override
    public void entriesDeleted(Collection<String> addresses) {
        Intent i = new Intent("jm.android.jmxmpp.ROSTER_UPDATED");
        /*val xmppConnectionService = riaServiceConnection.getXmppConnectionService();
        if (xmppConnectionService != null)
            xmppConnectionService.sendBroadcast(i);*/
    }

    @Override
    public void entriesUpdated(Collection<String> addresses) {
        Intent i = new Intent("jm.android.jmxmpp.ROSTER_UPDATED");
        /*val xmppConnectionService = riaServiceConnection.getXmppConnectionService();
        if (xmppConnectionService != null)
            xmppConnectionService.sendBroadcast(i);*/
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

    @Override
    public void entriesAdded(Collection<String> arg0) {
        Intent i = new Intent("jm.android.jmxmpp.ROSTER_UPDATED");
       /* val xmppConnectionService = riaServiceConnection.getXmppConnectionService();
        if (xmppConnectionService != null)
            xmppConnectionService.sendBroadcast(i);*/
    }
}

