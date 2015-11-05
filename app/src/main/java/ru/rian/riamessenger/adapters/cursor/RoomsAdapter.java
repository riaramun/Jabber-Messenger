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

import ru.rian.riamessenger.R;
import ru.rian.riamessenger.adapters.base.CursorRecyclerViewAdapter;
import ru.rian.riamessenger.adapters.list.RosterEntryIdGetter;
import ru.rian.riamessenger.adapters.viewholders.ChatViewHolder;
import ru.rian.riamessenger.adapters.viewholders.EmptyViewHolder;
import ru.rian.riamessenger.listeners.BaseRiaListClickListener;
import ru.rian.riamessenger.model.MessageContainer;
import ru.rian.riamessenger.model.RosterEntryModel;
import ru.rian.riamessenger.utils.DbHelper;
import ru.rian.riamessenger.utils.NetworkStateManager;
import ru.rian.riamessenger.utils.RiaTextUtils;
import ru.rian.riamessenger.utils.ViewUtils;
import ru.rian.riamessenger.utils.XmppUtils;

/**
 * Created by Roman on 6/30/2015.
 */
public class RoomsAdapter extends CursorRecyclerViewAdapter implements RosterEntryIdGetter {

    boolean mListIsEmpty = false;
    final String currentJid;
    final BaseRiaListClickListener roomsListClickListener;
    final View.OnLongClickListener onLongClickListener;

    static final int LIST_EMPTY_ITEMS_COUNT = 1;
    static final DateFormat timeFormat = new SimpleDateFormat("HH:mm");

    public RoomsAdapter(Context context, String currentJid, BaseRiaListClickListener roomsListClickListener, View.OnLongClickListener onLongClickListener) {
        super(context, null);
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
                final MessageContainer messageContainer = DbHelper.getModelByCursor(cursor, MessageContainer.class);
                if (messageContainer != null) {
                    final ChatViewHolder contactViewHolder = (ChatViewHolder) viewHolder;
                    String titleToSet = RiaTextUtils.capFirst(XmppUtils.roomNameFromJid(messageContainer.threadID));
                    contactViewHolder.contactName.setText(titleToSet);
                    int unread = DbHelper.getUnReadMessagesNum(messageContainer.threadID);
                    if (unread != 0) {
                        contactViewHolder.onlineStatus.setText("" + unread);
                    } else {
                        contactViewHolder.onlineStatus.setText("");
                    }
                    String bodyToSet = messageContainer.body;
                    String youStr = "";
                    if (messageContainer.fromJid.equals(currentJid)) {
                        youStr = mContext.getResources().getString(R.string.lastRoomMessageFromYou);

                    } else {
                        youStr = messageContainer.fromJid;
                    }
                    bodyToSet = youStr + ": " + bodyToSet;

                    SpannableString spannableString = new SpannableString(bodyToSet);
                    ForegroundColorSpan span = new ForegroundColorSpan(mContext.getResources().getColor(R.color.inserted_text));
                    spannableString.setSpan(span, 0, youStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    contactViewHolder.messageTextView.setText(spannableString);

                    contactViewHolder.dateTextView.setText(timeFormat.format(messageContainer.created));
                    if (NetworkStateManager.isNetworkAvailable(mContext)) {
                        ViewUtils.setOnlineStatus(contactViewHolder.onlineStatus, RosterEntryModel.UserStatus.USER_STATUS_AVAILIBLE.ordinal());
                    } else {
                        ViewUtils.setOnlineStatus(contactViewHolder.onlineStatus, RosterEntryModel.UserStatus.USER_STATUS_UNAVAILIBLE.ordinal());
                    }
                }
                break;
        }
    }


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
                itemView.setLayoutParams(new RecyclerView.LayoutParams(parent.getWidth(), parent.getHeight()-getActionBarHeight()));
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
    public String getUser(int index) {
        String jidRes = null;
        MessageContainer messageContainer = getItem(index);
        if (messageContainer != null) {
            jidRes = messageContainer.threadID;
        }
        return jidRes;
    }
}
