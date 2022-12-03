package linda.primenumbers;

import java.util.ArrayList;
import java.util.Collection;
import linda.*;

public class NombrePremiers {

    public static void main(String[] valeur) {  
                
        final Linda linda = new linda.shm.CentralizedLinda();
        // final Linda linda = new linda.server.LindaClient("//localhost:4000/aaa");

        final int k = Integer.parseInt(valeur[0]);
        final ArrayList<Tuple> res = new ArrayList<Tuple>();
        System.out.println("\nConstruction de l'espace des entiers de 1 à " + valeur[0] + "\n");

        // Creation de l'espace des entiers    
        for (int i = 1; i <= k; i++) {
            Tuple nombre = new Tuple(i,true);
            linda.write(nombre);
        }

        new Thread() {
            public void run() {
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int i = 2; i <= k; i++) {
                    Tuple nombrePremier = new Tuple(i,true);
                    if (linda.tryRead(new Tuple(i,false)) == null) {
                        final int inc = i;
                        new Thread() {
                            public void run() {
                            try {
                                Thread.sleep(2);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            for (int j = 2*inc; j <= k; j=j+inc) {
                                linda.write(new Tuple(j,false));
                            }
                            }
                        }.start();
                    }
                }
            }         
        }.start();


    new Thread() {
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int i = 2; i <= k; i++) {
                    Tuple motif = new Tuple(i,false);
                    if (linda.tryRead(motif) == null) {
                        res.add(motif);
                    }
                }
                System.out.println("Les nombres Premiers sont :" + res);
            }
    }.start();
    }
}
