/*
 *    Copyright (C) 2015 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package ru.rian.riamessenger.utils;

import android.content.Context;
import android.widget.TextView;

import ru.rian.riamessenger.R;
import ru.rian.riamessenger.model.RosterEntryModel;

public class ViewUtils {

    public static void setOnlineStatus(TextView onlineStatus, int presence) {
        int resId = -1;
        RosterEntryModel.UserStatus mode = RosterEntryModel.UserStatus.values()[presence];
        switch (mode) {
            case USER_STATUS_AVAILIBLE:
                resId = R.drawable.status_online;
                break;
            case USER_STATUS_AWAY:
                resId = R.drawable.status_away;
                break;
            case USER_STATUS_UNAVAILIBLE:
                resId = R.drawable.status_offline;
                break;
        }
        onlineStatus.setBackgroundResource(resId);
    }

    public static int getIconIdByPresence(RosterEntryModel rosterEntryModel, Context context) {
        int resId = -1;

        if (rosterEntryModel == null || !NetworkStateManager.isNetworkAvailable(context)) {
            resId = R.drawable.action_bar_status_offline;
        } else {
            RosterEntryModel.UserStatus mode = RosterEntryModel.UserStatus.values()[rosterEntryModel.presence];
            switch (mode) {
                case USER_STATUS_AVAILIBLE:
                    resId = R.drawable.action_bar_status_online;
                    break;
                case USER_STATUS_AWAY:
                    resId = R.drawable.action_bar_status_away;
                    break;
                case USER_STATUS_UNAVAILIBLE:
                    resId = R.drawable.action_bar_status_offline;
                    break;
            }
        }
        return resId;
    }
}
