package ru.rian.riamessenger.xmpp;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.activeandroid.ActiveAndroid;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.roster.RosterLoadedListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jxmpp.jid.Jid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;
import lombok.Getter;
import lombok.val;
import ru.rian.riamessenger.R;
import ru.rian.riamessenger.common.RiaEventBus;
import ru.rian.riamessenger.model.RosterEntryModel;
import ru.rian.riamessenger.model.RosterGroupModel;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.riaevents.response.XmppErrorEvent;
import ru.rian.riamessenger.utils.DbHelper;

/**
 * Created by Roman on 8/10/2015.
 */
public class SmackRosterManager implements RosterLoadedListener, RosterListener {

    @Getter
    final
    Roster roster;
    public static String FIRST_SORTED_GROUP;

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
        if (xmppConnection.isAuthenticated() && !ActiveAndroid.inTransaction()) {
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
        if (DbHelper.rosterTableIsNotEmpty()) {

            if (TextUtils.isEmpty(FIRST_SORTED_GROUP)) {
                List rosterList = DbHelper.getRosterGroupModels();
                Collections.sort(rosterList, new GroupSortBasedOnName());
                RosterGroupModel group = (RosterGroupModel) rosterList.get(0);
                FIRST_SORTED_GROUP = group.name;
            }
            return true;
        } else {
            return false;
        }
    }


    @Override
    public void onRosterLoaded(Roster roster) {
        roster.removeRosterLoadedListener(this);
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

            ArrayList rosterList = new ArrayList(roster.getGroups());
            Collections.sort(rosterList, new GroupSortBasedOnName());
            RosterGroup group = (RosterGroup) rosterList.get(0);
            FIRST_SORTED_GROUP = group.getName();

            for (RosterGroup rosterGroup : roster.getGroups()) {
                RosterGroupModel rosterGroupModel = new RosterGroupModel();
                rosterGroupModel.name = rosterGroup.getName();
                rosterGroupModel.save();

                for (RosterEntry rosterEntry : rosterGroup.getEntries()) {

                    RosterEntryModel rosterEntryModel = new RosterEntryModel();
                    rosterEntryModel.bareJid = rosterEntry.getJid().toString();

                    if (rosterGroupModel.name.equals(FIRST_SORTED_GROUP)) {
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

    class GroupSortBasedOnName implements Comparator {
        public int compare(Object o1, Object o2) {
            val dd1 = (RosterGroup) o1;// where FBFriends_Obj is your object class
            val dd2 = (RosterGroup) o2;
            return dd1.getName().compareToIgnoreCase(dd2.getName());//where uname is field name
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
