package vsue.rmi;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.*;
import vsue.communication.VSRemoteObjectManager;

public class VSAuctionClient extends VSShell implements VSAuctionEventHandler, Serializable {

	private final String AUCTION_SERVICE_NAME = "VS-RMI";
	VSAuctionEventHandler eventHandlerForSending = null;

	// The user name provided via command line.
	private final String userName;
	public Registry registry;
	private VSAuctionService auctionService;


	public VSAuctionClient(String userName) {
		this.userName = userName;
	}


	// #############################
	// # INITIALIZATION & SHUTDOWN #
	// #############################

	public void init(String registryHost, int registryPort) {
		System.out.println("Connecting to Registry...");
		try {
			registry = LocateRegistry.getRegistry(registryHost, registryPort);
			eventHandlerForSending = (VSAuctionEventHandler) VSRemoteObjectManager.getInstance().exportObject(this);
			auctionService = (VSAuctionService) registry.lookup(AUCTION_SERVICE_NAME);
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
		System.out.println("Registry reached.");

	}

	public void shutdown() {
		// try {
		// 	UnicastRemoteObject.unexportObject(this, false);
		// } catch (NoSuchObjectException e) {
		// 	e.printStackTrace();
		// }
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
		try {
            /*  „auctionService“ ist ein (gecastetes) Proxy-Obj, bzw. der Stub. Wegen des InvocationHandlers
             *  werden Aufrufe am Stub, also an „auctionService“, in die „invoke“-Methode des InvocationHandlers
             *  umgeleitet. Dort wird dann die Verbindung zum ursprünglichen Objekt per „VSObjectConnection“ 
             *  hergestellt. Endpunkt der Verbindung ist jedoch nicht das Objekt per se sondern der „VSServer“,
             *  der als Skeleton fungiert. In dieser Klasse, bzw. im Worker-Thread wird dann per
             *  „VSRemoteObjectManager.invokeMethod“ das passende Objekt herausgesucht und per Reflection
             *  die spezifizierte Methode aufgerufen.
             *  Das zurückschicken des Resultats geschieht analog.
             */
			auctionService.registerAuction(new VSAuction(auctionName, startingPrice), duration, this);
		} catch (RemoteException | VSAuctionException e) {
			e.printStackTrace();
		}
	}

	public void list() {
		VSAuction[] auctions;
		try{
			// „auctionService“ ist der Stub
			// auctionService = (VSAuctionService) registry.lookup(AUCTION_SERVICE_NAME);
            /*  „auctionService“ ist ein (gecastetes) Proxy-Obj, bzw. der Stub. Wegen des InvocationHandlers
             *  werden Aufrufe am Stub, also an „auctionService“, in die „invoke“-Methode des InvocationHandlers
             *  umgeleitet. Dort wird dann die Verbindung zum ursprünglichen Objekt per „VSObjectConnection“ 
             *  hergestellt. Endpunkt der Verbindung ist jedoch nicht das Objekt per se sondern der „VSServer“,
             *  der als Skeleton fungiert. In dieser Klasse, bzw. im Worker-Thread wird dann per
             *  „VSRemoteObjectManager.invokeMethod“ das passende Objekt herausgesucht und per Reflection
             *  die spezifizierte Methode aufgerufen.
             *  Das zurückschicken des Resultats geschieht analog.
             */
			auctions = auctionService.getAuctions();
		} catch (RemoteException e){
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
		try{
            /*  „auctionService“ ist ein (gecastetes) Proxy-Obj, bzw. der Stub. Wegen des InvocationHandlers
             *  werden Aufrufe am Stub, also an „auctionService“, in die „invoke“-Methode des InvocationHandlers
             *  umgeleitet. Dort wird dann die Verbindung zum ursprünglichen Objekt per „VSObjectConnection“ 
             *  hergestellt. Endpunkt der Verbindung ist jedoch nicht das Objekt per se sondern der „VSServer“,
             *  der als Skeleton fungiert. In dieser Klasse, bzw. im Worker-Thread wird dann per
             *  „VSRemoteObjectManager.invokeMethod“ das passende Objekt herausgesucht und per Reflection
             *  die spezifizierte Methode aufgerufen.
             *  Das zurückschicken des Resultats geschieht analog.
             */
			boolean success = auctionService.placeBid(userName, auctionName, price, this);
			if (!success) {
				System.out.println("Gebot war zu niedrig!");
			}
		} catch(RemoteException | VSAuctionException e){
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
			System.err.println("usage: java " + VSAuctionClient.class.getName() + " <user-name> <registry_host> <registry_port>");
			System.exit(1);
		}
	}

	private static void createAndExecuteClient(String[] args) {
		String userName = args[0];
		VSAuctionClient client = new VSAuctionClient(userName);

		String registryHost = args[1];
		int registryPort = Integer.parseInt(args[2]);
		client.init(registryHost, registryPort);
		client.shell();
		client.shutdown();
	}
}
