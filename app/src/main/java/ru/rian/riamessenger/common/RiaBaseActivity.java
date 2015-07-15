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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import de.greenrobot.event.EventBus;
import ru.rian.riamessenger.ContactsActivity;
import ru.rian.riamessenger.RiaBaseApplication;
import ru.rian.riamessenger.riaevents.response.XmppErrorEvent;


/**
 * Base activity which sets up a per-activity object graph and performs injection.
 */
public abstract class RiaBaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    protected abstract void authenticated();

    public void onEvent(final XmppErrorEvent xmppErrorEvent) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String msg = null;
                switch (xmppErrorEvent.state) {
                    case EConnected:
                        msg = "Server connected";
                        break;
                    case EReconnectionFailed:
                        msg = "Reconnection failed";
                        break;
                    case EAuthenticated:
                        authenticated();
                        msg = "Authenticated";
                        break;
                    case EConnectionClosed:
                        msg = "Connection closed";
                        break;
                    case EDbUpdated:
                        //preocessed in fragment
                        //dbUpdated();
                        msg = "Roster data base updated";
                        break;
                    case Empty:
                        msg = xmppErrorEvent.exceptionMessage;
                        break;
                }
                if(msg != null) {
                    Log.i("RiaBaseActivity", msg);
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}
