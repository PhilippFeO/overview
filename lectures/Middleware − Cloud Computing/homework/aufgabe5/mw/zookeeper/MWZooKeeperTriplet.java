package mw.zookeeper;

public class MWZooKeeperTriplet{
    public int serverId;
    public int clientId;
    public boolean requestProcessed;

    public MWZooKeeperTriplet(final int serverId, final int clientId, final boolean requestProcessed) {
        this.serverId = serverId;
        this.clientId = clientId;
        this.requestProcessed = requestProcessed;
    }
}
