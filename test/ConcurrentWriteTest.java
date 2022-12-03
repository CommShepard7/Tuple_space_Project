package linda.test;

import linda.*;
import java.util.*;


public class ConcurrentWriteTest {

    public static void main(String[] a) {

        //final Linda linda = new linda.shm.CentralizedLinda();
        final Linda linda = new linda.server.LindaClient("//localhost:4000/LindaRemoteSpace");

        System.out.println("\nWriting [String,Int,Bool] || [Int,Int]\n");
    
        for (int i = 1; i <= 10; i++) {
            final int j = i;
            new Thread() {  
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Tuple motif = new Tuple(String.valueOf(j), j,(j%2) == 0);
                    linda.write(motif);
                    //System.out.println("("+j+") Writing: " + "[" + String.valueOf(j) + ", " + String.valueOf(j) + "]");
                }
            }.start();
        }

        for (int i = 1; i <= 10; i++) {
            final int j = i;
            new Thread() {  
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Tuple motif = new Tuple(j, j+1);
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
                Tuple t1 = new Tuple(String.class, Integer.class,Boolean.class);
                System.out.println("Reading all " + "[String, Int,Bool]\n");   
                Collection<Tuple> r1 = linda.readAll(t1);
                System.out.println("Read all : " + r1 + "\n");
            }
        }.start();
        
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }           
                Tuple t1 = new Tuple(Integer.class, Integer.class);         
                System.out.println("Reading all " + "[Int, Int]\n");     
                Collection<Tuple> r1 = linda.readAll(t1);
                System.out.println("Read all : " + r1 + "\n");
                linda.debug("");
            }
        }.start();       
    }     
}
