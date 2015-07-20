package ru.rian.riamessenger.adapters.expandable;

import android.database.Cursor;

import ru.rian.riamessenger.adapters.base.AbstractExpandableDataProvider;
import ru.rian.riamessenger.model.RosterEntryModel;
import ru.rian.riamessenger.model.RosterGroupModel;
import ru.rian.riamessenger.utils.DbHelper;

/**
 * Created by Roman on 7/6/2015.
 */
public class GroupsDataProvider extends AbstractExpandableDataProvider {

    Cursor rosterGroupModelsCursor;

    public GroupsDataProvider(Cursor rosterGroups) {
        this.rosterGroupModelsCursor = rosterGroups;
    }

    @Override
    public int getGroupCount() {
        int count = 0;
        if (rosterGroupModelsCursor != null && !rosterGroupModelsCursor.isClosed()) {
            count = rosterGroupModelsCursor.getCount();
        }
        return count;
    }

    @Override
    public int getChildCount(int groupPosition) {
        RosterGroupModel rosterGroupModel = DbHelper.getModelByPos(groupPosition, rosterGroupModelsCursor, RosterGroupModel.class);
        int count = 0;
        if (rosterGroupModel != null && rosterGroupModel.items() != null && rosterGroupModel.items().size() > 0) {
            count = rosterGroupModel.items().size();
        }
        return count;
    }

    @Override
    public GroupData getGroupItem(int groupPosition) {
        RosterGroupModel rosterGroupModel = DbHelper.getModelByPos(groupPosition, rosterGroupModelsCursor, RosterGroupModel.class);
        return new RosterItemGroupData(rosterGroupModel);
    }
/*
    RosterGroupModel getRosterGroupModelByGroupPos(int groupPosition) {
        rosterGroupModelsCursor.moveToPosition(groupPosition);
        int columnIndex = rosterGroupModelsCursor.getColumnIndex(BaseColumns._ID);
        long id = rosterGroupModelsCursor.getLong(columnIndex);
        columnIndex = rosterGroupModelsCursor.getColumnIndex("Name");
        String name = rosterGroupModelsCursor.getString(columnIndex);
        RosterGroupModel rosterGroupModel = (RosterGroupModel) Cache.getEntity(RosterGroupModel.class, id);
        if (rosterGroupModel == null) {
            rosterGroupModel = new Select().from(RosterGroupModel.class).where(BaseColumns._ID+"=?", id).executeSingle();
        }
        return rosterGroupModel;
    }*/

    @Override
    public ChildData getChildItem(int groupPosition, int childPosition) {
        RosterGroupModel rosterGroupModel = DbHelper.getModelByPos(groupPosition, rosterGroupModelsCursor, RosterGroupModel.class);
        RosterItemChildData rosterItemChildData = null;
        if (rosterGroupModel.items() != null && rosterGroupModel.items().size() > childPosition) {
            RosterEntryModel rosterEntry = rosterGroupModel.items().get(childPosition);
            rosterItemChildData = new RosterItemChildData(rosterEntry);
        }
        return rosterItemChildData;
    }

    @Override
    public void moveGroupItem(int fromGroupPosition, int toGroupPosition) {

    }

    @Override
    public void moveChildItem(int fromGroupPosition, int fromChildPosition, int toGroupPosition, int toChildPosition) {

    }

    @Override
    public void removeGroupItem(int groupPosition) {

    }

    @Override
    public void removeChildItem(int groupPosition, int childPosition) {

    }

    @Override
    public long undoLastRemoval() {
        return 0;
    }

}
