package vsue.communication;

import java.io.*;

public class VSAnswerMessage implements Serializable {
    private boolean exceptionStatus = false;
    private Serializable message;
    private int id;

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

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String toString(){
        return String.format("ID: %d\nException: %b\nMessage-Typ: %s",
            this.id,
            this.exceptionStatus,
            message!=null ? this.message.toString() : "NULL");
    }
    
}