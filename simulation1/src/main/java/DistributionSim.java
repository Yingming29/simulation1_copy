public class DistributionSim {
    private String name;
    private Double[] probabilities;
    private int size;

    private boolean verifySum(){
        if (probabilities.length <= 1){
            return false;
        }
        Double sum = 0.0;
        for (Double each:
             this.probabilities) {
            sum += each;
        }
        return sum == 1.0;
    }
    private int getWriteSize(Double d){
        int writes = 0;
        for (int i = 0; i < probabilities.length; i++) {
            if (d < probabilities[i] && 0.0 < d){
                return writes;
            }
            if (i > 1 && probabilities[i-1] < d && d < probabilities[i]) {
                writes = i;
                return writes;
            }
        }
        return writes;
    }
    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Double[] getProbabilities() {
        return probabilities;
    }

    public void setProbabilities(Double[] probabilities) {
        this.probabilities = probabilities;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
