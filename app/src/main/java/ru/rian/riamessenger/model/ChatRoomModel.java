package ru.rian.riamessenger.model;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import ru.rian.riamessenger.common.DbColumns;

/**
 * Created by Roman on 7/9/2015.
 */

@Table(name = "ChatRoomModel", id = BaseColumns._ID)
public class ChatRoomModel extends Model {

    @Column(name = DbColumns.ThreadIdCol, unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String threadIdCol;


    @Column(name = DbColumns.NameCol)
    public String name;


    public ChatRoomModel() {
        super();
    }

    /*public List<RosterEntryModel> items() {
        return getMany(RosterEntryModel.class, "ChatRoomModel");
    }*/
}
