package vsue.communication;


public interface VSRemoteReference {
    public void setName(String name);
    public String getName();
    public void sayHello();
    public int getPort();
    public int getObjectID();
}