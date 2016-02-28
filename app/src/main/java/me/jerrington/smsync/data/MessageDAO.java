package me.jerrington.smsync.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.util.Date;

import me.jerrington.smsync.Message;
import me.jerrington.smsync.util.UncheckedThrow;
import timber.log.Timber;

public class MessageDAO {

    public static final String TABLE_NAME = "message_queue";

    public static final String COL_ID = "_id";
    public static final String COL_SENDER_NUMBER = "sender_number";
    public static final String COL_RECIPIENT_NUMBER = "recipient_number";
    public static final String COL_MESSAGE_BODY = "message_body";
    public static final String COL_MESSAGE_TIME = "message_time";

    final Message message;
    final SQLiteDatabase db;
    final SMSyncSQLiteOpenHelper dbHelper;
    final Context context;

    Long id;

    public static void createTable(final SQLiteDatabase db) {
        db.execSQL(
                String.format(
                        "CREATE TABLE %s ( " +
                                "%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "%s TEXT NOT NULL, " +
                                "%s TEXT NOT NULL, " +
                                "%s TEXT NOT NULL, " +
                                "%s TEXT NOT NULL " +
                                ");",
                        TABLE_NAME,
                        COL_ID,
                        COL_SENDER_NUMBER,
                        COL_RECIPIENT_NUMBER,
                        COL_MESSAGE_BODY,
                        COL_MESSAGE_TIME
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

    public static MessageCursor getAllMessages(final Context context) {
        final SQLiteDatabase db = new SMSyncSQLiteOpenHelper(context).getReadableDatabase();
        final MessageCursor messages = new MessageCursor(
                context,
                db.rawQuery(
                        String.format(
                                "SELECT %s, %s, %s, %s, %s FROM %s;",
                                COL_ID,
                                COL_SENDER_NUMBER,
                                COL_RECIPIENT_NUMBER,
                                COL_MESSAGE_BODY,
                                COL_MESSAGE_TIME,
                                TABLE_NAME
                        ),
                        new String[]{}
                )
        );
        Timber.d("Got all %d pending messages.", messages.getCount());
        return messages;
    }

    public MessageDAO(final Context context, final Message message) {
        this.message = message;
        this.context = context;
        this.dbHelper = new SMSyncSQLiteOpenHelper(context);
        this.db = dbHelper.getWritableDatabase();
        this.id = null;
    }

    public MessageDAO(final Context context, final long id) {
        this.context = context;
        this.dbHelper = new SMSyncSQLiteOpenHelper(context);
        this.db = dbHelper.getWritableDatabase();

        final Cursor cursor = db.rawQuery(
                String.format(
                        "SELECT (%s, %s, %s, %s) FROM %s WHERE %s = ?;",
                        COL_SENDER_NUMBER, // 1
                        COL_RECIPIENT_NUMBER, // 2
                        COL_MESSAGE_BODY, // 3
                        COL_MESSAGE_TIME, // 4
                        TABLE_NAME,
                        COL_ID
                ),
                new String[]{String.valueOf(id)}
        );

        cursor.moveToFirst();

        try {
            final String senderNumber = cursor.getString(1);
            final String recipientNumber = cursor.getString(2);
            final String messageBody = cursor.getString(3);
            final Date messageTime = ISO8601.FORMAT.parse(cursor.getString(4));

            this.message = new Message.MessageBuilder()
                    .withMessageBody(messageBody)
                    .withMessageTime(messageTime)
                    .withRecipientNumber(recipientNumber)
                    .withSenderNumber(senderNumber)
                    .build();
        } catch (ParseException e) {
            throw UncheckedThrow.throwUnchecked(e);
        }

        cursor.close();

        this.id = id;
    }

    public boolean isPersisted() {
        return this.id != null;
    }

    public long getId() {
        if (!isPersisted())
            throw new PersistenceStateException();
        return id;
    }

    public void close() {
        db.close();
    }

    public Message getMessage() {
        return message;
    }

    public synchronized void delete() {
        if (!isPersisted())
            throw new PersistenceStateException();

        db.execSQL(
                String.format(
                        "DELETE FROM %s WHERE %s = ?;",
                        TABLE_NAME,
                        COL_ID
                ),
                new Object[]{this.id}
        );

        this.id = null;
    }

    public synchronized void save() {
        if (isPersisted())
            update();
        else
            create();
    }

    private synchronized void update() {
        Timber.d("Message is already persisted in the backing store. Updating...");
        throw new RuntimeException("update not supported yet");
    }

    private synchronized void create() {
        final ContentValues contentValues = new ContentValues();

        contentValues.put(COL_MESSAGE_BODY, message.getMessageBody());
        contentValues.put(COL_MESSAGE_TIME, ISO8601.FORMAT.format(message.getMessageTime()));
        contentValues.put(COL_RECIPIENT_NUMBER, message.getRecipientNumber());
        contentValues.put(COL_SENDER_NUMBER, message.getSenderNumber());

        id = db.insert(TABLE_NAME, null, contentValues);
    }

    public static class MessageCursor extends CursorWrapper {
        final Context context;

        public MessageCursor(final Context context, final Cursor cursor) {
            super(cursor);
            this.context = context;
        }

        public MessageDAO getMessage() {
            final Cursor cursor = getWrappedCursor();

            final long id = cursor.getLong(cursor.getColumnIndex(COL_ID));
            final String senderNumber = cursor.getString(cursor.getColumnIndex(COL_SENDER_NUMBER));
            final String recipientNumber = cursor.getString(cursor.getColumnIndex(COL_RECIPIENT_NUMBER));
            final String messageBody = cursor.getString(cursor.getColumnIndex(COL_MESSAGE_BODY));

            try {
                final Date messageTime = ISO8601.FORMAT.parse(cursor.getString(cursor.getColumnIndex(COL_MESSAGE_TIME)));

                final Message message = new Message.MessageBuilder()
                        .withMessageBody(messageBody)
                        .withMessageTime(messageTime)
                        .withRecipientNumber(recipientNumber)
                        .withSenderNumber(senderNumber)
                        .build();

                final MessageDAO messageDAO = new MessageDAO(context, message);
                messageDAO.id = id;

                return messageDAO;
            } catch (ParseException e) {
                throw UncheckedThrow.throwUnchecked(e);
            }
        }
    }
}
