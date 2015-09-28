package ru.rian.riamessenger.listeners;

import android.content.Context;
import android.view.View;

import ru.rian.riamessenger.adapters.list.RosterEntryIdGetter;

/**
 * Created by Roman on 7/21/2015.
 */
public interface BaseRiaListClickListener {
    void onClick(String jid, Context context);

    int onClick(RosterEntryIdGetter rosterEntryIdGetter, View v);
}
