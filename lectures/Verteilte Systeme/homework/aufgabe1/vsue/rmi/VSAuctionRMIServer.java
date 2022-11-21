package vsue.rmi;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;

public class VSAuctionRMIServer {

    public static void main(String[] args){
        final int PORT = Integer.parseInt(args[0]);
        VSAuctionServiceImpl auctionService = new VSAuctionServiceImpl();
        VSAuctionService exportedAuctionService = null;
        try {
            exportedAuctionService = (VSAuctionService) UnicastRemoteObject.exportObject(auctionService, 0);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.out.println("Exporting of VSAuctionServiceImpl object failed");
        }
        // TODO: Port-Nummer? Habe die aus den Folien genommen
        Registry registry;
        try {
            registry = LocateRegistry.createRegistry(PORT);
            registry.bind("TEST", exportedAuctionService);
        } catch (RemoteException | AlreadyBoundException e) {
            e.printStackTrace();
        }
        System.out.println("Server started.");
        VSAuctionServiceImpl.superviseAuctions();
    }
    
}
