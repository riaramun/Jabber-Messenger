package ru.rian.riamessenger;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
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
import lombok.val;
import ru.rian.riamessenger.adapters.cursor.MessagesAdapter;
import ru.rian.riamessenger.common.RiaBaseActivity;
import ru.rian.riamessenger.loaders.ChatsOnlineStatesLoader;
import ru.rian.riamessenger.loaders.MessagesLoader;
import ru.rian.riamessenger.loaders.UserOnlineStatusLoader;
import ru.rian.riamessenger.loaders.base.CursorRiaLoader;
import ru.rian.riamessenger.model.RosterEntryModel;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.riaevents.request.RiaMessageEvent;
import ru.rian.riamessenger.utils.DbHelper;
import ru.rian.riamessenger.utils.RiaTextUtils;
import ru.rian.riamessenger.utils.ScreenUtils;

/**
 * Created by Roman on 7/21/2015.
 */
public class ConversationActivity extends RiaBaseActivity implements LoaderManager.LoaderCallbacks<CursorRiaLoader.LoaderResult<Cursor>> {

    final int MESSAGES_LOADER_ID = 1;
    final int USER_STATUS_LOADER_ID = 2;

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
        EventBus.getDefault().post(new RiaMessageEvent(getToJid(), messageEditText.getText().toString()));
        ScreenUtils.hideKeyboard(ConversationActivity.this);
        messageEditText.setText("");
    }

    public static final String ARG_FROM_JID = "from_jid";
    public static final String ARG_TO_JID = "to_jid";
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

        String jid_to = getToJid();

        RosterEntryModel rosterEntryModel = DbHelper.getRosterEntryByBareJid(jid_to);
        if (rosterEntryModel != null) {
            String titleToSet;
            if (rosterEntryModel.rosterGroupModel.name.equals(getString(R.string.robots))) {
                titleToSet = rosterEntryModel.name;
            } else {
                titleToSet = RiaTextUtils.capFirst(rosterEntryModel.name);
            }
            ((TextView) findViewById(R.id.action_bar_title)).setText(titleToSet);
            int resId = getIconIdByPresence(rosterEntryModel.presence);
            ((ImageView) findViewById(R.id.user_online_status)).setImageResource(resId);
        }
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        messagesAdapter = new MessagesAdapter(this, null, userAppPreference.getLoginStringKey());
        recyclerView.setAdapter(messagesAdapter);

        Bundle bundle = new Bundle();
        bundle.putString(ARG_TO_JID, jid_to);
        bundle.putString(ARG_FROM_JID, userAppPreference.getJidStringKey());

        initOrRestartLoader(MESSAGES_LOADER_ID, bundle, this);
        initOrRestartLoader(USER_STATUS_LOADER_ID, bundle, this);
    }

    String getToJid() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String arg_to_jid = bundle.getString(ARG_TO_JID);
        return arg_to_jid;
    }


    public int getIconIdByPresence(int presence) {
        int resId = -1;
        RosterEntryModel.UserStatus mode = RosterEntryModel.UserStatus.values()[presence];
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
        return resId;
    }

    @Override
    protected void authenticated(boolean isAuthenticated) {

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
                    int resId = getIconIdByPresence(rosterEntryModel.presence);
                    ((ImageView) findViewById(R.id.user_online_status)).setImageResource(resId);
                }
            }
            break;
        }
    }

    @Override
    public void onLoaderReset(Loader<CursorRiaLoader.LoaderResult<Cursor>> loader) {

    }
}