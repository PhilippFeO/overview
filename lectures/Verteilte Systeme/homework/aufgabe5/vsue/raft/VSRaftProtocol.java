package vsue.raft;

import java.io.Serializable;
import java.lang.Thread.State;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import vsue.raft.VSCounterMessages.VSCounterRequest;

// import org.graalvm.compiler.nodes.SnippetAnchorNode;

/**
 * Implementation of the raft protocol
 */
public class VSRaftProtocol implements VSRaftProtocolService {

	Serializable snapshot;
	Runnable protocol_worker;
	Thread protocol_thread;
	Object notifyObj = new Object();
	final int LOG_MAX_CAPACITY = 10;
	long lastSnapshotIndex = -1;
	int lastSnapshotTerm = -1;

	/**
	 * Roles of a replica as defined by raft
	 */
	enum VSRaftRole {
		LEADER,
		FOLLOWER,
		CANDIDATE
	}

	// Order of magnitude between timeouts, see raft paper section 5.6
	private static final int ELECTION_TIMEOUT = 3000;
	private static final int KEEP_ALIVE_TIMEOUT = 300;
	// Randomness for the timeout
	private static final int RANDOMNESS = 50; // TODO: choose randomness factor

	private final InetSocketAddress[] addresses;
	private final int myId;


	// Fields defined in Figure 2 of the raft paper
	// persistent
	private int currentTerm;
	private int votedFor; // -1 means not voted in current term
	private VSRaftLog log;

	// volatile
	private long commitIndex;
	private long lastApplied = 0;

	// volatile leader
	private long[] nextIndex = {0, 0, 0};
	private long[] matchIndex = new long[3];

	// Registry name
	final String REGISTRY_NAME = "Raft-Protocol";
	// Hashmap to cache Stubs TODO: eigentlich wäre auch ein Array mit Länge addresses.length ausreichend, da mit der replicaId eindeutig zugegriffen werden kann
	private HashMap<Integer, VSRaftProtocolService> stubs = new HashMap<>();
	// current Role
	VSRaftRole currentRole;
	// counter application
	VSCounterServer application;
	// timer
	Timer timer;

	/**
	 * Create a new instance of the raft protocol
	 *
	 * @param replicaId id of the current replica. addresses[replicaId] is the
	 *                  address of the registry for use by the current replica
	 * @param addresses addresses of the registry of every raft protocol instance
	 */
	public VSRaftProtocol(int replicaId, InetSocketAddress[] addresses) {
		myId = replicaId;
		this.addresses = addresses;

		// initialize protocol state
		currentTerm = 0;
		votedFor = -1;
		log = new VSRaftLog();
		currentRole = VSRaftRole.FOLLOWER;
	}


	/**
	 * Initialize the raft protocol instance. Exports the protocol instance and
	 * makes it accessible via a registry instance for this protocol. Runs a
	 * connection test afterwards and initiates periodic protocol tasks.
	 *
	 * @param application Counter server application. Provides status(), applyRequest(),
	 *                    createSnapshot() and applySnapshot() methods
	 * @throws RemoteException Failed to export protocol or setup registry
	 */
	public void init(VSCounterServer application) throws RemoteException {
		this.application = application;
		application.status(currentRole, -1);

		VSRaftProtocolService exportedRaftProtocol = null;
		try {
			exportedRaftProtocol = (VSRaftProtocolService) UnicastRemoteObject.exportObject(this, 0);
		} catch (RemoteException e) {
			System.out.println("Error exporting Object");
			e.printStackTrace();
		}
		try {
			System.setProperty("sun.rmi.registry.registryFilter", "vsue.**");
			final Registry registry = LocateRegistry.createRegistry(addresses[myId].getPort());
			registry.bind(REGISTRY_NAME, exportedRaftProtocol);
		} catch (RemoteException | AlreadyBoundException e) {
			e.printStackTrace();
		}

		// test connection, blocks until connected to all other replicas
		testConnection();

		// TODO: add randomness
		timer = new Timer();
		timer.schedule(new CandidateState(this), ELECTION_TIMEOUT);
	}

	/**
	 * Returns a VSRaftProtocolService stub for the specified replica.
	 *
	 * @param replicaId Id of replica to connect to
	 * @return VSRaftProtocolService stub
	 * @throws RemoteException Failed to retrieve the stub
	 */
	private VSRaftProtocolService getStub(int replicaId) throws RemoteException {
		VSRaftProtocolService stub = null;
		// check if there is currently a stub in the map
		if (!(stubs.get(replicaId) == null)) {
			stub = stubs.get(replicaId);
		// if not, get new stub
		} else {
			Registry registry;
			try {
				// load registry
				registry = LocateRegistry.getRegistry(addresses[replicaId].getHostName(), addresses[replicaId].getPort());
				stub = (VSRaftProtocolService) registry.lookup(REGISTRY_NAME);
				// save stub to hashmap
				stubs.put(replicaId, stub);
			} catch (NotBoundException e) {
				System.out.println("Error connecting to registry of replicaId " + replicaId);
<<<<<<< HEAD
				e.printStackTrace();
				// if no connection to the registry can be established, throw RemoteException
=======
				// e.printStackTrace();
>>>>>>> jannik5
				throw new RemoteException(e.getMessage());
			}
		}
		return stub;
	}

	/**
	 * Discard a cached VSRaftProtocolService stub for the specified replica.
	 *
	 * @param replicaId Id of replica whose stub should be dropped
	 */
	private void discardStub(int replicaId) {
		// "forget" stub by removing it from the hashmap
		if(stubs.get(replicaId) != null){
			stubs.remove(replicaId);
		}
	}

	/**
	 * Basic test whether communication with the other replicas is possible.
	 */
	private void testConnection() {
		for (int i = 0; i < addresses.length; i++) {
			if (i == myId) {
				continue;
			}

			while (true) {
				VSRaftProtocolService stub = null;
				try {
					stub = getStub(i);
				} catch (RemoteException e) {
					System.out.println("Retrying getStub for replica " + i + " after exception: " + e);
				}
				if (stub != null) {
					try {
						// issue request for old term which must always be rejected
						stub.requestVote(-1, myId, -1, -1);
						break;
					} catch (RemoteException e) {
						System.out.println("Retrying connection to replica " + i + " after exception: " + e);
						discardStub(i);
					} catch (UnsupportedOperationException e) {
						// UnsupportedOperationException is thrown by the rpc stub implementation
						break;
					}
				}
				try {
					//noinspection BusyWait
					Thread.sleep(500);
				} catch (InterruptedException interruptedException) {
					interruptedException.printStackTrace();
				}
			}
			System.out.println("Connection to replica " + i + " successful");
		}
		System.out.println("Connection test completed");
	}


	@Override
	// see VSRaftProtocolService.requestVote for the documentation
	public synchronized VSRaftRPCResult requestVote(int term, int candidateId, long lastLogIndex, int lastLogTerm) {
		System.out.println("Received requestVote message from Replica with ID: " + candidateId);
		final int own_last_log_term = log.getLatestEntry().term;

		// Implementierung aktuell nach Beschreibung von Aufgabe 5.2
		// check if term is higher or same term but already voted
		if (currentTerm > term || (currentTerm == term && votedFor != candidateId)) {
			System.out.println("Voted against this Replica: " + candidateId);
			return new VSRaftRPCResult(currentTerm, false);
	// Prüfen, ob Log des Kandidaten aktueller ist
		// Term des Kandidaten geringer => Log NICHT aktueller => Ablehnung
		} else if(own_last_log_term > lastLogTerm){
			System.out.println("Voted against this Replica: " + candidateId);
			return new VSRaftRPCResult(currentTerm, false);
		// Term des Kandidaten identisch mit eigenem aber Log d. Kandidaten hat geringere Länge => Log NICHT aktueller => Ablehnung
		} else if(own_last_log_term == lastLogTerm
					&& log.getLatestIndex() > lastLogIndex){
			System.out.println("Voted against this Replica: " + candidateId);
			return new VSRaftRPCResult(currentTerm, false);
		} else {
			currentTerm = term;
			votedFor = candidateId;
			currentRole = VSRaftRole.FOLLOWER;
			application.status(currentRole, -1);
			System.out.println("Voted for this Replica: " + candidateId);
			return new VSRaftRPCResult(currentTerm, true);
		}
	}

	@Override
	// see VSRaftProtocolService.appendEntries for the documentation
	public synchronized VSRaftRPCResult appendEntries(int term, int leaderId, long prevLogIndex, int prevLogTerm,
			VSRaftLogEntry[] entries, long leaderCommit) {
		System.out.println();
		System.out.println("===== appendEntries "+(entries==null?"Heartbeat von Leader " + leaderId:"Replizierung")+" =====");
		System.out.println("[appendEntries] prevLogIndex = " + prevLogIndex + "   prevLogTerm = " + prevLogTerm);
		
		if(log.getEntry(prevLogIndex) == null){
			System.out.println("[appendEntries] Kein Eintrag an Stelle " + prevLogIndex + " vorhanden");
			System.out.println("===== appendEntries Replizierung beendet =====");
			System.out.println();
			System.out.println();
			return new VSRaftRPCResult(currentTerm, false);
		}
		
		
		// System.out.println("[appendEntries] currentRole = " + currentRole + "   leaderId = " + leaderId + "   lastApplied = " + lastApplied);
	// 1. von AppendEntries
		if(term < currentTerm) {
			if(currentRole == VSRaftRole.FOLLOWER) {
				System.out.println("[appendEntries] Erhaltener Term = " + term + "  <  currentTerm = " + currentTerm);
				System.out.println("===== appendEntries Replizierung beendet =====");
				System.out.println();
				System.out.println();
				return new VSRaftRPCResult(currentTerm, false);  // old request, obsolete
			}
		}

	// 2. von AppendEntries? Man muss aber erst schauen, ob ein Entry vorhanden ist 
		// Leader beenden nach mind. 1 commit
		// Leader neustarten
		// Log von Replikat (muss nicht unbedingt wieder Leader sein) ist leer aber erhält über prevLogIndex Index > 0 => NullPointerException wegen term-Attribut von log.getEntry(prevLogIndex)
		if(log.getEntry(prevLogIndex) != null){
			if(prevLogTerm != log.getEntry(prevLogIndex).term) {
				// hier auch nach HEARTBEAT fragen und timer zurücksetzen:
				// wenn LEADER comes alive: muss Ind/Term nicht stimmen, kann trotzdem Heartbeat sein
				System.out.println("[appendEntries] log.getEntry(prevLogIndex).term = " + log.getEntry(prevLogIndex).term + "  !=  prevLogTerm = " + prevLogTerm);
				timer.cancel();
				timer = new Timer();
				timer.schedule(new CandidateState(this), ELECTION_TIMEOUT);
				System.out.println("===== appendEntries Replizierung beendet =====");
				System.out.println();
				System.out.println();
				return new VSRaftRPCResult(currentTerm, false);  // prevLogIndex/Term not matching
			}
		}

<<<<<<< HEAD
		// Zusätzlich setzen Follower bei jedem Heartbeat ihr Anführerwahl-Timeout zurück
		if(entries == null && currentRole == VSRaftRole.FOLLOWER) {
			timer.cancel();
			// wie geht das dann mit currentRole = VSRaftRole.CANDIDATE; application.status(currentRole, -1); ? -> passiert in FollowerState
			timer.schedule(new FollowerState(this), ELECTION_TIMEOUT);  // bei Ablauf des Timers, wird ein new FollwerState(this) erzeugt und implizit deren run() Methode aufgerufen. // TODO Namen ändern zu CandidateState
			// heartbeat kann neuen commitIndex enthalten
			if(commitIndex < leaderCommit) {
				while(commitIndex < leaderCommit) {
					commitIndex++;
					application.applyRequest(log.getEntry(commitIndex));
				}
				commitIndex = Math.min(leaderCommit, log.getLatestIndex());
			}
			return new VSRaftRPCResult(currentTerm, true);
		}

		if(log.getEntry(prevLogIndex).term == prevLogTerm && prevLogIndex == log.getEntry(prevLogIndex).index && entries != null) {
			// prevLogIndex/Term matching: store Entries and update commitIndex
=======
	// 3. von AppendEntries
		// Hier kann (evtl.) auch eine NullPointerException auftreten => "existing entry" => Abfrage, ob Log-Eintrag existiert
		if(log.getEntry(prevLogIndex) != null){
			if(log.getEntry(prevLogIndex).term != prevLogTerm && prevLogIndex == log.getEntry(prevLogIndex).index) {
				return new VSRaftRPCResult(term, false);
			}
		}

	// 4. von AppendEntries
		// Anführer schickt in „prevLog{Index, Term}“, was sein letzter {Index, Term} im Log ist
		// Wenn Follower diesen auch hat, sind alle Einträge bis zu diesem gleich und er hängt die in AppendEntries
		// mitgeschickten nach diesem an (die, die davor standen werden gelöscht)
		if(entries != null && prevLogIndex == log.getEntry(prevLogIndex).index && log.getEntry(prevLogIndex).term == prevLogTerm) {
			System.out.println();
			System.out.println("[appendEntries] Replizierung...");

			// prevLogIndex/Term matching: store Entries and update

			if(currentRole == VSRaftRole.FOLLOWER){
				timer.cancel();
				timer = new Timer();
				// wie geht das dann mit currentRole = VSRaftRole.CANDIDATE; application.status(currentRole, -1); ? -> passiert in CandidateState
				timer.schedule(new CandidateState(this), ELECTION_TIMEOUT);  // bei Ablauf des Timers, wird ein new FollwerState(this) erzeugt und implizit deren run() Methode aufgerufen. // TODO Namen ändern zu CandidateState
			}

			System.out.println("[appendEntries] log.getLatestIndex = " + log.getLatestIndex());
			// Einträge überschreiben
>>>>>>> jannik5
			log.storeEntries(entries);
			System.out.println("[appendEntries] log.getLatestIndex = " + log.getLatestIndex());
			
			currentTerm = term;
			System.out.println();

			if(leaderCommit > lastApplied){
				System.out.println("[appendEntries] Befehle ausführen");
				lastApplied++;
				while(lastApplied <= leaderCommit){
					// VSRaftLogEntry entry = null;
					// if(entry.request == null){ commitIndex++; continue;}

					// Sollte nur eintreten, wenn Replikat-Log länger als das des Anführers, es also über „leaderCommit“ viele Einträge hinaus geht
					if(log.getEntry(lastApplied) == null){
						// lastApplied--;
						break;
					}
					VSRaftLogEntry entry = log.getEntry(lastApplied);
					VSCounterRequest r = (VSCounterRequest) entry.request;
					System.out.println("[appendEntries] Inkrementieren auf Wert " + (r.requestCounter + 1));
					application.applyRequest(entry);
					lastApplied++;
				}
				// Wegen Schleife zurücksetzen
				lastApplied--;

				commitIndex = lastApplied;
			}
<<<<<<< HEAD
			commitIndex = Math.min(leaderCommit, log.getLatestIndex()); // should be equal to commitIndex = leaderCommit since it always send whole entries[]
=======

			snapshotHelper();

			// Mit dieser Antwort, weiß Anführer nun, dass Logs auf dem angeforderten Stand sind
			application.status(currentRole, leaderId);

			System.out.println("===== appendEntries Replizierung beendet =====");
			System.out.println();
			System.out.println();
>>>>>>> jannik5
			return new VSRaftRPCResult(currentTerm, true); 
		}

	// Heartbeats
		// Zusätzlich setzen Follower bei jedem Heartbeat ihr Anführerwahl-Timeout zurück
		if(entries == null && currentRole == VSRaftRole.FOLLOWER) {
			System.out.println();
			// System.out.println("[appendEntries] Heartbeat...");

			// if(timer != null){
				timer.cancel();
				timer = new Timer();
				// wie geht das dann mit currentRole = VSRaftRole.CANDIDATE; application.status(currentRole, -1); ? -> passiert in CandidateState
				timer.schedule(new CandidateState(this), ELECTION_TIMEOUT);  // bei Ablauf des Timers, wird ein new FollwerState(this) erzeugt und implizit deren run() Methode aufgerufen. // TODO Namen ändern zu CandidateState
			// }

			// Falls im Heartbeat prevLogIndex zu hoch ist, wird false zurückgegeben, damit
			// „nextIndex[]“ dekrementiert wird
			if(log.getEntry(prevLogIndex) == null){
				return new VSRaftRPCResult(currentTerm, false);
			}
			// Log-Einträge ausführen
			// System.out.println("[appendEntries] leaderCommit = " + leaderCommit + "   lastApplied = " + lastApplied);
			// if(!excuteCommands(leaderCommit)) return new VSRaftRPCResult(currentTerm, false);
			if(leaderCommit > lastApplied){
				System.out.println("[appendEntries] Befehle ausführen");
				lastApplied++;
				while(lastApplied <= leaderCommit){
					// VSRaftLogEntry entry = null;
					// if(entry.request == null){ commitIndex++; continue;}

					// Sollte nur eintreten, wenn Replikat-Log länger als das des Anführers, es also über „leaderCommit“ viele Einträge hinaus geht
					if(log.getEntry(lastApplied) == null){
						lastApplied--;
						System.out.println("[appendEntries] log.getEntry(lastApplied = " + lastApplied + ") = null => keine Befehle im Log, warten auf nächste Replizierung des Leaders");
						System.out.println("===== appendEntries Heartbeat beendet =====");
						System.out.println();
						System.out.println();				
						return new VSRaftRPCResult(currentTerm, false);
					}
					VSRaftLogEntry entry = log.getEntry(lastApplied);
					VSCounterRequest r = (VSCounterRequest) entry.request;
					System.out.println("[appendEntries] Inkrementieren auf Wert " + (r.requestCounter + 1));
					application.applyRequest(entry);
					lastApplied++;
				}
				// Wegen Schleife zurücksetzen
				lastApplied = leaderCommit;

				commitIndex = leaderCommit;
			}

			// Sicherungspunkt anlegen
			snapshotHelper();

			if(commitIndex < leaderCommit){
				commitIndex = Math.min(leaderCommit, log.getEntry(lastApplied).index);
			}


			// application.status(VSRaftRole.FOLLOWER, leaderId);
			application.status(currentRole, leaderId);

			System.out.println("===== appendEntries Heartbeat beendet: " + application.counter + " =====");
			System.out.println();
			System.out.println();
			return new VSRaftRPCResult(currentTerm, true);
		}

	// Wie ist das einzuordnen?
		if(currentTerm < term) {
			// if LEADER or CANDIDATE with outdated term, immediately revert to FOLLOWER state
			if(currentRole == VSRaftRole.CANDIDATE || currentRole == VSRaftRole.LEADER) {
				currentRole = VSRaftRole.FOLLOWER;
				application.status(currentRole, -1);
				System.out.println("Outdated Term, becoming a Follower");
				currentTerm = term;
				return new VSRaftRPCResult(currentTerm, false);
			}
			// else if FOLLOWER with smaller term: update own term
			currentTerm = term;
			// kein status aufruf, da nur term geändert und nicht eigenen Zustand ?
			timer.cancel();
			timer = new Timer();
			timer.schedule(new CandidateState(this), ELECTION_TIMEOUT);

			application.status(currentRole, leaderId);
			return new VSRaftRPCResult(currentTerm, true);
		}
		return new VSRaftRPCResult(currentTerm, false);
	}

	private boolean excuteCommands(final long leaderCommit){
		if(leaderCommit > lastApplied){
			System.out.println("[appendEntries] Befehle ausführen");
			lastApplied++;
			while(lastApplied <= leaderCommit){
				VSRaftLogEntry entry = null;
				if(entry.request == null){ commitIndex++; continue;}
				if(log.getEntry(lastApplied) == null){
					lastApplied--;
					System.out.println("[appendEntries] log.getEntry(lastApplied = " + lastApplied + ") = null => keine Befehle im Log, warten auf nächste Replizierung des Leaders");
					System.out.println("===== appendEntries Heartbeat beendet =====");
					System.out.println();
					System.out.println();				
					return false;
				}
				entry = log.getEntry(lastApplied);
				VSCounterRequest r = (VSCounterRequest) entry.request;
				System.out.println("[appendEntries] Inkrementieren auf Wert " + (r.requestCounter + 1));
				application.applyRequest(entry);
				lastApplied++;
			}
			// Wegen Schleife zurücksetzen
			lastApplied = leaderCommit;
			commitIndex = leaderCommit;
		}
		return true;
	}

	// Helfer-Funktion, die Snapshot anlegt, wenn er notwendig wird
	// Auslagerung in Funktion, da Logik an mehr als einer Stelle benötigt wird
	private void snapshotHelper(){
		if(commitIndex - log.getStartIndex() > LOG_MAX_CAPACITY){
			System.out.println();
			System.out.println("[appendEntries] Sicherungspunkt erstellen...");
			System.out.println("[appendEntries] Ante Sicherungspunkterstellung");
			System.out.println("[appendEntries] log.getStartIndex = " + log.getStartIndex());
			System.out.println("[appendEntries] log.getLatestIndex = " + log.getLatestIndex());

			snapshot = application.createSnapshot();
			lastSnapshotIndex = lastApplied;
			lastSnapshotTerm = log.getEntry(lastApplied).term;
			log.collectGarbage(lastSnapshotIndex, lastSnapshotTerm);

			System.out.println();
			System.out.println("[appendEntries] Post Sicherungspunkterstellung");
			System.out.println("[appendEntries] log.getStartIndex = " + log.getStartIndex());
			System.out.println("[appendEntries] log.getLatestIndex = " + log.getLatestIndex());
		}
	}

	/**
	 * Appends a request to the log for ordering. If the current replica is not
	 * the leader at the moment, the request is rejected. Called by the
	 * VSCounterServer.
	 *
	 * @param request Request to append to the log
	 * @return True when the current replica is the leader, false otherwise
	 */
	public synchronized boolean orderRequest(Serializable request) {
		/*
		 * Neu eingetroffene Anfragen werden dem Replikationsprotokoll vom lokalen Zähler-Server
		 * durch einzelne Aufrufe der Methode orderRequest()übergeben
		 */
		// Nicht-Leader-Replikate müssen solche Aufrufe durch Rückgabe von false zurückweisen ohne weitere Aktionen durchzuführen
		// Laut Aufgabe ok so, aber sollen nicht-LEADER nicht die Anfragen weiterleiten an den LEADER?
		if (currentRole == VSRaftRole.FOLLOWER || currentRole == VSRaftRole.CANDIDATE) {
			System.out.println("Reject request, I am not the leader.");
			return false;
		}

		// Leader reagiert auf einen orderRequest()-Aufruf indem er die übergebene Anfrage ans Ende seines Logs anfügt,
		// die anschließende Replikation durch Aufwecken seines Protokoll-Thread anstößt,
		// und den Erhalt der Anfrage mit true bestätigt		
<<<<<<< HEAD
		this.log.addEntry(new VSRaftLogEntry(log.getLatestIndex() + 1, currentTerm, request)); // weil wir als leader nur anhängen drürfen und nichts ersetzen wollen
		System.out.println("Leader with ID: " + myId + "added a request to its log");
		// final Runnable protocol_worker = new VSRaftProtocol_Worker(this);
		// final Thread protocol_thread = new Thread(protocol_worker);
		// protocol_thread.run();  // TODO: Protokoll Thread aufwecken ?? nicht sicher ob das so geht
		this.notify();
=======
		// this.log.addEntry(
		// 	new VSRaftLogEntry(log.getLatestIndex() + 1, currentTerm, request)); // weil wir als leader nur anhängen drürfen und nichts ersetzen wollen
		System.out.println("Leader with ID " + myId + " executes orderRequests");
		System.out.println("[orderReqeust] --- log.getLatestIndex() = " + log.getLatestIndex());
		
		log.addEntry(new VSRaftLogEntry(log.getLatestIndex() + 1, currentTerm, request));
		
		// synchronized(notifyObj){
		System.out.println("[orderReqeust] Thread benachrichtigen");
		this.notify();
		// }
>>>>>>> jannik5
		return true;
	}


	@Override
	// see VSRaftProtocolService.installSnapshot for the documentation
	public synchronized int installSnapshot(int term, int leaderId, long lastIncludedIndex,
	                                        int lastIncludedTerm, Serializable data) {

		System.out.println("===== installSnapshot =====");										
		
		if(term < currentTerm){
			return currentTerm;
		}
		// Wenn letzter im Snapshot enthaltener Eintrag im Log vorliegt, muss Snapshot nicht angewandt werden
		if(log.getEntry(lastIncludedIndex) != null){
			if(log.getEntry(lastIncludedIndex).index == lastIncludedIndex
				&& log.getEntry(lastIncludedIndex).term == lastIncludedTerm){
				return currentTerm;
			}
		}
		System.out.println("[installSnapshot] log.getStartIndex = " + log.getStartIndex() + "   log.getLatestIndex = " + log.getLatestIndex());
		// Wende Snapshot an
		application.applySnapshot(data);
		System.out.println("[installSnapshot] lastIncludedIndex = " + lastIncludedIndex);
		System.out.println("[installSnapshot] application.counter = " + application.counter);
		// Setzt StartIndex im Log passend zum Snapshot
		log.collectGarbage(lastIncludedIndex, lastIncludedTerm);
		System.out.println("[installSnapshot] log.getStartIndex = " + log.getStartIndex() + "   log.getLatestIndex = " + log.getLatestIndex());

		lastApplied = lastIncludedIndex;
		// speichere Snapshot, Index, Term, falls Replikat Anführer wird
		lastSnapshotIndex = lastIncludedIndex;
		lastSnapshotTerm = lastIncludedTerm;
		snapshot = data;

		System.out.println("===== installSnapshot beendet =====");
		System.out.println();
		System.out.println();
		return currentTerm;
	}

	class CandidateState extends TimerTask {
		// VSRaftProtocol instance is used for synchronization
		final private VSRaftProtocol parent;

		public CandidateState(VSRaftProtocol parent) {
			this.parent = parent;
		}

		@Override
		public void run() {
			System.out.println();
			// increase term, set current Role to candidate and vote for self
			// int timeout = ThreadLocalRandom.current().nextInt(0, RANDOMNESS + 1) + ELECTION_TIMEOUT;
			// votedFor = -1;
			int timeout = ((int) (Math.random() * 150)) + 150;
			System.out.println("[CandidateState] Sleeping for " + timeout + " ms before own vote");
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			// Stimme bereits abgegeben
			if(votedFor != -1){
				currentRole = VSRaftRole.FOLLOWER;
				application.status(currentRole, -1);
				parent.timer.schedule(new CandidateState(parent), VSRaftProtocol.ELECTION_TIMEOUT);
				// Stimme zurücksetzen
				votedFor = -1;
			// „Erstes“ Replikat, das Stimme abgibt
			} else {
				currentTerm++;
				System.out.println("Current term: " + currentTerm);
				currentRole = VSRaftRole.CANDIDATE;
				application.status(currentRole, -1);
				votedFor = myId;
				// count which vote the candidate gets, start at 1 because candidate votes for itself
				int successCounter = 1;
				boolean success = true;
				// Zähler, um festzustellen, ob man das einzige Replikat ist
				for (int i = 0; i < addresses.length; i++) {
					// request vote from every other replica
					if (i != myId) {
						try {
							System.out.println("[CandidateState] log.getLatestIndex() = " + log.getLatestIndex() + "   log.latestTerm = " + log.getLatestEntry().term);
							VSRaftProtocolService stub = getStub(i);
							VSRaftRPCResult result 
								= stub.requestVote(	currentTerm,
													myId,
													log.getLatestIndex(),
													log.getLatestEntry().term);
							if (result.term > currentTerm) {
								// stop the voting process if another replica has a higher term
								currentTerm = result.term;
								currentRole = VSRaftRole.FOLLOWER;
								application.status(currentRole, -1);
								votedFor = -1;
								// parent.schedule(new CandidateState(parent), VSRaftProtocol.ELECTION_TIMEOUT);
								success = false;
								break;
							}
							if (result.success) {
								successCounter++;
							}
						} catch (RemoteException e) {
							System.out.println("[CandidateState] Could not reach Replica with Id: " + i);
							discardStub(i);
						}
					}
				}
				// if candidate gets enough votes, set as leader and notify application
				if (success && successCounter >= Math.floor(addresses.length / 2) + 1) {
					currentRole = VSRaftRole.LEADER;
					application.status(currentRole, myId);
					System.out.println("Currently leader, got " + successCounter + " votes");
					Runnable lt = new LeaderThread();
					Thread t = new Thread(lt);
					t.start();
				} else {
					// if candidate does not get enough votes, revert to follower state
					currentRole = VSRaftRole.FOLLOWER;
					application.status(currentRole, -1);
					parent.timer.schedule(new CandidateState(parent), timeout);
				}
				// Stimme zurücksetzen
				votedFor = -1;
			}
		}
	}

	class LeaderThread implements Runnable{

		public LeaderThread(){
			for(int i = 0; i < addresses.length; i++) {
				nextIndex[i] = nextIndex[i] == 0 ? log.getLatestIndex() + 1 : nextIndex[i];
			}
			matchIndex[myId] = lastApplied;
		}

		@Override
		public void run() {
			int leader = myId;
			boolean endLeader = false;
			// Beim Start „nextIndex“ initialisieren (im Konstruktor)
			int count_replicated_logs = 1;
			while(!endLeader){
				// Zurücksetzen des Zählers vor jedem Log-Replizierungsdurchlauf
				count_replicated_logs = 1;
				// Gibt es keine neuen Befehle, wartet der Thread bis es soweit ist
				synchronized(this){
					try {
						System.out.println("[LeaderThread] Thread wartet...");
						System.out.println();
						this.wait(KEEP_ALIVE_TIMEOUT);
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					for(int i = 0; i < addresses.length; i++){
						if(i == myId) continue;
						
						// Unterscheidung ob Heartbeat oder Replizierung
						// Annahme nextInd zeigt auf nächstes zu schickendes noch leeres Feld
						if(nextIndex[i] <= log.getLatestIndex()) {
							System.out.println();
							System.out.println("===== Replizierung =====");

							// Heartbeat und Leader bearbeiten beide nextIndex, während der Leader repliziert sollte aber nicht durch einen Heartbeat-Aufruf
							// nextIndex[i] verändert werden. Nur die Schreibzugriffe zu synchronisieren reicht nicht, da die Heartbeats ständig geschickt werden
							
							System.out.println("[LeaderThread] Repliziere Log bei Replikat " + i + "...");
							System.out.println("[LeaderThread] log.getStartIndex = " + log.getStartIndex());
							System.out.println("[LeaderThread] log.getLatestIndex = " + log.getLatestIndex());
							System.out.println("[LeaderThread] nextIndex[" + i + "] = " + nextIndex[i]);
							try{
								VSRaftRPCResult result = null;
								VSRaftProtocolService stub = null;
									stub = getStub(i);

								int replicaTerm_from_installSnapshot_call = -1;
								
								long startIndex = log.getStartIndex();
								long index = nextIndex[i] <= startIndex + 1 ? startIndex + 1 : nextIndex[i] - 1;
								if(nextIndex[i] < log.getStartIndex() + 1){
								// oder
								// if(matchIndex[i] < log.getStartIndex()) ? 
									System.out.println();
									System.out.println("[LeaderThread] Verteile Sicherungspunkt an Replikat " + i + " (nextIndex[" + i + "] < log.getStartIndex + 1)...");
									System.out.println("[LeaderThread] Letzter Index im Snapshot = " + lastSnapshotIndex);
									replicaTerm_from_installSnapshot_call = stub.installSnapshot(currentTerm, myId, lastSnapshotIndex, lastSnapshotTerm, snapshot);
									if(replicaTerm_from_installSnapshot_call <= currentTerm){
										nextIndex[i] = lastSnapshotIndex + 1;
										System.out.println("[LeaderThread] nextIndex[" + i + "] = " + nextIndex[i] + " (nach erfolgreicher Snapshotverteilung");
									}
									if(nextIndex[i] > log.getLatestIndex()){
										continue;
									}
								}
								index = nextIndex[i] <= 0 ? 0 : nextIndex[i] - 1;
								System.out.println("[LeaderThread] prevLogIndex der mitgeschickt wird: " + index);
								result = stub.appendEntries(currentTerm, 
															myId, 
															log.getEntry(index).index,	
															log.getEntry(index).term,
															log.getEntriesSince(index + 1),
															commitIndex);
								
								if(result.success) {
									System.out.println("[LeaderThread] Log bei Replikat " + i + " repliziert");
									count_replicated_logs++;
									nextIndex[i] = log.getLatestIndex() + 1;
									// höchster replizierter Index
									matchIndex[i] = log.getLatestIndex();
									if(count_replicated_logs >= Math.floor(addresses.length / 2) + 1) {
										// Auf 0 setzen, da dieser Bereich nur einmal betreten werden soll.
										//	Sobald auf zwei (bspw. einschließlich Leader) repliziert wurde, wird dieser Bereich
										// 	betreten und Anführer übergibt Befehle zur Ausführung
										//	Wenn dritte Repli. auch klappt, würde dieser Bereich wieder betreten werden
										//		=> darf er aber nicht, da Befehle schon ausgeführt wurden
										//	setzt man count_matched_indices = 0, wird Code hier auch nicht mehr ausgeführt, da
										//	das Hochzählen bei Erfolg maximal unter der Hälfte bleibt (auf mehr als die Hälfte wurde ja schon repliziert)
										count_replicated_logs = 0;
										commitIndex = log.getLatestIndex();
										
										// matchIndex[leader] += 1;	// leader == myId (s. ganz oben von run())
										lastApplied += 1;
										while(lastApplied <= commitIndex){
											// falls matchIndex == 0 würde auf ersten Log-Eintrag zugegriffen werden, der leer ist => Fehler
											VSCounterRequest r = (VSCounterRequest) log.getEntry(lastApplied).request;
											
											// System.out.println("[LeaderThread] matchIndex[myId] = " + matchIndex[leader]);
											
											System.out.println("[LeaderThread] Inkrementieren auf Wert " + (r.requestCounter + 1));
											application.applyRequest( log.getEntry(lastApplied) );
											lastApplied += 1;
										}
										// matchIndex[leader == myId] auf höchsten Wert setzen, der angewandt wurde
										// matchIndex[leader] = commitIndex;
										lastApplied = commitIndex;									
									}
									System.out.println();
								}
								if(!result.success && result.term <= currentTerm) {
									// kein gleicher Term/Index runter zählen bis passender gefunden wird, damit in nächster Nachricht der vorherige Term/Index verglichen werden kann
									nextIndex[i] = nextIndex[i] <= 0 ? 0 : (nextIndex[i]-1);	
									//Zurücksetzen der Schleifenvariable, um Log-Replizierung für Replikat i mit neuem „nextIndex“ zu wiederholen
									i = i-1;
								}
								
								// Term veraltet => In Anhänger-Zustand wechseln
								if(result.term > currentTerm || replicaTerm_from_installSnapshot_call > currentTerm){
									currentRole = VSRaftRole.FOLLOWER;
									application.status(currentRole, -1);
									endLeader = true;
									break;
								}
							} catch(RemoteException e){
								System.out.println("Replicating logs failed to reach Replica with Id: " + i);
								// replicasIdx[i] = 0;
								// Replikat entfernen, wenn nicht erreichbar?
								discardStub(i);
								// nextIndex[i] = 0;
							}
							System.out.println("[LeaderThread] Replizierung für Replikat " + i + " beendet");
							System.out.println();
						}
						else {
							// Heartbeats
							// for(int i = 0; i < addresses.length; i++) {
							// 	nextIndex[i] = nextIndex[i] == 0 ? log.getLatestIndex() + 1 : nextIndex[i];
							// }
							int heartbeatCounter = 0;
							System.out.println("[Heartbeat] " + heartbeatCounter++);
							System.out.println("[Heartbeat] nextIndex[" + i + "] = " + nextIndex[i]);
							try {
								VSRaftProtocolService stub = null;
								VSRaftRPCResult result = null;
								stub = getStub(i);
								// leere Nachricht entspricht hearbeat
								// Falls nextIndex[i]==0, kann man man nicht „nextIndex[i]-1“ in „log.getEntry()“ einsetzen, da auf dem Resultat (null) „.index“ aufgerufen wird => NullPointerException
								VSRaftLogEntry prevLog = log.getEntry(nextIndex[i] <= 0 ? 0 : (nextIndex[i]-1));
								long prevLogInd = -1;
								int prevLogTerm = -1;
								if(prevLog == null){
									prevLogInd = 0;
									prevLogTerm = -1;
								} else {
									prevLogInd = prevLog.index;
									prevLogTerm = prevLog.term;
								}
								result = stub.appendEntries(currentTerm, myId, prevLogInd, prevLogTerm, null, commitIndex);
								if(!result.success) {
									nextIndex[i] = nextIndex[i] <= 0 ? 0 : (nextIndex[i]-1);
								}
								// Wenn Replikat höheren Term hat, wird man sofort zum Follower
								if(result.term > currentTerm){
									currentRole = VSRaftRole.FOLLOWER;
									application.status(currentRole, -1);
									endLeader = true;
									break; // um aus for schleife zu brechen
								}
							} catch (RemoteException e) {
								System.out.println("[Heartbeat_Thread] Could not reach Replica with Id: " + i);
								discardStub(i);
								// matchIndex[i] = 0;
							}
															
							System.out.println();
						}
					}
				}

				
				if(!endLeader
					&& lastApplied > 0
					&& log.getLatestIndex() - log.getStartIndex() > LOG_MAX_CAPACITY){
					System.out.println();


					System.out.println("[LeaderThread] Erstelle Snapshot...");
					System.out.println("[LeaderThread] log.getStartIndex = " + log.getStartIndex());
					System.out.println("[LeaderThread] log.getLatestIndex = " + log.getLatestIndex());
					snapshot = application.createSnapshot();
					lastSnapshotIndex = lastApplied;
					System.out.println("[LeaderThread] lastApplied " + lastApplied);
					lastSnapshotTerm = log.getEntry(lastApplied).term;
					log.collectGarbage(lastApplied - 1, log.getEntry(lastApplied - 1).term);
					System.out.println("[LeaderThread] log.getStartIndex = " + log.getStartIndex() + " (Nach Snapshoterstellung)");
					
					// for(int j = 0; j < addresses.length; j++){
					// 	if(j == leader) continue;
					// 	// nextIndex[j] += 1;
					// 	System.out.println("[LeaderThread] nextIndex[" + j + "] = " + nextIndex[j]);
					// }
				}
				System.out.println("===== Replizierung beendet =====");
				System.out.println();
			}
		}
	}	
}
