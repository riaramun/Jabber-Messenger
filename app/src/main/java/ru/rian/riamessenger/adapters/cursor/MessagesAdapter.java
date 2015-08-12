package ru.rian.riamessenger.adapters.cursor;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import lombok.val;
import ru.rian.riamessenger.R;
import ru.rian.riamessenger.adapters.base.CursorRecyclerViewAdapter;
import ru.rian.riamessenger.adapters.viewholders.EmptyViewHolder;
import ru.rian.riamessenger.adapters.viewholders.MessageViewHolder;
import ru.rian.riamessenger.model.MessageContainer;
import ru.rian.riamessenger.utils.DbHelper;

/**
 * Created by Roman on 6/30/2015.
 */
public class MessagesAdapter extends CursorRecyclerViewAdapter {

    protected static final int VIEW_TYPE_CONTENT_OUTCOME_MSG = 0x04;
    protected static final int VIEW_TYPE_CONTENT_INCOME_MSG = 0x05;

    EmptyViewHolder emptyViewHolder;
    final String currentJid;

    public MessagesAdapter(Context context, Cursor cursor, String currentJid) {
        super(context, cursor);
        this.currentJid = currentJid;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {
        switch (viewHolder.getItemViewType()) {
            case VIEW_TYPE_EMPTY_ITEM:
                emptyViewHolder = (EmptyViewHolder) viewHolder;
                break;
            case VIEW_TYPE_CONTENT_OUTCOME_MSG:
            case VIEW_TYPE_CONTENT_INCOME_MSG:
                final val messageContainer = DbHelper.getModelByCursor(cursor, MessageContainer.class);
                if (messageContainer != null) {
                    final val messageViewHolder = (MessageViewHolder) viewHolder;
                    messageViewHolder.messageTextView.setText(messageContainer.body);
                    messageViewHolder.dateTextView.setText(timeFormat.format(messageContainer.created));
                    if (viewHolder.getItemViewType() == VIEW_TYPE_CONTENT_OUTCOME_MSG) {
                        messageViewHolder.messageSentIcon.setVisibility(messageContainer.isSent ? View.VISIBLE : View.GONE);
                    } else {
                        if (!messageContainer.isRead) {
                            messageContainer.isRead = true;
                            messageContainer.save();
                        }
                    }
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
                    resType = VIEW_TYPE_CONTENT_OUTCOME_MSG;
                } else {
                    resType = VIEW_TYPE_CONTENT_INCOME_MSG;
                }
            }
        }
        return resType;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        RecyclerView.ViewHolder vh = null;
        final DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        switch (viewType) {
            case VIEW_TYPE_CONTENT_OUTCOME_MSG:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_message_outcome, parent, false);
                vh = new MessageViewHolder(itemView);
                ((MessageViewHolder) vh).messageTextView.setMaxWidth(displayMetrics.widthPixels / 2);
                break;
            case VIEW_TYPE_CONTENT_INCOME_MSG:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_message_income, parent, false);
                vh = new MessageViewHolder(itemView);
                ((MessageViewHolder) vh).messageTextView.setMaxWidth(displayMetrics.widthPixels / 2);
                break;
            case VIEW_TYPE_EMPTY_ITEM:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_empty, parent, false);
                // itemView.setLayoutParams(new RecyclerView.LayoutParams(parent.getWidth(), parent.getHeight()));
                vh = new EmptyViewHolder(itemView);
                emptyViewHolder = (EmptyViewHolder) vh;
                break;
        }
        return vh;
    }
}
