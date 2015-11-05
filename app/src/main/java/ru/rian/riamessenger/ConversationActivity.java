package ru.rian.riamessenger;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.malinskiy.materialicons.widget.IconTextView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import de.greenrobot.event.EventBus;
import ru.rian.riamessenger.adapters.cursor.MessagesAdapter;
import ru.rian.riamessenger.common.RiaBaseActivity;
import ru.rian.riamessenger.loaders.MessagesLoader;
import ru.rian.riamessenger.loaders.UserOnlineStatusLoader;
import ru.rian.riamessenger.loaders.base.CursorRiaLoader;
import ru.rian.riamessenger.model.ChatRoomModel;
import ru.rian.riamessenger.model.RosterEntryModel;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.riaevents.request.ChatMessageEvent;
import ru.rian.riamessenger.riaevents.request.RiaUpdateCurrentUserPresenceEvent;
import ru.rian.riamessenger.riaevents.request.RoomMessageEvent;
import ru.rian.riamessenger.riaevents.response.XmppErrorEvent;
import ru.rian.riamessenger.utils.DbHelper;
import ru.rian.riamessenger.utils.NetworkStateManager;
import ru.rian.riamessenger.utils.RiaTextUtils;
import ru.rian.riamessenger.utils.ScreenUtils;
import ru.rian.riamessenger.utils.ViewUtils;
import ru.rian.riamessenger.utils.XmppUtils;
import ru.rian.riamessenger.xmpp.SmackRosterManager;

/**
 * Created by Roman on 7/21/2015.
 */
public class ConversationActivity extends RiaBaseActivity implements LoaderManager.LoaderCallbacks<CursorRiaLoader.LoaderResult<Cursor>> {

    @Bind(R.id.progress_bar)

    ProgressBar progressBar;

    @Inject
    public
    UserAppPreference userAppPreference;

    @Bind(R.id.container)
    RelativeLayout relativeLayoutContainer;

    @Bind(R.id.send_icon_text_view)

    IconTextView sendIconTextView;

    @Bind(R.id.recycler_view)

    RecyclerView recyclerView;

    @Bind(R.id.message_edit_text)

    EditText messageEditText;
    MessagesAdapter messagesAdapter;
    LinearLayoutManager linearLayoutManager;

    @OnTextChanged(R.id.message_edit_text)
    void onTextChanged(CharSequence text) {
        sendIconTextView.setEnabled(!messageEditText.getText().toString().isEmpty());
    }

    @OnClick(R.id.send_icon_text_view)
    void onClick() {
        String jid_to = getExtraJid(ARG_TO_JID);
        if (!TextUtils.isEmpty(jid_to)) {
            EventBus.getDefault().post(new ChatMessageEvent(jid_to, messageEditText.getText().toString()));
        } else {
            jid_to = getExtraJid(ARG_ROOM_JID);
            EventBus.getDefault().post(new RoomMessageEvent(jid_to, messageEditText.getText().toString()));
        }

        ScreenUtils.hideKeyboard(ConversationActivity.this);
        messageEditText.setText("");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        RiaBaseApplication.component().inject(this);
        ButterKnife.bind(this);
        messageEditText.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (messagesAdapter.getItemCount() > 1)
                    linearLayoutManager.scrollToPosition(messagesAdapter.getItemCount() - 1);
            }
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.action_bar_title_layout);


        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        Bundle bundle = new Bundle();
        String jid_to = getExtraJid(ARG_TO_JID);
        if (!TextUtils.isEmpty(jid_to)) {
            messagesAdapter = new MessagesAdapter(this, userAppPreference.getLoginStringKey());
            bundle.putString(ARG_TO_JID, jid_to);
            bundle.putString(ARG_FROM_JID, userAppPreference.getUserStringKey());
            initOrRestartLoader(MESSAGES_LOADER_ID, bundle, this);
            initOrRestartLoader(USER_STATUS_LOADER_ID, bundle, this);
        } else {
            messagesAdapter = new MessagesAdapter(this, userAppPreference.getFirstSecondName());
            bundle.putString(ARG_ROOM_JID, getExtraJid(ARG_ROOM_JID));
            initOrRestartLoader(MESSAGES_LOADER_ID, bundle, this);
        }
        recyclerView.setAdapter(messagesAdapter);
    }


    @Override
    public Loader<CursorRiaLoader.LoaderResult<Cursor>> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case MESSAGES_LOADER_ID:
                return new MessagesLoader(this, args);
            case USER_STATUS_LOADER_ID:
                return new UserOnlineStatusLoader(this, args);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<CursorRiaLoader.LoaderResult<Cursor>> loader, CursorRiaLoader.LoaderResult<Cursor> data) {

        switch (loader.getId()) {
            case MESSAGES_LOADER_ID: {
                if (data.result != null && !data.result.isClosed()) {
                    messagesAdapter.changeCursor(data.result);
                    if (messagesAdapter.getItemCount() > 0) {
                        linearLayoutManager.scrollToPosition(messagesAdapter.getItemCount() - 1);
                    }
                }
                break;
            }
            case USER_STATUS_LOADER_ID: {
                if (data.result != null && !data.result.isClosed()) {
                    RosterEntryModel rosterEntryModel = DbHelper.getModelByCursor(data.result, RosterEntryModel.class);
                    int resId = ViewUtils.getIconIdByPresence(rosterEntryModel, this);
                    ((ImageView) findViewById(R.id.user_online_status_img)).setImageResource(resId);
                }
            }
            break;
        }
    }

    @Override
    public void onLoaderReset(Loader<CursorRiaLoader.LoaderResult<Cursor>> loader) {

    }

    protected void onResume() {
        super.onResume();
        updateStatusBar();
        EventBus.getDefault().post(new RiaUpdateCurrentUserPresenceEvent(true));
    }

    void updateStatusBar() {
        String jid_to = getExtraJid(ARG_TO_JID);
        String titleToSet = "";
        if (!TextUtils.isEmpty(jid_to)) {
            RosterEntryModel rosterEntryModel = DbHelper.getRosterEntryByBareJid(jid_to);
            if (rosterEntryModel != null) {
                progressBar.setVisibility(View.GONE);
                if (rosterEntryModel.rosterGroupModel != null &&
                        rosterEntryModel.rosterGroupModel.name.equals(SmackRosterManager.FIRST_SORTED_GROUP)) {
                    titleToSet = rosterEntryModel.name;
                } else {
                    titleToSet = RiaTextUtils.capFirst(rosterEntryModel.name);
                }
                int resId = ViewUtils.getIconIdByPresence(rosterEntryModel, this);
                ((ImageView) findViewById(R.id.user_online_status_img)).setImageResource(resId);
            } else {
                progressBar.setVisibility(View.VISIBLE);
            }
        } else {
            jid_to = getExtraJid(ARG_ROOM_JID);
            ChatRoomModel chatRoomModel = DbHelper.getChatRoomByJid(jid_to);
            if (chatRoomModel != null)
                titleToSet = RiaTextUtils.capFirst(chatRoomModel.name);
            else {
                titleToSet = RiaTextUtils.capFirst(XmppUtils.roomNameFromJid(jid_to));
            }
        }
        ((TextView) findViewById(R.id.action_bar_title)).setText(titleToSet);
    }

    public void onEvent(final XmppErrorEvent xmppErrorEvent) {
        switch (xmppErrorEvent.state) {

            case EMessageNotSend:
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ConversationActivity.this, "Message sending error", Toast.LENGTH_LONG);
                    }
                });
                break;
            case EDbUpdating:
            case EDbUpdated:
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(xmppErrorEvent.state == XmppErrorEvent.State.EDbUpdated ? View.GONE : View.VISIBLE);
                        updateStatusBar();
                    }
                });
                break;
            default:
                super.onEvent(xmppErrorEvent);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (getExtraJid(ARG_ROOM_JID) != null) {
            getMenuInflater().inflate(R.menu.menu_conversation, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_edit_room:
                Intent intent = new Intent(this, AddNewRoomActivity.class);
                intent.putExtras(getIntent().getExtras());
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}