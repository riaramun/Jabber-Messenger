package ru.rian.riamessenger.common;

import android.database.Cursor;
import android.support.v4.app.LoaderManager;

import ru.rian.riamessenger.loaders.base.CursorRiaLoader;

/**
 * Created by Roman on 7/20/2015.
 */
public abstract class TabsRiaBaseActivity extends RiaBaseActivity  {
    abstract protected int getIdByTabIndex(int tabIndex);
    abstract protected String getTagByTabIndex(int tabIndex);
}
