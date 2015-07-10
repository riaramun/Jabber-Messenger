/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.rian.riamessenger.common;

import android.support.v4.app.Fragment;

import org.jivesoftware.smack.roster.Roster;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import ru.rian.riamessenger.riaevents.client.RosterClientEvent;

/**
 * Base fragment which performs injection using the activity object graph of its parent.
 */
public class RiaBaseFragment extends Fragment {

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
