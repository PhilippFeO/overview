package mw.zookeeper;

import java.io.Serializable;


/**
 * Class representing the result of a method that has been invoked at the ZooKeeper service. 
 */
public class MWZooKeeperResponse implements Serializable {

	private String path;
	private byte[] data;
	private MWZooKeeperStat stat;
	public MWZooKeeperException exception;
	public int clientId;
	public long time;


	public MWZooKeeperResponse() {
		this.path = null;
		this.data = null;
		this.stat = null;
		this.exception = null;
	}

	public MWZooKeeperResponse(final String path, final byte[] data, final MWZooKeeperStat stat, final int clientId, final long time){
		this.path = path;
		this.data = data;
		this.stat = stat;
		this.exception = null;
		this.clientId = clientId;
		this.time = time;
	}

	public MWZooKeeperResponse(final MWZooKeeperException e){
		this.path = null;
		this.data = null;
		this.stat = null;
		this.exception = e;
	}
	

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() throws MWZooKeeperException {
		if(exception != null) throw exception;
		return path;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public byte[] getData() throws MWZooKeeperException {
		if(exception != null) throw exception;
		return data;
	}

	public void setStat(MWZooKeeperStat stat) {
		this.stat = stat;
	}

	public MWZooKeeperStat getStat() throws MWZooKeeperException {
		if(exception != null) throw exception;
		return stat;
	}

	public void setException(MWZooKeeperException exception) {
		this.exception = exception;
	}
	
}
