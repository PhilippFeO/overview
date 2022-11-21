package vsue.faults;

import vsue.communication.VSObjectConnection;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;

public class VSBuggyObjectConnection extends VSObjectConnection {
    // possible delays in ms
    private final int MIN_DELAY = 500;
    private final int MAX_DELAY = 10000;
    // Wahrscheinlichkeit, dass Fehler auftritt
    private final double faultProbability = 0.5;
    // Wahrscheinlichkeit, dass auftretender Fehler Verbindungsabbruch simuliert
    private final double connectionFaultProbability = 0.2;

    public VSBuggyObjectConnection(Socket socket) {
        super(socket);
    }

    public void sendObject(Serializable object) throws IOException {
        // createFault();
        super.sendObject(object);
    }

    public Serializable receiveObject () throws IOException, ClassNotFoundException {
        createFault();
        return super.receiveObject();
    }

    private void createFault() {
        // produziert simulierte Fehler in der Verbindung mit oben definierten Wahrscheinlichkeiten
        // gestaffelte Auswertung: 1. ob Fehler simuliert wird, 2. welcher Fehler simuliert wird (Verzögerung, Verbindungsabbruch)
        if (Math.random() >= faultProbability) {
            if (Math.random() <= connectionFaultProbability) {
                closeConnection();
            } else {
                delayMessage();
            }
        }
    }

    private void closeConnection() {
        // simuliert Verbindungsabbruch durch schließen der Socket
        this.close();
        System.out.println("Verbindungsabbruch!");
    }

    private void delayMessage() {
        // Simuliert Verzögerung in der Übertragung, Wartezeit zwischen MIN_DELAY und MAX_DELAY
        int delay = (int) ((Math.random() * (MAX_DELAY - MIN_DELAY)) + MIN_DELAY);
        System.out.println("Verzögerung um " + delay + " ms!");
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
