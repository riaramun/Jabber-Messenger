package ru.rian.riamessenger.adapters.cursor;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import lombok.val;
import ru.rian.riamessenger.R;
import ru.rian.riamessenger.adapters.base.CursorRecyclerViewAdapter;
import ru.rian.riamessenger.adapters.list.RosterEntryIdGetter;
import ru.rian.riamessenger.adapters.viewholders.ChatViewHolder;
import ru.rian.riamessenger.adapters.viewholders.EmptyViewHolder;
import ru.rian.riamessenger.listeners.ContactsListClickListener;
import ru.rian.riamessenger.model.MessageContainer;
import ru.rian.riamessenger.model.RosterEntryModel;
import ru.rian.riamessenger.utils.DbHelper;
import ru.rian.riamessenger.utils.RiaTextUtils;
import ru.rian.riamessenger.utils.ViewUtils;

/**
 * Created by Roman on 6/30/2015.
 */
public class ChatsAdapter extends CursorRecyclerViewAdapter implements RosterEntryIdGetter {

    EmptyViewHolder emptyViewHolder;
    final String currentJid;
    final ContactsListClickListener contactsListClickListener;

    public ChatsAdapter(Context context, Cursor cursor, String currentJid, ContactsListClickListener contactsListClickListener) {
        super(context, cursor);
        this.currentJid = currentJid;
        this.contactsListClickListener = contactsListClickListener;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {
        switch (viewHolder.getItemViewType()) {
            case VIEW_TYPE_EMPTY_ITEM:
                emptyViewHolder = (EmptyViewHolder) viewHolder;
                break;
            case VIEW_TYPE_CONTENT:
                final val messageContainer = DbHelper.getModelByCursor(cursor, MessageContainer.class);
                if (messageContainer != null) {
                    final val contactViewHolder = (ChatViewHolder) viewHolder;

                    RosterEntryModel rosterEntryModel = DbHelper.getRosterEntryByBareJid(messageContainer.threadID);

                    if (rosterEntryModel != null) {
                        String titleToSet;
                        if (rosterEntryModel.rosterGroupModel.name.equals(mContext.getString(R.string.robots))) {
                            titleToSet = rosterEntryModel.name;
                        } else {
                            titleToSet = RiaTextUtils.capFirst(rosterEntryModel.name);
                        }
                        contactViewHolder.contactName.setText(titleToSet);
                        ViewUtils.setOnlineStatus(contactViewHolder.onlineStatus, rosterEntryModel.presence);
                    }

                    String bodyToSet = messageContainer.body;

                    if (messageContainer.fromJid.equals(currentJid)) {
                        String youStr = mContext.getResources().getString(R.string.you);
                        bodyToSet = youStr + bodyToSet;
                        SpannableString spannableString = new SpannableString(bodyToSet);
                        ForegroundColorSpan span = new ForegroundColorSpan(mContext.getResources().getColor(R.color.inserted_text));
                        spannableString.setSpan(span, 0, youStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        contactViewHolder.messageTextView.setText(spannableString);
                    } else {
                        contactViewHolder.messageTextView.setText(bodyToSet);
                    }

                    contactViewHolder.dateTextView.setText(timeFormat.format(messageContainer.created));
                }
                break;
        }
    }

    private static final DateFormat timeFormat = new SimpleDateFormat("HH:mm");

    @Override
    public int getItemViewType(int position) {
        int resType = -1;
        if (getItemCount() <= 0) {
            resType = VIEW_TYPE_EMPTY_ITEM;
        } else {
            if (getCursor().moveToPosition(position)) {
                final val messageContainer = DbHelper.getModelByCursor(getCursor(), MessageContainer.class);
                if (messageContainer.fromJid.contains(currentJid)) {
                    resType = VIEW_TYPE_CONTENT;
                } else {
                    resType = VIEW_TYPE_CONTENT;
                }
            }
        }
        return resType;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        RecyclerView.ViewHolder vh = null;
        switch (viewType) {
            case VIEW_TYPE_CONTENT:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_chat, parent, false);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        contactsListClickListener.onClick(ChatsAdapter.this, v);
                    }
                });
                vh = new ChatViewHolder(itemView);
                break;
            case VIEW_TYPE_EMPTY_ITEM:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_empty, parent, false);
                vh = new EmptyViewHolder(itemView);
                emptyViewHolder = (EmptyViewHolder) vh;
                break;
        }
        return vh;
    }

    @Override
    public String getJid(int index) {
        String jidRes = null;
        if (getCursor() != null) {
            if (getCursor().moveToPosition(index)) {
                final val messageContainer = DbHelper.getModelByCursor(getCursor(), MessageContainer.class);
                if (messageContainer != null) {
                    jidRes = messageContainer.threadID;
                }
            }
        }
        return jidRes;
    }
}
