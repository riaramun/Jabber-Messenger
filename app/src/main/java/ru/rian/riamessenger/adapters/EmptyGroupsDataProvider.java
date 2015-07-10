package ru.rian.riamessenger.adapters;

/**
 * Created by Roman on 7/6/2015.
 */
public class EmptyGroupsDataProvider extends AbstractExpandableDataProvider {
    @Override
    public int getGroupCount() {
        return 1;
    }

    @Override
    public int getChildCount(int groupPosition) {
        return 1;
    }

    @Override
    public GroupData getGroupItem(int groupPosition) {
        return new GroupData() {
            @Override
            public boolean isSectionHeader() {
                return false;
            }

            @Override
            public long getGroupId() {
                return 0;
            }

            @Override
            public int getSwipeReactionType() {
                return 0;
            }

            @Override
            public String getText() {
                return null;
            }

            @Override
            public void setPinnedToSwipeLeft(boolean pinned) {

            }

            @Override
            public boolean isPinnedToSwipeLeft() {
                return false;
            }
        };
    }

    @Override
    public ChildData getChildItem(int groupPosition, int childPosition) {
        return new ChildData() {
            @Override
            public int getSwipeReactionType() {
                return 0;
            }

            @Override
            public String getText() {
                return null;
            }

            @Override
            public void setPinnedToSwipeLeft(boolean pinned) {

            }

            @Override
            public boolean isPinnedToSwipeLeft() {
                return false;
            }

            @Override
            public long getChildId() {
                return 0;
            }
        };
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
