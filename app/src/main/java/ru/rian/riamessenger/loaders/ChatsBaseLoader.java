package ru.rian.riamessenger.loaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

import com.activeandroid.Cache;
import com.activeandroid.content.ContentProvider;

import ru.rian.riamessenger.common.DbColumns;
import ru.rian.riamessenger.loaders.base.CursorRiaLoader;
import ru.rian.riamessenger.model.MessageContainer;

/**
 * Created by Roman Lebedenko.
 * fisher3421@gmail.com
 */

public class ChatsBaseLoader extends CursorRiaLoader {


    public ChatsBaseLoader(Context context) {
        super(context);
        setSubscription(ContentProvider.createUri(MessageContainer.class, null));
    }

    @Override
    protected Cursor loadCursor() throws Exception {
        Log.i("RiaService", "chats base loads cursor");
        String messages = Cache.getTableInfo(MessageContainer.class).getTableName();
        String req = "SELECT " + BaseColumns._ID + "," + DbColumns.ThreadIdCol + "," + DbColumns.MsgBodyCol + "," + DbColumns.FromJidCol + ","
                + "MAX(" + DbColumns.CreatedCol + ") AS " + DbColumns.CreatedCol + " FROM " + messages + " GROUP BY " + DbColumns.ThreadIdCol;
        Cursor resultCursor = Cache.openDatabase().rawQuery(req, null);
        /*boolean isMoved = resultCursor.moveToPosition(0);
        int count = resultCursor.getCount();*/

        return resultCursor;
    }

}
