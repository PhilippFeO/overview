package mw.zookeeper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


/**
 * Proxy class allowing a client to access the ZooKeeper service.
 *
 * Note: The implementation of this class is not thread-safe.
 */
public class MWZooKeeper {

	// #####################
	// # ZOOKEEPER METHODS #
	// #####################

	public String create(String path, byte[] data, boolean ephemeral) throws MWZooKeeperException {
		MWZooKeeperRequest request = new MWZooKeeperRequest(MWZooKeeperOperation.CREATE, path);
		request.setData(data);
		request.setEphemeral(ephemeral);
		MWZooKeeperResponse response = sendReceive(request);
		return response.getPath(); // Throws an exception in case of error
	}

	public void delete(String path, int version) throws MWZooKeeperException {
		MWZooKeeperRequest request = new MWZooKeeperRequest(MWZooKeeperOperation.DELETE, path);
		request.setVersion(version);
		MWZooKeeperResponse response = sendReceive(request);
		response.getPath(); // Throws an exception in case of error
	}

	public MWZooKeeperStat setData(String path, byte[] data, int version) throws MWZooKeeperException {
		MWZooKeeperRequest request = new MWZooKeeperRequest(MWZooKeeperOperation.SET_DATA, path);
		request.setData(data);
		request.setVersion(version);
		MWZooKeeperResponse response = sendReceive(request);
		return response.getStat(); // Throws an exception in case of error
	}

	public byte[] getData(String path, MWZooKeeperStat stat) throws MWZooKeeperException {
		MWZooKeeperRequest request = new MWZooKeeperRequest(MWZooKeeperOperation.GET_DATA, path);
		MWZooKeeperResponse response = sendReceive(request);
		MWZooKeeperStat responseStat = response.getStat(); // Throws an exception in case of error
		if(responseStat == null) throw new MWZooKeeperException("The response did not include a stat object");
		responseStat.copyTo(stat);
		return response.getData();
	}


	// ##########################
	// # COMMUNICATION HANDLING #
	// ##########################

	private Socket socket;
	private ObjectOutputStream socketOutput;
	private ObjectInputStream socketInput;


	public void openConnection(String host, int port) throws IOException {
		socket = new Socket(host, port);
		socket.setTcpNoDelay(true);
		socketOutput = new ObjectOutputStream(socket.getOutputStream());
		socketInput = new ObjectInputStream(socket.getInputStream());
	}

	public void closeConnection() throws IOException {
		socketOutput.close();
		socketInput.close();
		socket.close();
	}

	private MWZooKeeperResponse sendReceive(MWZooKeeperRequest request) throws MWZooKeeperException {
		// Check whether connection has been established
		if((socketOutput == null) || (socketInput == null)) throw new MWZooKeeperException("No connection to ZooKeeper. Please call openConnection() first.");

		// Exchange messages
		try {
			socketOutput.writeObject(request);
			socketOutput.flush();
			return (MWZooKeeperResponse) socketInput.readObject();
		} catch(Exception e) {
			throw new MWZooKeeperException(e.toString());
		}
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException, MWZooKeeperException {

		MWZooKeeper zk = new MWZooKeeper();

		if(false) {
				zk.openConnection("127.0.0.1", 6700 + Integer.parseInt(args[0]));
				System.out.println("create(): " + zk.create("asdf", new byte[5], false));
				System.out.println("create(): " + zk.create("asdf/qwer", new byte[5], false));
				System.out.println("setData()..."); zk.setData("asdf", new byte[7], 1);
				System.out.println("setData erfolgreich!");
				System.out.println("getData().length: " + zk.getData("asdf", null).length);
				System.out.println("delete()..."); zk.delete("asdf", -1);
				System.out.println("Delete erfolgreich!");
				System.out.println("getData() mit Fehlermeldung...");
				try{
						zk.getData("asdf", null);       // scheitert und beendet Verbindung => im Server überschlagen sich Fehlermeldungen
				} catch (MWZooKeeperException e){
						System.out.println(e.message);
				}
				System.out.println("Alles Erledigt.");
				zk.closeConnection();
		} else if(true) {
			zk.openConnection("127.0.0.1", 6700 + Integer.parseInt(args[0]));
			System.out.print("create(): ");
			System.out.println(zk.create("asdf", new byte[5], true));
			try{
					Thread.sleep(1000);
			} catch (InterruptedException e){
					e.printStackTrace();
			}
			zk.closeConnection();
			zk.openConnection("127.0.0.1", 6700 + Integer.parseInt(args[0]));
			// System.out.print("create(): ");
			System.out.println(zk.create("asdf", new byte[5], false));
			//System.out.println("setData()..."); zk.setData("asdf", new byte[7], 1);

			//System.out.println("Alles Erledigt.");
			zk.closeConnection();
		} else if(false) {
			zk.openConnection("127.0.0.1", 6700 + Integer.parseInt(args[0]));
			System.out.println("create(): " + zk.create("asdf", new byte[5], false));
			System.out.println("delete(): ");
			zk.delete("asdf", 1);
			System.out.println("create(): " + zk.create("asdf", new byte[5], false));
			System.out.println("delete(): ");
			zk.delete("asdf", 1);
			System.out.println("create(): " + zk.create("asdf1", new byte[5], false));
			System.out.println("create(): " + zk.create("asdf2", new byte[5], false));
			System.out.println("create(): " + zk.create("asdf3", new byte[5], false));
			try{
					Thread.sleep(10000);
			} catch (InterruptedException e){
					e.printStackTrace();
			}
			System.out.println("create(): " + zk.create("asdf4", new byte[5], false));
		}
	}

}
