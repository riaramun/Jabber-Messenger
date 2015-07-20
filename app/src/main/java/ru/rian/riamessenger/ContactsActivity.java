package ru.rian.riamessenger;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.karim.MaterialTabs;
import ru.rian.riamessenger.common.RiaBaseActivity;
import ru.rian.riamessenger.common.TabsRiaBaseActivity;
import ru.rian.riamessenger.di.DaggerD2EComponent;
import ru.rian.riamessenger.fragments.BaseTabFragment;
import ru.rian.riamessenger.fragments.ContactsFragment;
import ru.rian.riamessenger.fragments.GroupsFragment;
import ru.rian.riamessenger.fragments.RobotsFragment;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.utils.ScreenUtils;


public class ContactsActivity extends TabsRiaBaseActivity {


    int[] fragmentsIds = {BaseTabFragment.ROBOTS_FRAGMENT, BaseTabFragment.GROUPS_FRAGMENT, BaseTabFragment.CONTACTS_FRAGMENT};
    String[] fragmentsTags = {BaseTabFragment.ROBOTS_FRAGMENT_TAG, BaseTabFragment.GROUPS_FRAGMENT_TAG, BaseTabFragment.CONTACTS_FRAGMENT_TAG};

    @Inject
    UserAppPreference userAppPreference;

    @Bind(R.id.material_tabs)
    MaterialTabs contactsMaterialTabs;

    @Bind(R.id.view_pager)
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RiaBaseApplication.component().inject(this);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_contacts);
        ButterKnife.bind(this);

        final int numberOfTabs = fragmentsIds.length;
        SamplePagerAdapter adapter = new SamplePagerAdapter(getSupportFragmentManager(), numberOfTabs);
        viewPager.setAdapter(adapter);
        contactsMaterialTabs.setViewPager(viewPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }
    private void logout(boolean clean) {
        if (clean) {
            userAppPreference.setLoginStringKey("");
            userAppPreference.setPassStringKey("");
        }
        /*
        Intent intent = new Intent(this, EnterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);*/
        finish();
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

        private final String[] TITLES = {getString(R.string.robots), getString(R.string.groups), getString(R.string.contacts)};

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
        public Fragment getItem(int tabId) {
            String tag = getTagByTabIndex(tabId);
            Fragment fragment = null;
            if (tag != null) {
                fragment = getSupportFragmentManager().findFragmentByTag(tag);
                if (fragment == null /*|| !fragment.isVisible()*/) {
                    fragment = BaseTabFragment.newInstance(getIdByTabIndex(tabId));
                }
            }
            return fragment;
        }
    }

    @Override
    public int getIdByTabIndex(int tabIndex) {
        return fragmentsIds[tabIndex];
    }

    @Override
    public String getTagByTabIndex(int tabIndex) {
        return fragmentsTags[tabIndex];
    }


    @Override
    protected void authenticated(boolean isAuthenticated) {
        //nothing to do, since in this case the method launchNextActivity starts ContactsActivity
    }
    /*@Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
        } else {
            super.onBackPressed();
        }
    }*/
}
