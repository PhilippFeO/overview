package mw.zookeeper;

import java.io.Serializable;


public class MWZooKeeperTxn implements Serializable {
	public MWZooKeeperNode writeThisNode;
	public MWZooKeeperOperation op;
	public MWZooKeeperException error;
	public int clientId;
	public long time;
	public long zxid;

	// Konstruktor für den Fall, dass ein Knoten geschrieben werden darf
	public MWZooKeeperTxn(final MWZooKeeperNode writeThisNode, final MWZooKeeperOperation op, final int clientId, final long time, final long zxid){
		this.writeThisNode = writeThisNode;
		this.op = op;
		this.error = null;
		this.clientId = clientId;
		this.time = time;
		this.zxid = zxid;
	}

	// Für den Fall, dass ein Knoten nicht geschrieben werden darf (dieses Objekt erhält ausschließlich der Anhänger, der die Schreibanfrage stellte)
	public MWZooKeeperTxn(final MWZooKeeperException error){
		this.writeThisNode = null;
		this.error = error;
	}
	
	public String toString(){
		return "---\n" + writeThisNode.toString() + " \n" + "OP: " + op + "\nERROR: -";
	}
}
