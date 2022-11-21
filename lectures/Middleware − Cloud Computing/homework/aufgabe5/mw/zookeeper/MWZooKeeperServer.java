package mw.zookeeper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import java.time.Instant;

import java.util.Properties;
import java.io.Serializable;
import org.apache.zookeeper.zab.*;

import mw.zookeeper.MWZooKeeperRequest;
import mw.zookeeper.MWZooKeeperResponse;
import mw.zookeeper.MWZooKeeperTxn;

public class MWZooKeeperServer implements ZabCallback {

	final private static TreeSet<MWZooKeeperNode> tree = new TreeSet<MWZooKeeperNode>(new NodeComparator());
	final static MWZooKeeperImpl impl = new MWZooKeeperImpl(tree);
	final static HashMap<Integer, Integer> openRequestsHashMap = new HashMap<Integer, Integer>();
	// nimmt man HashMap, dann kann entfernen eines Paares ungünstig mit Prüfung im
	// Worker zusammenfallen und eine Exception geworfen werden
	final static List<MWZooKeeperTriplet> openRequests
		= Collections.synchronizedList(new ArrayList<MWZooKeeperTriplet>());

	final static ArrayList<MWZooKeeperResponse> responsesForClients = new ArrayList<MWZooKeeperResponse>();

	static HashMap<String, ArrayList<String>> ephemeralNodes = new HashMap<String, ArrayList<String>>();;


	static  public void addEphemeralNode(String clientaddr, String path) {
		if(!ephemeralNodes.containsKey(clientaddr)) {
			ephemeralNodes.put(clientaddr, new ArrayList<String>());
		}

		ephemeralNodes.get(clientaddr).add(path);
	}

	static public void removeEphemeralNode(String clientaddr, String path) {
		assert(ephemeralNodes.containsKey(clientaddr));

		ephemeralNodes.get(clientaddr).remove(path);
		if(ephemeralNodes.get(clientaddr).size() == 0) {
			ephemeralNodes.remove(clientaddr);
		}
	}

	static public void shutdownEphemeralNode(String path) {
		MWZooKeeperRequest request = new MWZooKeeperRequest(MWZooKeeperOperation.DELETE, path);
		request.setVersion(-1);


		System.out.println("shuting down: " + path);
		MWZooKeeperServer.zab.forwardRequest(request);
	}

	static public void shutdownEphemeralNodes(String clientaddr) {
		if(ephemeralNodes.containsKey(clientaddr)) {
			synchronized(MWZooKeeperServer.class) {
				for(String path : ephemeralNodes.get(clientaddr)) {
					shutdownEphemeralNode(path);

				}

				ephemeralNodes.remove(clientaddr);
			}
		}
	}

	static Zab zab;
	 static ZabStatus status = ZabStatus.LOOKING;
	static int idCounter = 0;
	static int basePort = 6700;

	static int openRequestsNum = 0;

	static int serverId;
	int port;

	public MWZooKeeperServer(int numServers) throws IOException, InterruptedException {
		serverId = idCounter++;
		port = basePort + serverId;

		Properties prob = new Properties();
		for (int i = 0; i < numServers; i++) {
			if (i == serverId) {
				prob.setProperty("myid", String.valueOf(serverId));
			} else {
				prob.setProperty("peer" + i, "127.0.0.1:" + (basePort + i));
			}
		}
		System.out.println(prob);
		// if(zabNode == null){

		// zab = new SingleZab(prob, this);
		zab = new SingleZab(prob, this);

		zab.startup();
		// }
	}

	// führen alle (Anführer und Anhänger) aus
	public void deliverTxn(Serializable txnFromLeader, long zxidFromLeader) {
		System.out.println("zab: deliverTxn " + txnFromLeader);
		MWZooKeeperTxn txn = (MWZooKeeperTxn) txnFromLeader;
		System.out.println("="+ txn);
		MWZooKeeperResponse response = impl.applyTxn(txn, zxidFromLeader);

		// Hier prüft jeder Server (Anhänger und Anführer), ob Client mit passender Id offene Anfrage hat
		for(final MWZooKeeperTriplet triplet : openRequests){ // wirft evtl. Fehler, da Iterator und und synch. ArrayList Sonderbehandlung benötigen
			if(triplet.serverId == serverId && triplet.clientId == txn.clientId){	// passende offene Anfrage existiert
				responsesForClients.add(response);
				triplet.requestProcessed = true;		// Markiere Anfrage als beantwortet
			}
		}
	}

	// führt Anführer aus
	public void deliverRequest(Serializable requestFromFollower) {
		System.out.println("zab: deliverRequest" + " " + serverId);
		MWZooKeeperRequest request = (MWZooKeeperRequest) requestFromFollower;
		long zxid = zab.createZXID();
		MWZooKeeperTxn txn = impl.processWriteRequest(request, zxid);
		System.out.println("="+ (txn == null));
		zab.proposeTxn(txn, zxid);
	}

	public void status(ZabStatus paramZabStatus, String paramString) {
		status=paramZabStatus;
	}

	void listen() throws IOException {
		int clientId = 0;

		ServerSocket socket = new ServerSocket(port);
		Socket connectionSocket = null;
		while (true) {
			// System.out.println("Starting server on socket: " + port);
			try {
				System.out.println("Awaiting client connection...");
				connectionSocket = socket.accept();
				connectionSocket.setTcpNoDelay(true);

				String clientaddr = connectionSocket.getRemoteSocketAddress().toString();
				System.out.println("recieve connection from " + clientaddr);

				ObjectOutputStream outToClient = new ObjectOutputStream(connectionSocket.getOutputStream());
				ObjectInputStream fromClient = new ObjectInputStream(connectionSocket.getInputStream());

				Thread worker = new Thread(new Worker(fromClient, outToClient, tree, clientId++, clientaddr));

				System.out.println("Starting worker on port " + port);
				worker.start();

			} catch (IOException e) {
				System.out.println("Connection failed");
				connectionSocket.close();
			}
		}
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		int numServers = 1;

		for (int i = 0; i < numServers; i++) {
			Thread worker = new Thread() {
				public void run() {
					try {
						MWZooKeeperServer server = new MWZooKeeperServer(1);
						server.listen();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			worker.start();
		}
	}
}

class Worker implements Runnable {
	ObjectInputStream fromClient;
	ObjectOutputStream toClient;
	TreeSet<MWZooKeeperNode> tree;
	int clientId;
	String clientaddr;

	public Worker(ObjectInputStream fromClient, ObjectOutputStream toClient, final TreeSet<MWZooKeeperNode> t,
			final int clientId, final String clientaddr) {
		this.fromClient = fromClient;
		this.toClient = toClient;
		this.tree = t;
		this.clientId = clientId;
		this.clientaddr = clientaddr;
	}

	@Override
	public void run() {
		MWZooKeeperImpl impl = new MWZooKeeperImpl(tree);
		MWZooKeeperRequest request = null;
		long time = -1;
		while (true) {
			System.out.println("Z_A.size(): " + impl.Z_A.size());
			System.out.println("Z_B.size(): " + impl.Z_B.size());
			try {
				request = (MWZooKeeperRequest) fromClient.readObject(); // Client schickt eine Anfrage
				// if (request == null) {
				// toClient.writeObject(null);
				// }
			} catch (Exception e) {
				// e.printStackTrace();
				System.out.println("Verbindung wurde Client-seitig beendet.");
				break;
			}

			System.out.println("operation: " + request.getOperation());
			// request mit Client-ID und Zeitstempel versehen
			time = Instant.now().toEpochMilli();
			request.clientId = clientId;
			request.time = time;

			MWZooKeeperTriplet openRequest = new MWZooKeeperTriplet(MWZooKeeperServer.serverId, clientId, false);
			MWZooKeeperServer.openRequests.add(openRequest);


			// LESEN
			if (request.getOperation() == MWZooKeeperOperation.GET_DATA) {
				MWZooKeeperResponse response = impl.processReadRequest(request);
				try {
					toClient.writeObject(response);
				} catch (IOException e) {
					e.printStackTrace();
				}
				// CREATE
			} else {

				synchronized(MWZooKeeperServer.class) {
					if(request.getOperation() == MWZooKeeperOperation.CREATE && request.getEphemeral() == true) {
						String path = request.getPath();
						System.out.println("creating ephemeral node with path: " + path);

						MWZooKeeperServer.addEphemeralNode(clientaddr, path);
					}
					// Schreibanfrage per ZabNode an Anführer weiterleiten
					MWZooKeeperServer.zab.forwardRequest(request);					// Schickt Anfrage auch automatisch an Master bzw. an sich selbst, sodass keine Unterscheidung notwendig ist, wie man Schreibanfragen behandelt.
					// Auf Antwort der Schreibanfrage warten (s. „deliverTxn()“)
					while(!openRequest.requestProcessed){								// TODO Geht bestimmt besser; Warten bis „deliverTxn“ entsprechenden Wert auf „false“ setzt
						try{
							Thread.sleep(200);
						} catch (InterruptedException e){
							e.printStackTrace();
						}
					}

					// Entferne offene Anfrage, da sie bearbeitet wurde
					MWZooKeeperServer.openRequests.remove(openRequest);
				}
				// Suche passende Antwort in Datenstruktur, die die Antworten verwaltet
				for(final MWZooKeeperResponse response : MWZooKeeperServer.responsesForClients){
					// Antwort wird über „clientId“ und „time“ identifiziert (gegenwärtig wäre „time“ nicht mehr notwendig, da ein Client nicht mehrere Requests auf einmal stellen kann aber vielleicht ist das Attribut später noch nützlich, da Mitteilung in „deliverTxn()“ an richtigen Client noch nicht richtig funktioniert)
					if(response.clientId == clientId){//} && response.serverId == MWZooKeeperServer.serverId){
						// Teile Client Antwort mit

						try {
							toClient.writeObject(response);
						} catch (IOException e) {
							e.printStackTrace();
						}
						MWZooKeeperServer.responsesForClients.remove(response);
						break;
					}
				}

			}
		}

		MWZooKeeperServer.shutdownEphemeralNodes(clientaddr);
	}
}

// Sortiert die Knoten gemäß ihres Pfades
class NodeComparator implements Comparator<MWZooKeeperNode> {
	@Override
	public int compare(final MWZooKeeperNode o1, final MWZooKeeperNode o2) {
		return o1.path.compareTo(o2.path);
	}

}
