package vsue.communication;

import java.io.*;
import java.net.Socket;

public class VSObjectConnection {
    public VSConnection vsConnection;


    public VSObjectConnection (Socket socket) {
        vsConnection = new VSConnection(socket);
    }

    public void sendObject ( Serializable object ) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out;

        // wandle Object in byte array
        out = new ObjectOutputStream(bos);
        out.writeObject(object);
        out.flush();
        byte[] outputBytes = bos.toByteArray();

        vsConnection.sendChunk(outputBytes);

    }


    public Serializable receiveObject () throws IOException, ClassNotFoundException {
        Object obj = null;

        // lese byte array vom VSConnection
        byte[] inputBytes = vsConnection.receiveChunk();
        ByteArrayInputStream bis = new ByteArrayInputStream(inputBytes);
        ObjectInputStream in = new ObjectInputStream(bis);
        // wandle byte array in Object
        obj = in.readObject();

        return (Serializable) obj;
    }


    public void close() {
        this.vsConnection.close();
    }
}
