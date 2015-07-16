package ru.rian.riamessenger;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.karim.MaterialTabs;
import ru.rian.riamessenger.common.RiaBaseActivity;
import ru.rian.riamessenger.fragments.BaseTabFragment;
import ru.rian.riamessenger.fragments.ContactsFragment;
import ru.rian.riamessenger.fragments.GroupsFragment;
import ru.rian.riamessenger.fragments.RobotsFragment;


public class ContactsActivity extends RiaBaseActivity {

    static public final int ROBOTS_FRAGMENT = 0;
    static public final int GROUPS_FRAGMENT = 1;
    static public final int CONTACTS_FRAGMENT = 2;
    public static final String ARG_TAB_ID = "tabId";
    public static final String ARG_IS_UPDATING = "isUpdating";


    static public final String ROBOTS_FRAGMENT_TAG = RobotsFragment.class.getSimpleName();
    static public final String GROUPS_FRAGMENT_TAG = GroupsFragment.class.getSimpleName();
    static public final String CONTACTS_FRAGMENT_TAG = ContactsFragment.class.getSimpleName();


    @Bind(R.id.material_tabs)
    MaterialTabs contactsMaterialTabs;

    @Bind(R.id.view_pager)
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_contacts);
        ButterKnife.bind(this);

        final int numberOfTabs = 3;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
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
            String tag = getTagByTabId(tabId);
            Fragment fragment = null;
            if (tag != null) {
                fragment = getSupportFragmentManager().findFragmentByTag(tag);
                if (fragment == null /*|| !fragment.isVisible()*/) {
                    fragment = BaseTabFragment.newInstance(tabId);
                }
            }
            return fragment;
        }
    }

    static String getTagByTabId(int tabId) {
        String tag = null;
        switch (tabId) {
            case ContactsActivity.CONTACTS_FRAGMENT:
                tag = CONTACTS_FRAGMENT_TAG;
                break;
            case ContactsActivity.ROBOTS_FRAGMENT:
                tag = ROBOTS_FRAGMENT_TAG;
                break;
            case ContactsActivity.GROUPS_FRAGMENT:
                tag = GROUPS_FRAGMENT_TAG;
                break;
        }
        return tag;
    }

    @Override
    protected void authenticated(boolean isAuthenticated) {
        //nothing to do, since in this case the method launchNextActivity starts ContactsActivity
    }
}
