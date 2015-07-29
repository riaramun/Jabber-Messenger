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
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gc.materialdesign.views.ButtonFloat;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lombok.val;
import ru.rian.riamessenger.ContactsActivity;
import ru.rian.riamessenger.R;
import ru.rian.riamessenger.RiaApplication;
import ru.rian.riamessenger.adapters.cursor.ChatsAdapter;
import ru.rian.riamessenger.loaders.ChatsBaseLoader;
import ru.rian.riamessenger.loaders.ChatsListenerLoader;
import ru.rian.riamessenger.loaders.ChatsOnlineStatesLoader;
import ru.rian.riamessenger.loaders.base.CursorRiaLoader;

public class ChatsFragment extends BaseTabFragment {

    protected LinearLayoutManager linearLayoutManager;

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;

    @Bind(R.id.buttonFloat)
    ButtonFloat buttonFloat;

    @OnClick(R.id.buttonFloat)
    void onClick() {
        Intent intent = new Intent(getActivity(), ContactsActivity.class);
        getActivity().startActivity(intent);
    }

    ChatsAdapter chatsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_chats, container, false);
        ButterKnife.bind(this, rootView);
        RiaApplication.component().inject(this);

        buttonFloat.setDrawableIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_add_white));
        buttonFloat.setBackgroundColor(getResources().getColor(R.color.floating_buton_color));
        chatsAdapter = new ChatsAdapter(getActivity(), null, userAppPreference.getJidStringKey(), contactsListClickListener);
        recyclerView.setAdapter(chatsAdapter);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        val itemAnimator = new DefaultItemAnimator();
        recyclerView.setItemAnimator(itemAnimator);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_chats, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_news).getActionView();
        setSearchViewListenersAndStyle(searchView);
    }

    @Override
    public Loader<CursorRiaLoader.LoaderResult<Cursor>> onCreateLoader(int id, Bundle args) {
        FragIds fragIds = FragIds.values()[id];
        switch (fragIds) {
            case CHATS_FRAGMENT:
                return new ChatsListenerLoader(getActivity(), args);
            case CHAT_USER_STATUS_LOADER_ID:
                return new ChatsOnlineStatesLoader(getActivity(), args);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<CursorRiaLoader.LoaderResult<Cursor>> loader, CursorRiaLoader.LoaderResult<Cursor> data) {
        chatsAdapter.changeCursor(data.result);
    }

    protected Bundle getBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_TAB_ID, tabId);
        bundle.putString(ARG_JID_TO_EXCLUDE, userAppPreference.getJidStringKey());
        return bundle;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FragIds.CHAT_USER_STATUS_LOADER_ID.ordinal(), getBundle(), this);
    }

    public static final String ARG_JID_TO_EXCLUDE = "jid_to_exclude";
}
