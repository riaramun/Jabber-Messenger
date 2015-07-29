package ru.rian.riamessenger.xmpp;

import android.text.TextUtils;

import com.activeandroid.query.Select;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterListener;
import org.jxmpp.jid.Jid;

import java.util.Collection;

import javax.inject.Inject;

import ru.rian.riamessenger.common.DbColumns;
import ru.rian.riamessenger.model.RosterEntryModel;

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
        String bareJid = presence.getFrom().asEntityBareJidIfPossible().toString();
        if (!TextUtils.isEmpty(bareJid)) {
            RosterEntryModel rosterEntryModel = new Select().from(RosterEntryModel.class).where(DbColumns.FromJidCol + "='" + bareJid + "'").executeSingle();
            if (rosterEntryModel != null) {
                rosterEntryModel.setPresence(presence);
                rosterEntryModel.save();
            }
        }
    }
}

