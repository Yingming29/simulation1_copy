public class SubTask {
    public double startTime;
    public double taskServiceTime;
    public double endTime;
    public double commitTime;
    public int objectTarget;
    public int complete;
    public SubTask() {
        endTime = Double.MAX_VALUE;
        complete = 0;   // 0: working or waiting
                        // 1: Committed
                        // -1: Aborted
    }

    public int getComplete() {
        return complete;
    }

    public void setComplete(int complete) {
        this.complete = complete;
    }
    public String toString(){
        String str = "Task start time: " + startTime + "\nTask service time: " + taskServiceTime + "\nTask end time: " + endTime
                + "\nObject target: " + objectTarget + "\nCompleted: " + complete;
        return str;
    }
    public double getTaskServiceTime() {
        return taskServiceTime;
    }

    public void setTaskServiceTime(double taskServiceTime) {
        this.taskServiceTime = taskServiceTime;
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
