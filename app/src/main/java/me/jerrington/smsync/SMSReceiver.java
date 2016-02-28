package me.jerrington.smsync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import timber.log.Timber;

public class SMSReceiver extends BroadcastReceiver {
    public SMSReceiver() {
        Timber.d("Creating SMS receiver.");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("Received SMS");
        final Bundle extras = intent.getExtras();
        final String smsFormat = intent.getStringExtra("format");

        if (extras == null) {
            Timber.d("Got empty SMS bundle?");
            return;
        }

        final Object[] pdus = (Object[]) extras.get("pdus");

        if (pdus == null) {
            Timber.e("SMS message contained no PDUs?");
            return;
        }

        for (Object pdu : pdus) {
            final SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu, smsFormat);
            final Message message = new Message.MessageBuilder()
                    .withSenderNumber(smsMessage.getOriginatingAddress())
                    .withRecipientNumber("me")
                    .withMessageBody(smsMessage.getDisplayMessageBody())
                    .build();
            SMSyncService.startActionRecordSMS(context, message);
        }
    }
}
