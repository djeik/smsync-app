package me.jerrington.smsync.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import timber.log.Timber;

public class LastSMSDAO {
    public static final String TABLE_NAME = "last_sms";

    public static final String COL_ID = "_id";
    public static final String COL_LAST_SMS_POSITION = "last_position";

    public static void createTable(final SQLiteDatabase db) {
        db.execSQL(
                String.format(
                        "CREATE TABLE %s ( " +
                                "%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "%s INTEGER" +
                                ");",
                        TABLE_NAME,
                        COL_ID,
                        COL_LAST_SMS_POSITION
                )
        );
    }

    public static void dropTable(final SQLiteDatabase db) {
        db.execSQL(
                String.format(
                        "DROP TABLE IF EXISTS %s;",
                        TABLE_NAME
                )
        );
    }

    final SMSyncSQLiteOpenHelper dbHelper;
    final SQLiteDatabase db;

    Long id;
    Long lastPosition;

    public LastSMSDAO(final Context context) {
        dbHelper = new SMSyncSQLiteOpenHelper(context);
        db = dbHelper.getWritableDatabase();

        id = null;
        lastPosition = null;

        final Cursor cursor = db.rawQuery(
                String.format(
                        "SELECT %s, %s FROM %s LIMIT 1;",
                        COL_ID,
                        COL_LAST_SMS_POSITION,
                        TABLE_NAME
                ),
                new String[]{}
        );

        if (cursor.getCount() == 0)
            return;

        cursor.moveToFirst();
        lastPosition = cursor.getLong(cursor.getColumnIndex(COL_LAST_SMS_POSITION));
        id = cursor.getLong(cursor.getColumnIndex(COL_ID));
        cursor.close();
    }

    public void setLastPosition(long id) {
        lastPosition = id;
    }

    public Long getLastPosition() {
        return lastPosition;
    }

    public boolean isPersisted() {
        return id != null;
    }

    public void close() {
        db.close();
    }

    public void save() {
        if (lastPosition == null)
            throw new RuntimeException("Can't persist last SMS information NULL !");

        if (isPersisted())
            update();
        else
            create();
    }

    private void create() {
        final ContentValues contentValues = new ContentValues();
        contentValues.put(COL_LAST_SMS_POSITION, lastPosition);
        id = db.insert(TABLE_NAME, null, contentValues);
    }

    private void update() {
        Timber.d("Updating last SMS position to %d", lastPosition);
        final ContentValues contentValues = new ContentValues();
        contentValues.put(COL_LAST_SMS_POSITION, lastPosition);
        db.execSQL(
                String.format(
                        "UPDATE %s SET %s = %d WHERE %s = %d;",
                        TABLE_NAME,
                        COL_LAST_SMS_POSITION,
                        lastPosition,
                        COL_ID,
                        id
                )
        );
    }
}
