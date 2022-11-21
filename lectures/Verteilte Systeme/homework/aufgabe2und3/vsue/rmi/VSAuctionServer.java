package vsue.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import vsue.communication.VSRemoteObjectManager;

public class VSAuctionServer {
    public static void main(String[] args){
        final int PORT = Integer.parseInt(args[0]);
        final String AUCTION_SERVICE_NAME = "VS-RMI";
        VSAuctionServiceImpl auctionService = new VSAuctionServiceImpl();

        // Exportiere Objekt nach eigener Logik
        Remote auctionServiceStub = VSRemoteObjectManager.getInstance().exportObject(auctionService);

        // Eigene Klassen im „bind“-Aufruf der Registry erlauben
        System.setProperty("sun.rmi.registry.registryFilter", "vsue.**");
        try {
            final Registry registry = LocateRegistry.createRegistry(PORT);
            registry.bind(AUCTION_SERVICE_NAME, auctionServiceStub);
        } catch (RemoteException | java.rmi.AlreadyBoundException e) {
            e.printStackTrace();
        }
        VSAuctionServiceImpl.superviseAuctions();
    }
}
