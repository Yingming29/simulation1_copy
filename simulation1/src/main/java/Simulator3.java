import java.io.*;
import java.util.Date;
import java.util.LinkedList;

public class Simulator3 {
    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("input3.txt"));
        String line = reader.readLine();
        Date start = new Date();
        FileWriter fileWriter = new FileWriter("output3.txt");
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        while(line != null) {
            line = reader.readLine();
            if (line != null) {
                String[] strings = line.split(" ");
                double arrivalRate = Double.parseDouble(strings[0]);
                double serviceRate = Double.parseDouble(strings[1]);
                int serverSize = Integer.parseInt(strings[2]);
                int objectSize = Integer.parseInt(strings[3]);
                int eventSize = Integer.parseInt(strings[4]);
                String endCondition = strings[5];
                String probabilities = "";
                for (int i = 6; i < strings.length; i++) {
                    probabilities += strings[i] + " ";
                }
                probabilities = probabilities.trim();
                SimSystem2 sim = new SimSystem2(arrivalRate, serviceRate, serverSize, objectSize, eventSize, endCondition, probabilities);
                LinkedList<Double> results = sim.run();
                bufferedWriter.write(results + "\n");
            } else {
                break;
            }
        }
        reader.close();
        bufferedWriter.close();
        Date end = new Date();
        System.out.println("Simulation time length: " + (end.getTime() - start.getTime()));
    }
}
