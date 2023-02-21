import java.util.LinkedList;
import java.util.Optional;

public class SimSystem {
    public static void main(String[] args) throws Exception {
        // initialize a worker 0.10526
        // parameters:
        // (double interarrivalAverage, double serviceAverage, int serversSize, int objectsSize)
        Worker worker = new Worker(Double.parseDouble(args[0]), Double.parseDouble(args[1]),
                Integer.parseInt(args[2]), Integer.parseInt(args[3]));
        System.out.println(worker);
        double[] probabilities = worker.parseProbabilities(args[6]);
        // parameters check
        boolean result = worker.parameterCheck();
        if (!result){
            throw new Exception("parameters do not pass.");
        }
        int eventCount = 0;
        while (true){
            if ((args[5].equals("AT") || args[5].equals("DT")) && eventCount == Integer.parseInt(args[4])){
                System.out.println("Happens " + Integer.parseInt(args[4]) + " " + args[5] + " events.");
                System.out.println("Stop and report");
                break;
            } else if (args[5].equals("Completed") && Integer.parseInt(args[4]) == worker.getFullRecord().size()){
                System.out.println("Completed " + Integer.parseInt(args[4]) + " events.");
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
                System.out.println("AT event:");
                System.out.println("Current time:" + worker.getCurrentTime());
                System.out.println("AT txn: " + newTxn);
                // add the txn in to the queue
                worker.getQueue().add(newTxn);
                // after the arrival event
                // if only one txn in the list, directly set up the next DT time
                if (worker.getQueue().size() == 1){
                    double serviceTime = worker.getEdService().sample(); // generate the service time of the txn and calculate the departure time of the txn
                    newTxn.setServiceTime(serviceTime);
                    newTxn.setDTbyAddition();
                    worker.setNextDT(newTxn.getDT()); // set up the next DT of the worker
                    System.out.println("Queue size == 1, next DT is: " + worker.getNextDT());
                } else if (worker.getQueue().size() != 1 && worker.getQueue().size() <= worker.getServersSize()){
                    double serviceTime = worker.getEdService().sample();
                    newTxn.setServiceTime(serviceTime);
                    newTxn.setDTbyAddition();
                    LinkedList<Transaction> list = worker.getWorkingTxns();
                    worker.setNextDT(list.get(0).getDT());
                    for (Transaction each:
                         list) {
                        System.out.println(each.toString());
                    }
                    System.out.println("AT: 1 < queue size <= servers' size, the new txn will work, and next DT is: " + worker.getNextDT());
                } else {
                    System.out.println("AT: queue size > servers' size, the new txn will wait.");
                }
                System.out.println("The current queue size: " + worker.getQueue().size() + ", the working txns' size: " + worker.getWorkingTxns().size());
                // generate the next new transaction
                double nextInterarrivalTime = worker.getEdInterarrival().sample();
                // set next AT of worker
                worker.setNextAT(worker.getCurrentTime() + nextInterarrivalTime);
                System.out.println("AT event end, generated next AT time:" + worker.getNextAT());
                if (args[5].equals("AT")){
                    eventCount += 1;
                }
                worker.updateArea();
            } else if (worker.getNextDT() < worker.getNextAT()){ // departure event
                System.out.println("DT event:");
                // set the current time to the DT time
                worker.setCurrentTime(worker.getNextDT());
                //get the working transactions list and order them by departure time ascending order
                LinkedList<Transaction> workingTxnsQueue = worker.getWorkingTxns();
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
                        boolean conflict = worker.detectConflict(workingTxnsQueue.get(0), workingTxnsQueue.get(i));
                        if (conflict){ // detected conflicts
                            if (worker.abortItselfOrNot()){ // 1.abort itself
                                System.out.println("----Abort itself----");
                                worker.recoverStatus();
                                currentTxn.setStatus(-1);
                                break;
                            } else { // abort another
                                System.out.println("----Abort another----");
                                workingTxnsQueue.get(i).setStatus(-1);
                            }
                        } else {  // detect no conflict between two txns
                            System.out.println("-----No conflict-----");
                        }
                        if (i == workingTxnsQueue.size() - 1 && currentTxn.getStatus() != -1){  //
                            currentTxn.setStatus(1);
                            System.out.println("Current txn :" + currentTxn);
                            System.out.println("Pass " + i + " detects, and txn commits successfully.");
                        }
                    }
                } else { // only one transaction in the queue
                    System.out.println("Only one txn in the queue, no conflict.");
                    // no conflict and record
                    currentTxn.setStatus(1);
                }
                // add completetxns methods
                int comp = worker.completeTxns();
                if (comp > 0){ // completed txns' size > 0
                    System.out.println("---Start more txns---");
                    System.out.println("The current queue size: " + worker.getQueue().size());
                    int workingSize = worker.getWorkingTxns().size();
                    System.out.println("The current working size: " + workingSize);
                    int moreTxn = worker.getServersSize() - workingSize;
                    System.out.println("The worker can service " + (worker.getServersSize() - workingSize) + " more txns.");
                    // start more transactions according to the completed txns' size after commit or abort
                    moreTxn = worker.pushTxns(moreTxn);
                    System.out.println("Actually " + moreTxn + " more txns start servicess.");
                    // after the commit or abort and start more waiting txns
                    if (worker.getWorkingTxns().size() == 0){
                        System.out.println("Worker's queue is empty, waits for next AT.");
                        worker.setNextDT(Double.MAX_VALUE);
                        worker.updateArea();
                        worker.setLastEvent(Double.MAX_VALUE);
                    } else {
                        System.out.println("Worker's queue is not empty and looks for the next DT(earliest DT).");
                        double smallestDT = worker.getWorkingTxns().getFirst().getDT();
                        worker.setNextDT(smallestDT);
                        worker.updateArea();
                    }
                    System.out.println("------");
                    System.out.println("Next AT: " + worker.getNextAT());
                    System.out.println("Next DT: " + worker.getNextDT());
                    if (args[5].equals("DT")){
                        eventCount += 1;
                    }
                } else {
                    throw new Exception("Cannot complete zero txn at once");
                }
            } else {
                throw new Exception("events error."); // equal AT and DT
            }
            System.out.println("Log size: " + worker.getFullRecord().size());
            System.out.println("............");
        }
        worker.printStatistics(probabilities.length);
    }
}
