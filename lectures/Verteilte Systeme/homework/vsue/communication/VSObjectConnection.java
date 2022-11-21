package vsue.communication;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

import vsue.faults.VSRPCSemantic;
import vsue.faults.VSRPCSemanticType;

public class VSObjectConnection {
    public VSConnection vsConnection;


    public VSObjectConnection (Socket socket) {
        vsConnection = new VSConnection(socket);
    }

    public VSObjectConnection (Socket socket, VSRPCSemanticType semantic){
        vsConnection = new VSConnection(socket, semantic);
    }

    public VSObjectConnection(Socket socket, VSRPCSemanticType semantic, boolean client, int trigger){
        vsConnection = new VSConnection(socket, semantic, client, trigger);
    }

    public void sendObject ( Serializable object ) {   
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out;
        try {
            try {
                Thread.sleep(7000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // wandle Object in byte array
            out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
            byte[] outputBytes = bos.toByteArray();

            // gebe byte array auf Konsole aus
            // for (byte aByte : outputBytes) {
            //     //System.out.print(Integer.toHexString(Byte.toUnsignedInt(aByte)) + " ");
            //     System.out.print((char) aByte + " ");
            // }

            vsConnection.sendChunk(outputBytes);

            // System.out.println("SendObject Fertig");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Serializable receiveObject () throws SocketTimeoutException {
        Object obj = null;
        try {
            // lese byte array vom VSConnection
            byte[] inputBytes = vsConnection.receiveChunk();
            ByteArrayInputStream bis = new ByteArrayInputStream(inputBytes);
            ObjectInputStream in = new ObjectInputStream(bis);
            // wandle byte array in Object
            obj = in.readObject();
        } catch (ClassNotFoundException e) {
            System.out.println("Error" + e.toString());
        } catch (SocketTimeoutException e){
            System.out.println("[ObjectConnection] SocketTimeoutException gefangen");
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (Serializable) obj;
    }


    public void close() {
        this.vsConnection.close();
    }
}
