package vsue.communication;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class VSServer {
    protected static AtomicInteger activeThreads = new AtomicInteger(0);
    protected static List<VSConnection> vsCons = new ArrayList<VSConnection>();
    protected static List<VSObjectConnection> objCons = new ArrayList<VSObjectConnection>();

    public static void main(String[] args){
        String address = null;
        try {
            address = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Serveradresse: " + address);
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }
        int portCounter = 12345;
        while(true){
            if(activeThreads.get() != 3){
                activeThreads.incrementAndGet();
                try {
                    ServerSocket serverSocket = new ServerSocket(portCounter);
                    Socket socket = serverSocket.accept();
                    objCons.add(new VSObjectConnection(socket));
                    final Runnable  rr = new VSServer_Worker2ObjCon(objCons.get(objCons.size()-1), portCounter);
                    final Thread tt = new Thread(rr);
                    tt.start();
                    portCounter++;
                } catch (IOException e) {
                    System.out.println("Fehler beim verbinden: " + e.toString());
                }
            }
        }
    }
}
class VSServer_Worker2ObjCon extends VSServer implements Runnable {
    private VSObjectConnection objCon = null;
    private int specifiedPort;

    VSServer_Worker2ObjCon(final VSObjectConnection objCon, final int specifiedPort){
        this.objCon = objCon;
        this.specifiedPort = specifiedPort;
    }

    @Override
    public void run() {
        System.out.println("Thread mit Port: " + specifiedPort);        
        final Serializable receivedObj = objCon.receiveObject();
        objCon.sendObject(receivedObj);
        objCon.close();
        activeThreads.decrementAndGet();
    }
}
