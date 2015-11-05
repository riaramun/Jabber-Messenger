package ru.rian.riamessenger.adapters.cursor;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

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
import ru.rian.riamessenger.xmpp.SmackRosterManager;

/**
 * Created by Roman on 6/30/2015.
 */
public class ChatsAdapter extends CursorRecyclerViewAdapter implements RosterEntryIdGetter {

    boolean mListIsEmpty = false;
    final String currentJid;
    final BaseRiaListClickListener contactsListClickListener;
    final View.OnLongClickListener onLongClickListener;
    //final int EMPTY_VIEW_ITEM_TYPE = 2;
    static final int LIST_EMPTY_ITEMS_COUNT = 1;
    static final DateFormat timeFormat = new SimpleDateFormat("HH:mm");

    public ChatsAdapter(Context context, String currentJid, BaseRiaListClickListener contactsListClickListener, View.OnLongClickListener onLongClickListener) {
        super(context, null);
        this.currentJid = currentJid;
        this.contactsListClickListener = contactsListClickListener;
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

                    RosterEntryModel rosterEntryModel = DbHelper.getRosterEntryByBareJid(messageContainer.threadID);
                    if (rosterEntryModel != null) {
                        String titleToSet;
                        if (rosterEntryModel.rosterGroupModel != null
                                && rosterEntryModel.rosterGroupModel.name.equals(SmackRosterManager.FIRST_SORTED_GROUP)) {
                            titleToSet = rosterEntryModel.name;
                        } else {
                            titleToSet = RiaTextUtils.capFirst(rosterEntryModel.name);
                        }
                        contactViewHolder.contactName.setText(titleToSet);

                        if (NetworkStateManager.isNetworkAvailable(mContext)) {
                            ViewUtils.setOnlineStatus(contactViewHolder.onlineStatus, rosterEntryModel.presence);
                        } else {
                            ViewUtils.setOnlineStatus(contactViewHolder.onlineStatus, RosterEntryModel.UserStatus.USER_STATUS_UNAVAILIBLE.ordinal());
                        }


                        int unread = DbHelper.getUnReadMessagesNum(messageContainer.threadID);
                        if (unread != 0) {
                            contactViewHolder.onlineStatus.setText("" + unread);
                        } else {
                            contactViewHolder.onlineStatus.setText("");
                        }
                    }
                    String bodyToSet = messageContainer.body;

                    if (messageContainer.fromJid.equals(currentJid)) {
                        String youStr = mContext.getResources().getString(R.string.lastRoomMessageFromYou);
                        bodyToSet = youStr +": " + bodyToSet;
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
                        contactsListClickListener.onClick(ChatsAdapter.this, v);
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
