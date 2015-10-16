package ru.rian.riamessenger.model;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import ru.rian.riamessenger.common.DbColumns;

/**
 * Created by Roman on 7/9/2015.
 */

@Table(name = "ChatRoomOccupantModel", id = BaseColumns._ID)
public class ChatRoomOccupantModel extends Model {

    @Column(name = DbColumns.ChatRoomModel, onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public ChatRoomModel chatRoomModel;

    @Column(name = DbColumns.FromJidCol)
    public String bareJid;


    public ChatRoomOccupantModel() {
        super();
    }
}
