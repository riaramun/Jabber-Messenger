package ru.rian.riamessenger.utils;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.id.StanzaIdUtil;

import java.util.Date;
import java.util.List;

import ru.rian.riamessenger.common.DbColumns;
import ru.rian.riamessenger.model.MessageContainer;
import ru.rian.riamessenger.model.RosterEntryModel;
import ru.rian.riamessenger.model.RosterGroupModel;

/**
 * Created by Roman on 7/14/2015.
 */
public class DbHelper {


    public static <T extends Model> T getModelByPos(int position, Cursor cursor, Class<T> cl) {
        cursor.moveToPosition(position);
        return getModelByCursor(cursor, cl);
    }

    public static <T extends Model> T getModelByCursor(Cursor cursor, Class<T> cl) {
        if (cursor.isClosed()) {
            Log.i("RiaService", "cursor.isClosed");
            return null;
        }
        int columnIndex = cursor.getColumnIndex(BaseColumns._ID);
        long id = cursor.getLong(columnIndex);

        T model = (T) Cache.getEntity(cl, id);
        if (model == null) {
            model = new Select().from(cl).where(BaseColumns._ID + "=?", id).executeSingle();
        }
        return model;
    }

    public static void clearDb() {
        new Delete().from(RosterGroupModel.class).execute();
        new Delete().from(RosterEntryModel.class).execute();
        new Delete().from(MessageContainer.class).execute();
    }
   /* static public Cursor getMessagesByJid(String jid) {
//        String tableName = Cache.getTableInfo(MessageContainer.class).getTableName();
        String resultRecords = new Select().
                from(MessageContainer.class).toSql();
        SQLiteDatabase db = ActiveAndroid.getDatabase();

        Cursor resultCursor = db.rawQuery(resultRecords, null);
        //return resultCursor;

      // List<MessageContainer>  messageContainers = new Select().from(MessageContainer.class).execute();
        String select = new Select().from(MessageContainer.class).where(DbColumns.FromJidCol + "='" + jid + "'").toSql();
      //  String select = new Select().from(MessageContainer.class).toSql();
        Cursor msgCursor = Cache.openDatabase().rawQuery(select, null);
        return msgCursor;
    }*/

    public static boolean rosterTableIsNotEmpty() {
        List<RosterEntryModel> rosterEntryModels = new Select().from(RosterEntryModel.class).execute();
        final int currenUserEtriesNumber = 1;
        return rosterEntryModels != null && rosterEntryModels.size() > currenUserEtriesNumber;
    }

    static public RosterEntryModel getRosterEntryByBareJid(String bareJid) {
        RosterEntryModel rosterEntryModel = new Select().from(RosterEntryModel.class).where(DbColumns.FromJidCol + "='" + bareJid + "'").executeSingle();
        return rosterEntryModel;
    }

    static public RosterEntryModel getRosterEntryById(long id) {
        RosterEntryModel rosterEntryModel = new Select().from(RosterEntryModel.class).where(BaseColumns._ID + "=" + id).executeSingle();
        return rosterEntryModel;
    }

    public static MessageContainer getLastMessageFrom(String jidFrom, String jidTo) {
        String select = new Select().from(MessageContainer.class)
                .where(DbColumns.FromJidCol + "='" + jidFrom + "' and " + DbColumns.ToJidCol + "='" + jidTo + "'" + " ORDER BY " + DbColumns.CreatedCol + " DESC"
                ).toSql();
        List<MessageContainer> messageContainers = SQLiteUtils.rawQuery(MessageContainer.class, select, null);
        return messageContainers.get(0);
    }

    public static MessageContainer getMessageByReceiptId(String stanzaId) {
        String select = new Select().from(MessageContainer.class).where(DbColumns.StanzaIdCol + "='" + stanzaId + "'").toSql();
        MessageContainer messageContainer = SQLiteUtils.rawQuerySingle(MessageContainer.class, select, null);
        return messageContainer;
    }

    public static List<MessageContainer> getAllNotReadMessages(String messageThreadId) {
        String select = new Select().from(MessageContainer.class).where(DbColumns.ReadFlagIdCol + "=0 and " + DbColumns.ThreadIdCol + "='" + messageThreadId + "'").toSql();
        List<MessageContainer> messageContainers = SQLiteUtils.rawQuery(MessageContainer.class, select, null);
        return messageContainers;
    }

    public static List<MessageContainer> getAllNotSentMessages(String currentUserJid) {
        String select = new Select().from(MessageContainer.class).where(DbColumns.SentFlagIdCol + "=0 and " + DbColumns.FromJidCol + "='" + currentUserJid + "'").toSql();
        List<MessageContainer> messageContainers = SQLiteUtils.rawQuery(MessageContainer.class, select, null);
        return messageContainers;
    }


    public static MessageContainer addMessageToDb(Message message, String messageId, boolean isRead) {
        MessageContainer messageContainer = new MessageContainer();
        messageContainer.body = message.getBody();
        messageContainer.stanzaID = message.getStanzaId();
        messageContainer.toJid = message.getTo().asEntityBareJidIfPossible().toString();
        messageContainer.fromJid = message.getFrom().asEntityBareJidIfPossible().toString();
        messageContainer.threadID = messageId;// message.getThread();
        messageContainer.created = new Date();
        messageContainer.isRead = isRead;
        messageContainer.stanzaID = message.getStanzaId();
        messageContainer.save();
        return messageContainer;
    }
}
