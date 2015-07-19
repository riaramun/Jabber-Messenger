package ru.rian.riamessenger.adapters.viewholders;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;


import org.jivesoftware.smack.packet.Presence;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.rian.riamessenger.R;
import ru.rian.riamessenger.model.RosterEntryModel;

/**
 * Created by Roman on 7/2/2015.
 */

public class ContactViewHolder extends RecyclerView.ViewHolder {

    // @Bind(R.id.contact_name)
    public TextView contactName;
    public TextView onlineStatus;

    public ContactViewHolder(View itemView) {
        super(itemView);
        contactName = (TextView) itemView.findViewById(R.id.contact_name);
        onlineStatus = (TextView) itemView.findViewById(R.id.user_online_status);
        //   ButterKnife.bind(itemView);
    }

    public void setOnlineStatus(int presence) {
        int resId = -1;
        RosterEntryModel.UserStatus mode = RosterEntryModel.UserStatus.values()[presence];
        switch (mode) {
            case USER_STATUS_AVAILIBLE:
                resId = R.drawable.status_online;
                break;
            case USER_STATUS_AWAY:
                resId = R.drawable.status_away;
                break;
            case USER_STATUS_UNAVAILIBLE:
                resId = R.drawable.status_offline;
                break;
        }
        onlineStatus.setBackgroundResource(resId);
    }
}
