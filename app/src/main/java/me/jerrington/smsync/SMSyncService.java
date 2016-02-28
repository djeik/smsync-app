package me.jerrington.smsync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import me.jerrington.smsync.data.MessageDAO;
import timber.log.Timber;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class SMSyncService extends IntentService {
    public static final String ACTION_RECORD_SMS = "me.jerrington.smsync.action.RECORD_SMS";
    public static final String ACTION_PUSH_SMS = "me.jerrington.smsync.action.PUSH_SMS";

    private static final String EXTRA_MESSAGE = "me.jerrington.smsync.extra.MESSAGE";

    public SMSyncService() {
        super("SMSyncService");
    }

    /**
     * Starts this service to record an SMS. If the service is already started, then the request to
     * record the SMS is queued.
     *
     * @see IntentService
     */
    public static void startActionRecordSMS(final Context context, final Message message) {
        Intent intent = new Intent(context, SMSyncService.class);
        intent.setAction(ACTION_RECORD_SMS);

        try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            final ObjectOutputStream ois = new ObjectOutputStream(new BufferedOutputStream(byteArrayOutputStream));
            ois.writeObject(message);
            ois.close();
            intent.putExtra(EXTRA_MESSAGE, byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            Timber.e("Failed to write message to intent.");
        }

        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.d("Received intent.");
        if (intent != null) {
            final String action = intent.getAction();

            if (ACTION_RECORD_SMS.equals(action)) {
                Timber.d("Matched intent to a request to record a message.");

                final byte[] messageByteArray = intent.getByteArrayExtra(EXTRA_MESSAGE);
                Message message;

                try (final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(messageByteArray)) {
                    final ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(byteArrayInputStream));
                    message = (Message) ois.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    Timber.e("Failed to deserialize message object.");
                    return;
                }

                handleActionRecordSMS(message);
            } else if (ACTION_PUSH_SMS.equals(action)) {
                Timber.d("Matched intent to a request to push SMS.");

                handleActionPushSMS();
            } else {
                Timber.d("Unhandled intent action received.");
            }
        }
    }

    private void handleActionPushSMS() {
        Timber.d("Pushing SMS to the remote.");
        final SMSUploader smsUploader = SMSUploadManager.getInstance();
        Timber.d("Got SMSUploader instance.");
        final MessageDAO.MessageCursor messages = MessageDAO.getAllMessages(getApplicationContext());
        Timber.d("Got all messages.");
        messages.moveToFirst();
        smsUploader.uploadAll(messages);
        Timber.d("All messages uploaded.");
    }


    /**
     * Persists the given message to the SQLite database that acts as a queue.
     *
     * @param message The message to persist.
     */
    private void handleActionRecordSMS(final Message message) {
        final MessageDAO messageDAO = new MessageDAO(getApplicationContext(), message);
        messageDAO.save();
        handleActionPushSMS();
    }
}
