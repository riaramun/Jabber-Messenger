package ru.rian.riamessenger;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import lombok.val;
import ru.rian.riamessenger.adapters.list.MessagesAdapter;
import ru.rian.riamessenger.common.RiaBaseActivity;
import ru.rian.riamessenger.loaders.MessagesLoader;
import ru.rian.riamessenger.loaders.base.CursorRiaLoader;
import ru.rian.riamessenger.model.RosterEntryModel;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.riaevents.request.RiaMessageEvent;
import ru.rian.riamessenger.utils.DbHelper;
import ru.rian.riamessenger.utils.RiaTextUtils;

/**
 * Created by Roman on 7/21/2015.
 */
public class ConversationActivity extends RiaBaseActivity implements LoaderManager.LoaderCallbacks<CursorRiaLoader.LoaderResult<Cursor>> {

    @Inject
    UserAppPreference userAppPreference;

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;

    @Bind(R.id.message_edit_text)
    EditText messageEditText;

    @OnClick(R.id.send_icon_text_view)
    public void onClick() {
        EventBus.getDefault().post(new RiaMessageEvent(getJid(), messageEditText.getText().toString()));
    }

    public static final String ARG_ENTRY_MODEL_ID = "entry_model_id";
    MessagesAdapter messagesAdapter;
    LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        RiaBaseApplication.component().inject(this);
        ButterKnife.bind(this);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.action_bar_title_layout);

        String jid = getJid();
        RosterEntryModel rosterEntryModel = DbHelper.getRosterEntryByBareJid(jid);

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

        val itemAnimator = new DefaultItemAnimator();
        recyclerView.setItemAnimator(itemAnimator);

        //messagesAdapter = new MessagesAdapter(this, null);
        recyclerView.setAdapter(messagesAdapter);

        Bundle bundle = new Bundle();
        bundle.putString(ARG_ENTRY_MODEL_ID, jid);
        getSupportLoaderManager().initLoader(0, bundle, this);
    }

    String getJid() {
        return getIntent().getStringExtra(ARG_ENTRY_MODEL_ID);
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
        return new MessagesLoader(this, args);
    }

    @Override
    public void onLoadFinished(Loader<CursorRiaLoader.LoaderResult<Cursor>> loader, CursorRiaLoader.LoaderResult<Cursor> data) {
        if (messagesAdapter == null || messagesAdapter.getCursor() == null) {
            messagesAdapter = new MessagesAdapter(this, data.result, userAppPreference.getLoginStringKey());
            recyclerView.setAdapter(messagesAdapter);
        } else {
            messagesAdapter.changeCursor(data.result);
        }
    }

    @Override
    public void onLoaderReset(Loader<CursorRiaLoader.LoaderResult<Cursor>> loader) {

    }
}