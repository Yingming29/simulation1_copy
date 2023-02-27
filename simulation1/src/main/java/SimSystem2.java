import java.util.HashMap;
import java.util.LinkedList;

public class SimSystem2 {
    double arrivalRate;
    double serviceRate;
    int serverSize;
    int objectSize;
    int eventSize;
    String end;
    String writeProbabilities;
    public SimSystem2(double ar, double sr, int ss, int os, int es, String end, String wp){
        this.arrivalRate = ar;
        this.serviceRate = sr;
        this.serverSize = ss;
        this.objectSize = os;
        this.eventSize = es;
        this.end = end;
        this.writeProbabilities = wp;
    }
    public LinkedList<Double> run() throws Exception {
        Worker worker = new Worker(this.arrivalRate, this.serviceRate,
                this.serverSize, this.objectSize);
        System.out.println(worker);
        double[] probabilities = worker.parseProbabilities(this.writeProbabilities);
        // parameters check
        boolean result = worker.parameterCheck();
        if (!result){
            throw new Exception("parameters do not pass.");
        }
        int eventCount = 0;
        while (true){
            if ((this.end.equals("AT") || this.end.equals("DT")) && eventCount == this.eventSize){
                System.out.println("Happens " + this.eventSize + " " + this.end + " events.");
                System.out.println("Stop and report");
                break;
            } else if (this.end.equals("Completed") && this.eventSize == worker.getFullRecord().size()){
                System.out.println("Completed " + this.eventSize + " events.");
                System.out.println("Stop and report");
                break;
            }
            // generate the first txn when the system is empty
            if (worker.getCurrentTime() == 0){
                // generate the first txn's AT time
                double firstTxnAT = worker.getEdInterarrival().sample();
                // set up the next AT time of worker
                worker.setNextAT(firstTxnAT);
                System.out.println("The first txn time: " + worker.getNextAT());
            }
            if (worker.getNextAT() < worker.getNextDT()){  // the arrival event
                // set the current time to AT
                worker.setCurrentTime(worker.getNextAT());
                // a new transaction arrives
                Transaction newTxn = new Transaction();
                newTxn.randomTxn(worker.getCurrentTime(), worker.getObjectsSize(), probabilities, worker.getTxnId());
                worker.txnIdAdd();
                newTxn.generateTasks(); // set sub-tasks
                System.out.println("AT event:");
                System.out.println("Current time:" + worker.getCurrentTime());
                // add the txn in to the queue
                worker.getQueue().add(newTxn);
                // after the arrival event
                // if only one txn in the list, directly set up the next DT time
                double subtaskServiceTime = worker.getEdService().sample();
                if (worker.getQueue().size() == 1){
                    worker.getQueue().getFirst().start1st(worker.getCurrentTime(), subtaskServiceTime);
                } else if (worker.getQueue().size() != 1 && worker.getQueue().size() <= worker.getServersSize()){
                    newTxn.start1st(worker.getCurrentTime(), subtaskServiceTime);
                    LinkedList<Transaction> list = worker.getWorkingTxns2();
                    Transaction nextDTTxn = list.getFirst(); //
                    worker.setNextDT(nextDTTxn.returnWorkingTask().getEndTime());
                    for (Transaction each:
                            list) {
                        System.out.println(each.toString());
                    }
                    System.out.println("AT: 1 < queue size <= servers' size, the new txn will work, and next DT is: " + worker.getNextDT());
                } else {
                    System.out.println("AT: queue size > servers' size, the new txn will wait.");
                }
                System.out.println("AT txn: " + newTxn);
                System.out.println("The current queue size: " + worker.getQueue().size() + ", the working txns' size: " + worker.getWorkingTxns2().size());
                // generate the next new transaction
                double nextInterarrivalTime = worker.getEdInterarrival().sample();
                // set next AT of worker
                worker.setNextAT(worker.getCurrentTime() + nextInterarrivalTime);
                System.out.println("AT event end, generated next AT time:" + worker.getNextAT());
                if (this.end.equals("AT")){
                    eventCount += 1;
                }
                worker.updateArea();
            } else if (worker.getNextDT() < worker.getNextAT()){ // departure event for a task
                System.out.println("DT event of a Task:");
                // set the current time to the DT time
                worker.setCurrentTime(worker.getNextDT());
                //get the working transactions list and order t hem by departure time ascending order
                LinkedList<Transaction> workingTxnsQueue = worker.getWorkingTxns2();
                System.out.println("Current time:" + worker.getCurrentTime());
                System.out.println("Working txns' size: " + workingTxnsQueue.size());
                // the departing txn
                Transaction currentTxn = workingTxnsQueue.getFirst();
                System.out.println("DT txn:  " + currentTxn);
                // contains multiple txns in the working list, needs to detect conflicts
                if (workingTxnsQueue.size() > 1){
                    for (int i = 1; i < workingTxnsQueue.size(); i++) {
                        System.out.println("----Detect conflicts with-----");
                        System.out.println(workingTxnsQueue.get(i).toString());
                        // detect the every two elements according to their "objects"
                        LinkedList<SubTask> conflicts = worker.detectConflict(workingTxnsQueue.get(0), workingTxnsQueue.get(i));
                        if (conflicts.size() > 0){ // detected conflicts
                            if (worker.abortItselfOrNotByTasks(conflicts)){ // 1.abort itself
                                System.out.println("----Abort itself----");
                                worker.recoverStatus();
                                currentTxn.setDT(worker.getCurrentTime());
                                currentTxn.setStatus(-1);
                                currentTxn.setAllTasksComplete(-1);
                                break;
                            } else { // abort another
                                System.out.println("----Abort another----");
                                workingTxnsQueue.get(i).setDT(worker.getCurrentTime());
                                workingTxnsQueue.get(i).setStatus(-1);
                                workingTxnsQueue.get(i).setAllTasksComplete(-1);
                            }
                        } else {  // detect no conflict between two txns
                            System.out.println("-----No conflict-----");
                        }
                        if (i == workingTxnsQueue.size() - 1 && currentTxn.getStatus() != -1){  //
                            SubTask t = currentTxn.returnWorkingTask();
                            t.setComplete(1);
                            System.out.println("Pass " + i + " detects, and the task ends successfully.");
                            currentTxn.setWorkingIndex(currentTxn.getWorkingIndex() + 1);
                            if (currentTxn.workingIndex == currentTxn.getTasks().size()){
                                currentTxn.setDT(worker.getCurrentTime());
                                currentTxn.calculateAndSetTotalService();
                                currentTxn.setStatus(1);
                                currentTxn.setWorkingIndex(currentTxn.getTasks().size() - 1);
                            } else if (currentTxn.workingIndex < currentTxn.getTasks().size()) {
                                SubTask startingTask = currentTxn.returnWorkingTask();
                                double serviceTask = worker.getEdService().sample();
                                startingTask.setStartTime(worker.getCurrentTime());
                                startingTask.setTaskServiceTime(serviceTask);
                                startingTask.setEndTime(worker.currentTime + serviceTask);
                            }
                            System.out.println("Current txn :" + currentTxn);
                        }
                    }
                } else { // only one transaction in the queue
                    System.out.println("Only one txn in the queue, no conflict.");
                    SubTask t = currentTxn.returnWorkingTask();
                    t.setComplete(1);
                    if (currentTxn.workingIndex != currentTxn.getTasks().size() - 1){
                        currentTxn.workingIndex += 1;
                    }
                    if (currentTxn.workingIndex == currentTxn.getTasks().size()){
                        currentTxn.setDT(worker.getCurrentTime());
                        currentTxn.calculateAndSetTotalService();
                        currentTxn.setStatus(1);
                        currentTxn.setWorkingIndex(currentTxn.getTasks().size() - 1);
                    } else if (currentTxn.workingIndex < currentTxn.getTasks().size()) {
                        SubTask startingTask = currentTxn.returnWorkingTask();
                        double serviceTask = worker.getEdService().sample();
                        startingTask.setStartTime(worker.getCurrentTime());
                        startingTask.setTaskServiceTime(serviceTask);
                        startingTask.setEndTime(worker.currentTime + serviceTask);
                    }
                }
                // add completetxns methods
                int comp = worker.completeTxns();
                if (comp > 0){ // completed txns' size > 0
                    System.out.println("---Start more txns---");
                    System.out.println("The current queue size: " + worker.getQueue().size());
                    int workingSize = worker.getWorkingTxns2().size();
                    System.out.println("The current working size: " + workingSize);
                    int moreTxn = worker.getServersSize() - workingSize;
                    System.out.println("The worker can service " + (worker.getServersSize() - workingSize) + " more txns.");
                    // start more transactions according to the completed txns' size after commit or abort
                    moreTxn = worker.pushTxns(moreTxn);
                    System.out.println("Actually " + moreTxn + " more txns start servicess.");
                    // after the commit or abort and start more waiting txns
                    if (worker.getWorkingTxns2().size() == 0){
                        System.out.println("Worker's queue is empty, waits for next AT.");
                        worker.setNextDT(Double.MAX_VALUE);
                        worker.updateArea();
                        worker.setLastEvent(Double.MAX_VALUE);
                    } else {
                        System.out.println("Worker's queue is not empty and looks for the next DT(earliest DT).");
                        double smallestDT = worker.getWorkingTxns2().getFirst().returnWorkingTask().getEndTime();
                        worker.setNextDT(smallestDT);
                        worker.updateArea();
                    }
                    System.out.println("------");
                    System.out.println("Next AT: " + worker.getNextAT());
                    System.out.println("Next DT: " + worker.getNextDT());
                    if (this.end.equals("DT")){
                        eventCount += 1;
                    }
                } else {
                    double smallestDT = worker.getWorkingTxns2().getFirst().returnWorkingTask().getEndTime();
                    worker.setNextDT(smallestDT);
                    System.out.println("------");
                    System.out.println("Next AT: " + worker.getNextAT());
                    System.out.println("Next DT: " + worker.getNextDT());
                }

            } else {
                throw new Exception("events error."); // equal AT and DT
            }
            System.out.println("Log size: " + worker.getFullRecord().size());
            System.out.println("............");
        }
        LinkedList<Double> results = worker.printStatistics(probabilities.length);
        return results;
    }
}
