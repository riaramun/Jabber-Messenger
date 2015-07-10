package ru.rian.riamessenger.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jivesoftware.smack.roster.RosterEntry;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import lombok.Getter;
import ru.rian.riamessenger.R;

/**
 * Created by Roman on 7/2/2015.
 */
public abstract class BaseRiaRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected static final int VIEW_TYPE_EMPTY_ITEM = 0x01;
    protected static final int VIEW_TYPE_HEADER = 0x02;
    protected static final int VIEW_TYPE_CONTENT = 0x03;


    protected List<?> entries = null;

    @Getter
    boolean isEmpty = false;
    EmptyViewHolder emptyViewHolder;

    public void updateEntries(List<?> entries) {
        if(entries != null) {
            this.entries = entries;
            notifyDataSetChanged();
        }
    }
    @Override
    public int getItemCount() {
        int count = entries == null ? 0 : entries.size();
        if (count == 0) {
            isEmpty = true;
            count = 1;//empty view
        } else {
            isEmpty = false;
        }
        return count;
    }

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

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        RecyclerView.ViewHolder vh = null;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.empty_list_item, parent, false);
       // itemView.setLayoutParams(new RecyclerView.LayoutParams(parent.getWidth(), parent.getHeight()));
        vh = new EmptyViewHolder(itemView);
        emptyViewHolder = (EmptyViewHolder) vh;
        return vh;
    }
}
