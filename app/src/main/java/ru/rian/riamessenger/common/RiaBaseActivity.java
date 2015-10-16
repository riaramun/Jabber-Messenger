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
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.devspark.appmsg.AppMsg;

import de.greenrobot.event.EventBus;
import ru.rian.riamessenger.R;
import ru.rian.riamessenger.loaders.base.CursorRiaLoader;
import ru.rian.riamessenger.riaevents.response.XmppErrorEvent;
import ru.rian.riamessenger.RiaXmppService;
import ru.rian.riamessenger.utils.SysUtils;


/**
 * Base activity which sets up a per-activity object graph and performs injection.
 */
public abstract class RiaBaseActivity extends AppCompatActivity {

    public static final String ARG_FROM_JID = "from_jid";
    public static final String ARG_TO_JID = "to_jid";
    public static final String ARG_ROOM_JID = "room_jid";

    protected final int MESSAGES_LOADER_ID = 100;
    protected final int USER_STATUS_LOADER_ID = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        if (!SysUtils.isMyServiceRunning(RiaXmppService.class, this)) {
            Intent intent = new Intent(this, RiaXmppService.class);
            startService(intent);
        }
    }

    protected String getExtraJid(String arg_jid) {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        return bundle.getString(arg_jid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    protected void initOrRestartLoader(int loaderId, Bundle bundle, LoaderManager.LoaderCallbacks<CursorRiaLoader.LoaderResult<Cursor>> callback) {
        if (getSupportLoaderManager().getLoader(loaderId) == null) {
            getSupportLoaderManager().initLoader(loaderId, bundle, callback);
        } else {
            getSupportLoaderManager().restartLoader(loaderId, bundle, callback);
        }
    }

    public void onEvent(final XmppErrorEvent xmppErrorEvent) {
        String msg = null;
        switch (xmppErrorEvent.state) {
            case EConnected:
                msg = "Server connected";
                break;
            case EReconnectionFailed:
                msg = "Reconnection failed";
                break;
            case EConnectionClosed:
                msg = "Connection closed";
                break;
            case EDbUpdated:
                //preocessed in fragment
                //dbUpdated();
                msg = "Roster updated";
                break;
            case Empty:
                msg = xmppErrorEvent.exceptionMessage;
                break;
        }
        if (msg != null) {
            Log.i("RiaBaseActivity", msg);
            //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    protected static void showAppMsgInView(Context aContext, String aMsg) {

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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
