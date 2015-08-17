package ru.rian.riamessenger;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;

import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;

import org.jivesoftware.smack.packet.Presence;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
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
import ru.rian.riamessenger.riaevents.response.XmppErrorEvent;
import ru.rian.riamessenger.riaevents.ui.ChatEvents;
import ru.rian.riamessenger.services.RiaXmppService;
import ru.rian.riamessenger.utils.DbHelper;
import ru.rian.riamessenger.utils.NetworkStateManager;
import ru.rian.riamessenger.utils.SysUtils;
import ru.rian.riamessenger.utils.ViewUtils;


public class ChatsActivity extends TabsRiaBaseActivity implements LoaderManager.LoaderCallbacks<CursorRiaLoader.LoaderResult<Cursor>> {


    BaseTabFragment.FragIds[] fragmentsIds = {BaseTabFragment.FragIds.CHATS_FRAGMENT, BaseTabFragment.FragIds.ROOMS_FRAGMENT};
    String[] fragmentsTags = {BaseTabFragment.CHATS_FRAGMENT_TAG, BaseTabFragment.ROOMS_FRAGMENT_TAG};

    @Inject
    UserAppPreference userAppPreference;

    @Bind(R.id.material_tabs)
    MaterialTabs contactsMaterialTabs;

    @Bind(R.id.view_pager)
    ViewPager viewPager;

    @Bind(R.id.progress_bar)
    ProgressBarCircularIndeterminate progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RiaBaseApplication.component().inject(this);
        setContentView(R.layout.activity_chats);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        final int numberOfTabs = fragmentsIds.length;
        SamplePagerAdapter adapter = new SamplePagerAdapter(getSupportFragmentManager(), numberOfTabs);
        viewPager.setAdapter(adapter);
        contactsMaterialTabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                removeFragment(ChatRemoveDialogFragment.TAG);
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
                addFragment(chatRemoveDialogFragment, ChatRemoveDialogFragment.TAG, R.id.container, true);
                break;
        }
    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contacts, menu);

        return true;
    }*/

    private void logout(boolean clean) {

        if (clean) {
            userAppPreference.setLoginStringKey("");
            userAppPreference.setPassStringKey("");
            DbHelper.clearDb();
            RiaEventBus.post(RiaServiceEvent.RiaEvent.TO_SIGN_OUT);
        }
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
            NetworkStateManager.setCurrentUserPresence(new Presence(Presence.Type.unavailable), userAppPreference.getJidStringKey());
        }
        Bundle bundle = new Bundle();
        bundle.putString(ARG_TO_JID, userAppPreference.getJidStringKey());
        initOrRestartLoader(USER_STATUS_LOADER_ID, bundle, this);

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
            logout(true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class SamplePagerAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = {getString(R.string.chats), getString(R.string.rooms)};

        private final ArrayList<String> mTitles;

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


    void addFragment(Fragment aFragment, String tag, int aContainerId, boolean addToBackStack) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment == null || !fragment.isVisible()) {
            if (addToBackStack) {
                fragmentTransaction = fragmentTransaction.addToBackStack(tag);
            }
            fragmentTransaction.replace(aContainerId, aFragment, tag).commit();
        }
    }

    void removeFragment(String tag) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            fragmentTransaction.remove(fragment).commit();
        }
    }

    @Override
    public int getIdByTabIndex(int tabIndex) {
        return fragmentsIds[tabIndex].ordinal();
    }

    @Override
    public String getTagByTabIndex(int tabIndex) {
        return fragmentsTags[tabIndex];
    }

    public void onEvent(final XmppErrorEvent xmppErrorEvent) {
        switch (xmppErrorEvent.state) {
            case EAuthenticated:
                NetworkStateManager.setCurrentUserPresence(new Presence(Presence.Type.available), userAppPreference.getJidStringKey());
                break;
            case EAuthenticationFailed:
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
                    int resId = ViewUtils.getIconIdByPresence(rosterEntryModel);
                    getSupportActionBar().setHomeAsUpIndicator(resId);
                }
            }
            break;
        }
    }

    @Override
    public void onLoaderReset(Loader<CursorRiaLoader.LoaderResult<Cursor>> loader) {

    }
}
