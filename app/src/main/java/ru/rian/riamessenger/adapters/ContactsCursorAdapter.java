package ru.rian.riamessenger.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Created by Roman on 7/9/2015.
 */
public class ContactsCursorAdapter extends CursorRecyclerViewAdapter<RecyclerView.ViewHolder> {


    public ContactsCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }
}
