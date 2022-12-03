package linda.server;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import linda.Callback;
import linda.Linda;
import linda.Tuple;
import linda.shm.CentralizedLinda;
import java.util.ArrayList;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.CopyOnWriteArrayList;

/** Client part of a client/server implementation of Linda.
 * It implements the Linda interface and propagates everything to the server it is connected to.
 * */
public class LindaClient implements Linda {

    private LindaRemoteSpace tupleSpace;
    private static ConcurrentHashMap<Callback,ArrayList<Tuple>> callbackMap = new ConcurrentHashMap<Callback,ArrayList<Tuple>> (); 
    private static CentralizedLinda centralizedLinda = new CentralizedLinda();
    private static CopyOnWriteArrayList<RemoteCallback> remoteCallbacks = new CopyOnWriteArrayList<RemoteCallback>();
    /** Initializes the Linda implementation.
     *  @param serverURI the URI of the server, e.g. "rmi://localhost:4000/LindaServer" or "//localhost:4000/LindaServer".
     */
    public LindaClient(String serverURI) {
        try {
        LindaRemoteSpace linda = (LindaRemoteSpace) Naming.lookup(serverURI);
        tupleSpace = linda;
        LocateRegistry.createRegistry(4100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(Tuple t) {
       try {
         tupleSpace.write(t);
       } catch (Exception e) {
        e.printStackTrace();
       }
    }

    @Override
    public Tuple take(Tuple template) {
        Tuple t = null;
        try {
            t = tupleSpace.take(template);
          } catch (Exception e) {
           e.printStackTrace();
          }
        return t;
    }

    @Override
    public Tuple read(Tuple template) {
        Tuple t = null;
        try {
            t = tupleSpace.read(template);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    @Override
    public Tuple tryTake(Tuple template) {
        Tuple t = null;
        try {
            t = tupleSpace.tryTake(template);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    @Override
    public Tuple tryRead(Tuple template) {
        Tuple t = null;
        try {
            t = tupleSpace.tryRead(template);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;

    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        Collection<Tuple> t = null;
        try {
           t = tupleSpace.takeAll(template);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        Collection<Tuple> t = null;
        try {
           t = tupleSpace.readAll(template);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
        
        try {
            if(!callbackMap.containsKey(callback)) {
                ArrayList<Tuple> callbackList = new ArrayList<Tuple>();
                callbackList.add(matchingTemplate(template));
                callbackMap.put(callback,callbackList);
            } else {
                callbackMap.get(callback).add(matchingTemplate(template));
            } 
            RemoteCallbackImpl remoteCallBack = new RemoteCallbackImpl();
            remoteCallbacks.add(remoteCallBack);
            Naming.rebind("//localhost:4100/RemoteCallback", remoteCallBack); 
            tupleSpace.eventRegister(mode,timing,template,"//localhost:4100/RemoteCallback");
         } catch (Exception e) {
             e.printStackTrace();
         }
        
    }

    public static void callbackEvent(Tuple t) {
        boolean b = true;
        Iterator<Callback> iter = callbackMap.keySet().iterator();
        Tuple template = matchingTemplate(t);
        while(iter.hasNext() && b) {
            Callback c = iter.next();
            ArrayList<Tuple> callbackList = callbackMap.get(c);
            if (callbackList.contains(template)) {
                c.call(t);
                callbackList.remove(template);
                try {
                    //RMI callback server shutdown
                    if(callbackList.isEmpty()) {
                        callbackMap.remove(c);
                         Naming.unbind("//localhost:4100/RemoteCallback");
                         Iterator<RemoteCallback> iterCallbacks = remoteCallbacks.iterator();
                         while(iterCallbacks.hasNext()) {
                            UnicastRemoteObject.unexportObject(iterCallbacks.next(), true);
                         }
                    }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                b = false;
            }
        }
    }

    public static Tuple matchingTemplate(Tuple t) {
        return centralizedLinda.getMatchingTemplate(t);
    }

    @Override
    public void debug(String prefix) {
        try {
            tupleSpace.debug("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
