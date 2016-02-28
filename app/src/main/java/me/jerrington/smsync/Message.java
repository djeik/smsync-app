package me.jerrington.smsync;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
    final String messageBody;
    final String senderNumber;
    final String recipientNumber;
    final Date messageTime;

    public Message(final String senderNumber, final String recipientNumber, final String messageBody) {
        this.messageBody = messageBody;
        this.recipientNumber = recipientNumber;
        this.senderNumber = senderNumber;
        this.messageTime = new Date();
    }

    public Message(final String senderNumber, final String recipientNumber, final String messageBody, final Date messageTime) {
        this.messageBody = messageBody;
        this.recipientNumber = recipientNumber;
        this.senderNumber = senderNumber;
        this.messageTime = messageTime;
    }

    public String getMessageBody() {
        return this.messageBody;
    }

    public String getSenderNumber() {
        return this.senderNumber;
    }

    public String getRecipientNumber() {
        return this.recipientNumber;
    }

    public Date getMessageTime() {
        return this.messageTime;
    }

    public static class MessageBuilder {
        String messageBody, senderNumber, recipientNumber;
        Date messageTime;

        public MessageBuilder withMessageBody(final String messageBody) {
            this.messageBody = messageBody;
            return this;
        }

        public MessageBuilder withRecipientNumber(final String recipientNumber) {
            this.recipientNumber = recipientNumber;
            return this;
        }

        public MessageBuilder withSenderNumber(final String senderNumber) {
            this.senderNumber = senderNumber;
            return this;
        }

        public MessageBuilder withMessageTime(final Date messageTime) {
            this.messageTime = messageTime;
            return this;
        }

        public Message build() {
            if (messageBody == null)
                throw new IncompleteObjectException(Message.class, "messageBody");
            if (senderNumber == null)
                throw new IncompleteObjectException(Message.class, "senderNumber");
            if (recipientNumber == null)
                throw new IncompleteObjectException(Message.class, "recipientNumber");

            if (messageTime == null)
                return new Message(senderNumber, recipientNumber, messageBody);
            else
                return new Message(senderNumber, recipientNumber, messageBody, messageTime);
        }
    }
}
