package linda.search.parallel;

import linda.Linda;
import linda.Tuple;
import java.util.UUID;
import linda.search.basic.Code;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;

public class ParallelSearcher implements Runnable {

    private Linda linda;
    private int start;
    private int stop;

    public ParallelSearcher(Linda linda,int start, int stop) {
        this.linda = linda;
        this.start = start;
        this.stop = stop;
    }

    public void run() {
        System.out.println("Ready to do a search");
        Tuple treq = linda.read(new Tuple(Code.Request, UUID.class, String.class));
        UUID reqUUID = (UUID)treq.get(1);
        String req = (String) treq.get(2);
        Tuple tv;
        //System.out.println("Looking for: " + req);

        Collection<Tuple> c = linda.readAll(new Tuple(Code.Value, String.class));
        ArrayList<Tuple> tupleList = new ArrayList<Tuple>(c);
        ArrayList<Tuple> tupleListSearch = new ArrayList<Tuple>();
        
        for(int k = this.start; k < this.stop; k++) {
            tupleListSearch.add(tupleList.get(k));
        }
        
        int i = 0;
        while (!tupleListSearch.isEmpty()) {
            String val = (String) tupleListSearch.get(i).get(1);
            tupleListSearch.remove(i);
            int dist = getLevenshteinDistance(req, val);
            if (dist < 10) { // arbitrary
                linda.write(new Tuple(Code.Result, reqUUID, val, dist));
            }
        }
        linda.write(new Tuple(Code.Searcher, "success", reqUUID));
        System.out.println("Success");
    }
    
    /*****************************************************************/

    /* Levenshtein distance is rather slow */
    /* Copied from https://www.baeldung.com/java-levenshtein-distance */
    static int getLevenshteinDistance(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];
        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                }
                else if (j == 0) {
                    dp[i][j] = i;
                }
                else {
                    dp[i][j] = min(dp[i - 1][j - 1] 
                                   + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)), 
                                   dp[i - 1][j] + 1, 
                                   dp[i][j - 1] + 1);
                }
            }
        }
        return dp[x.length()][y.length()];
    }

    private static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    private static int min(int... numbers) {
        return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
    }

}