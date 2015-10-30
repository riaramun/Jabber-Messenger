package ru.rian.riamessenger.utils;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.rian.riamessenger.common.DbColumns;
import ru.rian.riamessenger.common.RiaConstants;
import ru.rian.riamessenger.model.ChatRoomModel;
import ru.rian.riamessenger.model.ChatRoomOccupantModel;
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

    static public List<ChatRoomModel> getChatRooms() {
        return new Select().from(ChatRoomModel.class).execute();
    }
    static public List<RosterGroupModel> getRosterGroupModels() {
        return new Select().from(RosterGroupModel.class).execute();
    }
    static public ChatRoomModel getChatRoomByJid(String bareJid) {
        return new Select().from(ChatRoomModel.class).where(DbColumns.ThreadIdCol + "='" + bareJid + "'").executeSingle();
    }

    static public List<ChatRoomOccupantModel> getRoomOccupants(String roomJid) {
        ChatRoomModel chatRoomModel = getChatRoomByJid(roomJid);
        return new Select().from(ChatRoomOccupantModel.class).where(DbColumns.ChatRoomModel + "=" + chatRoomModel.getId()).execute();
    }

    static public List<ChatRoomOccupantModel> getRoomOccupantModelByUser(String userJid) {
        return new Select().from(ChatRoomOccupantModel.class).where(DbColumns.FromJidCol + "='" + userJid + "'").execute();
    }

    static public ChatRoomOccupantModel getRoomOccupant(long roomId, String userJid) {
        return new Select().from(ChatRoomOccupantModel.class).where(DbColumns.ChatRoomModel + "=" + roomId + " and " + DbColumns.FromJidCol + "='" + userJid + "'").executeSingle();
    }

    static public void addOccupantToDb(String participantJid, ChatRoomModel dbChatRoomModel) {
        ChatRoomOccupantModel chatRoomOccupantModel = new ChatRoomOccupantModel();
        chatRoomOccupantModel.bareJid = participantJid;
        chatRoomOccupantModel.chatRoomModel = dbChatRoomModel;
        chatRoomOccupantModel.save();
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

    /*static List<ChatRoomOccupantModel> getOccupantsByRoomThreadId(String roomThreadId) {
        String select = new Select().from(ChatRoomModel.class).where(DbColumns.ThreadIdCol + "='" + roomThreadId + "'").toSql();
        ChatRoomModel chatRoomModel = SQLiteUtils.rawQuerySingle(ChatRoomModel.class, select, null);
        return chatRoomModel.items();
    }*/

    public static List<MessageContainer> getAllNotSentMessages(String currentUserJid) {
        String select = new Select().from(MessageContainer.class).where(DbColumns.ChatTypeCol + " = " + MessageContainer.CHAT_SIMPLE + " and " + DbColumns.SentFlagIdCol + "=0 and " + DbColumns.FromJidCol + "='" + currentUserJid + "'").toSql();
        return SQLiteUtils.rawQuery(MessageContainer.class, select, null);
    }

    public static MessageContainer addMessageToDb(Message message, int chatType, Jid messageId, boolean isRead) {
        MessageContainer messageContainer = null;
        if (!TextUtils.isEmpty(message.getBody())) {

            String msgId;
            String from;
            String to;

            msgId = messageId.asBareJid().toString();//slashInd > 0 ? messageId.substring(0, slashInd) : messageId;
            //workaround for bugs...
            if (message.getFrom().toString().contains(RiaConstants.ROOM_DOMAIN)
                    || message.getTo().toString().contains(RiaConstants.ROOM_DOMAIN)) {
                chatType = MessageContainer.CHAT_GROUP;
            } else {
                chatType = MessageContainer.CHAT_SIMPLE;
            }

            if (chatType == MessageContainer.CHAT_SIMPLE) {
                from = message.getFrom().asBareJid().toString();
            } else {
                //if we send group message we must set resTo, another way resFrom
                Resourcepart resFrom = message.getFrom().getResourceOrNull();
                from = resFrom != null ? resFrom.toString() : message.getFrom().toString();
            }

            to = message.getTo().asBareJid().toString();
            String text;
            if (TextUtils.isEmpty(message.getSubject())) {
                text = message.getBody();
            } else {
                text = message.getSubject() + "\n" + message.getBody();
            }
            messageContainer = new MessageContainer(chatType);
            messageContainer.body = text;
            messageContainer.stanzaID = message.getStanzaId();
            messageContainer.toJid = to;
            messageContainer.fromJid = from;
            messageContainer.threadID = msgId.toLowerCase();// message.getThread();
            messageContainer.created = new Date();
            messageContainer.isRead = isRead;
            messageContainer.stanzaID = message.getStanzaId();
            messageContainer.save();
        }
        return messageContainer;
    }

    public static List<EntityBareJid> getRoomsJidFromDb() {
        String messages = Cache.getTableInfo(MessageContainer.class).getTableName();
        String req = "SELECT " + BaseColumns._ID + "," + DbColumns.ThreadIdCol + "," + DbColumns.MsgBodyCol + "," + DbColumns.FromJidCol + ","
                + "MAX(" + DbColumns.CreatedCol + ") AS " + DbColumns.CreatedCol + " FROM " + messages + " WHERE " + DbColumns.ChatTypeCol + "=" + MessageContainer.CHAT_GROUP + " GROUP BY " + DbColumns.ThreadIdCol;
        Cursor cursor = Cache.openDatabase().rawQuery(req, null);
        List<MessageContainer> roomsLastMessages = SQLiteUtils.processCursor(MessageContainer.class, cursor);
        List<EntityBareJid> roomsJids = new ArrayList<>();
        for (MessageContainer messageContainer : roomsLastMessages) {
            try {
                roomsJids.add(JidCreate.entityBareFrom(messageContainer.threadID));
            } catch (XmppStringprepException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        return roomsJids;
    }

    public static void addRoomToDb(EntityBareJid roomJid, String ownerJid, List<String> contactJids) {
        try {
            ActiveAndroid.beginTransaction();

            ChatRoomModel chatRoomModel = new ChatRoomModel();
            chatRoomModel.threadIdCol = roomJid.asBareJid().toString();
            chatRoomModel.name = roomJid.getLocalpart().toString();//roomJid.substring(0, roomJid.indexOf("@"));
            chatRoomModel.ownerJidCol = ownerJid;
            chatRoomModel.save();

            if (contactJids != null) {
                for (String entityFullJid : contactJids) {
                    ChatRoomOccupantModel chatRoomOccupantModel = new ChatRoomOccupantModel();
                    chatRoomOccupantModel.bareJid = entityFullJid;
                    chatRoomOccupantModel.chatRoomModel = chatRoomModel;
                    chatRoomOccupantModel.save();
                }
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    public static void deleteOccupantFromDb(EntityBareJid roomJid, String contactJid) {
        ChatRoomModel chatRoomModel = DbHelper.getChatRoomByJid(roomJid.asEntityBareJidString());
        if (chatRoomModel != null) {
            ChatRoomOccupantModel chatRoomOccupantModel = getRoomOccupant(chatRoomModel.getId(), contactJid);
            if (chatRoomOccupantModel != null) {
                chatRoomOccupantModel.delete();
            }
        }
    }

    /*public static void addOccupantToDb(EntityBareJid roomJid, List<String> contactJids) {
        try {
            ChatRoomModel chatRoomModel = DbHelper.getChatRoomByJid(roomJid.asEntityBareJidString());
            ActiveAndroid.beginTransaction();
            for (String entityFullJid : contactJids) {
                ChatRoomOccupantModel chatRoomOccupantModel = new ChatRoomOccupantModel();
                chatRoomOccupantModel.bareJid = entityFullJid;
                chatRoomOccupantModel.chatRoomModel = chatRoomModel;
                chatRoomOccupantModel.save();
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }*/
}
