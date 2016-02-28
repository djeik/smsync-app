package me.jerrington.smsync;

import java.io.IOException;

import timber.log.Timber;

public class SMSUploadManager {

    static SMSUploader instance;

    public static SMSUploader getInstance() {
        if (instance == null || !instance.isAlive())
            instance = createInstance();

        return instance;
    }

    static SMSUploader createInstance() {
        try {
            return new SocketSMSUploader();
        } catch (IOException e) {
            Timber.e("Failed to connect to SMS upload remote: %s", e.toString());
            Timber.e("Using dummy instance.");
            return new DummySMSUploader();
        }
    }

    SMSUploadManager() {
        // private so that nobody can instantiate these
    }
}
