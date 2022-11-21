package vsue.rmi;

import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;

// import javax.validation.constraints.NotNull;


public class VSAuctionRMIClient extends VSShell implements VSAuctionEventHandler {

	private final String AUCTION_SERVICE_NAME = "TEST";
	VSAuctionEventHandler eventHandlerForSending = null;

	// The user name provided via command line.
	private final String userName;
	public Registry registry;


	public VSAuctionRMIClient(String userName) {
		this.userName = userName;
	}


	// #############################
	// # INITIALIZATION & SHUTDOWN #
	// #############################

	public void init(String registryHost, int registryPort) {
		System.out.println("Connecting to Registry");
		try {
			registry = LocateRegistry.getRegistry(registryHost, registryPort);
			eventHandlerForSending = (VSAuctionEventHandler) UnicastRemoteObject.exportObject(this, 0);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		System.out.println("Registry reached.");

	}

	public void shutdown() {
		try {
			UnicastRemoteObject.unexportObject(this, false);
		} catch (NoSuchObjectException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}


	// #################
	// # EVENT HANDLER #
	// #################

	@Override
	public void handleEvent(VSAuctionEventType event, VSAuction auction) {
		System.out.println(String.format(
			"\n=== Auktionsbenachrichtigung ===\n" + 
			"Name: %s\n" + 
			"Ereignis: %s\n",
				auction.getName(),
				event.name()));
	}


	// ##################
	// # CLIENT METHODS #
	// ##################

	public void register(String auctionName, int duration, int startingPrice) {
		VSAuctionService auctionService;
		try {
			auctionService = (VSAuctionService) registry.lookup(AUCTION_SERVICE_NAME);
			auctionService.registerAuction(new VSAuction(auctionName, startingPrice), duration, this);
		} catch (RemoteException | NotBoundException | VSAuctionException e) {
			e.printStackTrace();
		}
	}

	public void list() {
		VSAuctionService auctionService;
		VSAuction[] auctions;
		try{
			auctionService = (VSAuctionService) registry.lookup(AUCTION_SERVICE_NAME);
			auctions = auctionService.getAuctions();
		} catch (RemoteException | NotBoundException e){
			e.printStackTrace();
			return;
		}
		if(auctions.length == 0) {
			System.out.println("Gegenwärtig keine Auktionen");
		} else {
			System.out.println("Gegenwärtig stattfindende Auktionen:");
			for(VSAuction auction : auctions){
				System.out.println(String.format(
					"<Name> - <Preis>\n" +
					"%s - %d", auction.getName(), auction.getPrice()));
			}
		}
	}

	public void bid(String auctionName, int price) {
		VSAuctionService auctionService;
		try{
			auctionService = (VSAuctionService) registry.lookup(AUCTION_SERVICE_NAME);
			auctionService.placeBid(userName, auctionName, price, this);
		} catch(RemoteException | VSAuctionException | NotBoundException e){
			e.printStackTrace();
			System.out.println("Gebotabgabe gescheitert.");
		}
	}


	// #########
	// # SHELL #
	// #########

	protected boolean processCommand(String[] args) {
		switch (args[0]) {
		case "help":
		case "h":
			System.out.println("The following commands are available:\n"
					+ "  help\n"
					+ "  bid <auction-name> <price>\n"
					+ "  list\n"
					+ "  register <auction-name> <duration> [<starting-price>]\n"
					+ "  quit"
			);
			break;
		case "register":
		case "r":
			if (args.length < 3)
				throw new IllegalArgumentException("Usage: register <auction-name> <duration> [<starting-price>]");
			int duration = Integer.parseInt(args[2]);
			int startingPrice = (args.length > 3) ? Integer.parseInt(args[3]) : 0;
			register(args[1], duration, startingPrice);
			break;
		case "list":
		case "l":
			list();
			break;
		case "bid":
		case "b":
			if (args.length < 3) throw new IllegalArgumentException("Usage: bid <auction-name> <price>");
			int price = Integer.parseInt(args[2]);
			bid(args[1], price);
			break;
		case "exit":
		case "quit":
		case "x":
		case "q":
			return false;
		default:
			throw new IllegalArgumentException("Unknown command: " + args[0] + "\nUse \"help\" to list available commands");
		}
		return true;
	}


	// ########
	// # MAIN #
	// ########

	public static void main(String[] args) {
		checkArguments(args);
		createAndExecuteClient(args);
	}

	private static void checkArguments(String[] args) {
		if (args.length < 3) {
			System.err.println("usage: java " + VSAuctionRMIClient.class.getName() + " <user-name> <registry_host> <registry_port>");
			System.exit(1);
		}
	}

	private static void createAndExecuteClient(String[] args) {
		String userName = args[0];
		VSAuctionRMIClient client = new VSAuctionRMIClient(userName);

		String registryHost = args[1];
		int registryPort = Integer.parseInt(args[2]);
		client.init(registryHost, registryPort);
		client.shell();
		client.shutdown();
	}
}
