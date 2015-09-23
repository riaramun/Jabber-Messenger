package ru.rian.riamessenger.listeners;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import ru.rian.riamessenger.ConversationActivity;
import ru.rian.riamessenger.adapters.list.RosterEntryIdGetter;

/**
 * Created by Roman on 7/21/2015.
 */
public class ContactsListClickListener implements BaseRiaListClickListener{

    public void onClick(String jid, Context context) {
        Intent intent = new Intent(context, ConversationActivity.class);
        intent.putExtra(ConversationActivity.ARG_TO_JID, jid);
        context.startActivity(intent);
    }

    public int onClick(RosterEntryIdGetter rosterEntryIdGetter, View v) {
        RecyclerView recyclerView = (RecyclerView) v.getParent();
        int childPosition = recyclerView.getChildAdapterPosition(v);
        //RosterEntryIdGetter rosterEntryIdGetter = (RosterEntryIdGetter) recyclerView.getAdapter();
        if(childPosition >= 0) {
            String jid = rosterEntryIdGetter.getUser(childPosition);
            if (jid != null) {
                onClick(jid, v.getContext());
            } else {
                Log.i("RiaService", "onClick jid = null");
            }
        }
        return childPosition;
    }
}
