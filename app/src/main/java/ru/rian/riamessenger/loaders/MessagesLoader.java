package ru.rian.riamessenger.loaders;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import com.activeandroid.Cache;
import com.activeandroid.content.ContentProvider;
import com.activeandroid.query.Select;

import ru.rian.riamessenger.ConversationActivity;
import ru.rian.riamessenger.common.DbColumns;
import ru.rian.riamessenger.loaders.base.CursorRiaLoader;
import ru.rian.riamessenger.model.MessageContainer;
import ru.rian.riamessenger.utils.DbHelper;

/**
 * Created by Roman Lebedenko.
 * fisher3421@gmail.com
 */

public class MessagesLoader extends CursorRiaLoader {

    String jid_to = null;
    String jid_from = null;

    public MessagesLoader(Context ctx, Bundle args) {
        super(ctx);
        jid_to = args.getString(ConversationActivity.ARG_TO_JID);
        jid_from = args.getString(ConversationActivity.ARG_FROM_JID);

        setSubscription(ContentProvider.createUri(MessageContainer.class, null));
    }


    @Override
    protected Cursor loadCursor() throws Exception {
        Cursor resultCursor;
        // String tableName = Cache.getTableInfo(MessageContainer.class).getTableName();
 /*       String resultRecords = new Select().from(MessageContainer.class).toSql();
        resultCursor = Cache.openDatabase().rawQuery(resultRecords, null);
        boolean isMoved = resultCursor.moveToPosition(0);
        int count = resultCursor.getCount();
*/
        //  resultCursor = DbHelper.getMessagesByJid(jid_to);

        String select = new Select().from(MessageContainer.class)
                .where(DbColumns.FromJidCol + "='" + jid_from + "' and " + DbColumns.ToJidCol + "='" + jid_to + "'" + " OR " +
                                DbColumns.FromJidCol + "='" + jid_to + "' and " + DbColumns.ToJidCol + "='" + jid_from + "'"
                ).toSql();
        //  String select = new Select().from(MessageContainer.class).toSql();
        Cursor msgCursor = Cache.openDatabase().rawQuery(select, null);

        return msgCursor;//DbHelper.getMessagesByUserId(rosterEntryModel);
    }

}
