package linda.primenumbers;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import linda.Tuple;
import linda.shm.CentralizedLinda;
import linda.Linda;

public class PrimeThread implements Runnable {

    private Linda tupleSpace = new CentralizedLinda();
    private int startInt;
    private int upperBound;

    public PrimeThread(Linda linda,int primeInt,int upperBound) {
        this.tupleSpace = linda;
        this.startInt = primeInt;
        this.upperBound = upperBound;
    }

    public int getInt(Tuple t) {

        Iterator<Serializable> iter = t.iterator();
        return (Integer) iter.next();
        
    }

    @Override
    public void run() {
        
        Collection<Tuple> tupleList = tupleSpace.readAll(new Tuple(Integer.class,Boolean.class));
        Iterator<Tuple> iter = tupleList.iterator();

        while(iter.hasNext()) {
            int i = getInt(iter.next());
            if ((this.startInt % i) == 0) {
                tupleSpace.tryTake(new Tuple(i,Boolean.class));
            }
        }      
    }    
}
