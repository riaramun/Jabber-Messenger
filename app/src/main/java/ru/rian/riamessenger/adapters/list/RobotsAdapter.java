package ru.rian.riamessenger.adapters.list;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import lombok.val;
import ru.rian.riamessenger.R;
import ru.rian.riamessenger.adapters.base.CursorRecyclerViewAdapter;
import ru.rian.riamessenger.adapters.viewholders.ContactViewHolder;
import ru.rian.riamessenger.adapters.viewholders.EmptyViewHolder;
import ru.rian.riamessenger.model.RosterEntryModel;
import ru.rian.riamessenger.utils.DbHelper;

/**
 * Created by Roman on 6/30/2015.
 */
public class RobotsAdapter extends CursorRecyclerViewAdapter {

    EmptyViewHolder emptyViewHolder;

    public RobotsAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {

        switch (viewHolder.getItemViewType()) {
            case VIEW_TYPE_EMPTY_ITEM:
                emptyViewHolder = (EmptyViewHolder) viewHolder;
                break;
            case VIEW_TYPE_CONTENT:
                val rosterEntry = (RosterEntryModel) DbHelper.getModelByCursor(cursor, RosterEntryModel.class);
                if(rosterEntry != null) {
                    val contactViewHolder = (ContactViewHolder) viewHolder;
                    contactViewHolder.contactName.setText(rosterEntry.name);
                    contactViewHolder.setOnlineStatus(rosterEntry.presence);
                }
                break;
        }
    }


    @Override
    public int getItemViewType(int position) {
        int resType = -1;
        if (getItemCount() <= 0) {
            resType = VIEW_TYPE_EMPTY_ITEM;
        } else {
            resType = VIEW_TYPE_CONTENT;
        }
        return resType;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        RecyclerView.ViewHolder vh = null;
        switch (viewType) {
            case VIEW_TYPE_CONTENT:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
                //itemView.setOnLongClickListener(mOnLongClickListener);
                vh = new ContactViewHolder(itemView);
                break;
            case VIEW_TYPE_EMPTY_ITEM:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.empty_list_item, parent, false);
                // itemView.setLayoutParams(new RecyclerView.LayoutParams(parent.getWidth(), parent.getHeight()));
                vh = new EmptyViewHolder(itemView);
                emptyViewHolder = (EmptyViewHolder) vh;
                break;
        }
        return vh;
    }
}
