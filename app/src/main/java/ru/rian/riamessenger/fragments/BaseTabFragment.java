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


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.SearchView;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import ru.rian.riamessenger.RiaApplication;
import ru.rian.riamessenger.common.RiaBaseFragment;
import ru.rian.riamessenger.listeners.ContactsListClickListener;
import ru.rian.riamessenger.listeners.RoomsListClickListener;
import ru.rian.riamessenger.loaders.base.CursorRiaLoader;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.riaevents.response.XmppErrorEvent;
import ru.rian.riamessenger.utils.ScreenUtils;


public abstract class BaseTabFragment extends RiaBaseFragment implements LoaderManager.LoaderCallbacks<CursorRiaLoader.LoaderResult<Cursor>> {

    @Inject
    UserAppPreference userAppPreference;

    @Inject
    ContactsListClickListener contactsListClickListener;

    @Inject
    RoomsListClickListener roomsListClickListener;


    static public final String CHATS_FRAGMENT_TAG = ChatsFragment.class.getSimpleName();
    static public final String ROOMS_FRAGMENT_TAG = RoomsFragment.class.getSimpleName();
    static public final String ROBOTS_FRAGMENT_TAG = RobotsFragment.class.getSimpleName();
    static public final String GROUPS_FRAGMENT_TAG = GroupsFragment.class.getSimpleName();
    static public final String CONTACTS_FRAGMENT_TAG = ContactsFragment.class.getSimpleName();

    int tabId = 0;

    public enum FragIds {
        ROBOTS_FRAGMENT,
        GROUPS_FRAGMENT,
        CONTACTS_FRAGMENT,
        CHATS_FRAGMENT,
        ROOMS_FRAGMENT,
        CHAT_USER_STATUS_LOADER_ID
    }


    public static final String ARG_TAB_ID = "tabId";
    public static final String ARG_TITLE_FILTER = "title_filter";

    public static BaseTabFragment newInstance(int tabId) {

        BaseTabFragment tabFragment = null;
        switch (FragIds.values()[tabId]) {
            case CONTACTS_FRAGMENT:
                tabFragment = new ContactsFragment();
                break;
            case GROUPS_FRAGMENT:
                tabFragment = new GroupsFragment();
                break;
            case ROBOTS_FRAGMENT:
                tabFragment = new RobotsFragment();
                break;
            case CHATS_FRAGMENT:
                tabFragment = new ChatsFragment();
                break;
            case ROOMS_FRAGMENT:
                tabFragment = new RoomsFragment();
                break;
        }
        if (tabFragment != null) {
            Bundle b = new Bundle();
            b.putInt(ARG_TAB_ID, tabId);
            tabFragment.setArguments(b);
        }
        return tabFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RiaApplication.component().inject(this);
        if (getArguments() != null)
            tabId = getArguments().getInt(ARG_TAB_ID);
        else {
            //we put contacts fragment as a defalt
            tabId = FragIds.CONTACTS_FRAGMENT.ordinal();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
        } else {
        }
    }

    Bundle getBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_TAB_ID, tabId);
        return bundle;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        initOrRestartLoader(tabId, getBundle(), BaseTabFragment.this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    /*public void dbRestartLoader() {
        getLoaderManager().restartLoader(tabId, getBundle(), this);
    }*/

    protected abstract void rosterLoaded(boolean isLoaded);

    public void onEvent(final XmppErrorEvent xmppErrorEvent) {
        switch (xmppErrorEvent.state) {
            case ENotConnecting:
            case EConnecting:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rosterLoaded(xmppErrorEvent.state == XmppErrorEvent.State.ENotConnecting ? true : false);
                    }
                });
                break;
            case EMessageReceived:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (FragIds.values()[tabId] == FragIds.CHATS_FRAGMENT
                                || FragIds.values()[tabId] == FragIds.ROBOTS_FRAGMENT
                                || FragIds.values()[tabId] == FragIds.ROOMS_FRAGMENT) {
                            initOrRestartLoader(tabId, getBundle(), BaseTabFragment.this);
                        }
                    }
                });
                //Message loader is not restarted immediately for unknown reason, so we do it by this event

                break;
        }
    }

    String title_to_search = null;

    void setSearchViewListenersAndStyle(SearchView searchView) {

        ScreenUtils.styleSearchView(searchView, title_to_search, getActivity());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                ScreenUtils.hideKeyboard(getActivity());
                title_to_search = query;
                initOrRestartLoader(tabId, getBundle(), BaseTabFragment.this);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                title_to_search = newText;
                initOrRestartLoader(tabId, getBundle(), BaseTabFragment.this);
                return false;
            }
        });
    }

    void initOrRestartLoader(int loaderId, Bundle bundle, LoaderManager.LoaderCallbacks<CursorRiaLoader.LoaderResult<Cursor>> callback) {
        if (getLoaderManager().getLoader(loaderId) == null) {
            getLoaderManager().initLoader(loaderId, bundle, callback);
        } else {
            getLoaderManager().restartLoader(loaderId, bundle, callback);
        }
    }

    @Override
    public void onLoaderReset(Loader<CursorRiaLoader.LoaderResult<Cursor>> loader) {

    }

}
