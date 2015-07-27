package ru.rian.riamessenger.listeners;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import ru.rian.riamessenger.ConversationActivity;
import ru.rian.riamessenger.adapters.list.ContactsAdapter;
import ru.rian.riamessenger.adapters.list.RosterEntryIdGetter;
import ru.rian.riamessenger.model.RosterEntryModel;
import ru.rian.riamessenger.utils.DbHelper;

/**
 * Created by Roman on 7/21/2015.
 */
public class ContactsListClickListener {

    public void onClick(String jid, Context context) {
        Intent intent = new Intent(context, ConversationActivity.class);
        intent.putExtra(ConversationActivity.ARG_ENTRY_MODEL_ID, jid);
        context.startActivity(intent);
    }

    public void onClick(RosterEntryIdGetter rosterEntryIdGetter, View v) {
        RecyclerView recyclerView = (RecyclerView) v.getParent();
        int childPosition = recyclerView.getChildAdapterPosition(v);
        //RosterEntryIdGetter rosterEntryIdGetter = (RosterEntryIdGetter) recyclerView.getAdapter();
        String jid = rosterEntryIdGetter.getJid(childPosition);
        if(jid != null) {
            onClick(jid, v.getContext());
        }
    }
}
