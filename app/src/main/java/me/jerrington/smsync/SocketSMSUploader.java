package me.jerrington.smsync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import me.jerrington.smsync.data.MessageDAO;
import timber.log.Timber;

public class SocketSMSUploader implements SMSUploader {
    public static final String REMOTE_HOST = "10.0.0.25";
    public static final int REMOTE_PORT = 7777;

    Socket socket;
    SMSyncProtocol protocol;
    boolean isAlive;

    public SocketSMSUploader() throws IOException {
        socket = new Socket(REMOTE_HOST, REMOTE_PORT);
        protocol = new SMSyncProtocol(
                new BufferedReader(new InputStreamReader(socket.getInputStream())),
                new PrintWriter(socket.getOutputStream(), true),
                "XXX"
        );

        isAlive = true;

        try {
            protocol.begin();
            Timber.e("Failed to begin protocol.");
        } catch (SMSyncProtocol.ProtocolException e) {
            isAlive = false;
        }
    }

    @Override
    public void uploadMessage(final MessageDAO message) {
        Timber.d("Uploading a single SMS over the socket.");
        try {
            protocol.sendMessage(message);
            message.delete();
        } catch (SMSyncProtocol.ProtocolException e) {
            Timber.e("Protocol error occurred in message upload.");
            isAlive = false;
        }
    }

    @Override
    public void uploadAll(final MessageDAO.MessageCursor messages) {
        Timber.d("Batch socket SMS upload.");
        final List<MessageDAO> uploadedMessages = new ArrayList<MessageDAO>();

        do {
            final MessageDAO messageDAO = messages.getMessage();

            try {
                protocol.sendMessage(messageDAO);
            } catch (SMSyncProtocol.ProtocolException e) {
                Timber.e("Protocol error occurred in batch upload.");
                isAlive = false;
                break;
            }

            uploadedMessages.add(messageDAO);
        } while (messages.moveToNext());

        // We can delete any messages that were successfully uploaded.
        for (final MessageDAO messageDAO : uploadedMessages) {
            messageDAO.delete();
            messageDAO.close();
        }
    }

    @Override
    public boolean isAlive() {
        return isAlive;
    }
}
