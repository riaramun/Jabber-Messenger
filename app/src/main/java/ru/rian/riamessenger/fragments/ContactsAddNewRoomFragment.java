package ru.rian.riamessenger.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.util.SQLiteUtils;
import com.malinskiy.materialicons.widget.IconTextView;
import com.tonicartos.superslim.LayoutManager;

import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import de.greenrobot.event.EventBus;
import ru.rian.riamessenger.ConversationActivity;
import ru.rian.riamessenger.R;
import ru.rian.riamessenger.adapters.list.ContactsAdapter;
import ru.rian.riamessenger.adapters.list.FastScroller;
import ru.rian.riamessenger.adapters.list.RosterEntryIdGetter;
import ru.rian.riamessenger.common.RiaBaseActivity;
import ru.rian.riamessenger.common.RiaConstants;
import ru.rian.riamessenger.listeners.ContactsListClickListener;
import ru.rian.riamessenger.loaders.ContactsLoader;
import ru.rian.riamessenger.loaders.base.CursorRiaLoader;
import ru.rian.riamessenger.model.RosterEntryModel;
import ru.rian.riamessenger.riaevents.request.RoomCreateEvent;
import ru.rian.riamessenger.services.RiaXmppService;
import ru.rian.riamessenger.utils.DbHelper;
import ru.rian.riamessenger.utils.ScreenUtils;
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

/**
 * Fragment that displays a list of country names.
 */
public class ContactsAddNewRoomFragment extends BaseTabFragment {

    ContactsListClickListener contactsListClickListener = new ContactsListClickListener() {
        public int onClick(RosterEntryIdGetter rosterEntryIdGetter, View v) {
            //  CheckBox checkBox = (CheckBox) v.findViewById(R.id.contact_selected);
            //checkBox.setChecked(!checkBox.isChecked());
            RecyclerView recyclerView = (RecyclerView) v.getParent();
            int childPosition = recyclerView.getChildAdapterPosition(v);
            //RosterEntryIdGetter rosterEntryIdGetter = (RosterEntryIdGetter) recyclerView.getAdapter();
            if (childPosition >= 0) {
                String jid = rosterEntryIdGetter.getJid(childPosition);
                if (jid != null) {

                } else {
                    Log.i(RiaXmppService.TAG, "onClick jid = null");
                }
            }
            String textToSet = "";
            for (String value : mAdapter.getSelectedUsersJidMap().values()) {
                textToSet += DbHelper.getRosterEntryByBareJid(value).name;
                textToSet += ";\t";
            }
            participantsTextView.setText(textToSet);
            return childPosition;
        }
    };

    private static final String KEY_HEADER_POSITIONING = "key_header_mode";

    private static final String KEY_MARGINS_FIXED = "key_margins_fixed";

    private ViewHolder mViews;

    private ContactsAdapter mAdapter;

    private int mHeaderDisplay;

    private boolean mAreMarginsFixed;

    private Random mRng = new Random();

    private Toast mToast = null;

    // private GridSLM mGridSLM;

    // private SectionLayoutManager mLinearSectionLayoutManager;

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
        View view = inflater.inflate(R.layout.fragment_contacts_add_new_room, container, false);
        participantsTextView = (TextView) view.findViewById(R.id.participantsTextView);
        return view;
    }

    TextView participantsTextView;

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
        mAdapter = new ContactsAdapter(ContactsAdapter.ListItemMode.ECheckNox, getActivity(), mHeaderDisplay, contactsListClickListener);
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

    private static class ViewHolder {

        private final RecyclerView mRecyclerView;

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
        ArrayList objectArrayList = getContactsList(usersNames);
        mAdapter.updateEntries(objectArrayList);
    }

    ArrayList<ContactsAdapter.LineItem> getContactsList(List<RosterEntryModel> usersNames) {
        ArrayList objectArrayList = new ArrayList<ContactsAdapter.LineItem>();
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
        inflater.inflate(R.menu.menu_add_new_line, menu);
        RelativeLayout relativeLayout = (RelativeLayout) menu.findItem(R.id.add_new_line_edit).getActionView();
        final EditText roomEditText = (EditText) relativeLayout.findViewById(R.id.edit_query);
        roomEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);

        final IconTextView addButton = (IconTextView) relativeLayout.findViewById(R.id.add_news_line_icon_text_view);
        roomEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    addNewRoom(roomEditText);

                }
                return true;
            }
        });
        roomEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addButton.setVisibility(roomEditText.getText().length() > 3 ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewRoom(roomEditText);
            }
        });
    }

    void addNewRoom(EditText editText) {
        if (editText.getText().length() > 3) {
            ScreenUtils.hideKeyboard(getContext());
            ArrayList<String> participantsArrayList = new ArrayList<>();
            for (String value : mAdapter.getSelectedUsersJidMap().values()) {
                participantsArrayList.add(value);
            }
            String roomName = editText.getText().toString();
            EventBus.getDefault().post(new RoomCreateEvent(roomName, participantsArrayList));
            try {
                EntityBareJid bareJid = JidCreate.entityBareFrom(roomName + "@" + RiaConstants.ROOM_DOMAIN);
                Intent intent = new Intent(getActivity(), ConversationActivity.class);
                intent.putExtra(RiaBaseActivity.ARG_ROOM_JID, bareJid.toString());
                getActivity().startActivity(intent);
            } catch (XmppStringprepException e) {
                e.printStackTrace();
            }
            getActivity().onBackPressed();
        }
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
