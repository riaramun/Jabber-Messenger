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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.rian.riamessenger.common.DbColumns;
import ru.rian.riamessenger.model.ChatRoomModel;
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


        new Delete().from(ChatRoomModel.class).execute();
        new Delete().from(RosterEntryModel.class).execute();
        new Delete().from(RosterGroupModel.class).execute();
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
        final int currenUserEtriesNumber = 2;
        return rosterEntryModels != null && rosterEntryModels.size() > currenUserEtriesNumber;
    }

    static public ChatRoomModel getChatRoomByJid(String bareJid) {
        return new Select().from(ChatRoomModel.class).where(DbColumns.ThreadIdCol + "='" + bareJid + "'").executeSingle();
    }

    static public RosterEntryModel getRosterEntryByBareJid(String bareJid) {
        return new Select().from(RosterEntryModel.class).where(DbColumns.FromJidCol + "='" + bareJid + "'").executeSingle();
    }

    static public RosterEntryModel getRosterEntryById(long id) {
        return new Select().from(RosterEntryModel.class).where(BaseColumns._ID + "=" + id).executeSingle();
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
        return SQLiteUtils.rawQuerySingle(MessageContainer.class, select, null);
    }

    public static int getUnReadMessagesNum(String messageThreadId) {
        List<MessageContainer> messageContainers = getUnReadMessages(messageThreadId);
        return messageContainers == null ? 0 : messageContainers.size();
    }

    static List<MessageContainer> getUnReadMessages(String messageThreadId) {
        String select = new Select().from(MessageContainer.class).where(DbColumns.ReadFlagIdCol + "=0 and " + DbColumns.ThreadIdCol + "='" + messageThreadId + "'").toSql();
        return SQLiteUtils.rawQuery(MessageContainer.class, select, null);
    }

    public static List<MessageContainer> getAllNotSentMessages(String currentUserJid) {
        String select = new Select().from(MessageContainer.class).where(DbColumns.SentFlagIdCol + "=0 and " + DbColumns.FromJidCol + "='" + currentUserJid + "'").toSql();
        return SQLiteUtils.rawQuery(MessageContainer.class, select, null);
    }

    /*public static MessageContainer addGroupMessageToDb(Message message, int chatType, String messageId, String from, String to, boolean isRead) {
        MessageContainer messageContainer = new MessageContainer(chatType);
        messageContainer.body = message.getBody();
        messageContainer.stanzaID = message.getStanzaId();
        messageContainer.toJid = to;
        messageContainer.fromJid = from;
        messageContainer.threadID = messageId;// message.getThread();
        messageContainer.created = new Date();
        messageContainer.isRead = isRead;
        messageContainer.stanzaID = message.getStanzaId();
        messageContainer.save();
        return messageContainer;
    }*/

    public static MessageContainer addMessageToDb(Message message, int chatType, String messageId, boolean isRead) {
        int slashInd;
        String msgId;
        String from;
        String to;
        //from cijcjcjc@conference.kis-jabber/skurzhansky
        //to lebedenko@kis-jabber/ria_mobile

        slashInd = messageId.indexOf('/');
        msgId = slashInd > 0 ? messageId.substring(0, slashInd) : messageId;

        slashInd = message.getTo().indexOf('/');
        to = slashInd > 0 ? message.getTo().substring(0, slashInd) : message.getTo();

        slashInd = message.getFrom().indexOf('/');
        if (chatType == MessageContainer.CHAT_SIMPLE) {
            from = slashInd > 0 ? message.getFrom().substring(0, slashInd) : message.getFrom();
        } else {
            from = slashInd > 0 ? message.getFrom().substring(slashInd + 1) : message.getFrom();
        }
        MessageContainer messageContainer = new MessageContainer(chatType);
        messageContainer.body = message.getBody();
        messageContainer.stanzaID = message.getStanzaId();
        messageContainer.toJid = to;
        messageContainer.fromJid = from;
        messageContainer.threadID = msgId.toLowerCase();// message.getThread();
        messageContainer.created = new Date();
        messageContainer.isRead = isRead;
        messageContainer.stanzaID = message.getStanzaId();
        messageContainer.save();
        return messageContainer;
    }

    public static List<String> getRoomsJidFromDb() {
        String messages = Cache.getTableInfo(MessageContainer.class).getTableName();
        String req = "SELECT " + BaseColumns._ID + "," + DbColumns.ThreadIdCol + "," + DbColumns.MsgBodyCol + "," + DbColumns.FromJidCol + ","
                + "MAX(" + DbColumns.CreatedCol + ") AS " + DbColumns.CreatedCol + " FROM " + messages + " WHERE " + DbColumns.ChatTypeCol + "=" + MessageContainer.CHAT_GROUP + " GROUP BY " + DbColumns.ThreadIdCol;
        Cursor cursor = Cache.openDatabase().rawQuery(req, null);
        List<MessageContainer> roomsLastMessages = SQLiteUtils.processCursor(MessageContainer.class, cursor);
        List<String> roomsJids = new ArrayList<>();
        for (MessageContainer messageContainer : roomsLastMessages) {
            roomsJids.add(messageContainer.threadID);
        }
        cursor.close();
        return roomsJids;
    }
}
