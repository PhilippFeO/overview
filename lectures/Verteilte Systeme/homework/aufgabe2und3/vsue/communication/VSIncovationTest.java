package vsue.communication;

import java.lang.reflect.Proxy;

public class VSIncovationTest {
    public static void main(String[] args) {
        // Erzeugung des eigentlichen Objekts
        VSRemoteReference object = new VSRemoteReferenceImpl();

        // Erzeugung eines Invocation-Handler
        VSInvocationHandler handler = new VSInvocationHandler(object);

        // Proxy-Erzeugung
        ClassLoader ldr = ClassLoader.getSystemClassLoader();
        Class<?>[] intfs = new Class[] {VSRemoteReference.class};
        VSRemoteReference proxy = (VSRemoteReference) Proxy.newProxyInstance(ldr,intfs,handler);
        
        // Test: Methodenaufrufe am Proxy
        proxy.setName("Benutzer");
        proxy.sayHello();
        System.out.println(proxy.getName());
    }
}
