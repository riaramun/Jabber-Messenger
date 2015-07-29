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
import ru.rian.riamessenger.model.RosterEntryModel;
import ru.rian.riamessenger.utils.DbHelper;

/**
 * Created by Roman Lebedenko.
 * fisher3421@gmail.com
 */

public class UserOnlineStatusLoader extends CursorRiaLoader {

    String user_jid = null;

    public UserOnlineStatusLoader(Context ctx, Bundle args) {
        super(ctx);
        user_jid = args.getString(ConversationActivity.ARG_TO_JID);
        setSubscription(ContentProvider.createUri(RosterEntryModel.class, null));
    }


    @Override
    protected Cursor loadCursor() throws Exception {

        String select = new Select().from(RosterEntryModel.class).where(DbColumns.FromJidCol + "='" + user_jid + "'").toSql();
        Cursor msgCursor = Cache.openDatabase().rawQuery(select, null);
        boolean ret = msgCursor.moveToFirst();
        int size = msgCursor.getCount();
        return msgCursor;
    }

}
