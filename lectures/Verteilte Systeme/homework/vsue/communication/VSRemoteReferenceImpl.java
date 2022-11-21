package vsue.communication;

import java.io.Serializable;

public class VSRemoteReferenceImpl  implements VSRemoteReference, Serializable {
    private String host;
    private int port;
    private int objectID;

    public VSRemoteReferenceImpl(String host, int port, int objectID) {
        this.host = host;
        this.port = port;
        this.objectID = objectID;
    }

    public VSRemoteReferenceImpl() {
    }

    public int getPort() {
        return this.port;
    }

    public int getObjectID() {
        return this.objectID;
    }

    @Override
    public void setName(String name) {
        this.host = name;        
    }

    @Override
    public String getName() {
        return this.host;
    }

    @Override
    public void sayHello() {
        System.out.println("Hallo " + host);        
    }
}
