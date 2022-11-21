package vsue.rmi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import vsue.faults.VSRPCSemantic;
import vsue.faults.VSRPCSemanticType;

public class VSAuctionServiceImpl implements VSAuctionService {

    public static Map<String, VSAuction>
        auctions= new HashMap<String, VSAuction>();                 // (Auktionsname, Auktion)
    public static Map<String, VSAuctionEventHandler>
        buyers = new HashMap<String, VSAuctionEventHandler>(),      // (Auktionsname, Handler des höchsten Gebots)
        sellers = new HashMap<String, VSAuctionEventHandler>();     // (Auktionsname, Handler des Verkäufers)
    public static Map<String, Integer>
        durations = new HashMap<String, Integer>();                 // (Auktionsname, Dauer)

    VSAuctionServiceImpl(){

    }

    @Override
// TODO Die Implementierung der Methoden oder in der Schnittstelle annotieren?
    @VSRPCSemantic(VSRPCSemanticType.LAST_OF_MANY)
    public void registerAuction(VSAuction auction, int duration, VSAuctionEventHandler handler)
            throws VSAuctionException, RemoteException {
        // superviseAuctions();
        if(duration < 0) throw new VSAuctionException("<duration> was negative");
        // ! Anmerkung zur „synchronized“-Notation: s. „superviseAuctions()“
        synchronized(VSAuctionServiceImpl.class){
            if(auctions.containsKey(auction.getName())) throw new VSAuctionException("An auction with the same <name> already exists");
            System.out.println(String.format(
                "=== Neue Auktion ===\n" + 
                "Auktion: %s - Startpreis: %s - Dauer: %d",
                auction.getName(), auction.getPrice(), duration));
            final String auctionName = auction.getName();
            auctions.put(auctionName, auction);
            buyers.put(auctionName, handler);
            sellers.put(auctionName, handler);
            durations.put(auctionName, duration);
        }
    }

    @Override
    public VSAuction[] getAuctions() throws RemoteException {
        System.out.println("getAuctions!");
        return auctions.values().toArray(new VSAuction[auctions.size()]);
    }

    @Override
    public boolean placeBid(String userName, String auctionName, int price, VSAuctionEventHandler handler)
            throws VSAuctionException, RemoteException {
        // Prüfen, ob Auktion existiert
        if(!auctions.containsKey(auctionName)) throw new VSAuctionException(
            String.format("No auction with the name <%s> exists", auctionName));
        // Prüfen, ob neues Gebot höher ist
        if(auctions.get(auctionName).getPrice() >= price){
            return false;
        } else {
            // ! Anmerkung zur „synchronized“-Notation: s. „superviseAuctions()“
            synchronized(VSAuctionServiceImpl.class){
                // Alten Client über höheres Gebot benachrichtigen
                buyers.get(auctionName).handleEvent(
                    VSAuctionEventType.HIGHER_BID,
                    auctions.get(auctionName));
                // Höheren Preis speichern
                auctions.get(auctionName).setPrice(price);
                // EventHandler aktualisieren
                buyers.put(auctionName, handler);
                return true;
            }
        }
    }
    
    /*
        Die Methode überwacht die Dauer der Auktionen und wird in
        „VSAuctionService“ gestartet. Deswegen sind alle Variablen
        „static“
    */
    public static void superviseAuctions(){
        System.out.println("superviseAuctions");
        try {
            System.out.println(
                "Serveradresse: " + InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }
        while(true){
            /*
                Synchronisation am Klassen-Objekt, damit jeder synchronized-Block garantiert nur 
                von einem Thread und damit die Datenstrukturen in einem Rutsch nur von einem Thread
                modifiziert werden.
                Ein Thread ist hierbei:
                    - VSAuctionRMIServerImpl.superviseAuctions() in „VSAuctionRMIServer“
                    - die auf dem exportierten AuctionService (s. VSAuctionRMIServer und VSAuctionRMIClient)
                        aufgerufenen Methoden, um Auktionen zu registrieren bzw. Preise zu überbieten, also
                        „registerAuction()“ und „placeBid()“
                
                Vorteil: Einfach und schnell umzusetzen, da das Fehlen dieses Mechanismuses in der Korrektur angemerkt wurde
                Nachteil: Unschön; Die anderen Threads haben nur ein Zeitfenster von 1s um ihre Aufgaben zu erledigen
                                    (ist aber in unseren Dimensionen nicht ausschlaggebend)
                Besser: Prioritätswarteschlange in Abhängigkeit von der Dauer und solange den Überwachungsthread schlafen legen,
                        dann muss aber in „registerAuction()“ auf diesen Umstand Acht gegeben werden, da dort die Prioritäts-
                        warteschlange modifiziert werden kann.
            */
            synchronized(VSAuctionServiceImpl.class){
                for(VSAuction auction: auctions.values()) {  // Auktionen sind eindeutig wegen ihres Namens
                    final String auctionName = auction.getName();
                    // Dauer der Auktion abfragen
                    int duration = durations.get(auctionName);
                    // Entferne Auktion, wenn sie von Thread bearbeitet wurde; „Integer.MIN_VALUE“ markiert dies
                    if (duration == Integer.MIN_VALUE) {
                        auctions.remove(auctionName);
                        buyers.remove(auctionName);
                        sellers.remove(auctionName);
                        durations.remove(auctionName);
                        continue;
                    }
                    // Eine Sekunde von Dauer abziehen
                    durations.put(auctionName, --duration);
                    // Behandlung/Benachrichtigung des Auktionsende in Thread ausgelagert, da
                    // main-Thread hauptsächlich die Zeit überwachen soll und das Versenden von
                    // Nachrichten an Gewinner und Verkäufer die Dauer verzerrt.
                    if (duration == 0) {
                        final Thread handleAuctionEnd_Thread
                                = new HandleAuctionEnd(auctionName);
                        handleAuctionEnd_Thread.start();
                    }
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}


class HandleAuctionEnd extends Thread {

    private String auctionName;

    HandleAuctionEnd(final String auctionName){
        this.auctionName = auctionName;
    }

    // Wäre vllt. schöner, wenn „auctions“, „buyers“, „sellers“ private wären
    @Override
    public void run() {
        System.out.println("Auktionsende-Thread für Auktion <" + auctionName + "> gestartet.");
        try {
            // Benachrichtige Käufer
            VSAuctionServiceImpl.buyers.get(auctionName).handleEvent(
                VSAuctionEventType.AUCTION_WON,
                VSAuctionServiceImpl.auctions.get(auctionName));
            // Benachrichtige Verkäufer
            VSAuctionServiceImpl.sellers.get(auctionName).handleEvent(
                VSAuctionEventType.AUCTION_END,
                VSAuctionServiceImpl.auctions.get(auctionName));
            // Markiere Auktion mit „Integer.MIN_VALUE“, damit sie main-Thread entfernen kann
            // Würde man die Auktion hier entfernen, könnten sich die Thread in die Quere kommen,
            // da main-Thrad stetig liest und schreibt und wenn währrenddessen gelöscht wird, wäre das ungünstig.
            VSAuctionServiceImpl.durations.put(auctionName, Integer.MIN_VALUE);
        } catch (RemoteException e) {
            e.printStackTrace();
        }       
    }    
}
