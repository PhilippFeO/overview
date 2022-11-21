package vsue.communication;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class MyData implements Externalizable {
    private int objectID;
    private String genericMethodName;
    private Object[] args;
    // Wird nicht im Konstruktor initial gesetzt, da man sie für at-least-once nicht benötigt
    private int id = 0;

    public MyData() {}

    public MyData(int objectID, String genericMethodName, Object[] args) {
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

    // Wird nicht im Konstruktor initial gesetzt, da man sie für at-least-once nicht benötigt
    public void setId(final int id){
        this.id = id;
    }

    public int getId(){
        return this.id;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        objectID = in.readInt();
        genericMethodName = (String) in.readObject();
        args = (Object[]) in.readObject();
        id = in.readInt();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(this.objectID);
        out.writeObject(this.genericMethodName);
        out.writeObject(this.args);
        out.writeInt(this.id);
    }

}
