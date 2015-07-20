package ru.rian.riamessenger.loaders;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.TableInfo;
import com.activeandroid.content.ContentProvider;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import lombok.val;
import ru.rian.riamessenger.ContactsActivity;
import ru.rian.riamessenger.R;
import ru.rian.riamessenger.adapters.list.ContactsAdapter;
import ru.rian.riamessenger.fragments.BaseTabFragment;
import ru.rian.riamessenger.loaders.base.BaseCursorRiaLoader;
import ru.rian.riamessenger.loaders.base.BaseRiaLoader;
import ru.rian.riamessenger.loaders.base.CursorRiaLoader;
import ru.rian.riamessenger.model.RosterEntryModel;
import ru.rian.riamessenger.model.RosterGroupModel;

/**
 * Created by Roman Lebedenko.
 * fisher3421@gmail.com
 */

public class ContactsLoader extends CursorRiaLoader {

    int tabIdloader = -1;
    String title_to_search = null;

    public ContactsLoader(Context ctx, Bundle args) {
        super(ctx);
        tabIdloader = args.getInt(BaseTabFragment.ARG_TAB_ID);
        title_to_search = args.getString(BaseTabFragment.ARG_TITLE_FILTER);

        switch (tabIdloader) {
            case BaseTabFragment.CONTACTS_FRAGMENT:
                setSubscription(ContentProvider.createUri(RosterEntryModel.class, null));
                break;
            case BaseTabFragment.ROBOTS_FRAGMENT:
                setSubscription(ContentProvider.createUri(RosterGroupModel.class, null));
                break;
            case BaseTabFragment.GROUPS_FRAGMENT:
                setSubscription(ContentProvider.createUri(RosterGroupModel.class, null));
                break;
        }
    }


    Cursor retrieveDataFromRoster() {

        String resultRecords;
        Cursor resultCursor = null;

        switch (tabIdloader) {
            case BaseTabFragment.CONTACTS_FRAGMENT: {

                String groupToExcludeRequest = new Select().from(RosterGroupModel.class).where("name ='" + getContext().getString(R.string.robots) + "'").toSql();
                Cursor groupCursor = Cache.openDatabase().rawQuery(groupToExcludeRequest, null);
                List<RosterGroupModel> groupModelList = SQLiteUtils.processCursor(RosterGroupModel.class, groupCursor);
                groupCursor.close();
                long groupToExcludeId = 0;
                if(groupModelList != null && groupModelList.size() > 0) {
                    groupToExcludeId = groupModelList.get(0).getId();
                }
                From from = new Select().from(RosterEntryModel.class);

                String sqlReqText = "";
                if(groupToExcludeId != 0) {
                    sqlReqText += " RosterGroupModel != " + groupToExcludeId;
                }

                if (!TextUtils.isEmpty(title_to_search)) {
                    if(!TextUtils.isEmpty(sqlReqText)) {
                        sqlReqText += " and ";
                    }
                    sqlReqText += "Name LIKE '%" + title_to_search.toLowerCase(Locale.getDefault()) + "%'";
                }

                if(!TextUtils.isEmpty(sqlReqText)) {
                    from.where(sqlReqText);
                }


                String queryResults = from.orderBy("Name ASC").toSql();
                resultCursor = Cache.openDatabase().rawQuery(queryResults, null);

                //listCursor = getContactsList(queryResults);
                //listCursor = fetchResultCursor(RosterEntryModel.class);
                //SQLiteUtils.processCursor(RosterEntryModel.class, listCursor);
            }
            break;
            case BaseTabFragment.ROBOTS_FRAGMENT: {
                //  List<RosterEntry> rosterEntries =
                /*RosterGroupModel rosterGroupModel = new Select().from(RosterGroupModel.class)//.where("name = ?", getContext().getString(R.string.robots))
                        .orderBy("name ASC")
                        .executeSingle();
                if (rosterGroupModel != null) {
                    listCursor = rosterGroupModel.items();
                }*/
                String groupTableName = Cache.getTableInfo(RosterGroupModel.class).getTableName();
                resultRecords = new Select().from(RosterEntryModel.class).where("RosterEntryModels.RosterGroupModel IN (SELECT _id" + " FROM " + groupTableName + " WHERE name ='" + getContext().getString(R.string.robots) + "')").orderBy("name ASC").toSql();

                //  tableName = Cache.getTableInfo(RosterGroupModel.class).getTableName();
                //  resultRecords = new Select().from(RosterGroupModel.class).where("name = ?", getContext().getString(R.string.robots)).toSql();
                resultCursor = Cache.openDatabase().rawQuery(resultRecords, null);
            }
            break;
            case BaseTabFragment.GROUPS_FRAGMENT: {
                /*List<RosterGroupModel> queryResults = new Select().from(RosterGroupModel.class)
                        .orderBy("name ASC").execute();

                for (RosterGroupModel group : queryResults) {
                    group.items()
                    Collections.sort(group.getEntries(), new EntrySortBasedOnName());
                }*/
                String tableName = Cache.getTableInfo(RosterGroupModel.class).getTableName();
                resultRecords = new Select("*").from(RosterGroupModel.class).toSql();
                resultCursor = Cache.openDatabase().rawQuery(resultRecords, null);
            }
            break;
        }

        return resultCursor;
    }

    @Override
    protected Cursor loadCursor() throws Exception {
        return retrieveDataFromRoster();
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
