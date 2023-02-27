import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;

public class Test3 {
    public static void main(String[] args) throws IOException {
        FileWriter fileWriter = new FileWriter("output.txt");
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write("1234");
        bufferedWriter.close();
        BufferedReader reader = new BufferedReader(new FileReader("input2.txt"));
        String line = reader.readLine();
        while (line != null) {
            System.out.println(line);
            line = reader.readLine();

        }
        reader.close();
    }
}
