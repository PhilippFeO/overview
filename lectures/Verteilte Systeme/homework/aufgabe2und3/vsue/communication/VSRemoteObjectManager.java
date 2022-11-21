package vsue.communication;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

public class VSRemoteObjectManager{
    private static VSRemoteObjectManager instance = null;
    private Map<Integer, Remote> originalObjects = new HashMap<Integer, Remote>();
    // Falls „exportObject“ mit bereits exportiertem Objekt aufgerufen wird, wird einfach der bereits kreierte Proxy/Stub zurückgegeben
    private Map<Integer, Remote> proxies = new HashMap<Integer, Remote>();
    private VSServer vsServer = new VSServer();
    private static String host;
    
    public synchronized static VSRemoteObjectManager getInstance(){
         if (instance==null) {
             instance = new VSRemoteObjectManager();
             try {
                 host = InetAddress.getLocalHost().getHostAddress();
             } catch (UnknownHostException e) {
                 e.printStackTrace();
             }
         }
         return instance;
    }

    // Gibt den Stub zurück; Dieser wird (wasl.) an den Client geschickt
    public Remote exportObject(Remote object){
        // „id“ fungiert als Schlüssel für HashMaps
        // identityHashCode erstellt eindeutigen HashCode für jedes Objekt
        final int id = System.identityHashCode(object);
        // Falls „exportObject“ mit bereits exportiertem Objekt aufgerufen wird, wird einfach der bereits kreierte Proxy/Stub zurückgegeben
        if(originalObjects.get(id) != null){ return proxies.get(id); }

        // Alle Oberklassen abfragen, um dem Stellvertreterobjekt auch wirklich alle Schnittstellen mitzugeben
        Class<?> c = object.getClass();
        List<Class<?>> interfaces = new ArrayList<Class<?>>();
        while(c != null){
            // Prüft ob Interfaces von Remote erben, nur dann die Methoden exportieren
            for (Class<?> currentInterface : c.getInterfaces()) {
                if (Remote.class.isAssignableFrom(currentInterface)) {
                    interfaces.add(currentInterface);
                }
            }
            c = c.getSuperclass();
        }

        // Erstelle ServerSocket an neuem Port und warte auf Eingänge
        int port = vsServer.addServerSocket();

        final VSRemoteReference ref = new VSRemoteReferenceImpl(host, port, id);
        final VSInvocationHandler handler = new VSInvocationHandler(ref);
        final Remote stub = (Remote) Proxy.newProxyInstance(
            object.getClass().getClassLoader(),
            interfaces.toArray(new Class[interfaces.size()]),
            handler);
        // Das Objekt, NICHT den Proxy in die HashMap einfügen, da sonst die „invoke“-Methode des
        // InvocationHandlers ausgeführt wird und die verschickt die notwendigen Daten, zum Server.
        // Der Server ruft „invokeMethod“ auf, dort wird wiederum die passende Methode per Reflection
        // des Objekt genutzt. Würde man das Proxy-Objekt aufrufen, würde man sich ständig im Kreis drehen
        originalObjects.put(id, object);
        proxies.put(id, stub);
        return stub;
    }

    public void unexportObject(Remote object){
        final int id = System.identityHashCode(object);
        originalObjects.remove(id);
        proxies.remove(id);
        // TODO: remove serverSocker for unexported Object
        // vsServer.removePort(object.);
    }

    // Ruft die passende Methode (Reflection) des per „objectID“-identifizierten Objekts auf
    public Object invokeMethod(int objectID, String genericMethodName , Object[] args)
        throws InvocationTargetException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, SecurityException {
        // s. Folien: Reflection#5
        final Remote objectForMethodCall = originalObjects.get(objectID);
        Class<?> c = objectForMethodCall.getClass();

        Object returnValue = null;
        Method method = null;
        // Iterieren über alle Interfaces, abgleichen aller Methoden der Interfaces als generische Strings mit eingehender Methode
        Class<?>[] interfaces = c.getInterfaces();
        for (Class<?> currentInterface : interfaces) {
            for(Method m : currentInterface.getMethods()){
                if(m.toGenericString().equals(genericMethodName)){
                    method = m;
                }
            }
        }

        // Die hier auftretenden Exceptions werden in „VSServer“ gefangen, s. dort für mehr Informationen
        returnValue = method.invoke(objectForMethodCall, args);

        Object exportedObject = getExportedObject(returnValue);
        if (exportedObject != null) {
            returnValue = exportedObject;
        }

        return returnValue;
    }

    // Gibt proxy des Objekts zurück falls existent, sonst null
    public Remote getExportedObject(Object object) {
        return proxies.get(System.identityHashCode(object));
    }

}