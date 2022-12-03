package linda.test;

import linda.*;


import java.util.*;

public class TakeReadTemplateTest {

    public static void main(String[] a) {

        //final Linda linda = new linda.shm.CentralizedLinda();
        final Linda linda = new linda.server.LindaClient("//localhost:4000/LindaRemoteSpace");

        System.out.println("\nWriting [String,Bool]\n");
               
        for (int i = 1; i <= 10; i++) {
            final int j = i;
            new Thread() {  
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Tuple motif = new Tuple(j, (j%2) == 0);
                    linda.write(motif);
                    //System.out.println("("+j+") Writing: " + "[" + String.valueOf(j) + ", " + String.valueOf(j) + "]");
                }
            }.start();
        }

        new Thread() {
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }           
                Tuple t1 = new Tuple(Integer.class, true);
                System.out.println("Take even number " + "[String, True]\n");   
                Tuple r1 = linda.take(t1);
                System.out.println("Take : " + r1 + "\n");
            }
        }.start();
        
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }           
                Tuple t1 = new Tuple(Integer.class, false);
                System.out.println("Read odd number " + "[String, False]\n");     
                Tuple r1 = linda.read(t1);
                System.out.println("Read : " + r1 + "\n");
                //linda.debug("");
            }
        }.start();

        new Thread() {
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }           
                Tuple t1 = new Tuple(Integer.class, true);
                System.out.println("Trying read " + "[String, False]\n");     
                Collection <Tuple> r1 = linda.readAll(t1);
                System.out.println("Read all even numbers || take : " + r1 + "\n");
                linda.debug("");
            }
        }.start();
    }  
}
