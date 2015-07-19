package ru.rian.riamessenger.xmpp;

import android.content.Context;
import android.util.Log;

import com.activeandroid.ActiveAndroid;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smack.roster.RosterLoadedListener;

import java.util.Locale;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import bolts.Continuation;
import bolts.Task;
import de.greenrobot.event.EventBus;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import ru.rian.riamessenger.R;
import ru.rian.riamessenger.common.RiaEventBus;
import ru.rian.riamessenger.model.RosterEntryModel;
import ru.rian.riamessenger.model.RosterGroupModel;
import ru.rian.riamessenger.riaevents.response.XmppErrorEvent;


/**
 * Created by Roman on 7/10/2015.
 */

@RequiredArgsConstructor
public class SmackRosterLoadedListener implements RosterLoadedListener {

    final Context context;
    @Override
    public void onRosterLoaded(Roster roster) {

        saveRosterToDb(roster);
    }

    public void saveRosterToDb(final Roster roster) {
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
        Log.i("SmackWrapper", "doSaveRosterToDb b");
        try {
            ActiveAndroid.beginTransaction();
            for (RosterGroup rosterGroup : roster.getGroups()) {
                RosterGroupModel rosterGroupModel = new RosterGroupModel();
                rosterGroupModel.name = rosterGroup.getName();
                rosterGroupModel.save();

                for (RosterEntry rosterEntry : rosterGroup.getEntries()) {
                    RosterEntryModel rosterEntryModel = new RosterEntryModel();
                    rosterEntryModel.bareJid = rosterEntry.getUser().asBareJidIfPossible().toString();

                    if(rosterGroupModel.name.equals(context.getString(R.string.robots))) {
                        rosterEntryModel.name = rosterEntry.getName();
                    } else {
                        rosterEntryModel.name = rosterEntry.getName().toLowerCase(Locale.getDefault());
                    }

                    rosterEntryModel.rosterGroupModel = rosterGroupModel;
                    rosterEntryModel.setPresence(roster.getPresence(rosterEntry.getUser()));
                    rosterEntryModel.save();
                }
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
        Log.i("SmackWrapper", "doSaveRosterToDb e");
    }
}
