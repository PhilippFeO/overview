package vsue.communication;

import java.io.IOException;
import java.net.Socket;

public class VSClient {

    public static void main(String[] args){
        // Lese ip und port aus parametern
        final String SERVER_ADDRESS = args[0];
        final int port = Integer.parseInt(args[1]);
        System.out.println("Port: " + port);
        try {
            // erstelle Socket zur verbindung zum server
            Socket socket = new Socket(SERVER_ADDRESS, port);
            final VSObjectConnection objCon = new VSObjectConnection(socket);
            final VSTestMessage a = new VSTestMessage(0, null, null);
            objCon.sendObject(a);
            System.out.println("Gesendet, jetzt empfangen");
            final VSTestMessage r = (VSTestMessage) objCon.receiveObject();
            System.out.println("Received message");
            objCon.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.toString());
        }
    }
}
