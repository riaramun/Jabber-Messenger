package ru.rian.riamessenger.adapters.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import ru.rian.riamessenger.R;

/**
 * Created by Roman on 7/2/2015.
 */

public class ChatViewHolder extends RecyclerView.ViewHolder {

    public TextView messageTextView;
    public TextView dateTextView;
    public TextView contactName;
    public TextView onlineStatus;

    public ChatViewHolder(View itemView) {
        super(itemView);
        messageTextView = (TextView) itemView.findViewById(R.id.message_text);
        dateTextView = (TextView) itemView.findViewById(R.id.message_created_date);
        contactName = (TextView) itemView.findViewById(R.id.message_from);
        onlineStatus = (TextView) itemView.findViewById(R.id.user_online_status);
    }

}
