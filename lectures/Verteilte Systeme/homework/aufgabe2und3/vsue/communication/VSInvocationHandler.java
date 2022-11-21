package vsue.communication;

import vsue.faults.VSBuggyObjectConnection;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

public class VSInvocationHandler implements InvocationHandler, Serializable {
    private VSRemoteReference object;
    private String uuid;
    private int sequenceNumber = 0;
    private final int TRIES = 5;
    private final int SOCKET_TIMEOUT = 3000;

    public VSInvocationHandler(VSRemoteReference remoteReference) {
        // remoteReference kennzeichnet das repräsentierte entfernte Objekt eindeutig
        this.object = remoteReference;
        this.uuid = UUID.randomUUID().toString();
    }

    // führt die Umwandlung des lokalen Aufrufs in einen Fernaufruf durch
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // erstellt dafür für jeden Aufruf eine Verbindung zum Server,
        // Lese ip und port aus parametern
        final String SERVER_ADDRESS = this.object.getName();
        final int SERVER_PORT = this.object.getPort();

        // Testen ob in args ein exportiertes Objekt ist und ersetze dieses
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                Remote temp = VSRemoteObjectManager.getInstance().getExportedObject(args[i]);
                if (temp != null) {
                    args[i] = temp;
                }
            }
        }

        VSObjectConnection objCon = null;
        VSAnswerMessage receivingData = null;
        Serializable message = null;

        try {
            int counter = 0;
            while (counter < TRIES) {
                try {
                    System.out.println("Versuch: " + counter);

                    // Verbindungsaufbau zu Server
                    Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                    socket.setSoTimeout(SOCKET_TIMEOUT);
                    objCon = new VSObjectConnection(socket);

                    // generiert eine passende Anfrage (Marshalling der Aufrufparameter)
                    final VSRequestMessage sendingData = new VSRequestMessage(this.object.getObjectID(), method.toGenericString(), args);
                    sendingData.setCallerID(uuid);
                    sendingData.setSequenceNumber(sequenceNumber);

                    // sendet die Anfrage (sendObject serialisiert sendingData)
                    objCon.sendObject(sendingData);

                    // empfängt Antwort und receiveObject Unmarshallt den Rückgabewert; erwarte Rückgabewert oder Error
                    receivingData = (VSAnswerMessage) objCon.receiveObject();
                    message = receivingData.getMessage();
                    break;

                } catch (IOException e) {
                    counter++;
                }
            }

            // erhöhe Sequenz Nummer
            if (sequenceNumber == Integer.MAX_VALUE) {
                sequenceNumber = 0;
            } else {
                sequenceNumber++;
            }

            if (receivingData == null) {
                throw new RemoteException();
            }

            // Prüfen ob am Server eine Exception generiert wurde, falls ja, je nach Exception Typ werfen oder RemoteException erstellen
            if (receivingData.getExceptionStatus()) {
                if(message instanceof IllegalAccessException || message instanceof IllegalArgumentException){
                    throw new RemoteException("New RemoteException", (Throwable) message);
                }
                else if(message instanceof InvocationTargetException){
                    throw ((InvocationTargetException)message).getTargetException();
                }
                throw (Throwable) message;
            }

            // schließt die Verbindung zum Server
            objCon.close();
            return message;
        } catch (IOException e) {
            sequenceNumber++;
            e.printStackTrace();
            throw new RemoteException();
        }
    }
}
