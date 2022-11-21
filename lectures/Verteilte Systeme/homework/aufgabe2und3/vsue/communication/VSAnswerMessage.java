package vsue.communication;

import java.io.*;

public class VSAnswerMessage implements Serializable {
    private boolean exceptionStatus = false;
    private Serializable message;
    private int sequenceNumber = 0;

    public VSAnswerMessage() {}

    public VSAnswerMessage(Serializable message, boolean exceptionStatus) {
        this.message = message;
        this.exceptionStatus = exceptionStatus;
    }

    public boolean getExceptionStatus() {
        return exceptionStatus;
    }

    public void setExceptionStatus(boolean exceptionStatus) {
        this.exceptionStatus = exceptionStatus;
    }

    public Serializable getMessage() {
        return message;
    }

    public void setMessage(Serializable message) {
        this.message = message;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
}