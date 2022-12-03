package linda.test;

import linda.*;
import linda.Linda.eventTiming;
import linda.Linda.eventMode;

import java.io.Serializable;
import java.util.*;

public class CallbackTest {

    private static class CallbackIntSum implements Callback {
        public void call(Tuple t) {
                System.out.println("Callback on " + t);
                Iterator<Serializable> iter = t.iterator();
                int s = 0;
                while(iter.hasNext()){
                    int i = (Integer) iter.next();
                    s+=i;
                }
                System.out.println("Callback result : " + s);
        }       
    }
        

    public static void main(String[] a) {

        final Linda linda = new linda.shm.CentralizedLinda();
        //final Linda linda = new linda.server.LindaClient("//localhost:4000/MonServeur");

        System.out.println("\nWriting [Int,Int,Int]\n");
               
        for (int i = 1; i <= 10; i++) {
            final int j = i;
            new Thread() {  
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Tuple motif = new Tuple(j,j+1,j+2);
                    linda.write(motif);
                    //System.out.println("("+j+") Writing: " + "[" + String.valueOf(j) + ", " + String.valueOf(j) + "]");
                }
            }.start();
        }

        new Thread() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }           
                Tuple t1 = new Tuple(Integer.class,Integer.class,Integer.class);
                System.out.println("Event on [Int,Int,Int]\n");
                linda.eventRegister(eventMode.READ,eventTiming.IMMEDIATE, t1, new AsynchronousCallback(new CallbackIntSum()));
                linda.eventRegister(eventMode.READ,eventTiming.FUTURE,new Tuple(20,21,100),new AsynchronousCallback(new CallbackIntSum()));
                //System.out.println("Take : " + r1 + "\n");
            }
        }.start();
        
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Writing [20,21,100]");       
                linda.write(new Tuple(20,21,100));
            }
        }.start();
        
    }   
}
