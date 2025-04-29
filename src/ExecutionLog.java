class ExecutionLog {
    int startTime;
    int endTime;
    int processId;
    ExecutionLog(int start, int end, int pid) {
        this.startTime = start;
        this.endTime = end;
        this.processId = pid;
    }
}