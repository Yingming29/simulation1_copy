import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import org.apache.commons.math3.distribution.ExponentialDistribution;

public class Test1 {

    public static void main(String[] args) throws Exception {
        //
        LinkedList<Double> list1 =  new LinkedList<>();
        ExponentialDistribution ed1 = new ExponentialDistribution(5);
        System.out.println("exponential distribution mean:" + ed1.getMean());
        System.out.println(ed1.getNumericalVariance());
        double sum = 0;
        for (int i = 0; i < 1000000; i++) {
            double rand = ed1.sample();
            sum += rand;
            list1.push(rand);
        }
        System.out.println("End");
        System.out.println(sum/list1.size());

//        for (int i = 0; i < 10; i++) {
//            System.out.println(list1.get(i));
//        }

//        Worker worker = new Worker();
//        double[] d = worker.parseProbabilities(args[0]);
//        System.out.println(Arrays.toString(d));
//        System.out.println("Start.");
//        for (int i = 0; i < 5; i++) {
//            Random r = new Random();
//            double randomProbability = r.nextDouble();
//            Transaction txn = new Transaction();
//            int result = txn.getWritesSize(d, randomProbability);
//            System.out.println("Random probability:" + randomProbability);
//            System.out.println("Random writes size:" + result);
//        }
//
//        Transaction txn1 = new Transaction();
//        txn1.setWrites(2);
//        HashMap<Integer, Integer> writesStats = new HashMap<>();
//        writesStats.put(0, 0);
//        if (writesStats.containsKey(txn1.getWrites())){
//            System.out.println(1);
//        }
    }
}
