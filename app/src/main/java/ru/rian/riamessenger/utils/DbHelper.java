package ru.rian.riamessenger.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import java.util.List;

import ru.rian.riamessenger.common.DbColumns;
import ru.rian.riamessenger.model.MessageContainer;
import ru.rian.riamessenger.model.RosterEntryModel;

/**
 * Created by Roman on 7/14/2015.
 */
public class DbHelper {


    public static <T extends Model> T getModelByPos(int position, Cursor cursor, Class<T> cl) {
        cursor.moveToPosition(position);
        return getModelByCursor(cursor, cl);
    }

    public static <T extends Model> T getModelByCursor(Cursor cursor, Class<T> cl) {
        if (cursor.isClosed()) return null;
        int columnIndex = cursor.getColumnIndex(BaseColumns._ID);
        long id = cursor.getLong(columnIndex);

        T model = (T) Cache.getEntity(cl, id);
        if (model == null) {
            model = new Select().from(cl).where(BaseColumns._ID + "=?", id).executeSingle();
        }
        return model;
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
}
