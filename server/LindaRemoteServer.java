package linda.server;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;


import linda.shm.*;

public class LindaRemoteServer {
    
    private static CentralizedLinda linda = new CentralizedLinda();

    public static void main(String[] args) {

    try {
        LocateRegistry.createRegistry(4000);
        LindaRemoteSpaceImpl tupleSpace = new LindaRemoteSpaceImpl(linda);
        Naming.rebind("//localhost:4000/LindaRemoteSpace", tupleSpace);
        System.out.println("Remote tuple space starting.");
       } catch(Exception e) {
            e.printStackTrace();
       }
     
    }
}


