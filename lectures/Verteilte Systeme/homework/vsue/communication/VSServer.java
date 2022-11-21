package vsue.communication;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.HashMap;

/**
 * VSServer verwaltet die Sockets, an denen auf Aufrufe gewartet wird
 * Pro exportiertem Objekt wird ein Thread (VSServer_ServerSocketTask) erstellt, welcher eine ServerSocket bereit stellt
 * Bei einer Verbindung an dieser ServerSocket wird ein VSServer_ObjConTask erstellt, welcher die Anfrage abarbeitet
 */
public class VSServer {
    public static HashMap<Integer, Thread> activeSockets = new HashMap<>();
    private int port = 12350; // Port Zähler, der benutzt wird einen freien Port zu finden

    public VSServer () {}

    // Erstelle neue ServerSocket und gebe den dazugehörigen Port zurück
    // Jedes exportierte Objekt benötigt einen Port unter dem es erreichbar ist.
    // Dieser wird mit dieser Methode gesucht.
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
        // Für exportiertes Objekt wurde ein Port gefunden => Starte Thread, der an diesem Port lauscht
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

// Thread, der gestartet wird, wenn ein Objekt exportiert wurde. Dieser lauscht an Port
// und startet für jede eingehende Anfrage einen Arbeiter-Thread
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
                socket.setTcpNoDelay(true);
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

// Arbeiter-Thread, der die Anfrage bearbeitet
class VSServer_ObjConTask implements Runnable {
    private Socket socket;

    VSServer_ObjConTask(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        while (true) {
            // Endpunkt für in „invoke“ des VSInvH eröffnete Verbindung
            final VSObjectConnection objCon = new VSObjectConnection(socket);
            MyData dataReceivedForMethodCall = null;
            try {
                dataReceivedForMethodCall = (MyData) objCon.receiveObject();
            } catch (SocketTimeoutException e1) {
                System.out.println("[Server] SocketTimeoutException");
                /*  Wird an dieser Stelle nie geworfen werden, da der Socket auf der anderen Seite
                    mit einem Timeout konfiguriert wurde. Da VSConnection (VSObjectConnection) für beide
                    Richtungen verwendet wird, sich der Timeout aber nur auf den Client bezieht,
                    muss hier formal trotzdem die Exception gefangen werden.
                    Auf dem hier verwendeten Socket des Servers wurde nie „setSoTimeout()“ aufgerufen.

                    Alternativ könnte man auch eine gesonderte „receiveObject“-Methode erstellen, die der
                    Client verwendet. Dann bräuchte man hier kein try-catch-Block.
                */
            }
            final VSRemoteObjectManager remoteObjectManager = VSRemoteObjectManager.getInstance();
            Object response = null;

            // Falls aufgerufene Methode eine Exception wirft, soll diese nicht im Server geworfen werden,
            // sondern beim Client, da der Aufruf für ihn wie ein lokaler aussehen soll.
            // Dazu müssen „IllegalAccessException“, „IllegalArgumentException“ und „InvocationTargetException“
            // verschickt werden können. Ein try-catch innerhalb von „invokeMethod“ ist nicht möglich, da der
            // Rückgabetyp „Object“ ist und mir gerade keine Möglichkeit einfällt, wie man vom Datentyp „Object“
            // zurück zu „*Exception“ gelangen kann. Da alle Exceptions serialisierbar sind, entstehen hier keine
            // Probleme.
            VSAnswerMessage answerMessage = new VSAnswerMessage();
            try{
                response = remoteObjectManager.invokeMethod(
                        dataReceivedForMethodCall.getObjectID(),
                        dataReceivedForMethodCall.getGenericMethodName(),
                        dataReceivedForMethodCall.getArgs()
                );
                answerMessage.setMessage((Serializable) response);
                // Setzen der Id passend zur Anfrage
                // answerMessage.setId(dataReceivedForMethodCall.getId());
                answerMessage.setExceptionStatus(false);
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
        // Pausieren, um das Socket-Zeitgrenze zu provozieren [funktioniert iwie nicht]
            // try {
            //     Thread.sleep(5000);
            // } catch (InterruptedException e) {
            //     // TODO Auto-generated catch block
            //     e.printStackTrace();
            // }
            System.out.println("[Server] recId = " + dataReceivedForMethodCall.getId());
            answerMessage.setId(dataReceivedForMethodCall.getId());
            System.out.println("[Server] " + answerMessage.toString());
            objCon.sendObject(answerMessage);

            // objCon.close();
            System.out.println("[Server] Gesendet.");
        }

    }
}
