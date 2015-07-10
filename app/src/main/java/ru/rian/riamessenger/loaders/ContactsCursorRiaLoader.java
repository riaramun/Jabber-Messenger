package ru.rian.riamessenger.loaders;

import android.content.Context;
import android.database.Cursor;

/**
 * Created by Roman on 7/9/2015.
 */
public class ContactsCursorRiaLoader extends CursorRiaLoader {


    public ContactsCursorRiaLoader(Context context) {
        super(context);
    }

    @Override
    protected Cursor loadCursor() throws Exception {
        return null;
    }

}
