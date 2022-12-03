package linda.primenumbers;

import linda.Linda;
import linda.shm.CentralizedLinda;

import java.util.ArrayList;
import java.util.Scanner;
import linda.Tuple;
import linda.primenumbers.PrimeThread;

public class ParallelPrimeNumbers {

    public static void main(String[] args) {
        final Linda tupleSpace = new CentralizedLinda();
        boolean arg = false;
        int upperBoundsquare = 100;
        Scanner sc = new Scanner(System.in);
        try {
            upperBoundsquare = Integer.parseInt(args[0]);
        } catch (Exception e1) {
            while(!arg) {
                try {
                System.out.println("Enter an integer greater than 2");
                upperBoundsquare = sc.nextInt();
                arg = true;
                } catch (NumberFormatException e2) {
                    System.out.println("Enter an integer greater than 2");
                }
            }
        }
        sc.close();
        int upperBound = (int) Math.pow(Math.floor(Math.sqrt(upperBoundsquare)),2);
        System.out.println("Computing "+ "prime numbers from 1 to " + upperBound);
        for (int k = 0; k < upperBound;k++) {
            tupleSpace.write(new Tuple((k+1),true));
        }

        for (int k = 0; k < upperBound;k++) {
            tupleSpace.write(new Tuple((k+1),true));
        }


        ArrayList<PrimeThread> threadList = new ArrayList<PrimeThread>();

        for(int threadNumber = 0; threadNumber <= Math.sqrt(upperBound); threadNumber++ ) {

           PrimeThread primeThread = threadList.get(threadNumber);
           primeThread = new PrimeThread(tupleSpace,2, upperBound);

        }
    }
}
