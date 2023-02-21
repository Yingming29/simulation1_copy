import java.util.*;

public class Test2 {
    public static void main(String[] args) throws Exception {
//        LinkedList<Integer> l1 = new LinkedList<>();
//        l1.add(0);
//        l1.add(1);
//        l1.add(2);
//        l1.add(3);
//        System.out.println("l1" + l1);
//
//        LinkedList<Integer> l2 = new LinkedList<>();
//        l2.add(0);
//        l2.add(1);
//        l2.add(2);
//        l2.add(3);
//        l2.add(4);
//        l2.add(5);
//        System.out.println("l2" + l2);
//        boolean result = detectConflict(l1, l2);
//        System.out.println(result);

        // boolean b = abortOrNot();
        // System.out.println(b);

//        for (int i = 0; i < 100000000; i++) {
//            Random r = new Random();
//            double d = r.nextDouble();
//            if (d == 0.0 || d == 0.5 || d == 1.0){
//                System.out.println(d);
//            }
//            if (d > 1){
//                System.out.println(d);
//            }
//        }
//        System.out.println("----------------------------------");
//        for (Integer integer : l2) {
//            System.out.println(integer);
//        }
//
//        System.out.println("----------------------------------");
//        l2.remove(0);
//        l2.add(7);
//        l2.add(10);
//        for (Integer integer : l2) {
//            System.out.println(integer);
//        }
//        LinkedList<Integer> objs = new LinkedList<>();
//        int[] arr = new int[10]; // 10
//        // 0-20
//        for (int i = 0; i < 10; i++) {
//            arr[i] = i;
//        }
//        System.out.println("All objects:" + Arrays.toString(arr));
        // generate 10 objects
        // no of objects from 0-19.
        //
//        for (int i = 0; i < 5; i++) {
//            Random random = new Random();
//            int r = random.nextInt(10 - i) ;
//            System.out.println("Random position = " + r);
//            int temp = arr[r];
//            System.out.println("Random value = " + temp);
//            arr[r] = arr[arr.length - 1 - i];
//            arr[arr.length - 1 - i] = temp;
//            objs.add(temp);
//        }
//        System.out.println(objs);
//        System.out.println("All objects:" + Arrays.toString(arr));
//
//
//        System.out.println(Double.MAX_VALUE);
//
//        Worker worker = new Worker();
//        worker.setServersSize(10);
//        LinkedList<Transaction> list = new LinkedList<>();
//        Transaction t1 = new Transaction();
//        Transaction t2 = new Transaction();
//        Transaction t3 = new Transaction();
//        t1.setAT(1.0);
//        t1.setServiceTime(1.0);
//        list.add(t1);
//        list.add(t2);
//        list.add(t3);
//        worker.setQueue(list);
//        System.out.println("____________________");
//        LinkedList<Transaction> list2 = new LinkedList<>();
//        list2.add(t1);
//        list2.add(t2);
//        list2.add(t3);
//        if (list.get(0).equals(list2.get(0))){
//            System.out.println("equal");
//        }
        String[] a = args;
        double[] d = new double[args.length - 3];
        System.out.println(a.length); // 6
        System.out.println(d.length); // 3
        // 0 1 2 3 4 5
        String[] dStr = Arrays.copyOfRange(a, 3, a.length);
        System.out.println(dStr.length);
        for (int i = 0; i < d.length; i++) {
            System.out.println(dStr[i]);
        }


        String str1 = "a b c d";
        String str2 = "1 2 3 4";
        System.out.println("----------------------------------");
        System.out.println(args[args.length - 1]);
        String input = args[args.length - 1];
        String delimiter = " ";
        String[] temp = input.split(delimiter);
        double[] probability = new double[temp.length];
        for (int i = 0; i < temp.length; i++) {
            probability[i] = Double.parseDouble(temp[i]);
//            System.out.println(probability[i]);
        }
        System.out.println("----------------------------------");
    }


    public static Double testRandomAverage(int size, int max){
        Random r = new Random();
        LinkedList<Integer> l = new LinkedList<>();
        int total = 0;
        for (int i = 0; i < size; i++) {
            int random = r.nextInt(max) + 1;
            l.add(random);
            total += random;
        }
        return total * 1.0 / size;
    }
    public void order(){
        Transaction temp;
        LinkedList<Transaction> list = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            Transaction txn = new Transaction();
            txn.setDT(new Random().nextDouble());
            list.add(txn);
            System.out.println(txn.getDT());
        }
        Transaction[] txnArray = list.toArray(new Transaction[0]);
        System.out.println(txnArray.length);
        for (int i = 0; i < txnArray.length - 1; i++) {
            for (int j = 0; j < txnArray.length - 1 - i; j++) {
                if (txnArray[j].getDT() > txnArray[j + 1].getDT()){
                    temp = txnArray[j];
                    txnArray[j] = txnArray[j + 1];
                    txnArray[j + 1] = temp;
                }
            }
        }
        System.out.println("end");
        for (Transaction t:
                txnArray) {
            System.out.println(t.getDT());
        }
        System.out.println("to list");
        list = new LinkedList<>(Arrays.asList(txnArray));
        list.forEach((e) -> {
            System.out.println(e.getDT());
        });
    }

    public static boolean detectConflict(List<Integer> l1, List<Integer> l2){
        boolean result = false;
        HashMap<Integer, Integer> map = new HashMap<>();
        for (int each:
             l1) {
            map.put(each, each);
        }
        LinkedList<Integer> conflict = new LinkedList<>();
        for (int each:
             l2) {
            if (map.containsKey(each)){
                conflict.add(each);
                result = true;
            }
        }
        System.out.println(conflict);
        return result;
    }

    public static boolean abortItselfOrNot(){
        boolean b = false;
        Random r = new Random();
        double d = r.nextDouble();
        System.out.println(d);
        // 0-0.5 abort itself
        // 0.5-1 abort another one
        if (d >= 0.5 && d <= 1) {
            b = true;
        }
        return b;
    }
}
