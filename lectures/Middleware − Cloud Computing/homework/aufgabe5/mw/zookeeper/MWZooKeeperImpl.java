package mw.zookeeper;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import mw.zookeeper.MWZooKeeperNode;

import java.util.ArrayList;
import java.util.Comparator;

public class MWZooKeeperImpl {

	public TreeSet<MWZooKeeperNode> Z_B;	// Für den Anführer gilt, dass Z_B immer aktuell ist, also kann Anführer auf Z_B Prüfungen durchführen bevor geschrieben wird
	public ArrayList<MWZooKeeperNode> Z_A;

	private final int DEFAULT_MAXVERSION = -2;	// „-1“ ist reserviert für unbedingtes Schreiben

	public MWZooKeeperImpl(TreeSet<MWZooKeeperNode> tree) {
		Z_B = tree;
		Z_A = new ArrayList<MWZooKeeperNode>();
	}

	public MWZooKeeperImpl(){
		Z_B = new TreeSet<MWZooKeeperNode>(new NodeComparator());
		Z_A = new ArrayList<MWZooKeeperNode>();
	}

	public MWZooKeeperResponse processReadRequest(MWZooKeeperRequest request){
		MWZooKeeperNode nodeToReadFrom = null;
		SortedSet<MWZooKeeperNode> nodes;
		nodes = Z_B.tailSet(new MWZooKeeperNode(request.getPath()));
		// Fehlerfall: Knoten existiert nicht
		if(nodes.isEmpty()){
			return new MWZooKeeperResponse(new MWZooKeeperException("Der Knoten <" + request.getPath() + "> ist nicht vorhanden."));
		}
		// Knoten existiert
		nodeToReadFrom = nodes.first();
		//System.out.println("version: " + nodeToReadFrom.version);
		final MWZooKeeperResponse response = new MWZooKeeperResponse(
			nodeToReadFrom.path,
			nodeToReadFrom.data,
			new MWZooKeeperStat(
				nodeToReadFrom.version,
				nodeToReadFrom.createdAt,
				nodeToReadFrom.zxid),								// TODO zxid bei Leseanfragen?
			request.clientId,
			request.time);
		return response;
	}


	public MWZooKeeperTxn processWriteRequest(MWZooKeeperRequest request, long zxid) {
		// System.out.println("version in Impl: " + request.getVersion());
		System.out.println(request);
		final MWZooKeeperOperation op = request.getOperation();
		final int maxVersionInZ_A = findMaxVersionInZ_A(request.getPath()), 				// Man könnte auch eine HashSet<MWZooKeeperNode, Integer> verwenden
					maxVersionInZ_B = findMaxVersionInZ_B(request.getPath());
		if(op == MWZooKeeperOperation.SET_DATA){
			// initialisiere Knoten, der die neuen Daten enthält. In „applyTxn()“ werden Daten einfach übertragen
			final MWZooKeeperNode setDataOfCorrespondingNode = new MWZooKeeperNode(
					request.getPath(),
					request.getData(),					// Neue Daten
					request.getVersion() + 1,			// Inkrementiertung der Version, damit Anfragen mit derselben Versionsnummer ignoriert werden (Änderung der Versionsnummer in „applyTxn()“ zu spät)
					request.getEphemeral(),
					Instant.now().toEpochMilli(),
					zxid);
			// Knoten exisitiert in Z_B oder Z_A  mit entsprechender Version oder soll unbedingt verändert werden
			if(Math.max(maxVersionInZ_A, maxVersionInZ_B) == request.getVersion() || request.getVersion() == -1){
				Z_A.add(setDataOfCorrespondingNode);								// An sich könnte auch ein anderer Client auf diesem Knoten setData() aufrufen, man hätte als zwei Schreibanfragen. Die, die den Anführer zu erst erreicht, setzt sich durch, da sie die Versionsnummer des Knotens erhöht.
				return new MWZooKeeperTxn(setDataOfCorrespondingNode, op, request.clientId, request.time, zxid);
			} else {
				return new MWZooKeeperTxn(new MWZooKeeperException("Spezifizierter Knoten nicht vorhanden (Pfad oder Version falsch)."));
			}
		}
		if(op == MWZooKeeperOperation.DELETE){
			final MWZooKeeperNode deleteCorrespondingNode = new MWZooKeeperNode(
				request.getPath(),
				request.getData(),
				request.getVersion(),
				request.getEphemeral(),
				Instant.now().toEpochMilli(),
				zxid);		// Zeit braucht man in „applyTxn()“ für MWZooKeeperStat
			// Knoten exisitiert in Z_B oder Z_A  mit entsprechender Version oder soll unbedingt gelöscht werden
			if(Math.max(maxVersionInZ_A, maxVersionInZ_B) == request.getVersion() || request.getVersion() == -1){
				Z_A.add(deleteCorrespondingNode);
				return new MWZooKeeperTxn(deleteCorrespondingNode, op, request.clientId, request.time, zxid);
			} else {
				return new MWZooKeeperTxn(new MWZooKeeperException("Knoten existiert nicht."));
			}
		}
		if(op == MWZooKeeperOperation.CREATE){
			if(Math.max(maxVersionInZ_A, maxVersionInZ_B) == DEFAULT_MAXVERSION || request.getVersion() == -1){
				final MWZooKeeperNode createThisNode = new MWZooKeeperNode(
					request.getPath(),
					request.getData(),
					0,
					request.getEphemeral(),
					Instant.now().toEpochMilli(),
					zxid);		// Zeit braucht man in „applyTxn()“ für MWZooKeeperStat
				Z_A.add(createThisNode);
				System.out.println(createThisNode);
				return new MWZooKeeperTxn(createThisNode, op, request.clientId, request.time, zxid);
			} else {
				return new MWZooKeeperTxn(new MWZooKeeperException("Knoten existiert schon."));
			}
		}
		// Wird nur erreicht, falls spezifizierte Schreiboperation nicht vorhanden ist, wird aber nie soweit kommen, da schon davor gefiltert wird
		return null;
	}

		// „zxid“ ist eine Art Sequenznummer und kann für Garbage-Collection verwendet werden, also um den passenden Knoten aus „Z_A“ zu löschen
	public MWZooKeeperResponse applyTxn(MWZooKeeperTxn txn, long zxid) {
		MWZooKeeperNode writeThisNode = txn.writeThisNode;
		// Entferne Knoten mit passender „zxid“ aus „Z_A“, wenn „Z_A“ nicht leer ist (nur „Z_A“ des Masters ist nicht leer und deswegen kann der Knoten nur dort entfernt werden)
		if(!Z_A.isEmpty()){		// Wasl. kann man auch aus leeren Listen bedenkenlos löschen
			Z_A.removeIf(node -> node.zxid == zxid);
		}
		// CREATE
		if(txn.op == MWZooKeeperOperation.CREATE){
			Z_B.add(writeThisNode);
		// SET_DATA
		} else if(txn.op == MWZooKeeperOperation.SET_DATA){
			MWZooKeeperNode modify = Z_B.tailSet(new MWZooKeeperNode(writeThisNode.path)).first();
			modify.data = writeThisNode.data;
			++modify.version;
			modify.createdAt = writeThisNode.createdAt; // evtl. nicht notwendig
		// DELETE
		} else {
			// löscht Knoten und alle Kinder, da diese den Knotenpfad enthalten
			Z_B.removeIf(node -> node.path.contains(writeThisNode.path));
		}

		return new MWZooKeeperResponse(
			writeThisNode.path,
			writeThisNode.data,
			new MWZooKeeperStat(
				++writeThisNode.version,
				writeThisNode.createdAt,
				zxid),
			txn.clientId,
			txn.time);
	}



	// HILFSMETHODEN

	private int findMaxVersionInZ_A(final String path){
		int maxVersionInZ_A = DEFAULT_MAXVERSION;
		// Suche höchste Versionsnummer zu Knoten mit Pfad „path“
		for(MWZooKeeperNode n : Z_A){
			if(n.path.equals(path) && n.version > maxVersionInZ_A){
				maxVersionInZ_A = n.version;
			}
		}
		return maxVersionInZ_A;
	}

	private int findMaxVersionInZ_B(final String path){
		int maxVersionInZ_B = DEFAULT_MAXVERSION;
		// Suche höchste Versionsnummer zu Knoten mit Pfad „path“
		for(MWZooKeeperNode n : Z_B){
			if(n.path.equals(path) && n.version > maxVersionInZ_B){
				maxVersionInZ_B = n.version;
			}
		}
		return maxVersionInZ_B;
	}


}
