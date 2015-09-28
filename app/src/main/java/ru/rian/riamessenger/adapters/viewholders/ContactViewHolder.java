package ru.rian.riamessenger.adapters.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import ru.rian.riamessenger.R;

/**
 * Created by Roman on 7/2/2015.
 */

public class ContactViewHolder extends RecyclerView.ViewHolder {

    public final TextView contactName;
    public final TextView onlineStatus;
    public final CheckBox contactSelected;
    public ContactViewHolder(View itemView) {
        super(itemView);
        contactName = (TextView) itemView.findViewById(R.id.contact_name);
        onlineStatus = (TextView) itemView.findViewById(R.id.user_online_status);
        contactSelected = (CheckBox) itemView.findViewById(R.id.contact_selected);
    }
}
