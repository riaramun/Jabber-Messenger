package ru.rian.riamessenger.model;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.Date;

import ru.rian.riamessenger.common.DbColumns;

@Table(name = "MessageContainer", id = BaseColumns._ID)
public class MessageContainer extends Model {

    public MessageContainer() {
        super();
        isSent = false;
        isRead = false;
    }

    @Column(name = DbColumns.ToJidCol)
    public String toJid;

    @Column(name = DbColumns.FromJidCol)
    public String fromJid;

    @Column(name = DbColumns.ThreadIdCol)
    public String threadID;

    @Column(name = DbColumns.CreatedCol)
    public Date created;
    /*
    @Column(name = "readed")
    public boolean readed;
    @Column(name = "sended")
    public boolean sended;*/
    @Column(name = DbColumns.MsgBodyCol)
    public String body;

    @Column(name = DbColumns.ReadFlagIdCol)
    public Boolean isRead;

    @Column(name = DbColumns.SentFlagIdCol)
    public Boolean isSent;

    @Column(name = DbColumns.StanzaIdCol, unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String stanzaID;

}
