package me.jerrington.smsync;

import me.jerrington.smsync.data.MessageDAO;
import timber.log.Timber;

public class DummySMSUploader implements SMSUploader {

    @Override
    public void uploadMessage(MessageDAO message) {
        Timber.d("Dummy upload of a single message.");
    }

    @Override
    public void uploadAll(MessageDAO.MessageCursor messages) {
        Timber.d("Dummy batch message upload.");
    }

    @Override
    public boolean isAlive() {
        return false;
    }
}
