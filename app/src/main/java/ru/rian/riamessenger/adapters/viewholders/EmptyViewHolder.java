package ru.rian.riamessenger.adapters.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import lombok.Getter;
import ru.rian.riamessenger.R;

/**
 * Created by Roman on 7/14/2015.
 */
public class EmptyViewHolder extends RecyclerView.ViewHolder {

    @Getter
    @Bind(R.id.progress_bar)
    ProgressBar riaProgress;

    @Getter
    @Bind(R.id.empty_text)
    TextView emptyText;

    public EmptyViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(itemView);
    }
}
