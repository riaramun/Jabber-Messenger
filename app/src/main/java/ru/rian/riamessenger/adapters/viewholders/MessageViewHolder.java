package ru.rian.riamessenger.adapters.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.rian.riamessenger.R;
import ru.rian.riamessenger.model.RosterEntryModel;

/**
 * Created by Roman on 7/2/2015.
 */

public class MessageViewHolder extends RecyclerView.ViewHolder {

    public TextView messageTextView;

    public MessageViewHolder(View itemView) {
        super(itemView);
        messageTextView = (TextView) itemView.findViewById(R.id.message_text_view);
    }

}
