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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.devspark.appmsg.AppMsg;

import de.greenrobot.event.EventBus;
import ru.rian.riamessenger.ContactsActivity;
import ru.rian.riamessenger.R;
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
                        authenticated(true);
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
                    case EAuthenticationFailed:
                        authenticated(false);
                        showAppMsgInView(RiaBaseActivity.this, getString(R.string.sign_in_error));
                        break;
                }
                if (msg != null) {
                    Log.i("RiaBaseActivity", msg);
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public static void showAppMsgInView(Context aContext, String aMsg) {

        AppMsg.Style style = new AppMsg.Style(AppMsg.LENGTH_LONG, R.color.app_msg_bg);
        AppMsg appMsg = AppMsg.makeText((Activity) aContext, aMsg, style, R.layout.app_msg);
        //String str = appMsg.toString();
        //TextView textView = new TextView(aContext);
        //textView.setText(aMsg);
        //appMsg.setView(textView);
        //textView.setBackgroundColor(aContext.getResources().getColor(R.color.discovery_high_lighted_button));
        appMsg.setParent(R.id.app_message_view);
        appMsg.setDuration(1000);
        appMsg.show();
    }

    protected abstract void authenticated(boolean isAuthenticated);
}
