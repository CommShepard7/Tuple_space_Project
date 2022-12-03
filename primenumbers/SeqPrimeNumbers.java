package linda.primenumbers;
import java.util.ArrayList;
import java.util.Scanner;
import linda.Tuple;
import linda.Linda;
import linda.shm.CentralizedLinda;
import java.util.Collection;

public class SeqPrimeNumbers {

    public static void main(String[] args) {
        final Linda tupleSpace = new CentralizedLinda();
        boolean arg = false;
        int upperBound = 100;
        Scanner sc = new Scanner(System.in);
        try {
            upperBound = Integer.parseInt(args[0]);
        } catch (Exception e1) {
            while(!arg) {
                try {
                System.out.println("Enter an integer greater than 2");
                upperBound = sc.nextInt();
                arg = true;
                } catch (NumberFormatException e2) {
                    System.out.println("Enter an integer greater than 2");
                }
            }
        }
        sc.close();
        for (int k = 0; k < upperBound;k++) {
            tupleSpace.write(new Tuple((k+1),true));
        }

        ArrayList <Tuple> tupleList = new ArrayList<Tuple>(tupleSpace.readAll(new Tuple(Integer.class,Boolean.class))); 
    
        int i = 2;
        
        for(i = 2 ; Math.pow(i, 2) <= upperBound;i++) {
            //System.out.println("\n\n\n" +i+"\n\n\n");
            if(tupleList.get(i-1).matches(new Tuple(Integer.class,true))) {
            //System.out.println(tupleList + "\n\n");
                for(int k = (int) Math.pow(i, 2)-1;k <= upperBound;k+=(i)) {
                    //System.out.println(k);
                    
                    tupleSpace.tryTake(new Tuple(k+1,Boolean.class));
    
                }
            }
        }

        //System.out.println(tupleList + "\n\n");
        tupleSpace.debug("");

    }
    
}
