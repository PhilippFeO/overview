package mw.zookeeper;

import java.io.Serializable;

public class MWZooKeeperNode implements Serializable{
	public String path;
	public byte[] data;
	public int version;
	public boolean ephemeral;
	public long createdAt;
	public long zxid;

	public MWZooKeeperNode(final String path, final byte[] data, final int version, final boolean ephemeral, final long createdAt, final long zxid){
		this.path = path;
		this.data = data;
		this.version = version;
		this.ephemeral = ephemeral;
		this.createdAt = createdAt;
		this.zxid = zxid;
	}

	// Konstruktor für den Fall, dass man Knoten mit Pfad „path“ lesen möchte
	public MWZooKeeperNode(final String path){
		this.path = path;
	}

	public String toString(){
		// System.out.println("MWNode.createdat: " + this.createdAt);
		// System.out.println("MWNode.zxid: " + this.zxid);
		return new String(
				"--- NODE ---"
				+ "\nPATH: " + path
				+ "\nDATA: " + data==null ? "null" : Byte.toString(data[0]) + "  " + Byte.toUnsignedInt(data[0])
				+ "\nVERSION: " + version
				+ "\nEPHEMERAL: " + ephemeral
				+ "\nCREATEDAT: " + this.createdAt
				+ "\nZXID: " + this.zxid);
	}
	
}
