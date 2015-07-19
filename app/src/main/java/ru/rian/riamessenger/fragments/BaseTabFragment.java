/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.rian.riamessenger.fragments;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import de.greenrobot.event.EventBus;
import ru.rian.riamessenger.ContactsActivity;
import ru.rian.riamessenger.common.RiaBaseFragment;
import ru.rian.riamessenger.loaders.ContactsLoader;
import ru.rian.riamessenger.loaders.base.BaseCursorRiaLoader;
import ru.rian.riamessenger.loaders.base.CursorRiaLoader;
import ru.rian.riamessenger.riaevents.response.XmppErrorEvent;


public abstract class BaseTabFragment extends RiaBaseFragment implements LoaderManager.LoaderCallbacks<CursorRiaLoader.LoaderResult<Cursor>> {

    protected int tabId;


    public static BaseTabFragment newInstance(int tabId) {
        BaseTabFragment tabFragment = null;
        switch (tabId) {
            case ContactsActivity.CONTACTS_FRAGMENT:
                tabFragment = new ContactsFragment();
                break;
            case ContactsActivity.GROUPS_FRAGMENT:
                tabFragment = new GroupsFragment();
                break;
            case ContactsActivity.ROBOTS_FRAGMENT:
                tabFragment = new RobotsFragment();
                break;
        }
        if (tabFragment != null) {
            Bundle b = new Bundle();
            b.putInt(ContactsActivity.ARG_TAB_ID, tabId);
            tabFragment.setArguments(b);
        }
        return tabFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //RiaApplication.component().inject(this);
        tabId = getArguments().getInt(ContactsActivity.ARG_TAB_ID);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
        } else {

        }
        getLoaderManager().initLoader(tabId, getBundle(), this);

    }

    protected Bundle getBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt(ContactsActivity.ARG_TAB_ID, tabId);
        return bundle;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    /*public void dbRestartLoader() {
        getLoaderManager().restartLoader(tabId, getBundle(), this);
    }*/

    public void onEvent(final XmppErrorEvent xmppErrorEvent) {

        switch (xmppErrorEvent.state) {
            case EDbUpdated:
                isUpdating = false;
                break;
            case EDbUpdating:
                isUpdating = true;
                break;
        }
    }

    Boolean isUpdating = false;


    @Override
    public Loader<CursorRiaLoader.LoaderResult<Cursor>> onCreateLoader(int id, Bundle args) {
        return new ContactsLoader(getActivity(), args);
    }

    @Override
    public void onLoaderReset(Loader<CursorRiaLoader.LoaderResult<Cursor>> loader) {

    }
}
