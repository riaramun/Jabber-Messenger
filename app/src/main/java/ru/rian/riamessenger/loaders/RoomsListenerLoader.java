package ru.rian.riamessenger.loaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.activeandroid.Cache;
import com.activeandroid.content.ContentProvider;

import ru.rian.riamessenger.common.DbColumns;
import ru.rian.riamessenger.loaders.base.CursorRiaLoader;
import ru.rian.riamessenger.model.MessageContainer;

/**
 * Created by Roman Lebedenko.
 * fisher3421@gmail.com
 */

public class RoomsListenerLoader extends CursorRiaLoader {

    public RoomsListenerLoader(Context context) {
        super(context);
        setSubscription(ContentProvider.createUri(MessageContainer.class, null));
    }

    @Override
    protected Cursor loadCursor() throws Exception {
        String messages = Cache.getTableInfo(MessageContainer.class).getTableName();
        String req = "SELECT " + BaseColumns._ID + "," + DbColumns.ThreadIdCol + "," + DbColumns.MsgBodyCol + "," + DbColumns.FromJidCol + ","
                + "MAX(" + DbColumns.CreatedCol + ") AS " + DbColumns.CreatedCol + " FROM " + messages + " WHERE " + DbColumns.ChatTypeCol + "=" + MessageContainer.CHAT_GROUP + " GROUP BY " + DbColumns.ThreadIdCol + " ORDER BY " + DbColumns.CreatedCol + " DESC ";
        return Cache.openDatabase().rawQuery(req, null);
    }
}
