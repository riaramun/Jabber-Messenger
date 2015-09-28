package ru.rian.riamessenger.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.activeandroid.util.SQLiteUtils;
import com.tonicartos.superslim.LayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import lombok.val;
import ru.rian.riamessenger.R;
import ru.rian.riamessenger.adapters.list.ContactsAdapter;
import ru.rian.riamessenger.adapters.list.FastScroller;
import ru.rian.riamessenger.loaders.ContactsLoader;
import ru.rian.riamessenger.loaders.base.CursorRiaLoader;
import ru.rian.riamessenger.model.RosterEntryModel;
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

/**
 * Fragment that displays a list of country names.
 */
public class ContactsFragment extends BaseTabFragment {

     static final String KEY_HEADER_POSITIONING = "key_header_mode";

     static final String KEY_MARGINS_FIXED = "key_margins_fixed";

     ViewHolder mViews;

     ContactsAdapter mAdapter;

     int mHeaderDisplay;

     boolean mAreMarginsFixed;

     final Random mRng = new Random();

     Toast mToast = null;



    //  GridSLM mGridSLM;

    //  SectionLayoutManager mLinearSectionLayoutManager;

    public boolean areHeadersOverlaid() {
        return (mHeaderDisplay & LayoutManager.LayoutParams.HEADER_OVERLAY) != 0;
    }

    public boolean areHeadersSticky() {
        return (mHeaderDisplay & LayoutManager.LayoutParams.HEADER_STICKY) != 0;
    }

    public boolean areMarginsFixed() {
        return mAreMarginsFixed;
    }

    public int getHeaderMode() {
        return mHeaderDisplay;
    }

    public void setHeaderMode(int mode) {
        mHeaderDisplay = mode | (mHeaderDisplay & LayoutManager.LayoutParams.HEADER_OVERLAY) | (
                mHeaderDisplay & LayoutManager.LayoutParams.HEADER_STICKY);
        mAdapter.setHeaderDisplay(mHeaderDisplay);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            mHeaderDisplay = savedInstanceState
                    .getInt(KEY_HEADER_POSITIONING,
                            getResources().getInteger(R.integer.default_header_display));
            mAreMarginsFixed = savedInstanceState
                    .getBoolean(KEY_MARGINS_FIXED,
                            getResources().getBoolean(R.bool.default_margins_fixed));
        } else {
            mHeaderDisplay = getResources().getInteger(R.integer.default_header_display);
            mAreMarginsFixed = getResources().getBoolean(R.bool.default_margins_fixed);
        }

        mViews = new ViewHolder(view);
        mViews.initViews(new LayoutManager(getActivity()));
        mAdapter = new ContactsAdapter(ContactsAdapter.ListItemMode.EPresence, getActivity(), mHeaderDisplay, contactsListClickListener);
        mAdapter.setMarginsFixed(mAreMarginsFixed);
        mAdapter.setHeaderDisplay(mHeaderDisplay);
        mViews.setAdapter(mAdapter);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_HEADER_POSITIONING, mHeaderDisplay);
        outState.putBoolean(KEY_MARGINS_FIXED, mAreMarginsFixed);
    }

    public void scrollToRandomPosition() {
        int position = mRng.nextInt(mAdapter.getItemCount());
        String s = "Scroll to position " + position
                + (mAdapter.isItemHeader(position) ? ", header " : ", item ")
                + mAdapter.itemToString(position) + ".";
        if (mToast != null) {
            mToast.setText(s);
        } else {
            mToast = Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT);
        }
        mToast.show();
        mViews.scrollToPosition(position);
    }

    public void setHeadersOverlaid(boolean areHeadersOverlaid) {
        mHeaderDisplay = areHeadersOverlaid ? mHeaderDisplay
                | LayoutManager.LayoutParams.HEADER_OVERLAY
                : mHeaderDisplay & ~LayoutManager.LayoutParams.HEADER_OVERLAY;
        mAdapter.setHeaderDisplay(mHeaderDisplay);
    }

    public void setHeadersSticky(boolean areHeadersSticky) {
        mHeaderDisplay = areHeadersSticky ? mHeaderDisplay
                | LayoutManager.LayoutParams.HEADER_STICKY
                : mHeaderDisplay & ~LayoutManager.LayoutParams.HEADER_STICKY;
        mAdapter.setHeaderDisplay(mHeaderDisplay);
    }

    public void setMarginsFixed(boolean areMarginsFixed) {
        mAreMarginsFixed = areMarginsFixed;
        mAdapter.setMarginsFixed(areMarginsFixed);
    }

    public void smoothScrollToRandomPosition() {
        int position = mRng.nextInt(mAdapter.getItemCount());
        String s = "Smooth scroll to position " + position
                + (mAdapter.isItemHeader(position) ? ", header " : ", item ")
                + mAdapter.itemToString(position) + ".";
        if (mToast != null) {
            mToast.setText(s);
        } else {
            mToast = Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT);
        }
        mToast.show();
        mViews.smoothScrollToPosition(position);
    }

     static class ViewHolder {

         final RecyclerView mRecyclerView;

        VerticalRecyclerViewFastScroller fastScroller;

        public ViewHolder(View view) {
            mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
            FastScroller fastScroller = (FastScroller) view.findViewById(R.id.fastscroller);
            fastScroller.setRecyclerView(mRecyclerView);
        }

        public void initViews(LayoutManager lm) {
            mRecyclerView.setLayoutManager(lm);
        }

        public void scrollToPosition(int position) {
            mRecyclerView.scrollToPosition(position);
        }

        public void setAdapter(RecyclerView.Adapter<?> adapter) {
            mRecyclerView.setAdapter(adapter);


        }

        public void smoothScrollToPosition(int position) {
            mRecyclerView.smoothScrollToPosition(position);
        }
    }

    @Override
    public void onLoadFinished(Loader<CursorRiaLoader.LoaderResult<Cursor>> loader, CursorRiaLoader.LoaderResult<Cursor> data) {
        List<RosterEntryModel> usersNames = SQLiteUtils.processCursor(RosterEntryModel.class, data.result);
        val objectArrayList = getContactsList(usersNames);
        mAdapter.updateEntries(objectArrayList);
    }

     ArrayList<ContactsAdapter.LineItem> getContactsList(List<RosterEntryModel> usersNames) {
        val objectArrayList = new ArrayList<ContactsAdapter.LineItem>();
        //Insert headers into list of items.
        String lastHeader = "";
        int sectionManager = -1;
        int headerCount = 0;
        int sectionFirstPosition = 0;
        for (int i = 0; i < usersNames.size(); i++) {
            RosterEntryModel rosterEntryModel = usersNames.get(i);
            if (rosterEntryModel != null && !TextUtils.isEmpty(rosterEntryModel.name)) {
                String header = rosterEntryModel.name.substring(0, 1).toUpperCase(Locale.getDefault());
                if (!TextUtils.equals(lastHeader, header)) {
                    sectionManager = (sectionManager + 1) % 2;
                    sectionFirstPosition = i + headerCount;
                    lastHeader = header;
                    headerCount += 1;
                    objectArrayList.add(new ContactsAdapter.LineItem(header, true, sectionManager, sectionFirstPosition, -1, null));
                }
                objectArrayList.add(new ContactsAdapter.LineItem(rosterEntryModel.name, false, sectionManager, sectionFirstPosition, rosterEntryModel.presence, rosterEntryModel.getId()));
            }
        }
        return objectArrayList;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        /** Create an option menu from res/menu/items.xml */
        inflater.inflate(R.menu.menu_contacts, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_news).getActionView();
        setSearchViewListenersAndStyle(searchView);
    }

    @Override
    protected Bundle getBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt(BaseTabFragment.ARG_TAB_ID, tabId);
        bundle.putString(BaseTabFragment.ARG_TITLE_FILTER, title_to_search);
        return bundle;
    }

    @Override
    protected void rosterLoaded(boolean isLoaded) {

    }

    @Override
    public Loader<CursorRiaLoader.LoaderResult<Cursor>> onCreateLoader(int id, Bundle args) {
        return new ContactsLoader(getActivity(), args);
    }
}
