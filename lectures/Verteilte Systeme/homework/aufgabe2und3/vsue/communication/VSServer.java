package vsue.communication;

import vsue.faults.VSBuggyObjectConnection;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.sql.Timestamp;
import java.util.HashMap;

/**
 * VSServer verwaltet die Sockets, an denen auf Aufrufe gewartet wird
 * Pro exportiertem Objekt wird ein Thread (VSServer_ServerSocketTask) erstellt, welcher eine ServerSocket bereit stellt
 * Bei einer Verbindung an dieser ServerSocket wird ein VSServer_ObjConTask erstellt, welcher die Anfrage abarbeitet
 */
public class VSServer {
    public static HashMap<Integer, Thread> activeSockets = new HashMap<>();
    private int port = 12350; // Port Zähler, der benutzt wird einen freien Port zu finden
    public static HashMap<String, VSAnswerMessage> answerObjects = new HashMap<>();
    public static HashMap<String, Integer> sequenceNumber = new HashMap<>();
    public static HashMap<String, Timestamp> timestamps = new HashMap<>();

    public VSServer () {}

    // Erstelle neue ServerSocket und gebe den dazugehörigen Port zurück
    public synchronized int addServerSocket() {
        boolean success = false;
        ServerSocket serverSocket = null;
        while (!success) {
            try {
                serverSocket = new ServerSocket(port);
                success = true;
            } catch (IOException e) {
                port++;
            }
        }
        final Runnable  rr = new VSServer_ServerSocketTask(serverSocket);
        final Thread tt = new Thread(rr);
        activeSockets.put(port, tt);
        tt.start();
        return port;
    }

    // Löschen der ServerSocket welche an $port lauscht
    public synchronized void removePort(int port) {
        final Thread tt = activeSockets.get(port);
        tt.interrupt();
        activeSockets.remove(port);
    }

}

class VSServer_ServerSocketTask implements Runnable {
    ServerSocket serverSocket;

    public VSServer_ServerSocketTask(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        int port = serverSocket.getLocalPort();
        while (VSServer.activeSockets.containsKey(port)) {
            try {
                Socket socket = serverSocket.accept();
                final Runnable  rr = new VSServer_ObjConTask(socket);
                final Thread tt = new Thread(rr);
                tt.start();
            } catch (IOException e) {
                System.out.println("Fehler beim erstellen der Socket Verbindung mit Port: " + port);
                e.printStackTrace();
            }
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

class VSServer_ObjConTask implements Runnable {
    private Socket socket;

    VSServer_ObjConTask(Socket socket){
        System.out.println("Erstellen einer neuen Socket");
        this.socket = socket;
    }

    @Override
    public void run() {
        final VSBuggyObjectConnection objCon = new VSBuggyObjectConnection(socket);
        final VSRequestMessage dataReceivedForMethodCall;
        // TODO: catch block
        try {
            dataReceivedForMethodCall = (VSRequestMessage) objCon.receiveObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
        final VSRemoteObjectManager remoteObjectManager = VSRemoteObjectManager.getInstance();
        Object response = null;

        VSAnswerMessage answerMessage = new VSAnswerMessage();

        // TODO: synchronisation in Maps

        // Prüfe ob diese Anfrage bereits gesendet wurde, Anfrage ist eindeutig mittels callerID und SequenceNumber
        String callerID = dataReceivedForMethodCall.getCallerID();
        int sequenceNumber = dataReceivedForMethodCall.getSequenceNumber();
        if (VSServer.sequenceNumber.containsKey(callerID) && VSServer.sequenceNumber.get(callerID) == sequenceNumber) {
            // falls diese Nachricht bereits gesendet wurde, die gespeicherte Antwort zurückgeben
            answerMessage = VSServer.answerObjects.get(callerID);
            System.out.println("Selbe Nachricht wieder, wiederverwenden der Antwort");
        } else {
            // Falls aufgerufene Methode eine Exception wirft, soll diese nicht im Server geworfen werden,
            // sondern beim Client, da der Aufruf für ihn wie ein lokaler aussehen soll.
            // Dazu müssen „IllegalAccessException“, „IllegalArgumentException“ und „InvocationTargetException“
            // verschickt werden können. Ein try-catch innerhalb von „invokeMethod“ ist nicht möglich, da der
            // Rückgabetyp „Object“ ist und mir gerade keine Möglichkeit einfällt, wie man vom Datentyp „Object“
            // zurück zu „*Exception“ gelangen kann. Da alle Exceptions serialisierbar sind, entstehen hier keine
            // Probleme.
            try{
                response = remoteObjectManager.invokeMethod(
                        dataReceivedForMethodCall.getObjectID(),
                        dataReceivedForMethodCall.getGenericMethodName(),
                        dataReceivedForMethodCall.getArgs()
                );
                answerMessage.setMessage((Serializable) response);
                // catch-Block für „c.getMethod“ (Aufruf geschieht in „invokeMethod“)
                // Diese Ausnahmen sollten eigentlich nicht auftreten. Da man die Methoden a priori richtig aufruft,
                // existiert sie auch und Sicherheitsprobleme sollte es auch nicht geben, da man nur vordefinierte
                // Funktionen nutzt.
            } catch (NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
                // catch-Block für „m.invoke“ (Aufruf geschieht in „invokeMethod“)
                // Müssen an Client weitergeleitet und dort geworfen werden.
                // Weg: invokeMethod -> m.invoke() -> catch hier -> per sendObject() an InvocationHandler -> dort werfen
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                answerMessage.setExceptionStatus(true);
                answerMessage.setMessage(e);
            }

            // speichern/überschreiben der vorherigen Daten für diese callerID (impliziertes freigeben)
            VSServer.sequenceNumber.put(callerID, sequenceNumber);
            VSServer.answerObjects.put(callerID, answerMessage);
            VSServer.timestamps.put(callerID, new Timestamp(System.currentTimeMillis()));
        }

        // Senden der Antwortnachricht
        try {
            System.out.println(answerMessage.getMessage());
            objCon.sendObject(answerMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        objCon.close();
    }

    /*
    class VSServer_GarbageTask implements Runnable {
        public VSServer_GarbageTask() {}

        @Override
        public void run() {
            // TODO: synchronisation
            while(true) {
                for (Map.Entry callerID: VSServer.sequenceNumber.entrySet()) {

                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
     */
}
