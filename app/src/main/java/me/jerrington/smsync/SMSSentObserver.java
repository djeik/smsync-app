package me.jerrington.smsync;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;

import java.util.Date;

import me.jerrington.smsync.data.LastSMSDAO;
import me.jerrington.smsync.data.MessageDAO;
import me.jerrington.smsync.data.SMSyncSQLiteOpenHelper;
import timber.log.Timber;

public class SMSSentObserver extends ContentObserver {
    public static final Uri CONTENT_SMS = Uri.parse("content://sms");

    public static final String COL_ID = "_id";
    public static final String COL_ADDRESS = "address";
    public static final String COL_DATE = "date";
    public static final String COL_STATUS = "status";
    public static final String COL_BODY = "body";
    public static final String COL_CREATOR = "creator";
    public static final String COL_TYPE = "type";

    final Context context;
    final SMSyncSQLiteOpenHelper dbHelper;
    final SQLiteDatabase db;

    public SMSSentObserver(Context context, Handler handler) {
        super(handler);
        this.context = context;
        this.dbHelper = new SMSyncSQLiteOpenHelper(context);
        this.db = dbHelper.getWritableDatabase();
    }

    @Override
    public synchronized void onChange(boolean selfChange) {
        super.onChange(selfChange);

        Timber.d("SMS content change occurred.");

        final Cursor cursor = context.getContentResolver().query(
                CONTENT_SMS,
                null,
                "type=2", // outgoing messages only
                null,
                COL_DATE // sort by date
        );

        if (cursor == null) {
            Timber.e("SMS cursor is null!");
            return;
        }

        if (cursor.getCount() == 0) {
            Timber.d("No SMS have been sent yet, ever.");
            return;
        }

        cursor.moveToLast(); // now positioned at the last SMS that was sent

        final LastSMSDAO lastSMSDAO = new LastSMSDAO(context);
        final Long lastPosition = lastSMSDAO.getLastPosition();
        final int initialCursorPos = cursor.getPosition();


        Timber.d("Last processed sent message id %d (position %d)",
                lastPosition == null ? -1 : lastPosition,
                cursor.getPosition()
        );

        while (lastPosition == null || lastPosition != cursor.getLong(cursor.getColumnIndex(COL_ID))) {
            if (!cursor.moveToPrevious())
                break;
        }

        final int finalCursorPos = cursor.getPosition();

        if (initialCursorPos == finalCursorPos) {
            cursor.close();
            lastSMSDAO.close();
            return;
        }

        while (cursor.moveToNext()) {
            final long messageId = cursor.getLong(cursor.getColumnIndex(COL_ID));
            final String address = cursor.getString(cursor.getColumnIndex(COL_ADDRESS));
            final String body = cursor.getString(cursor.getColumnIndex(COL_BODY));
            final Date messageTime = new Date(cursor.getInt(cursor.getColumnIndex(COL_DATE)));

            final Message message = new Message("me", address, body, messageTime);
            final MessageDAO messageDAO = new MessageDAO(context, message);

            Timber.d("Persisting sent message id %d", messageId);
            messageDAO.save();
            messageDAO.close();

            lastSMSDAO.setLastPosition(messageId);
        }

        lastSMSDAO.save();
        lastSMSDAO.close();

        cursor.close();
    }

    @Override
    public boolean deliverSelfNotifications() {
        return false;
    }
}
