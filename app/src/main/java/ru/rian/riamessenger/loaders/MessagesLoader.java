package ru.rian.riamessenger.loaders;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;

import com.activeandroid.Cache;
import com.activeandroid.content.ContentProvider;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import lombok.val;
import ru.rian.riamessenger.ContactsActivity;
import ru.rian.riamessenger.ConversationActivity;
import ru.rian.riamessenger.R;
import ru.rian.riamessenger.fragments.BaseTabFragment;
import ru.rian.riamessenger.loaders.base.CursorRiaLoader;
import ru.rian.riamessenger.model.MessageContainer;
import ru.rian.riamessenger.model.RosterEntryModel;
import ru.rian.riamessenger.model.RosterGroupModel;
import ru.rian.riamessenger.utils.DbHelper;

/**
 * Created by Roman Lebedenko.
 * fisher3421@gmail.com
 */

public class MessagesLoader extends CursorRiaLoader {

    String jid = null;

    public MessagesLoader(Context ctx, Bundle args) {
        super(ctx);
        jid = args.getString(ConversationActivity.ARG_ENTRY_MODEL_ID);
        setSubscription(ContentProvider.createUri(MessageContainer.class, null));
    }


    @Override
    protected Cursor loadCursor() throws Exception {

        Cursor resultCursor;
       // String tableName = Cache.getTableInfo(MessageContainer.class).getTableName();
        String resultRecords = new Select().from(MessageContainer.class).toSql();
        resultCursor = Cache.openDatabase().rawQuery(resultRecords, null);
        boolean isMoved = resultCursor.moveToPosition(0);
        int count = resultCursor.getCount();

        //RosterEntryModel rosterEntryModel = DbHelper.getRosterEntryByBareJid(jid);
        return resultCursor;//DbHelper.getMessagesByUserId(rosterEntryModel);
    }

}
