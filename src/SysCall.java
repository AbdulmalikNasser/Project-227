/**
 * Simulated system calls for process control, memory management,
 * and information maintenance in the CPU scheduler simulator.
 */
public class SysCall {
    /**
     * Process control: create a new process (PCB).
     */
    public static PCB createProcess(int id, int burst, int priority, int memory) {
        PCB p = new PCB(id, burst, priority, memory);
        p.setState(ProcessState.NEW);
        return p;
    }

    /**
     * Information maintenance: enqueue a job into the job queue.
     */
    public static void enqueueJob(ProcessQueue queue, PCB p) {
        p.setState(ProcessState.READY);
        queue.enqueue(p);
        System.out.println("[SysCall] Enqueued job: " + p);
    }

    /**
     * Memory management: allocate memory for a process (blocks until available).
     * We assume Loader.freeMemory(-amount) will decrement, so we wrap accordingly.
     */
    public static void allocateMemory(Loader loader, PCB p) {
        // Here we simply call freeMemory with negative amount to allocate
        loader.freeMemory(-p.getMemoryRequired());
        p.setState(ProcessState.READY);
        System.out.println("[SysCall] Allocated " + p.getMemoryRequired() + "MB for " + p);
    }

    /**
     * Memory management: free memory when a process terminates.
     */
    public static void freeMemory(Loader loader, PCB p) {
        loader.freeMemory(p.getMemoryRequired());
        System.out.println("[SysCall] Freed " + p.getMemoryRequired() + "MB from " + p);
    }

    /**
     * Process control: dispatch a process to RUNNING state.
     */
    public static void dispatchProcess(PCB p) {
        p.setState(ProcessState.RUNNING);
        System.out.println("[SysCall] Dispatched process: " + p);
    }

    /**
     * Information maintenance: log an execution interval.
     */
    public static void logExecution(java.util.List<ExecutionLog> logs, int start, int end, int pid) {
        logs.add(new ExecutionLog(start, end, pid));
    }
}
