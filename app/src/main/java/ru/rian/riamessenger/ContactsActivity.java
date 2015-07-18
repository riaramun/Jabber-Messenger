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
import ru.rian.riamessenger.di.DaggerD2EComponent;
import ru.rian.riamessenger.fragments.BaseTabFragment;
import ru.rian.riamessenger.fragments.ContactsFragment;
import ru.rian.riamessenger.fragments.GroupsFragment;
import ru.rian.riamessenger.fragments.RobotsFragment;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.utils.ScreenUtils;


public class ContactsActivity extends RiaBaseActivity {

    static public final int ROBOTS_FRAGMENT = 0;
    static public final int GROUPS_FRAGMENT = 1;
    static public final int CONTACTS_FRAGMENT = 2;
    public static final String ARG_TAB_ID = "tabId";
    public static final String ARG_IS_UPDATING = "isUpdating";


    static public final String ROBOTS_FRAGMENT_TAG = RobotsFragment.class.getSimpleName();
    static public final String GROUPS_FRAGMENT_TAG = GroupsFragment.class.getSimpleName();
    static public final String CONTACTS_FRAGMENT_TAG = ContactsFragment.class.getSimpleName();

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

        final int numberOfTabs = 3;
        SamplePagerAdapter adapter = new SamplePagerAdapter(getSupportFragmentManager(), numberOfTabs);
        viewPager.setAdapter(adapter);
        contactsMaterialTabs.setViewPager(viewPager);
    }

    SearchView  searchView;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.search_news);
        // mSearchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);

        // mSearchView = new SearchView(getActivity());
        /*actionBar.setCustomView(mSearchView, new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER_VERTICAL | Gravity.LEFT));*/
        searchView = (SearchView) menu.findItem(R.id.search_news).getActionView();
        //mSearchView.setBackgroundColor(Color.BLACK);
        searchView.setQuery("", true);
        searchView.setQueryHint(getString(R.string.search_hint));
        int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate = searchView.findViewById(searchPlateId);
        if (searchPlate != null) {
            int searchTextId = searchPlate.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
            TextView searchText = (TextView) searchPlate.findViewById(searchTextId);
            if (searchText != null) {
                searchText.setTextColor(Color.WHITE);
            }
            searchPlate.setBackgroundResource(R.drawable.search_view_bg);
        }

///        int crossId = mSearchView.getContext().getResources().getIdentifier("android:id/search_close_btn", null, null);            // Getting the 'search_plate' LinearLayout.
        //     ImageView image = (ImageView) mSearchView.findViewById(crossId);
        //   image.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);

        /*int searchButtonId = mSearchView.getContext().getResources().getIdentifier("android:id/search_mag_icon", null, null);
        ImageView searchButton = (ImageView) mSearchView.findViewById(searchButtonId);
        searchButton.setImageResource(R.drawable.abc_textfield_search_default_mtrl_alpha);
        */
        //int searchImgId = getResources().getIdentifier("android:id/search_mag_icon", null, null);
        //ImageView v = (ImageView) mSearchView.findViewById(searchImgId);
        //v.setImageResource(R.drawable.ic_search);



        //ImageView searchIcon = (ImageView)mSearchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
        //searchIcon.setImageResource(R.drawable.abc_ic_search_api_mtrl_alpha);

        searchView.setIconified(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                ScreenUtils.hideKeyboard(ContactsActivity.this);
                //Toast.makeText(getActivity(), "Search : " + query, Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Toast.makeText(getActivity(), "Search : " + newText, Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        /*searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                ScreenUtils.hideKeyboard(ContactsActivity.this);
                return true;
            }
        });*/
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
    /*@Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
        } else {
            super.onBackPressed();
        }
    }*/
}
