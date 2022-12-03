package linda.server;

import java.rmi.RemoteException;
import linda.Tuple;
import java.rmi.server.UnicastRemoteObject;

public class RemoteCallbackImpl extends UnicastRemoteObject implements RemoteCallback {
    
    public RemoteCallbackImpl() throws RemoteException {

    }

    @Override
    public void callbackNotify(Tuple t) {
        LindaClient.callbackEvent(t);
       
    }

    
}
