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
import android.support.v4.app.DialogFragment;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import lombok.val;
import ru.rian.riamessenger.R;
import ru.rian.riamessenger.adapters.cursor.RobotsAdapter;
import ru.rian.riamessenger.common.RiaBaseFragment;
import ru.rian.riamessenger.loaders.ContactsLoader;
import ru.rian.riamessenger.loaders.base.CursorRiaLoader;
import ru.rian.riamessenger.riaevents.ui.ChatEvents;

public class ChatRemoveDialogFragment extends DialogFragment {

    static public final String TAG = ChatRemoveDialogFragment.class.getSimpleName();
    static public final String ARG_REMOVE_CHAT = "remove_chat_arg";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_remove_chat, container, false);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        rootView.findViewById(R.id.chat_remove_buttton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chatThreadId = getArguments().getString(ARG_REMOVE_CHAT);
                EventBus.getDefault().post(new ChatEvents(ChatEvents.DO_REMOVE_CHAT, chatThreadId));
                ChatRemoveDialogFragment.this.dismiss();
            }
        });
        return rootView;
    }

}
