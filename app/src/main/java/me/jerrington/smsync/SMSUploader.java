package me.jerrington.smsync;

import me.jerrington.smsync.data.MessageDAO;

public interface SMSUploader {
    void uploadMessage(MessageDAO message);

    void uploadAll(MessageDAO.MessageCursor messages);

    boolean isAlive();
}
