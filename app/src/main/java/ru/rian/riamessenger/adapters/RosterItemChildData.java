package ru.rian.riamessenger.adapters;


import ru.rian.riamessenger.model.RosterEntryModel;

/**
 * Created by Roman on 7/6/2015.
 */
public class RosterItemChildData extends AbstractExpandableDataProvider.ChildData {

    RosterEntryModel rosterEntry;

    public RosterItemChildData(RosterEntryModel rosterEntry) {
        this.rosterEntry = rosterEntry;
    }

    @Override
    public long getChildId() {
        return rosterEntry.hashCode();
    }

    @Override
    public int getSwipeReactionType() {
        return 0;
    }

    @Override
    public String getText() {
        return rosterEntry.name;
    }

    @Override
    public void setPinnedToSwipeLeft(boolean pinned) {

    }

    @Override
    public boolean isPinnedToSwipeLeft() {
        return false;
    }
}
