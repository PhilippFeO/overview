package mw.zookeeper;


/**
 * Class for signaling ZooKeeper-specific exceptions.
 */
public class MWZooKeeperException extends Exception {

	public String message;

	public MWZooKeeperException(String message) {
		super(message);
		this.message = message;
	}

}
