package vsue.communication;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class VSRequestMessage implements Externalizable {
    private int objectID;
    private String genericMethodName;
    private Object[] args;
    private int sequenceNumber = 0;
    private String callerID = "";

    public VSRequestMessage() {}

    public VSRequestMessage(int objectID, String genericMethodName, Object[] args) {
            this.objectID = objectID;
            this.genericMethodName = genericMethodName;
            this.args = args;
    }

    public int getObjectID() {
        return this.objectID;
    }

    public String getGenericMethodName() {
        return this.genericMethodName;
    }

    public Object[] getArgs() {
        return this.args;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getCallerID() {
        return callerID;
    }

    public void setCallerID(String callerID) {
        this.callerID = callerID;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        objectID = in.readInt();
        genericMethodName = (String) in.readObject();
        args = (Object[]) in.readObject();
        sequenceNumber = in.readInt();
        callerID = (String) in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(this.objectID);
        out.writeObject(this.genericMethodName);
        out.writeObject(this.args);
        out.writeInt(this.sequenceNumber);
        out.writeObject(this.callerID);
    }

}
