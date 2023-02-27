import org.apache.commons.math3.distribution.ExponentialDistribution;

import java.util.*;

public class Worker {
    // for exponential distribution
    double interarrivalAverage;
    double serviceAverage;
    double interarrivalRate;
    double serviceRate;
    // the size of servers and objects of worker
    int serversSize;
    int objectsSize;
    // set the default percent, half percent to abort
    // set the size of queue is infinity.
    ExponentialDistribution edService;
    ExponentialDistribution edInterarrival;
    LinkedList<Transaction> queue;
    LinkedList<Transaction> fullRecord;
    // times
    double currentTime;
    double nextAT;
    double nextDT;
    // current maximum txn ID
    int txnId;
    double area;
    double lastEvent;
    double lastSize;

    public Worker(double interarrivalRate, double serviceRate, int serversSize, int objectsSize){
        this.interarrivalRate = interarrivalRate;
        this.serviceRate = serviceRate;
        this.interarrivalAverage = 1.0/interarrivalRate;
        this.serviceAverage = 1.0/serviceRate;
        this.serversSize = serversSize;
        this.objectsSize = objectsSize;
        this.queue = new LinkedList<>();
        this.fullRecord = new LinkedList<>();
        this.edInterarrival = new ExponentialDistribution(this.interarrivalAverage); // !!!
        this.edService = new ExponentialDistribution(this.serviceAverage); // !!!
        this.nextDT = Double.MAX_VALUE;
        this.lastEvent = Double.MAX_VALUE;
    }
    public Worker(){
    }

    public double[] parseProbabilities(String probabilities){
        String delimiter = " ";
        String[] temp = probabilities.split(delimiter);
        double[] probabilityArray = new double[temp.length];
        for (int i = 0; i < temp.length; i++) {
            probabilityArray[i] = Double.parseDouble(temp[i]);
//            System.out.println(probability[i]);
        }
        return probabilityArray;
    }
    public void updateArea() throws Exception {
        if (this.getLastEvent() != Double.MAX_VALUE){  //
            double thisArea;
            thisArea =  this.getLastSize() * (this.getCurrentTime() - this.getLastEvent());
            if ((this.getCurrentTime() - this.getLastEvent()) <= 0){
                throw new Exception("area calculation cannot be negative or zero");
            }
            System.out.println("Last size: " + this.getLastSize() + " * " + (this.getCurrentTime() - this.getLastEvent()));
            System.out.println("Update the area, " + this.getArea() + " + " + thisArea + " = " + (this.getArea() + thisArea));
            this.setArea(this.getArea() + thisArea);
        }
        this.setLastEvent(this.getCurrentTime());
        this.setLastSize(this.getQueue().size());
    }
    public String toString(){
        return "The worker: \nAverage interarrival time: " + this.interarrivalAverage
                + "\nAverage service time: " + this.serviceAverage + "\nInterarrival rate: " + this.interarrivalRate
                + "\nService rate: " + this.serviceRate +"\nServers size: " + this.serversSize
                + "\nObjects' size: " + this.objectsSize + "\nThe current queue size: " + this.queue.size()
                + "\nThe current time: " + this.currentTime + "\nNext AT: " + this.getNextAT() + "\nNext DT: "
                + this.getNextDT() + "\nThe record size: " + this.getFullRecord().size() + "\nThe next txn id: " + this.getTxnId()
                + "\nThe current area: " + this.getArea() + "\nThe time of last event: " + this.getLastEvent()
                + "\nThe queue size of last event: " + this.getLastSize();
    }
    public boolean parameterCheck(){
        if (this.interarrivalAverage <= 0){
            System.out.println("Average interarrival time is wrong.");
            return false;
        } else if (this.serviceAverage <= 0){
            System.out.println("Average service time is wrong.");
            return false;
        } else if (this.serversSize <= 0){
            System.out.println("Server number is wrong.");
            return false;
        } else if (this.objectsSize <= 0){
            System.out.println("Max objects number is wrong.");
            return false;
        } else if (this.edInterarrival == null){
            System.out.println("Exponential distribution of interarrival time is null.");
            return false;
        } else if (this.edService == null){
            System.out.println("Exponential distribution of service time is null.");
            return false;
        } else if (this.interarrivalAverage != this.edInterarrival.getMean()){
            System.out.println("Mean of exponential distribution of interarrival time is different.");
            return false;
        } else if (this.serviceAverage != this.edService.getMean()){
            System.out.println("Mean of exponential distribution of service is different.");
            return false;
        } else if (this.getQueue() == null) {
            System.out.println("Queue is null.");
            return false;
        } else if (this.getFullRecord() == null) {
            System.out.println("Record is null.");
            return false;
        } else if (this.getArea() != 0) {
            System.out.println("Area is not zero");
            return false;
        } else if (this.getLastEvent() != Double.MAX_VALUE) {
            System.out.println("The last event time is not maximum value.");
            return false;
        } else if (this.getLastSize() != 0) {
            System.out.println("The last size is not zero.");
            return false;
        } else{
            System.out.println("Parameters' check passes.");
            return true;
        }
    }
    public Integer pushTxns(int size){
        if (this.getQueue().size() == 0){ // no more txns in the queue
            return 0;
        }
        int i = 0;
        for (Transaction each:this.getQueue()) {
            if (each.getWorkingIndex() == -1){
                i += 1;
            }
        }
        System.out.println(i + " txns are idle.");
        if (i > size){
            i = size;
        }
        size = 0;
        if (i == 0) { // no more idle txns
            return 0;
        } else {
            for (Transaction each:this.getQueue()) { // push not enough or enough txns to working
                if (size == i) {
                    break;
                }
                if (each.getWorkingIndex() == -1){
                    double serviceTime = this.getEdService().sample();// generate the service time of this task of a txn
                    each.start1st(this.getCurrentTime(), serviceTime);
                    size += 1;
                }
            }
        }
        return i;
    }
    public boolean abortItselfOrNotByTasks(LinkedList<SubTask> conflicts){
        boolean b = false;
        for (int i = 0; i < conflicts.size(); i++) {
            Random r = new Random();
            double d = r.nextDouble();
            if (d >= 0.5 && d <= 1) {
                b = true;
                break;
            }
        }
        // 0.5-1 abort itself
        // 0-0.5 abort another one
        return b;
    }
    public boolean abortItselfOrNot(){
        boolean b = false;
        Random r = new Random();
        double d = r.nextDouble();
        // 0.5-1 abort itself
        // 0-0.5 abort another one
        if (d >= 0.5 && d <= 1) {
            b = true;
        }
        return b;
    }
    public void recoverStatus(){
        for (Transaction t:
             this.getQueue()) {
            if (t.status == -1){
                t.setStatus(0);
            }
            for (SubTask each:
                 t.tasks) {
                if (each.complete == -1){
                    each.setComplete(0);
                }
            }
        }
    }
    public Integer completeTxns() throws Exception {
        System.out.println("Before remove, the queue size:" + this.getQueue().size());
        int i = 0;
        Iterator<Transaction> iterator = this.getQueue().iterator();
        while (iterator.hasNext()){
            Transaction t = iterator.next();
            if (t.getStatus() == -1 || t.getStatus() == 1){
                System.out.println("The completed txn: " + t);
                t.setDT(this.getCurrentTime());
                this.getFullRecord().add(t);
                iterator.remove();
                i += 1;
            }
        }
        System.out.println("Complete " + i + " txns.");
        System.out.println("The current queue size: " + this.getQueue().size());
        return i;
    }
    public LinkedList<Transaction> getWorkingTxns2() throws Exception {
        // get all working transactions
        LinkedList<Transaction> list = new LinkedList<>();
        for (Transaction each:this.getQueue()) {
            if (each.workingIndex != -1){
                list.add(each);
            }
        }
        if (list.size() > this.serversSize){
            throw new Exception("The size of working transactions is error");
        }
        Transaction temp;
        Transaction[] txnArray = list.toArray(new Transaction[0]);
        for (int i = 0; i < txnArray.length - 1; i++) {
            for (int j = 0; j < txnArray.length - 1 - i; j++) {
                Transaction t1 = txnArray[j];
                Transaction t2 = txnArray[j + 1];
                double d1 = t1.getTasks().get(t1.getWorkingIndex()).getEndTime();
                double d2 = t2.getTasks().get(t2.getWorkingIndex()).getEndTime();
                if (d1 > d2){
                    temp = txnArray[j + 1];
                    txnArray[j + 1] = txnArray[j];
                    txnArray[j] = temp;
                }
            }
        }
        list.clear();
        list.addAll(Arrays.asList(txnArray));
        return list;
    }

    public LinkedList<Double> printStatistics(int maxWritesSize) throws Exception {
        LinkedList<Double> results = new LinkedList<>();
        int totalTxnId = this.getTxnId();
        int totalRecord = this.getFullRecord().size();
        int abort = 0;
        int commit = 0;
        double totalTime = 0.0;
        HashMap<Integer, Integer> writesStats = new HashMap<>();
        for (int i = 0; i < maxWritesSize; i++) {
            writesStats.put(i, 0);
        }
        for (Transaction each:
             this.getFullRecord()) {
            int status = each.getStatus();
            totalTime += each.getDT() - each.getAT();
            if (status == -1){
                abort += 1;
            } else if (status == 1){
                commit += 1;
            }
            if (writesStats.containsKey(each.getWrites())){
                int add = writesStats.get(each.getWrites()) + 1;
                writesStats.put(each.getWrites(), add);
            }
        }
        int test = 0;
        for (Transaction each:
             this.getQueue()) {
            if (each.getStatus() == 0){
                test += 1;
            } else {
                throw new Exception(" not record");
            }
        }
        double averageResponseTime = totalTime / this.getFullRecord().size();
        results.add(averageResponseTime);
        System.out.println("The total txns' size: " + totalTxnId + "\nThe total completed txns' size: " + totalRecord + "\nAborted txns: " + abort +
                "\nCommitted txns: " + commit + "\nNot finished txns: " + test);
        System.out.println("The average response time: " + averageResponseTime);
        double abortRate = abort/this.getCurrentTime();
        System.out.println("The average aborts per unit time: " + abortRate);
        results.add(abortRate);
        System.out.println("The average commits per unit time: " + commit/this.getCurrentTime());
        double d = this.getArea()/this.getCurrentTime();
        double u = this.getInterarrivalRate()/this.getServiceRate();
        results.add(d);
        System.out.println("The total area: " + this.getArea() + "\nThe total time: " + this.getCurrentTime()
                + "\nThe total area/Total time = " + d + "\nu = Average arrival per unit time/Average service per unit time:" + u
                );
        for (int each :
             writesStats.keySet()) {
            System.out.println("The count of txns with " + each + " writes: " + writesStats.get(each));
            System.out.println("The percent: " + writesStats.get(each)*1.0/totalRecord);
        }
        return results;
    }
    public LinkedList<SubTask> detectConflict(Transaction txn1, Transaction txn2){
        // txn1 is the coming transaction
        SubTask task = txn1.getTasks().get(txn1.getWorkingIndex());
        double taskEndTime = task.getEndTime();
        HashMap<Integer, Integer> map = new HashMap<>();
        for (int each:
                txn1.getObjects()) {
            map.put(each, each);
        }
        map.remove(-1);
        LinkedList<SubTask> conflict = new LinkedList<>();
        for (SubTask each:
             txn2.getTasks()) {
            if (map.containsKey(each.getObjectTarget()) && each.getComplete()== 1 && each.getEndTime() < taskEndTime){
                conflict.add(each);
            }
        }
        System.out.println("The objects of conflict detection with two txns: " + conflict);
        return conflict;
    }

    public double getLastSize() {
        return lastSize;
    }

    public void setLastSize(double lastSize) {
        this.lastSize = lastSize;
    }

    public double getLastEvent() {
        return lastEvent;
    }

    public void setLastEvent(double lastEvent) {
        this.lastEvent = lastEvent;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public void txnIdAdd(){
        this.setTxnId(this.txnId + 1);
    }

    public double getServiceRate() {
        return serviceRate;
    }

    public void setServiceRate(double serviceRate) {
        this.serviceRate = serviceRate;
    }

    public double getInterarrivalRate() {
        return interarrivalRate;
    }

    public void setInterarrivalRate(double interarrivalRate) {
        this.interarrivalRate = interarrivalRate;
    }

    public int getTxnId() {
        return txnId;
    }

    public void setTxnId(int txnId) {
        this.txnId = txnId;
    }

    public LinkedList<Transaction> getFullRecord() {
        return fullRecord;
    }

    public void setFullRecord(LinkedList<Transaction> fullRecord) {
        this.fullRecord = fullRecord;
    }

    public double getNextDT() {
        return nextDT;
    }

    public void setNextDT(double nextDT) {
        this.nextDT = nextDT;
    }

    public double getNextAT() {
        return nextAT;
    }

    public void setNextAT(double nextAT) {
        this.nextAT = nextAT;
    }

    public double getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(double currentTime) {
        this.currentTime = currentTime;
    }

    public LinkedList<Transaction> getQueue() {
        return queue;
    }

    public void setQueue(LinkedList<Transaction> queue) {
        this.queue = queue;
    }

    public ExponentialDistribution getEdInterarrival() {
        return edInterarrival;
    }

    public void setEdInterarrival(ExponentialDistribution edInterarrival) {
        this.edInterarrival = edInterarrival;
    }

    public ExponentialDistribution getEdService() {
        return edService;
    }

    public void setEdService(ExponentialDistribution edService) {
        this.edService = edService;
    }

    public int getObjectsSize() {
        return objectsSize;
    }

    public void setObjectsSize(int objectsSize) {
        this.objectsSize = objectsSize;
    }

    public int getServersSize() {
        return serversSize;
    }

    public void setServersSize(int serversSize) {
        this.serversSize = serversSize;
    }

    public double getServiceAverage() {
        return serviceAverage;
    }

    public void setServiceAverage(double serviceAverage) {
        this.serviceAverage = serviceAverage;
    }

    public double getInterarrivalAverage() {
        return interarrivalAverage;
    }

    public void setInterarrivalAverage(double interarrivalAverage) {
        this.interarrivalAverage = interarrivalAverage;
    }
}
