package ru.rian.riamessenger.adapters.expandable;


import ru.rian.riamessenger.adapters.base.AbstractExpandableDataProvider;
import ru.rian.riamessenger.model.RosterGroupModel;

/**
 * Created by Roman on 7/6/2015.
 */
public class RosterItemGroupData extends AbstractExpandableDataProvider.GroupData {

    RosterGroupModel rosterGroup;

    public RosterItemGroupData(RosterGroupModel rosterGroup) {
        this.rosterGroup = rosterGroup;
    }

    @Override
    public boolean isSectionHeader() {
        return false;
    }

    @Override
    public long getGroupId() {
        long id = 0;
        if (rosterGroup != null)
            id = rosterGroup.hashCode();
        return id;
    }

    @Override
    public int getSwipeReactionType() {
        return 0;
    }

    @Override
    public int getPresence() {
        return 0;
    }

    @Override
    public String getText() {
        String name = "";
        if (rosterGroup != null) {
            name = rosterGroup.name;
        }
        return name;
    }

    @Override
    public void setPinnedToSwipeLeft(boolean pinned) {

    }

    @Override
    public boolean isPinnedToSwipeLeft() {
        return false;
    }
}
