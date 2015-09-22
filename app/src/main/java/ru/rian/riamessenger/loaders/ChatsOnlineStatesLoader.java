package ru.rian.riamessenger.loaders;

import android.content.Context;
import android.os.Bundle;

import com.activeandroid.content.ContentProvider;

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
