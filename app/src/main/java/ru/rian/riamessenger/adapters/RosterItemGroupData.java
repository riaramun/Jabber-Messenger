package ru.rian.riamessenger.adapters;


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
        return rosterGroup.hashCode();
    }

    @Override
    public int getSwipeReactionType() {
        return 0;
    }

    @Override
    public String getText() {
        return rosterGroup.name;
    }

    @Override
    public void setPinnedToSwipeLeft(boolean pinned) {

    }

    @Override
    public boolean isPinnedToSwipeLeft() {
        return false;
    }
}
