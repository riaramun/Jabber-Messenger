package ru.rian.riamessenger;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.jivesoftware.smack.packet.Presence;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import io.karim.MaterialTabs;
import ru.rian.riamessenger.common.RiaEventBus;
import ru.rian.riamessenger.common.TabsRiaBaseActivity;
import ru.rian.riamessenger.fragments.BaseTabFragment;
import ru.rian.riamessenger.fragments.ChatRemoveDialogFragment;
import ru.rian.riamessenger.loaders.UserOnlineStatusLoader;
import ru.rian.riamessenger.loaders.base.CursorRiaLoader;
import ru.rian.riamessenger.model.RosterEntryModel;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.riaevents.request.RiaServiceEvent;
import ru.rian.riamessenger.riaevents.request.RiaUpdateCurrentUserPresenceEvent;
import ru.rian.riamessenger.riaevents.response.XmppErrorEvent;
import ru.rian.riamessenger.riaevents.ui.ChatEvents;
import ru.rian.riamessenger.utils.DbHelper;
import ru.rian.riamessenger.utils.NetworkStateManager;
import ru.rian.riamessenger.utils.ViewUtils;


public class ChatsActivity extends TabsRiaBaseActivity implements LoaderManager.LoaderCallbacks<CursorRiaLoader.LoaderResult<Cursor>> {


    final BaseTabFragment.FragIds[] fragmentsIds = {BaseTabFragment.FragIds.CHATS_FRAGMENT, BaseTabFragment.FragIds.ROOMS_FRAGMENT};
    final String[] fragmentsTags = {BaseTabFragment.CHATS_FRAGMENT_TAG, BaseTabFragment.ROOMS_FRAGMENT_TAG};

    @Inject
    public
    UserAppPreference userAppPreference;

    @Bind(R.id.material_tabs)
    MaterialTabs contactsMaterialTabs;

    @Bind(R.id.view_pager)
    ViewPager viewPager;

    @Bind(R.id.progress_bar)

    ProgressBar progressBar;

    @Bind(R.id.buttonFloat)
    FloatingActionButton buttonFloat;

    @OnClick(R.id.buttonFloat)
    void onClick() {
        Class activityClass;
        if (viewPager.getCurrentItem() == 0) {
            activityClass = ContactsActivity.class;
        } else {
            activityClass = AddNewRoomActivity.class;
        }
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RiaBaseApplication.component().inject(this);
        setContentView(R.layout.activity_chats);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        int resId = R.drawable.action_bar_status_offline;
        getSupportActionBar().setHomeAsUpIndicator(resId);

        final int numberOfTabs = fragmentsIds.length;
        SamplePagerAdapter adapter = new SamplePagerAdapter(getSupportFragmentManager(), numberOfTabs);
        viewPager.setAdapter(adapter);
        contactsMaterialTabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                removeFragment();
                switch (position) {
                    case 0:
                        buttonFloat.setImageDrawable(ContextCompat.getDrawable(ChatsActivity.this, R.drawable.ic_add_white));
                        break;
                    case 1:
                        buttonFloat.setImageDrawable(ContextCompat.getDrawable(ChatsActivity.this, R.drawable.ic_group_add_white));
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        contactsMaterialTabs.setViewPager(viewPager);
    }


    public void onEvent(ChatEvents chatEvents) {
        switch (chatEvents.getChatEventId()) {
            case ChatEvents.SHOW_REMOVE_DIALOG:
                ChatRemoveDialogFragment chatRemoveDialogFragment = new ChatRemoveDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putString(ChatRemoveDialogFragment.ARG_REMOVE_CHAT, chatEvents.getChatThreadId());
                chatRemoveDialogFragment.setArguments(bundle);
               // addFragment(chatRemoveDialogFragment, ChatRemoveDialogFragment.TAG, R.id.container);
                chatRemoveDialogFragment.show(getSupportFragmentManager(),ChatRemoveDialogFragment.TAG);
                break;
        }
    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contacts, menu);

        return true;
    }*/

    void logout() {
        userAppPreference.setLoginStringKey("");
        userAppPreference.setPassStringKey("");
        DbHelper.clearDb();
        RiaEventBus.post(RiaServiceEvent.RiaEvent.TO_SIGN_OUT);
        Intent intent = new Intent(this, StartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!NetworkStateManager.isNetworkAvailable(this)) {
            NetworkStateManager.setCurrentUserPresence(new Presence(Presence.Type.unavailable), userAppPreference.getUserStringKey());
        } else {
            EventBus.getDefault().post(new RiaUpdateCurrentUserPresenceEvent(true));
        }
        if (!TextUtils.isEmpty(userAppPreference.getUserStringKey())) {
            //it is possible that our roster is empty
            //in this case we init our loader when we get it
            Bundle bundle = new Bundle();
            bundle.putString(ARG_TO_JID, userAppPreference.getUserStringKey());
            initOrRestartLoader(USER_STATUS_LOADER_ID, bundle, this);
        }
        progressBar.setVisibility(!userAppPreference.getConnectingStateKey() ? View.GONE : View.VISIBLE);

    }

    @Override
    protected void onStart() {
        // RiaEventBus.post(RiaServiceEvent.RiaEvent.TO_GET_ROSTER);
        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            logout();
            return true;
        }
        if (id == android.R.id.home) {
            Toast.makeText(this, getText(R.string.lastRoomMessageFromYou) + " " + userAppPreference.getFirstSecondName(), Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void addFragment(Fragment aFragment, String tag, int aContainerId) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(ChatRemoveDialogFragment.TAG);
        if (fragment == null || !fragment.isVisible()) {
            fragmentTransaction = fragmentTransaction.addToBackStack(ChatRemoveDialogFragment.TAG);
            fragmentTransaction.replace(R.id.container, aFragment, ChatRemoveDialogFragment.TAG).commit();
        }
    }

    void removeFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(ChatRemoveDialogFragment.TAG);
        if (fragment != null) {
            fragmentTransaction.remove(fragment).commit();
        }
    }

    @Override
    protected int getIdByTabIndex(int tabIndex) {
        return fragmentsIds[tabIndex].ordinal();
    }

    @Override
    protected String getTagByTabIndex(int tabIndex) {
        return fragmentsTags[tabIndex];
    }

    public void onEvent(final XmppErrorEvent xmppErrorEvent) {
        int resId;
        switch (xmppErrorEvent.state) {
            case EAuthenticated:
                resId = R.drawable.action_bar_status_online;
                getSupportActionBar().setHomeAsUpIndicator(resId);
                break;
            case EDbUpdated:
                Bundle bundle = new Bundle();
                bundle.putString(ARG_TO_JID, userAppPreference.getUserStringKey());
                initOrRestartLoader(USER_STATUS_LOADER_ID, bundle, this);
                break;
            case EAuthenticationFailed:
                resId = R.drawable.action_bar_status_offline;
                getSupportActionBar().setHomeAsUpIndicator(resId);
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                break;
            case ENotConnecting:
            case EConnecting:
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(xmppErrorEvent.state == XmppErrorEvent.State.ENotConnecting ? View.GONE : View.VISIBLE);
                    }
                });
                break;
            default:
                super.onEvent(xmppErrorEvent);
        }
    }

    @Override
    public Loader<CursorRiaLoader.LoaderResult<Cursor>> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case USER_STATUS_LOADER_ID:
                return new UserOnlineStatusLoader(this, args);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<CursorRiaLoader.LoaderResult<Cursor>> loader, CursorRiaLoader.LoaderResult<Cursor> data) {

        switch (loader.getId()) {
            case USER_STATUS_LOADER_ID: {
                if (data.result != null && data.result.moveToFirst()) {
                    RosterEntryModel rosterEntryModel = DbHelper.getModelByCursor(data.result, RosterEntryModel.class);
                    int resId = ViewUtils.getIconIdByPresence(rosterEntryModel, this);
                    getSupportActionBar().setHomeAsUpIndicator(resId);
                }
            }
            break;
        }
    }

    @Override
    public void onLoaderReset(Loader<CursorRiaLoader.LoaderResult<Cursor>> loader) {

    }

    public class SamplePagerAdapter extends FragmentPagerAdapter {

        final String[] TITLES = {getString(R.string.chats), getString(R.string.rooms)};

        final ArrayList<String> mTitles;

        public SamplePagerAdapter(FragmentManager fm, int numberOfTabs) {
            super(fm);
            mTitles = new ArrayList<>();
            for (int i = 0; i < numberOfTabs; i++) {
                mTitles.add(TITLES[i]);
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles.get(position);
        }

        @Override
        public int getCount() {
            return mTitles.size();
        }

        @Override
        public Fragment getItem(int tabIndex) {
            String tag = getTagByTabIndex(tabIndex);
            Fragment fragment = null;
            if (tag != null) {
                fragment = getSupportFragmentManager().findFragmentByTag(tag);
                if (fragment == null /*|| !fragment.isVisible()*/) {
                    fragment = BaseTabFragment.newInstance(getIdByTabIndex(tabIndex));
                }
            }
            return fragment;
        }
    }
}
