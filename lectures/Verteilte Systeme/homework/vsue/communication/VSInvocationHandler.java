package vsue.communication;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

import vsue.faults.VSRPCSemantic;
import vsue.faults.VSRPCSemanticType;

public class VSInvocationHandler implements InvocationHandler, Serializable {
    private VSRemoteReference object;
    private Socket socket;
    VSObjectConnection objCon;
    final int VERSUCHE = 3;

    public VSInvocationHandler(VSRemoteReference remoteReference) {
        // remoteReference kennzeichnet das repräsentierte entfernte Objekt eindeutig
        this.object = remoteReference;
    }

    // führt die Umwandlung des lokalen Aufrufs in einen Fernaufruf durch
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // if(method.getName() == "getAuctions"){
        //     System.out.println(method.getName() + ": " + method.getAnnotation(VSRPCSemantic.class).value());
        // }
        // erstellt dafür für jeden Aufruf eine Verbindung zum Server,
        // Lese ip und port aus parametern
        final String SERVER_ADDRESS = this.object.getName();
        final int SERVER_PORT = this.object.getPort();

        // Testen ob in args ein exportiertes Objekt ist und ersetze dieses
        // Wichtig für Rückrufe, sonst funktioniert es nicht
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                Remote temp = VSRemoteObjectManager.getInstance().getExportedObject(args[i]);
                if (temp != null) {
                    args[i] = temp;
                }
            }
        }


        try {
            if (socket == null) {
                // Verbindungsaufbau zu Server
                socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                socket.setTcpNoDelay(true);
                // objCon = new VSObjectConnection(socket);
            }

            // generiert eine passende Anfrage (Marshalling der Aufrufparameter)
            final MyData sendingData = new MyData(this.object.getObjectID(), method.toGenericString(), args);
            

            VSRPCSemanticType semantic = null;
            if(method.getName() == "getAuctions" || method.getName() == "registerAuction"){
                semantic = method.getAnnotation(VSRPCSemantic.class).value();
            }
            VSAnswerMessage receivingData = null;
            Serializable message = null;
            
    /* LAST_OF_MANY */
            if(semantic == VSRPCSemanticType.LAST_OF_MANY){
                final int TIMEOUT = 4000;
                System.out.println("[InvocationHandler] aufgerufene Methode: " + method.getName());
// TODO der Verbindung Semantik mitgeben, bei at-least-once soll nicht gewartet werden!
                int counter = 0;
                while(counter < VERSUCHE){
                    try {
                        if (socket == null) {
                            // Verbindungsaufbau zu Server
                            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                            socket.setTcpNoDelay(true);
                        }
                    } catch (IOException e){
                        System.out.println("[InvocationHandler] IOException");
                    }
                    System.out.println("[InvocationHandler] =========================");
                    System.out.println("[InvocationHandler] Versuch: " + counter);
                    socket.setSoTimeout(TIMEOUT);
                    objCon = new VSObjectConnection(socket, semantic, true, counter);
                    // Abschicken
                    objCon.sendObject(sendingData);
                    while(true){
                        try{
                            System.out.println("[InvocationHandler] receive...(" + counter + ")");
                            final long START = System.nanoTime();
                            receivingData = (VSAnswerMessage) objCon.receiveObject();
                            final long END = System.nanoTime();
                            final long DIFF = TimeUnit.NANOSECONDS.toMillis(END - START);  // Differenz in Milliseckunden
                            System.out.println("[InvocationHandler] DIFF = " + DIFF);
                            System.out.println("[InvocationHandler] Versuch: " + counter + "   recId: " + receivingData.getId());                            
                            /* Antwort-Id prüfen */
                            if(receivingData.getId() == sendingData.getId()){
                            // Antwort-Ids passen zusammen => normal weiter (wenn nicht, s. „implizites else“)                          
                                message = receivingData.getMessage();
                                counter = VERSUCHE; // Abbruchbedingung für äußere while-Schleife (wird zwar trotzdem noch einnmal hochgezählt aber das ist egal)
                                break;
                            } else {
                                receivingData = null;
                                // read-Aufruf für die restliche Zeit starten
                                // try {
                                //     if (socket == null) {
                                //         // Verbindungsaufbau zu Server
                                //         socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                                //         socket.setTcpNoDelay(true);
                                //     }
                                // } catch (IOException e){
                                //     System.out.println("[InvocationHandler] IOException");
                                // }
                                // socket.setSoTimeout(TIMEOUT-(int)DIFF);
                                // objCon = new VSObjectConnection(socket, semantic, true, counter);
                                continue;
                            }
                        } catch (SocketTimeoutException e){
                            System.out.println("[InvocationHandler] SocketTimeoutException gefangen");
                            break;
                        }
                    }
                    // Zähler für die nächste Anfrage erhöhen
                    ++counter;
                    sendingData.setId(counter);
                }
    /* AT_MOST_ONCE */
            } else if(semantic == VSRPCSemanticType.AT_MOST_ONCE) {
                // TODO Jannik
                return null;
            } else {
                objCon = new VSObjectConnection(socket);
                objCon.sendObject(sendingData);
                // empfängt Antwort und receiveObject Unmarshallt den Rückgabewert; erwarte Rückgabewert oder Error
                receivingData = (VSAnswerMessage) objCon.receiveObject();
                message = receivingData.getMessage();
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
                // objCon.close();
                return message;
            }
        } catch (IOException e) {
            throw new RemoteException();
        }
        return -1;
    }
}
