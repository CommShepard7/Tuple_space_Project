package linda.test;

import linda.*;

public class TryTakeTryReadTest {

    public static void main(String[] a) {

        //final Linda linda = new linda.shm.CentralizedLinda();
        final Linda linda = new linda.server.LindaClient("//localhost:4000/LindaRemoteSpace");

        System.out.println("\nWriting [String,String,String,String]\n");
               
        for (int i = 1; i <= 10; i++) {
            final int j = i;
            new Thread() {  
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Tuple motif = new Tuple(String.valueOf(j), String.valueOf(j),String.valueOf(j+1),String.valueOf(j+2));
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
                Tuple t1 = new Tuple("1","1");
                System.out.println("Taking " + "[1,1]\n");   
                Tuple r1 = linda.tryTake(t1);
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
                Tuple t1 = new Tuple(String.class,Integer.class);
                System.out.println("Reading " + "[String, Integer]\n");     
                Tuple r1 = linda.tryRead(t1);
                System.out.println("Read : " + r1 + "\n");
                //linda.debug("");
            }
        }.start();
    }     
}
