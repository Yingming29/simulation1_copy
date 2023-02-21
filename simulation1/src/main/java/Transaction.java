import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Transaction {
    double AT;
    double DT;
    double serviceTime;
    int writes;
    ArrayList<Integer> objects = new ArrayList<>();
    // 1:commited -1: aborted
    int status;
    int id;
    public Transaction(){}

    public void randomTxn(double currentTime, int maxObjects, double[] pros, int id) throws Exception {
        this.setAT(currentTime);
        this.setDT(Double.MAX_VALUE);
        int size = this.getWritesSize(pros, getRandomWrites());
        this.setWrites(size);
        this.setRandomObjects(maxObjects);
        this.setId(id);
    }
    public double getRandomWrites(){
        Random r = new Random();
        return r.nextDouble();
    }
    public void setRandomObjects(int maxObjects) throws Exception {
        if (this.objects == null){
            this.setObjects(new ArrayList<>());
        }
        int[] arr = new int[maxObjects]; // 10
        // 0-20
        for (int i = 0; i < maxObjects; i++) {
            arr[i] = i;
        }
        // 0-9
        for (int i = 0; i < this.writes; i++) {
            Random random = new Random();
            int r = random.nextInt(maxObjects - i);
            int temp = arr[r];
            arr[r] = arr[arr.length - 1 -i];
            arr[arr.length - 1 - i] = temp;
            this.getObjects().add(temp);
        }
    }
    // can be deleted
    public void setDTbyAddition(){
        double DT = this.serviceTime + this.AT;
        this.setDT(DT);
    }
    @Override
    public String toString(){

        String str = "---Txn---" + "\nTxn ID: " + this.id + "\nAT: " + this.AT + "\nDT: " + this.DT + "\nService time: " + this.serviceTime + "\nStatus: " + this.status
                + "\nWrites size: "+ this.writes + "\nObjects: " + this.objects+ "\n---Txn---" ;

        return str;
    }
    public int getWritesSize(double[] probabilities, Double d) throws Exception {
        int result = 0;
        if (d < 0 || d > 1){
            throw new Exception("The random probabilities is error.");
        }
        double[] temp = new double[probabilities.length];
        for (int i = 0; i < probabilities.length; i++) {
            if (i == 0){
                temp[i] = probabilities[i];
            } else {
//                System.out.println(temp[i-1] + " " + probabilities[i]);
                temp[i] = temp[i-1] + probabilities[i];
            }
        }
        System.out.println(Arrays.toString(temp));
        for (int i = 0; i < temp.length; i++) {
            //System.out.println("i = " + i + ", temp[i]: " + temp[i]);
            if (i == 0){
                if (d < temp[i]){
                    System.out.println("Writes of the txn: 0, " + d + " < " + temp[i]);
                    break;
                }
            } else {
                if (d >= temp[i-1] && d <= temp[i]){
                    System.out.println("Writes of the txn: " + i + ", " + temp[i-1] + " <= " + d + " <= " + temp[i]);
                    result = i;
                    break;
                }
            }
        }
        return result;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getServiceTime() {
        return serviceTime;
    }

    public void setServiceTime(double serviceTime) {
        this.serviceTime = serviceTime;
    }

    public double getAT() {
        return AT;
    }

    public void setAT(double AT) {
        this.AT = AT;
    }

    public double getDT() {
        return DT;
    }

    public void setDT(double DT) {
        this.DT = DT;
    }

    public int getWrites() {
        return writes;
    }

    public void setWrites(int writes) {
        this.writes = writes;
    }

    public ArrayList<Integer> getObjects() {
        return objects;
    }

    public void setObjects(ArrayList<Integer> objects) {
        this.objects = objects;
    }
}
