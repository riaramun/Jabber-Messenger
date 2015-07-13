package ru.rian.riamessenger.adapters;

import org.jivesoftware.smack.roster.RosterEntry;

import java.util.ArrayList;

import ru.rian.riamessenger.model.RosterEntryModel;
import ru.rian.riamessenger.model.RosterGroupModel;

/**
 * Created by Roman on 7/6/2015.
 */
public class GroupsDataProvider extends AbstractExpandableDataProvider {

    ArrayList<RosterGroupModel> rosterGroupModels;

    public GroupsDataProvider(ArrayList<RosterGroupModel> rosterGroups) {
        this.rosterGroupModels = rosterGroups;
    }

    @Override
    public int getGroupCount() {
        int count = 0;
        if (rosterGroupModels != null) {
            count = rosterGroupModels.size();
        }
        return count;
    }

    @Override
    public int getChildCount(int groupPosition) {
        return rosterGroupModels.get(groupPosition).items().size();
    }

    @Override
    public GroupData getGroupItem(int groupPosition) {
        RosterGroupModel rosterGroupModel = rosterGroupModels.get(groupPosition);
        return new RosterItemGroupData(rosterGroupModel);
    }

    @Override
    public ChildData getChildItem(int groupPosition, int childPosition) {
        RosterGroupModel rosterGroup = rosterGroupModels.get(groupPosition);
        RosterItemChildData rosterItemChildData = null;
        if(rosterGroup.items() != null && rosterGroup.items().size() > childPosition) {
            RosterEntryModel rosterEntry = rosterGroup.items().get(childPosition);
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
