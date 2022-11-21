package vsue.communication;

import java.io.*;
import java.net.Socket;

public class VSObjectConnection {
    public VSConnection vsConnection;


    public VSObjectConnection (Socket socket) {
        vsConnection = new VSConnection(socket);
    }

    // TODO: error handling

    public void sendObject ( Serializable object ) {   
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out;
        try {
            // wandle Object in byte array
            out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
            byte[] outputBytes = bos.toByteArray();

            // gebe byte array auf Konsole aus
            for (byte aByte : outputBytes) {
                System.out.print(Integer.toHexString(Byte.toUnsignedInt(aByte)) + " ");
            }

            System.out.println();

            vsConnection.sendChunk(outputBytes);

            System.out.println("SendObject Fertig");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Serializable receiveObject () {
        Object obj = null;
        try {
            // lese byte array vom VSConnection
            byte[] inputBytes = vsConnection.receiveChunk();
            ByteArrayInputStream bis = new ByteArrayInputStream(inputBytes);
            ObjectInputStream in = new ObjectInputStream(bis);
            // wandle byte array in Object
            obj = in.readObject();

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error" + e.toString());

        }
        return (Serializable) obj;
    }


    public void close() {
        this.vsConnection.close();
    }
}
