package ru.rian.riamessenger.utils;

import android.database.Cursor;
import android.provider.BaseColumns;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import java.util.Arrays;
import java.util.List;

import ru.rian.riamessenger.R;
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
        int columnIndex = cursor.getColumnIndex(BaseColumns._ID);
        long id = cursor.getLong(columnIndex);

        T model = (T) Cache.getEntity(cl, id);
        if (model == null) {
            model = new Select().from(cl).where(BaseColumns._ID + "=?", id).executeSingle();
        }
        return model;
    }

    static public RosterEntryModel getRosterEntryByBareJid(String bareJid) {
        String rosterEntryToSelect = new Select().from(RosterEntryModel.class).where("BareJid ='" + bareJid + "'").toSql();
        Cursor rosterEntryCursor = Cache.openDatabase().rawQuery(rosterEntryToSelect, null);
        List<RosterEntryModel> rosterEntryModels = SQLiteUtils.processCursor(RosterEntryModel.class, rosterEntryCursor);
        rosterEntryCursor.close();
        RosterEntryModel rosterEntryModel = null;
        if (rosterEntryModels != null && rosterEntryModels.size() > 0) {
            rosterEntryModel = rosterEntryModels.get(0);
        }
        return rosterEntryModel;
    }

}
