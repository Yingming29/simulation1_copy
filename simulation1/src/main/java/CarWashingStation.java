import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import org.apache.commons.math3.distribution.ExponentialDistribution;

public class CarWashingStation {
    double currentTime;  // t
    double arrivalTime;  // AT
    double departureTime;  // DT

    double lastEmpty; // E: the time of last empty
    double lastEvent; // the time of the last event for calculating area
    double area; // total area

    double interarrivalTimeAverage;
    double serviceTimeAverage;
    int departureMax;

    // N(t):  the total number of cars in the station at the time t
    // N(t) = washing cars + waiting cars
    int carTotalNumber;
    // M = N(t)-1
    int carWaitingNumber;
    // T0: total time that the machine is idle
    double totalIdleTime;
    // T: the estimated total time
    double totalTime;
    // idle fraction: T0/T
    double fractionIdle;
    // U: 1-T0/T
    double fractionBusy;
    double profit; // P: average profit per unit time
    // needs P = aU - b;
    double a;
    double b;
    HashMap<String, ArrayList<Double>> results;
    ExponentialDistribution edService;
    ExponentialDistribution edInterarrival;

    public CarWashingStation(double interarrivalTimeAverage, double serviceTimeAverage, int departureMax){
        // Initialise T and M to infinity
        this.totalTime = Double.MAX_VALUE; // T
        this.carWaitingNumber = Integer.MAX_VALUE; // M
        this.departureTime = Double.MAX_VALUE; // DT = infinity
        this.interarrivalTimeAverage = interarrivalTimeAverage;
        this.serviceTimeAverage = serviceTimeAverage;
        this.departureMax = departureMax;
        this.lastEvent = Double.MAX_VALUE;
    }
    public static void main(String[] args) throws Exception {
        // a new car washing station with rates and arrival times number
        CarWashingStation cws = new CarWashingStation(Double.parseDouble(args[0]), Double.parseDouble(args[1]),
                Integer.parseInt(args[2]));
        System.out.println("Initialization parameters:");
        System.out.println("Max departure number: " + cws.getDepartureMax() + ", interarrival Average: "
                + cws.interarrivalTimeAverage + ", service Average: " + cws.serviceTimeAverage);
        // set random ED number generator
        cws.setRandomGenerators();
        // create the hashmap for storing results
        cws.resultsCreate();
        System.out.println("Car washing station starts.");
        // parameter check
        boolean boolParameter = cws.parameterCheck();
        if (!boolParameter){
            throw new Exception("Parameter check does not pass.");
        }
        while (true){
            // generate the first car's arrival time
            if (cws.getResults().get("AT").size() == 0){
                double nextArrival = cws.randomTime(1);
                cws.setArrivalTime(nextArrival);
            }
            // departure number meets the max value, break the loop
            if (cws.getResults().get("DT").size() == cws.getDepartureMax()){
                cws.setTotalTime(cws.getCurrentTime());
                break; // print report and STOP
            } else{ // work
                // arrival event
                if (cws.getArrivalTime() < cws.getDepartureTime()){
                    cws.setCurrentTime(cws.getArrivalTime()); // set t to AT
                    cws.recordEventTime("AT", cws.getCurrentTime());
                    // if N <= M then increment N by 1
                    if (cws.getCarTotalNumber() <= cws.getCarWaitingNumber()){
                        cws.setCarTotalNumber(cws.getCarTotalNumber() + 1);
                    }
                    // if N == 1,
                    if (cws.getCarTotalNumber() == 1){
                        // set DT = t + service time
                        double nextDeparture = cws.randomTime(2);
                        cws.setDepartureTime(nextDeparture);
                        // increment T0 by AT - E
                        double totalIdleTime = cws.getTotalIdleTime() + cws.getArrivalTime() - cws.getLastEmpty();
                        cws.setTotalIdleTime(totalIdleTime);
                    }
                    // set AT to t + interarrival time
                    double nextArrival = cws.randomTime(1);
                    cws.setArrivalTime(nextArrival);
                    cws.updateArea(1);
                } else if (cws.getArrivalTime() > cws.getDepartureTime()){ // departure event
                    cws.setCurrentTime(cws.getDepartureTime()); // set t to DT
                    cws.recordEventTime("DT", cws.getCurrentTime());
                    cws.setCarTotalNumber(cws.getCarTotalNumber() - 1); // decrement N by 1
                    cws.updateArea(2);
                    if (cws.getCarTotalNumber() > 0){
                        if (cws.getResults().get("DT").size() < cws.getDepartureMax()){
                            double nextDeparture = cws.randomTime(2); // set DT to t + service time
                            cws.setDepartureTime(nextDeparture);
                        }
                    } else{ // set DT to infinity and set E to t
                        cws.setDepartureTime(Double.MAX_VALUE);
                        cws.setLastEmpty(cws.getCurrentTime());
                        cws.setLastEvent(Double.MAX_VALUE);
                    }
                } else {
                    throw new Exception("Error");
                }
            }
        }

        System.out.println("Car washing station ends.");
        if (cws.getResults().get("AT").size() != cws.getResults().get("Interarrival").size()){
            cws.getResults().get("Interarrival").remove(cws.getResults().get("Interarrival").size() - 1);
        }
        cws.calculateAndPrint();

    }

    public void updateArea(int event){
        if (this.getLastEvent() != Double.MAX_VALUE){  //
            double thisArea;
            if (event == 1){ // arrival
                thisArea = (this.getCarTotalNumber() - 1) * (this.getCurrentTime() - this.getLastEvent());
            } else { // departure
                thisArea = (this.getCarTotalNumber() + 1) * (this.getCurrentTime() - this.getLastEvent());
            }
//            System.out.println(this.area + " + " + thisArea);
            this.setArea(this.getArea() + thisArea);
        }
        this.setLastEvent(this.getCurrentTime());
    }
    public void setRandomGenerators() throws Exception {
        if (this.serviceTimeAverage > 0 && this.interarrivalTimeAverage >0){
            this.setEdService(new ExponentialDistribution(this.getServiceTimeAverage()));
            this.setEdInterarrival(new ExponentialDistribution(this.getInterarrivalTimeAverage()));
        } else {
            throw new Exception("rate parameters error");
        }
    }

    public double randomTime(int randomType) throws Exception {
        ExponentialDistribution ed;
        switch (randomType) {
            case 1 -> ed = this.getEdInterarrival();
            case 2 -> ed = this.getEdService();
            default -> throw new Exception("random type error");
        }
        double next;
        double random;
        // the first car arrival time
        if (this.getResults().get("AT").size() == 0){
            random = ed.sample();
            this.recordEventTime("Interarrival", random);
            next = this.getCurrentTime()+ random;
            return next;
        } else {
            // car leave or car (not first) arrival
            while (true){
                random = ed.sample();
                next = this.getCurrentTime()+ random;
                if (next != this.getDepartureTime() && next != this.getArrivalTime()){
                    if (randomType == 1){
                        this.recordEventTime("Interarrival", random);
                    } else {
                        this.recordEventTime("Service", random);
                    }
                    return next;
                }
            }
        }
    }

    public void calculateAndPrint(){
        // T0/T
        this.fractionIdle = this.totalIdleTime / this.getTotalTime();
        this.fractionBusy = 1 - this.fractionIdle;
        // P = aU - b
        this.setA(20.0);
        this.setB(10.0);
        double p = this.a * fractionBusy - this.b;
        this.setProfit(p);
        // actual average
        double averageService = 0.0;
        double averageArrival = 0.0;
        for (double each:this.getResults().get("Interarrival")) {
            averageArrival += each;
        }
        for (double each:this.getResults().get("Service")) {
            averageService += each;
        }
        averageService = averageService / this.getResults().get("Service").size();
        averageArrival = averageArrival / this.getResults().get("Interarrival").size();
        System.out.println("Print results.");
        // rate
        System.out.println("Average arrival: " + this.getInterarrivalTimeAverage());
        System.out.println("Average service: " + this.getServiceTimeAverage());
        System.out.println("Arrival rate: " + 1.0 / this.getInterarrivalTimeAverage());
        System.out.println("Service rate: " + 1.0 / this.getServiceTimeAverage());
        System.out.println("Arrival times: " + this.getResults().get("Interarrival").size());
        System.out.println("Actual average interarrival time: " + averageArrival);
        System.out.println("Service times: " + this.getResults().get("Service").size());
        System.out.println("Actual average service time: " + averageService);

        System.out.println("The area: " + this.getArea());
        System.out.println("Total time: " + this.getCurrentTime());
        System.out.println("Area/Total time = " + this.getArea() / this.getCurrentTime());

        double u = (1.0 / this.getInterarrivalTimeAverage()) / (1.0 / this.getServiceTimeAverage());
        double l = u / (1.0 - u);
        System.out.println("u = Arrival per unit time/Service per unit time:" + u);
        System.out.println("l = u / (1.0 - u) = " + l);
        // print result

        //System.out.println("Size of AT: " + this.getResults().get("AT").size());
        //System.out.println("Size of DT: " + this.getResults().get("DT").size());
//        System.out.println("Total idle time: " + this.getTotalIdleTime());
        // T0/T
//        System.out.println("Fraction of idle: " + this.getFractionIdle() + ", Fraction of busy (U): " + this.getFractionBusy());
//        System.out.println("a: " + this.getA() + ", b: " + this.getB());
//        System.out.println("P = aU - b, Profit per unit time: " + this.getProfit());
        // formatted
//        System.out.println("Results after format 0.000:");
//        DecimalFormat df = new DecimalFormat("0.000");
//        System.out.println("Fraction of idle: " + df.format(this.getFractionIdle()));
//        System.out.println("Fraction of busy: " + df.format(this.getFractionBusy()));
//        System.out.println("Profit per unit time: " + df.format(this.getProfit()));
        //

//        System.out.println(this.getResults().get("AT"));
//        System.out.println(this.getResults().get("Service"));
//        System.out.println(this.getResults().get("DT"));
//        System.out.println(this.getResults().get("Interarrival"));

    }

    public void recordEventTime(String event, Double time) throws Exception {
        if (!event.equals("AT") && !event.equals("DT") && !event.equals("Interarrival")
                && !event.equals("Service")){
            throw new Exception("Record error for event and time");
        }
        HashMap<String, ArrayList<Double>> results = this.getResults();
        switch (event) {
            case "AT" -> results.get("AT").add(time);
            case "DT" -> results.get("DT").add(time);
            case "Interarrival" -> results.get("Interarrival").add(time);
            default -> results.get("Service").add(time);
        }
    }
    public void resultsCreate(){
        HashMap<String, ArrayList<Double>> results = new HashMap<>();
        // AT
        results.put("AT", new ArrayList<>());
        // DT
        results.put("DT", new ArrayList<>());
        results.put("Interarrival", new ArrayList<>());
        results.put("Service", new ArrayList<>());
        this.setResults(results);
    }
    public boolean parameterCheck(){
        if (this.getServiceTimeAverage() <= 0.0){
            System.out.println("service time Average is not greater than 0.");
            return false;
        } else if (this.getInterarrivalTimeAverage() <= 0.0){
            System.out.println("interarrival time Average is not greater than 0.");
            return false;
        } else if (this.getDepartureMax() <= 0){
            System.out.println("departure max number is not greater than 0.");
            return false;
        } else if (this.getCarTotalNumber() != 0){
            System.out.println("car total number is not 0.");
            return false;
        } else if (this.getCarWaitingNumber() <= 0){
            System.out.println("car waiting number is less than or equal to 0.");
            return false;
        } else if (this.getArrivalTime() != 0.0){
            System.out.println("arrival time is not 0.");
            return false;
        } else if (this.getDepartureTime() != Double.MAX_VALUE){
            System.out.println("departure time is not infinity.");
            return false;
        } else if (this.getLastEmpty() != 0.0){
            System.out.println("last empty time is not 0.");
            return false;
        } else if (this.getTotalIdleTime() != 0.0){
            System.out.println("idle time is not 0.");
            return false;
        } else if (this.getCurrentTime() != 0.0){
            System.out.println("current time is not 0.");
            return false;
        } else if (this.getResults() == null || this.getResults().size() != 4){
            System.out.println("results Hashmap is wrong");
        }
        System.out.println("All parameters' check passed.");
        return true;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public double getLastEvent() {
        return lastEvent;
    }

    public void setLastEvent(double lastEvent) {
        this.lastEvent = lastEvent;
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

    public int getDepartureMax() {
        return departureMax;
    }

    public void setDepartureMax(int departureMax) {
        this.departureMax = departureMax;
    }

    public double getServiceTimeAverage() {
        return serviceTimeAverage;
    }

    public void setServiceTimeAverage(double serviceTimeAverage) {
        this.serviceTimeAverage = serviceTimeAverage;
    }

    public double getInterarrivalTimeAverage() {
        return interarrivalTimeAverage;
    }

    public void setInterarrivalTimeAverage(double interarrivalTimeAverage) {
        this.interarrivalTimeAverage = interarrivalTimeAverage;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public HashMap<String, ArrayList<Double>> getResults() {
        return results;
    }

    public void setResults(HashMap<String, ArrayList<Double>> results) {
        this.results = results;
    }


    public double getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(double arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public double getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(double departureTime) {
        this.departureTime = departureTime;
    }

    public double getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(double currentTime) {
        this.currentTime = currentTime;
    }
    public double getLastEmpty() {
        return lastEmpty;
    }

    public void setLastEmpty(double lastEmpty) {
        this.lastEmpty = lastEmpty;
    }

    public int getCarTotalNumber() {
        return carTotalNumber;
    }

    public void setCarTotalNumber(int carTotalNumber) {
        this.carTotalNumber = carTotalNumber;
    }

    public int getCarWaitingNumber() {
        return carWaitingNumber;
    }

    public void setCarWaitingNumber(int carWaitingNumber) {
        this.carWaitingNumber = carWaitingNumber;
    }

    public double getTotalIdleTime() {
        return totalIdleTime;
    }

    public void setTotalIdleTime(double totalIdleTime) {
        this.totalIdleTime = totalIdleTime;
    }

    public double getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(double totalTime) {
        this.totalTime = totalTime;
    }

    public double getFractionIdle() {
        return fractionIdle;
    }

    public void setFractionIdle(double fractionIdle) {
        this.fractionIdle = fractionIdle;
    }

    public double getFractionBusy() {
        return fractionBusy;
    }

    public void setFractionBusy(double fractionBusy) {
        this.fractionBusy = fractionBusy;
    }

    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }
}

