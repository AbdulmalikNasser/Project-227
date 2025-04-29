enum ProcessState {
    NEW,
    READY,
    RUNNING,
    WAITING,
    TERMINATED
}


public class PCB {
    private int id;
    private int burstTime;
    private int priority;
    private int memoryRequired;
    private int waitingTime;
    private int turnaroundTime;
    private int readyTime;
    private ProcessState state;

    public PCB(int id, int burstTime, int priority, int memoryRequired) {
        this.id = id;
        this.burstTime = burstTime;
        this.priority = priority;
        this.memoryRequired = memoryRequired;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
        this.state = ProcessState.NEW;
    }

 

    public void setReadyTime(int t) { 
        this.readyTime = t; 
    }
    public int getReadyTime() { 
        return readyTime; 
    }
    public int getId() {
        return id;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public void setBurstTime(int burstTime) {
        this.burstTime = burstTime;
    }

    public int getPriority() {
        return priority;
    }

    public int getMemoryRequired() {
        return memoryRequired;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    public int getTurnaroundTime() {
        return turnaroundTime;
    }

    public void setTurnaroundTime(int turnaroundTime) {
        this.turnaroundTime = turnaroundTime;
    }

    public ProcessState getState() {
        return state;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }

   
    public String toString() {
        return String.format(
            "PCB{id=%d, state=%s, burstTime=%d, priority=%d, memory=%dMB, waiting=%d, turnaround=%d}",
            id, state, burstTime, priority, memoryRequired, waitingTime, turnaroundTime
        );
    }
}
