package ru.rian.riamessenger.xmpp;

import android.content.Context;
import android.database.sqlite.SQLiteDiskIOException;
import android.text.TextUtils;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.roster.RosterLoadedListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jxmpp.jid.Jid;


import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;
import lombok.Getter;
import ru.rian.riamessenger.R;
import ru.rian.riamessenger.common.DbColumns;
import ru.rian.riamessenger.common.RiaEventBus;
import ru.rian.riamessenger.model.RosterEntryModel;
import ru.rian.riamessenger.model.RosterGroupModel;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.riaevents.response.XmppErrorEvent;
import ru.rian.riamessenger.utils.DbHelper;
import ru.rian.riamessenger.utils.XmppUtils;

/**
 * Created by Roman on 8/10/2015.
 */
public class SmackRosterManager implements RosterLoadedListener, RosterListener, StanzaListener {

    @Getter
    final
    Roster roster;

     final XMPPTCPConnection xmppConnection;
     final Context context;
     final UserAppPreference userAppPreference;

    public SmackRosterManager(Context context, UserAppPreference userAppPreference, XMPPTCPConnection xmppConnection) {
        this.xmppConnection = xmppConnection;
        this.context = context;
        this.userAppPreference = userAppPreference;
        roster = Roster.getInstanceFor(xmppConnection);

/*        String path = userAppPreference.getRosterPathStringKey();
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            DirectoryRosterStore store = DirectoryRosterStore.init(file);
            // roster.setRosterStore(store);
        }
*/
        //roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
        xmppConnection.addSyncStanzaListener(this, StanzaTypeFilter.PRESENCE);
        roster.setRosterLoadedAtLogin(false);
        roster.addRosterLoadedListener(this);
        roster.addRosterListener(this);
    }

    public boolean isRosterLoaded() {
        boolean isLoaded = false;
        if (roster != null) {
            isLoaded = roster.isLoaded();
        }
        return isLoaded;
    }

    public boolean tryGetRosterFromServer() {
        if (roster != null && /*roster.isLoaded() &&*/  DbHelper.rosterTableIsNotEmpty()/*&& roster.getEntryCount() > 0 || */) {
            return true;
        } else {
            if (xmppConnection.isAuthenticated()) {
                try {
                    roster.reload();
                    Log.i("RiaService", "tryGetRosterFromServer");
                } catch (SmackException.NotLoggedInException e) {
                    e.printStackTrace();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    }

    @Override
    public void onRosterLoaded(Roster roster) {
        saveRosterToDb(roster);
    }

     void saveRosterToDb(final Roster roster) {
        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() {
                RiaEventBus.post(XmppErrorEvent.State.EDbUpdating);
                doSaveRosterToDb(roster);
                return null;
            }
        }).continueWith(new Continuation<Object, Void>() {
            public Void then(Task<Object> object) throws Exception {
                RiaEventBus.post(XmppErrorEvent.State.EDbUpdated);
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

     void doSaveRosterToDb(final Roster roster) {
        if (ActiveAndroid.inTransaction()) return;
        Log.i("RiaService", "doSaveRosterToDb b");
        try {
            ActiveAndroid.beginTransaction();
            for (RosterGroup rosterGroup : roster.getGroups()) {
                RosterGroupModel rosterGroupModel = new RosterGroupModel();
                rosterGroupModel.name = rosterGroup.getName();
                rosterGroupModel.save();

                for (RosterEntry rosterEntry : rosterGroup.getEntries()) {
                    RosterEntryModel rosterEntryModel = new RosterEntryModel();
                    rosterEntryModel.bareJid = rosterEntry.getUser();

                    if (rosterGroupModel.name.equals(context.getString(R.string.robots))) {
                        rosterEntryModel.name = rosterEntry.getName();
                    } else {
                        rosterEntryModel.name = rosterEntry.getName().toLowerCase(Locale.getDefault());
                    }

                    rosterEntryModel.rosterGroupModel = rosterGroupModel;
                    rosterEntryModel.setPresence(roster.getPresence(rosterEntry.getJid()));
                    rosterEntryModel.save();
                }
            }
            //add current user entry to track his presence via loader
            RosterEntryModel rosterEntryModel = new RosterEntryModel();
            rosterEntryModel.bareJid = userAppPreference.getUserStringKey();
            rosterEntryModel.setPresence(new Presence(Presence.Type.available));
            rosterEntryModel.save();

            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
        Log.i("RiaService", "doSaveRosterToDb e");
    }


    @Override
    public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
        Presence presence = (Presence) packet;
        if (presence != null && presence.getFrom() != null) {
            //Log.i("Service", "presence = " + presence.getStatus() + " mode " + presence.getMode() + " from " + presence.getFrom());
            String bareJid = presence.getFrom().asBareJid().toString();//XmppUtils.entityJid(presence.getFrom());
            if (!TextUtils.isEmpty(bareJid)) {
                RosterEntryModel rosterEntryModel = new Select().from(RosterEntryModel.class).where(DbColumns.FromJidCol + "='" + bareJid + "'").executeSingle();
                if (rosterEntryModel != null) {
                    rosterEntryModel.setPresence(presence);
                    try {
                        rosterEntryModel.save();
                    } catch (SQLiteDiskIOException e) {
                        Log.i("Service", e.getMessage());
                    }
                }
            }
        }
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

    }
}
