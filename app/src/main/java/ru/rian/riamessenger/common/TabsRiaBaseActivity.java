package ru.rian.riamessenger.common;

/**
 * Created by Roman on 7/20/2015.
 */
public abstract class TabsRiaBaseActivity extends RiaBaseActivity  {
    abstract protected int getIdByTabIndex(int tabIndex);
    abstract protected String getTagByTabIndex(int tabIndex);
}
