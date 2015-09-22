package ru.rian.riamessenger.adapters.cursor;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
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
import ru.rian.riamessenger.listeners.BaseRiaListClickListener;
import ru.rian.riamessenger.model.ChatRoomModel;
import ru.rian.riamessenger.model.MessageContainer;
import ru.rian.riamessenger.model.RosterEntryModel;
import ru.rian.riamessenger.utils.DbHelper;
import ru.rian.riamessenger.utils.NetworkStateManager;
import ru.rian.riamessenger.utils.RiaTextUtils;
import ru.rian.riamessenger.utils.ViewUtils;

/**
 * Created by Roman on 6/30/2015.
 */
public class RoomsAdapter extends CursorRecyclerViewAdapter implements RosterEntryIdGetter {

    private boolean mListIsEmpty = false;
    final String currentJid;
    final BaseRiaListClickListener roomsListClickListener;
    final View.OnLongClickListener onLongClickListener;
    //final int EMPTY_VIEW_ITEM_TYPE = 2;
    private static final int LIST_EMPTY_ITEMS_COUNT = 1;

    public RoomsAdapter(Context context, Cursor cursor, String currentJid, BaseRiaListClickListener roomsListClickListener, View.OnLongClickListener onLongClickListener) {
        super(context, cursor);
        this.currentJid = currentJid;
        this.roomsListClickListener = roomsListClickListener;
        this.onLongClickListener = onLongClickListener;
    }

    @Override
    public int getItemCount() {
        int count = 0;
        Cursor cursor = getCursor();
        if (cursor != null && !cursor.isClosed()) {
            count = cursor.getCount();
        }
        if (count == 0) {
            mListIsEmpty = true;
            count = LIST_EMPTY_ITEMS_COUNT;//empty view
        } else {
            mListIsEmpty = false;
        }
        return count;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {
        switch (viewHolder.getItemViewType()) {
            case VIEW_TYPE_EMPTY_ITEM:
                break;
            case VIEW_TYPE_CONTENT:
                final val messageContainer = DbHelper.getModelByCursor(cursor, MessageContainer.class);
                if (messageContainer != null) {
                    final val contactViewHolder = (ChatViewHolder) viewHolder;

                    ChatRoomModel chatRoomModel = DbHelper.getChatRoomByJid(messageContainer.threadID);
                    if (chatRoomModel != null) {
                        String titleToSet;
                        titleToSet = RiaTextUtils.capFirst(chatRoomModel.name);
                        contactViewHolder.contactName.setText(titleToSet);
                        int unread = DbHelper.getUnReadMessagesNum(messageContainer.threadID);
                        if (unread != 0) {
                            contactViewHolder.onlineStatus.setText("" + unread);
                        } else {
                            contactViewHolder.onlineStatus.setText("");
                        }
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
        // final int count = getItemCount();
        if (mListIsEmpty) {
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
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_chat, parent, false);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        roomsListClickListener.onClick(RoomsAdapter.this, v);
                    }
                });
                itemView.setOnLongClickListener(onLongClickListener);
                vh = new ChatViewHolder(itemView);
                break;
            case VIEW_TYPE_EMPTY_ITEM:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_chats_empty, parent, false);
                itemView.setLayoutParams(new RecyclerView.LayoutParams(parent.getWidth(), parent.getHeight()));
                vh = new EmptyViewHolder(itemView);
                break;
        }
        return vh;
    }

    public MessageContainer getItem(int index) {
        MessageContainer messageContainer = null;
        Cursor cursor = getCursor();
        if (!cursor.isClosed() && cursor.moveToPosition(index)) {
            messageContainer = DbHelper.getModelByCursor(cursor, MessageContainer.class);
        }
        return messageContainer;

    }

    @Override
    public String getJid(int index) {
        String jidRes = null;
        MessageContainer messageContainer = getItem(index);
        if (messageContainer != null) {
            jidRes = messageContainer.threadID;
        }
        return jidRes;
    }
}
