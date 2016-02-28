package me.jerrington.smsync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import me.jerrington.smsync.data.ISO8601;
import me.jerrington.smsync.data.MessageDAO;
import timber.log.Timber;

public class SMSyncProtocol {
    final BufferedReader socketIn;
    final PrintWriter socketOut;
    final String smsyncKey;

    final ResponseParser responseParser;

    boolean isStarted;

    public SMSyncProtocol(final BufferedReader socketIn, final PrintWriter socketOut, final String smsyncKey) {
        this.socketIn = socketIn;
        this.socketOut = socketOut;
        this.smsyncKey = smsyncKey;

        this.responseParser = new ResponseParser();

        this.isStarted = false;
    }

    public void begin() throws ProtocolException {
        if (this.isStarted)
            throw new StateException();

        socketOut.printf("SMS\n\n");
        nextResponse().expect(StatusCode.HELLO);
        Timber.d("Got first hello.");
        socketOut.printf("Key %s\n\n", smsyncKey);
        nextResponse().expect(StatusCode.HELLO);
        Timber.d("Got second hello.");

        this.isStarted = true;
    }

    public void sendMessage(final MessageDAO messageDAO) throws ProtocolException {
        if (!isStarted)
            throw new StateException();

        Timber.d("Sending message header.");
        socketOut.printf("Id %d\n", messageDAO.getId());
        socketOut.printf("Time %s\n", ISO8601.FORMAT.format(messageDAO.getMessage().getMessageTime()));
        socketOut.printf("From %s\n", messageDAO.getMessage().getSenderNumber());
        socketOut.printf("To %s\n", messageDAO.getMessage().getRecipientNumber());
        socketOut.printf("Length %d\n\n", messageDAO.getMessage().getMessageBody().length());
        final Response response = nextResponse();
        Timber.d("Got response.");

        if (response.getStatusCode() == StatusCode.NO_THANKS) {
            // no op
        } else if (response.getStatusCode() == StatusCode.GO_AHEAD) {
            socketOut.printf("%s\n\n", messageDAO.getMessage().getMessageBody());
            nextResponse().expect(StatusCode.OK);
        } else {
            throw new UnexpectedResponseException(response.getStatusCode(), StatusCode.GO_AHEAD);
        }
    }

    public enum StatusCode {
        OK,
        HELLO,
        BAD_KEY,
        BYE,
        NO_THANKS,
        GO_AHEAD,
    }

    Response nextResponse() throws ProtocolException {
        try {
            return responseParser.parse(socketIn.readLine());
        } catch (IOException e) {
            throw new ProtocolException();
        }
    }

    public class Response {
        final StatusCode statusCode;

        Response(final StatusCode statusCode) {
            this.statusCode = statusCode;
        }

        public void expect(final StatusCode status) throws UnexpectedResponseException {
            if (statusCode != status) {
                throw new UnexpectedResponseException(statusCode, status);
            }
        }

        public StatusCode getStatusCode() {
            return statusCode;
        }
    }

    public class ResponseParser {
        public Response parse(final String rawResponse) throws ResponseParseException {
            if ("Hello".equals(rawResponse)) {
                return new Response(StatusCode.HELLO);
            }
            if ("Ok".equals(rawResponse)) {
                return new Response(StatusCode.OK);
            }
            if ("BadKey".equals(rawResponse)) {
                return new Response(StatusCode.BAD_KEY);
            }
            if ("Bye".equals(rawResponse)) {
                return new Response(StatusCode.BYE);
            }
            if ("NoThanks".equals(rawResponse)) {
                return new Response(StatusCode.NO_THANKS);
            }
            if ("GoAhead".equals(rawResponse)) {
                return new Response(StatusCode.GO_AHEAD);
            }

            throw new ResponseParseException();
        }
    }

    /**
     * When a protocol exception is thrown, the protocol must begin again from the start.
     */
    public class ProtocolException extends Exception {

    }

    public class UnexpectedResponseException extends ProtocolException {
        public UnexpectedResponseException(StatusCode actual, StatusCode expected) {

        }
    }

    public class ResponseParseException extends ProtocolException {
    }

    public class StateException extends ProtocolException {

    }
}
