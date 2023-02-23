import java.util.Arrays;
import java.util.LinkedList;

public class Test3 {
    public static void main(String[] args) throws Exception {
        ///
        LinkedList<Integer> lista = new LinkedList<>();
        lista.add(3);
        lista.add(1);
        lista.add(4);
        lista.add(45);
        lista.add(9);
        lista.add(0);
        int[] arr = lista.stream().mapToInt(i -> i).toArray();
        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = 0; j < arr.length - 1 - i; j++) {
                int temp = 0;
                if (arr[j] > arr[j+1]){
                    temp = arr[j + 1];
                    arr[j + 1] = arr[j];
                    arr[j] = temp;
                }
            }
        }
        System.out.println(Arrays.toString(arr));

        ///
        SubTask s11 = new SubTask();
        s11.setEndTime(0.33);
        SubTask s12 = new SubTask();
        s12.setEndTime(0.2);
        SubTask s13 = new SubTask();
        s13.setEndTime(0.3);
        SubTask s21 = new SubTask();
        s21.setEndTime(0.44);
        SubTask s22 = new SubTask();
        s22.setEndTime(0.22);
        SubTask s31 = new SubTask();
        s31.setEndTime(0.1);
        Transaction t1 = new Transaction();
        t1.getTasks().add(s11);
        t1.getTasks().add(s12);
        t1.getTasks().add(s13);
        t1.setWorkingIndex(0);
        Transaction t2 = new Transaction();
        t2.getTasks().add(s21);
        t2.getTasks().add(s22);
        t2.setWorkingIndex(0);


        Transaction t3 = new Transaction();
        t3.getTasks().add(s31);
        t3.setWorkingIndex(0);
        System.out.println("here" + t3.getTasks().size());

        Worker worker = new Worker();
        LinkedList<Transaction> queue = new LinkedList<>();
        queue.add(t1);
        queue.add(t2);
        queue.add(t3);
        worker.setQueue(queue);
        worker.setServersSize(10);
        System.out.println(worker.getQueue().getFirst().getTasks().getFirst().getEndTime());
        LinkedList<Transaction> list1 = worker.getWorkingTxns2();
        System.out.println("------------------");
        for (Transaction each:
             list1) {
            System.out.println(each.getTasks().size());
        }
    }
}
