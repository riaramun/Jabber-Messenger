package ru.rian.riamessenger.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.devspark.robototextview.widget.RobotoTextView;

import lombok.Getter;
import lombok.ToString;
import ru.rian.riamessenger.R;

/**
 * Created by Roman on 7/2/2015.
 */
@ToString
public class ContactViewHolder extends RecyclerView.ViewHolder {

    @Getter
    // @Bind(R.id.contact_name)
            TextView contactName;

    public ContactViewHolder(View itemView) {
        super(itemView);
        contactName = (TextView) itemView.findViewById(R.id.text);
        //ButterKnife.bind(itemView);
    }
}
