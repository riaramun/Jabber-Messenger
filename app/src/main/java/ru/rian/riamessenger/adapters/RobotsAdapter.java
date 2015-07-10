package ru.rian.riamessenger.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;


import butterknife.Bind;
import butterknife.ButterKnife;
import lombok.Getter;
import lombok.val;
import ru.rian.riamessenger.R;
import ru.rian.riamessenger.model.RosterEntryModel;

/**
 * Created by Roman on 6/30/2015.
 */
public class RobotsAdapter extends BaseRiaRecyclerAdapter {
    
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_EMPTY_ITEM:
                emptyViewHolder = (EmptyViewHolder) holder;
                break;
            case VIEW_TYPE_CONTENT:
                val rosterEntry = (RosterEntryModel) entries.get(position);
                val contactViewHolder = (ContactViewHolder) holder;
                contactViewHolder.getContactName().setText(rosterEntry.name);
                break;
        }
    }


    @Override
    public int getItemViewType(int position) {
        int resType = -1;
        if (isEmpty) {
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
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list_item, parent, false);
                //itemView.setOnLongClickListener(mOnLongClickListener);
                vh = new ContactViewHolder(itemView);
                break;
            case VIEW_TYPE_EMPTY_ITEM:
                vh = super.onCreateViewHolder(parent,viewType);
                break;
        }
        return vh;
    }
}
