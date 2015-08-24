package ru.rian.riamessenger.adapters.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.malinskiy.materialicons.widget.IconTextView;

import ru.rian.riamessenger.R;

/**
 * Created by Roman on 7/2/2015.
 */

public class MessageViewHolder extends RecyclerView.ViewHolder {

    public IconTextView messageSentIcon;
    public TextView messageTextView;
    public TextView dateTextView;
    public TextView messageTodayDate;

    public MessageViewHolder(View itemView) {
        super(itemView);
        messageTodayDate = (TextView) itemView.findViewById(R.id.message_today_date);
        messageSentIcon = (IconTextView) itemView.findViewById(R.id.message_sent_icon);
        messageTextView = (TextView) itemView.findViewById(R.id.message_text);
        dateTextView = (TextView) itemView.findViewById(R.id.message_created_date);
    }

}
