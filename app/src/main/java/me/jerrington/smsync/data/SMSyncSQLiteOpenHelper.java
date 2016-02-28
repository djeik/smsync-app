package me.jerrington.smsync.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import timber.log.Timber;

public class SMSyncSQLiteOpenHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "smsync.db";
    public static final int DATABASE_VERSION = 2;

    public SMSyncSQLiteOpenHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqliteDatabase) {
        LastSMSDAO.createTable(sqliteDatabase);
        MessageDAO.createTable(sqliteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqliteDatabase, int i, int i1) {
        Timber.w("Upgrading database.");
        MessageDAO.dropTable(sqliteDatabase);
        LastSMSDAO.dropTable(sqliteDatabase);
        onCreate(sqliteDatabase);
    }
}
