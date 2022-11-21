package vsue.communication;

import java.io.*;

public class VSTestMessage implements Externalizable {
    private int integer;
    private String string;
    private Object[] objects;

    public VSTestMessage(int integer, String string, Object[] objects) {
        this.integer = integer;
        this.string = string;
        this.objects = objects;
    }

    public VSTestMessage(){}

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(integer);
        out.writeObject(this.string);
        out.writeObject(objects);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.integer = in.readInt();
        this.string = (String) in.readObject();
        this.objects = (Object[]) in.readObject();
    }
}
