package linda.server;


import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import linda.Callback;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.Tuple;
import linda.shm.CentralizedLinda;

public class LindaRemoteSpaceImpl extends UnicastRemoteObject implements LindaRemoteSpace {

    public class CallbackToClient implements Callback {

        @Override
        public void call(Tuple t) {
            try {
            System.out.println("Calling back");
            RemoteCallback remoteCallBack = (RemoteCallback) Naming.lookup("//localhost:4100/RemoteCallback");
            remoteCallBack.callbackNotify(t);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private CentralizedLinda tupleSpace;


    public LindaRemoteSpaceImpl(CentralizedLinda linda) throws RemoteException {

        this.tupleSpace = linda;

    }

    @Override
    public void write (Tuple t) {
        try{
        System.out.println("Writing ");
        tupleSpace.write(t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Tuple read(Tuple template) throws RemoteException {

        return tupleSpace.read(template);

    }

    @Override
    public Tuple take(Tuple template) throws RemoteException {
        return tupleSpace.take(template);

    }

    @Override
    public Tuple tryRead(Tuple template) throws RemoteException {
        return tupleSpace.tryRead(template);
    }

    @Override
    public Tuple tryTake(Tuple template) throws RemoteException {
        return tupleSpace.tryTake(template);
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) throws RemoteException {

        return tupleSpace.readAll(template);

    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) throws RemoteException {
       
        return tupleSpace.takeAll(template);
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, String CallbackRegistry) throws RemoteException {

        try {
        //Callback c = new CallbackToClient();
        tupleSpace.eventRegister(mode, timing, template, new CallbackToClient());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void debug (String prefix) {
        tupleSpace.debug(prefix);
    }
  
}
