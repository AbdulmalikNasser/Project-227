/**
 * Simulated system calls for process control, memory management,
 * and information maintenance in the CPU scheduler simulator.
 */
public class SysCall {
	 //Create a new process (PCB) in NEW state.
     
    public static PCB createProcess(int id, int burst, int priority, int memory) {
        PCB p = new PCB(id, burst, priority, memory);
        p.setState(ProcessState.NEW);
        return p;
    }

     //Enqueue a new job into the job queue and set its state to READY.
     
    public static void enqueueJob(ProcessQueue queue, PCB p) {
        p.setState(ProcessState.READY);
        queue.enqueue(p);
        
    }

    //Allocate memory for a process (blocks if insufficient) by adjusting freeMemory.
    
    public static void allocateMemory(Loader loader, PCB p) {
        loader.freeMemory(-p.getMemoryRequired());
        p.setState(ProcessState.READY);
       
    }

    // Enqueue a process into the ready queue after memory allocation.
     
    public static void enqueueReady(ProcessQueue queue, PCB p) {
        p.setState(ProcessState.READY);
        queue.enqueue(p);
       
    }

    // Dispatch a process to RUNNING state.
     
    public static void dispatchProcess(PCB p) {
        p.setState(ProcessState.RUNNING);
       
    }

    // Information maintenance: log an execution interval.
  
    public static void logExecution(java.util.List<ExecutionLog> logs,
                                    int start, int end, int pid) {
        logs.add(new ExecutionLog(start, end, pid));
    }

    // Free memory when a process finishes, waking any waiting allocators.
    
    public static void freeMemory(Loader loader, PCB p) {
        loader.freeMemory(p.getMemoryRequired());
      
    }
}
