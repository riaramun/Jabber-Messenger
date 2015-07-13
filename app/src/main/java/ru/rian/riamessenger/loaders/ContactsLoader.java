package ru.rian.riamessenger.loaders;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.activeandroid.query.Select;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smack.roster.RosterLoadedListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import lombok.val;
import ru.rian.riamessenger.ContactsActivity;
import ru.rian.riamessenger.R;
import ru.rian.riamessenger.adapters.ContactsAdapter;
import ru.rian.riamessenger.common.RiaConstants;
import ru.rian.riamessenger.model.RosterEntryModel;
import ru.rian.riamessenger.model.RosterGroupModel;

/**
 * Created by Roman Lebedenko.
 * fisher3421@gmail.com
 */

public class ContactsLoader extends BaseRiaLoader {

    int tabIdloader = -1;
    Boolean isUpdating = false;

    public ContactsLoader(Context ctx, Bundle args) {
        super(ctx);
        tabIdloader = args.getInt(ContactsActivity.ARG_TAB_ID);
        isUpdating = args.getBoolean(ContactsActivity.ARG_IS_UPDATING);

    }

    /*public static List<RosterEntryModel> getAll(Category category) {
        return new Select()
                .from(RosterEntryModel.class)
                .where("Category = ?", category.getId())
                .orderBy("Name ASC")
                .execute().;
    }*/

    @Override
    public List<?> loadInBackground() {
        if (!isUpdating) {
            return retrieveDataFromRoster();
        } else return null;
        // This method is called on a background thread and should generate a
        // new set of data to be delivered back to the client.
    }

    List<?> retrieveDataFromRoster() {
        List<?> loaderList = null;


        switch (tabIdloader) {
            case ContactsActivity.CONTACTS_FRAGMENT: {
                List<RosterEntryModel> queryResults = new Select().from(RosterEntryModel.class)
                        .orderBy("name ASC").execute();

                loaderList = getContactsList(queryResults);
            }
            break;
            case ContactsActivity.ROBOTS_FRAGMENT: {
                //  List<RosterEntry> rosterEntries =
                RosterGroupModel rosterGroupModel = new Select().from(RosterGroupModel.class)//.where("name = ?", getContext().getString(R.string.robots))
                        .orderBy("name ASC")
                        .executeSingle();
                if (rosterGroupModel != null) {
                    loaderList = rosterGroupModel.items();
                }
            }
            break;
            case ContactsActivity.GROUPS_FRAGMENT: {
                List<RosterGroupModel> queryResults = new Select().from(RosterGroupModel.class)
                        .orderBy("name ASC").execute();

                /*for (RosterGroupModel group : queryResults) {
                    group.items()
                    Collections.sort(group.getEntries(), new EntrySortBasedOnName());
                }*/
                loaderList = queryResults;
            }
            break;
        }

        return loaderList;
    }

    ArrayList<?> getContactsList(List<RosterEntryModel> countryNames) {
        val objectArrayList = new ArrayList<>();
        //Insert headers into list of items.
        String lastHeader = "";
        int sectionManager = -1;
        int headerCount = 0;
        int sectionFirstPosition = 0;
        for (int i = 0; i < countryNames.size(); i++) {
            String header = countryNames.get(i).name.substring(0, 1);
            if (!TextUtils.equals(lastHeader, header)) {
                // Insert new header view and update section data.
                sectionManager = (sectionManager + 1) % 2;
                sectionFirstPosition = i + headerCount;
                lastHeader = header;
                headerCount += 1;
                objectArrayList.add(new ContactsAdapter.LineItem(header, true, sectionManager, sectionFirstPosition));
            }
            objectArrayList.add(new ContactsAdapter.LineItem(countryNames.get(i).name, false, sectionManager, sectionFirstPosition));
        }
        return objectArrayList;
    }

    class EntrySortBasedOnName implements Comparator {
        public int compare(Object o1, Object o2) {
            val dd1 = (RosterEntry) o1;// where FBFriends_Obj is your object class
            val dd2 = (RosterEntry) o2;
            return dd1.getName().compareToIgnoreCase(dd2.getName());//where uname is field name
        }
    }

    class GroupSortBasedOnName implements Comparator {
        public int compare(Object o1, Object o2) {
            val dd1 = (RosterGroup) o1;// where FBFriends_Obj is your object class
            val dd2 = (RosterGroup) o2;
            return dd1.getName().compareToIgnoreCase(dd2.getName());//where uname is field name
        }
    }
}
