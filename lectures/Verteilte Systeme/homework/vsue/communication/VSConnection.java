package vsue.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import vsue.faults.VSRPCSemanticType;



public class VSConnection {
    Socket socket;
    OutputStream out;
    InputStream in;
    VSRPCSemanticType semantic;
    boolean client = false;
    int trigger = 0;

    public VSConnection (Socket socket) {
        this.socket = socket;
    }

    public VSConnection (Socket socket, VSRPCSemanticType semantic){
        this.socket = socket;
        this.semantic = semantic;
    }

    public VSConnection(Socket socket, VSRPCSemanticType semantic, boolean client, int trigger){
        this.socket = socket;
        this.semantic = semantic;
        this.client = client;
        this.trigger = trigger;
    }


    public void sendChunk (byte[] chunk) throws IOException {
        // Lade OutputStream
        out = this.socket.getOutputStream();
        // Berechne Nachrichtenlänge und konvertiere zu byte Array
        byte[] messageLength = ByteBuffer.allocate(Integer.BYTES).putInt(chunk.length).array();
        
        // try {
        //     Thread.sleep(10000);
        // } catch (InterruptedException e) {
        //     e.printStackTrace();
        // }

        // Sende erst Nachrichtenlänge und anschließend Nachricht
        
        //out.write(messageLength);
// Pause, um Verzögerung zu provozieren. Funktioniert aber nur genau 1x
        if(trigger == 0 && client){
            System.out.println("[Connection] trigger = " + trigger + "   client = " + client);
            try {
                Thread.sleep(4100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        trigger++;
        //out.write(chunk);

        byte[] both = Arrays.copyOf(messageLength, messageLength.length + chunk.length);
        System.arraycopy(chunk, 0, both, messageLength.length, chunk.length);
        out.write(both);
        
        // System.out.println("Nachricht gesendet");
    }

    // „SocketTimeoutException“ wird nur geworfen, falls der im Konstruktor übergebene Socket so konfiguriert wurde.
    // An sich ist aber keine Extrabehandlung notwendig.
    public byte [] receiveChunk () throws IOException, SocketTimeoutException {
        System.out.println("[Connection] Socket-Zeitlimit: " + socket.getSoTimeout());
        // Lade InputStream
        in = this.socket.getInputStream();
        // Das erste Byte, das gesendet wird, gibt immer die Anzahl der Bytes an, die gesendet werden
        byte[] receivedBytes = null;
        try{
            // while (in.available() == 0) {}
            final byte[] messageLengthBytes = in.readNBytes(Integer.BYTES);
            System.out.println("[Connection] Länge gelesen");
            // wandle bytes in int und lese <messageLength> bytes aus dem Stream
            final int messageLength = ByteBuffer.wrap(messageLengthBytes).getInt();
            // System.out.println("[Connection] messageLength = " + messageLength);
            receivedBytes = in.readNBytes(messageLength);
            System.out.println("[Connection] Inhalt gelesen");
        } catch(SocketTimeoutException e){
            System.out.println("[Connection] SocketTimeoutException gefangen");
            throw e;
        }
        // System.out.print(String.format("Gelesene Bytes: %d\n", messageLength));
        return receivedBytes;
    }

    public void close() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Error closing socket: " + e.toString());
        }

    }
}
