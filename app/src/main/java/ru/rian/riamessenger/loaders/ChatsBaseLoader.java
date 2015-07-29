package ru.rian.riamessenger.loaders;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;

import com.activeandroid.Cache;
import com.activeandroid.content.ContentProvider;
import com.activeandroid.query.Select;

import ru.rian.riamessenger.ConversationActivity;
import ru.rian.riamessenger.common.DbColumns;
import ru.rian.riamessenger.fragments.BaseTabFragment;
import ru.rian.riamessenger.fragments.ChatsFragment;
import ru.rian.riamessenger.loaders.base.CursorRiaLoader;
import ru.rian.riamessenger.model.MessageContainer;

/**
 * Created by Roman Lebedenko.
 * fisher3421@gmail.com
 */

public class ChatsBaseLoader extends CursorRiaLoader {


    public ChatsBaseLoader(Context context) {
        super(context);
    }

    @Override
    protected Cursor loadCursor() throws Exception {

        String messages = Cache.getTableInfo(MessageContainer.class).getTableName();

        String req = "SELECT " + BaseColumns._ID + "," + DbColumns.ThreadIdCol + "," + DbColumns.MsgBodyCol + "," + DbColumns.FromJidCol + "," + "MAX(" + DbColumns.CreatedCol + ") AS " + DbColumns.CreatedCol +
                " FROM " + messages + " GROUP BY " + DbColumns.ThreadIdCol;

        Cursor resultCursor = Cache.openDatabase().rawQuery(req, null);
        boolean isMoved = resultCursor.moveToPosition(0);
        int count = resultCursor.getCount();

        return resultCursor;
    }

}
