package vsue.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;


public class VSConnection {
    Socket socket;
    OutputStream out;
    InputStream in;

    public VSConnection (Socket socket) {
        this.socket = socket;
    }


    public void sendChunk (byte[] chunk) throws IOException {
        // Lade OutputStream
        out = this.socket.getOutputStream();
        // Berechne Nachrichtenlänge und konvertiere zu byte Array
        byte[] messageLength = ByteBuffer.allocate(Integer.BYTES).putInt(chunk.length).array();
        // Sende erst Nachrichtenlänge und anschließend Nachricht
        out.write(messageLength);
        out.write(chunk);
    }

    public byte [] receiveChunk () throws IOException {
        try {
            // Lade InputStream
            in = this.socket.getInputStream();

            // Das erste Byte, das gesendet wird, gibt immer die Anzahl der Bytes an, die gesendet werden
            final byte[] messageLengthBytes = in.readNBytes(Integer.BYTES);
            // wandle bytes in int und lese <messageLength> bytes aus dem Stream
            final int messageLength = ByteBuffer.wrap(messageLengthBytes).getInt();
            return in.readNBytes(messageLength);
        } catch (BufferUnderflowException e) {
            throw new IOException(e);
        }
    }

    public void close() {
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            socket.close();
        } catch (IOException e) {
            System.out.println("Error closing socket: " + e.toString());
        }

    }
}
