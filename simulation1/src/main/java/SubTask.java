public class SubTask {
    public double startTime;
    public double endTime;
    public double commitTime;
    public int objectTarget;
    public SubTask() {

    }

    public int getObjectTarget() {
        return objectTarget;
    }

    public void setObjectTarget(int objectTarget) {
        this.objectTarget = objectTarget;
    }

    public double getCommitTime() {
        return commitTime;
    }

    public void setCommitTime(double commitTime) {
        this.commitTime = commitTime;
    }

    public double getEndTime() {
        return endTime;
    }

    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }
}
