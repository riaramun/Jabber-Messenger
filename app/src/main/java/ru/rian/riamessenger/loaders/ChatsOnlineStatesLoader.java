package ru.rian.riamessenger.loaders;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;

import com.activeandroid.Cache;
import com.activeandroid.content.ContentProvider;

import ru.rian.riamessenger.common.DbColumns;
import ru.rian.riamessenger.fragments.BaseTabFragment;
import ru.rian.riamessenger.loaders.base.CursorRiaLoader;
import ru.rian.riamessenger.model.MessageContainer;
import ru.rian.riamessenger.model.RosterEntryModel;

/**
 * Created by Roman Lebedenko.
 * fisher3421@gmail.com
 */

public class ChatsOnlineStatesLoader extends ChatsBaseLoader {

    public ChatsOnlineStatesLoader(Context ctx, Bundle args) {
        super(ctx);
        setSubscription(ContentProvider.createUri(RosterEntryModel.class, null));
    }

}
