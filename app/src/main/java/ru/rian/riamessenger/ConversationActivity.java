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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
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
import ru.rian.riamessenger.utils.RiaTextUtils;
import ru.rian.riamessenger.utils.ScreenUtils;
import ru.rian.riamessenger.utils.ViewUtils;

/**
 * Created by Roman on 7/21/2015.
 */
public class ConversationActivity extends RiaBaseActivity implements LoaderManager.LoaderCallbacks<CursorRiaLoader.LoaderResult<Cursor>> {

    @Bind(R.id.progress_bar)
    ProgressBarCircularIndeterminate progressBar;

    @Inject
    UserAppPreference userAppPreference;

    @Bind(R.id.container)
    RelativeLayout relativeLayoutContainer;

    @Bind(R.id.send_icon_text_view)
    IconTextView sendIconTextView;

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;

    @Bind(R.id.message_edit_text)
    EditText messageEditText;

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

    MessagesAdapter messagesAdapter;
    LinearLayoutManager linearLayoutManager;

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

        messagesAdapter = new MessagesAdapter(this, null, userAppPreference.getLoginStringKey());
        recyclerView.setAdapter(messagesAdapter);

        Bundle bundle = new Bundle();
        String jid_to = getExtraJid(ARG_TO_JID);
        if (!TextUtils.isEmpty(jid_to)) {
            bundle.putString(ARG_TO_JID, jid_to);
            bundle.putString(ARG_FROM_JID, userAppPreference.getUserStringKey());
            initOrRestartLoader(MESSAGES_LOADER_ID, bundle, this);
            initOrRestartLoader(USER_STATUS_LOADER_ID, bundle, this);
        } else {
            bundle.putString(ARG_ROOM_JID, getExtraJid(ARG_ROOM_JID));
            initOrRestartLoader(MESSAGES_LOADER_ID, bundle, this);
        }


    }

    String getExtraJid(String arg_jid) {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String arg_to_jid = bundle.getString(arg_jid);
        return arg_to_jid;
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
                messagesAdapter.changeCursor(data.result);
                if (messagesAdapter.getItemCount() > 0) {
                    linearLayoutManager.scrollToPosition(messagesAdapter.getItemCount() - 1);
                }
                break;
            }
            case USER_STATUS_LOADER_ID: {
                if (data.result != null) {
                    RosterEntryModel rosterEntryModel = DbHelper.getModelByCursor(data.result, RosterEntryModel.class);
                    int resId = ViewUtils.getIconIdByPresence(rosterEntryModel);
                    ((ImageView) findViewById(R.id.user_online_status)).setImageResource(resId);
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
                        rosterEntryModel.rosterGroupModel.name.equals(getString(R.string.robots))) {
                    titleToSet = rosterEntryModel.name;
                } else {
                    titleToSet = RiaTextUtils.capFirst(rosterEntryModel.name);
                }
                int resId = ViewUtils.getIconIdByPresence(rosterEntryModel);
                ((ImageView) findViewById(R.id.user_online_status)).setImageResource(resId);
            } else {
                progressBar.setVisibility(View.VISIBLE);
            }
        } else {
            jid_to = getExtraJid(ARG_ROOM_JID);
            ChatRoomModel chatRoomModel = DbHelper.getChatRoomByJid(jid_to);
            if(chatRoomModel != null)
            titleToSet = RiaTextUtils.capFirst(chatRoomModel.name);
        }
        ((TextView) findViewById(R.id.action_bar_title)).setText(titleToSet);
    }

    public void onEvent(final XmppErrorEvent xmppErrorEvent) {
        switch (xmppErrorEvent.state) {
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
}